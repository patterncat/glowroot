/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.server;

import java.io.IOException;
import java.util.List;

import com.datastax.driver.core.exceptions.ReadTimeoutException;
import io.grpc.internal.ServerImpl;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.glowroot.common.repo.AggregateRepository;
import org.glowroot.common.repo.GaugeValueRepository;
import org.glowroot.common.repo.TraceRepository;
import org.glowroot.common.repo.util.AlertingService;
import org.glowroot.server.storage.AgentDao;
import org.glowroot.wire.api.model.AgentConfigOuterClass.AgentConfig;
import org.glowroot.wire.api.model.AggregateOuterClass.AggregatesByType;
import org.glowroot.wire.api.model.CollectorServiceGrpc.CollectorServiceImplBase;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.AggregateMessage;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.EmptyMessage;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.GaugeValue;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.GaugeValueMessage;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.InitMessage;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.InitResponse;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.LogEvent;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.LogMessage;
import org.glowroot.wire.api.model.CollectorServiceOuterClass.TraceMessage;
import org.glowroot.wire.api.model.Proto;

class GrpcServer {

    private static final int GRPC_MAX_MESSAGE_SIZE_MB =
            Integer.getInteger("grpc.max.message.size.mb", 100);

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    private final AgentDao agentDao;
    private final AggregateRepository aggregateRepository;
    private final GaugeValueRepository gaugeValueRepository;
    private final TraceRepository traceRepository;
    private final AlertingService alertingService;

    private final DownstreamServiceImpl downstreamService;

    private final ServerImpl server;

    GrpcServer(int port, AgentDao agentDao, AggregateRepository aggregateRepository,
            GaugeValueRepository gaugeValueRepository, TraceRepository traceRepository,
            AlertingService alertingService) throws IOException {
        this.agentDao = agentDao;
        this.aggregateRepository = aggregateRepository;
        this.gaugeValueRepository = gaugeValueRepository;
        this.traceRepository = traceRepository;
        this.alertingService = alertingService;

        downstreamService = new DownstreamServiceImpl(agentDao);

        server = NettyServerBuilder.forPort(port)
                .addService(new CollectorServiceImpl().bindService())
                .addService(downstreamService.bindService())
                .maxMessageSize(1024 * 1024 * GRPC_MAX_MESSAGE_SIZE_MB)
                .build()
                .start();
    }

    DownstreamServiceImpl getDownstreamService() {
        return downstreamService;
    }

    void close() {
        server.shutdown();
    }

    private class CollectorServiceImpl extends CollectorServiceImplBase {

        @Override
        public void collectInit(InitMessage request,
                StreamObserver<InitResponse> responseObserver) {
            AgentConfig updatedAgentConfig;
            try {
                updatedAgentConfig = agentDao.store(request.getAgentId(), request.getEnvironment(),
                        request.getAgentConfig());
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                responseObserver.onError(t);
                return;
            }
            if (updatedAgentConfig.equals(request.getAgentConfig())) {
                responseObserver.onNext(InitResponse.getDefaultInstance());
            } else {
                responseObserver.onNext(InitResponse.newBuilder()
                        .setAgentConfig(updatedAgentConfig)
                        .build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void collectAggregates(AggregateMessage request,
                StreamObserver<EmptyMessage> responseObserver) {
            List<AggregatesByType> aggregatesByTypeList = request.getAggregatesByTypeList();
            if (!aggregatesByTypeList.isEmpty()) {
                try {
                    aggregateRepository.store(request.getAgentId(), request.getCaptureTime(),
                            aggregatesByTypeList, request.getSharedQueryTextList());
                } catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                    responseObserver.onError(t);
                    return;
                }
            }
            try {
                alertingService.checkTransactionAlerts(request.getAgentId(),
                        request.getCaptureTime(), ReadTimeoutException.class);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                // don't fail collectAggregates()
            }
            responseObserver.onNext(EmptyMessage.getDefaultInstance());
            responseObserver.onCompleted();
        }

        @Override
        public void collectGaugeValues(GaugeValueMessage request,
                StreamObserver<EmptyMessage> responseObserver) {
            long maxCaptureTime = 0;
            try {
                gaugeValueRepository.store(request.getAgentId(), request.getGaugeValuesList());
                for (GaugeValue gaugeValue : request.getGaugeValuesList()) {
                    maxCaptureTime = Math.max(maxCaptureTime, gaugeValue.getCaptureTime());
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                responseObserver.onError(t);
                return;
            }
            try {
                alertingService.checkGaugeAlerts(request.getAgentId(), maxCaptureTime,
                        ReadTimeoutException.class);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                // don't fail collectGaugeValues()
            }
            responseObserver.onNext(EmptyMessage.getDefaultInstance());
            responseObserver.onCompleted();
        }

        @Override
        public void collectTrace(TraceMessage request,
                StreamObserver<EmptyMessage> responseObserver) {
            try {
                traceRepository.store(request.getAgentId(), request.getTrace());
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                responseObserver.onError(t);
                return;
            }
            responseObserver.onNext(EmptyMessage.getDefaultInstance());
            responseObserver.onCompleted();
        }

        @Override
        public void log(LogMessage request, StreamObserver<EmptyMessage> responseObserver) {
            try {
                LogEvent logEvent = request.getLogEvent();
                Proto.Throwable t = logEvent.getThrowable();
                if (t == null) {
                    logger.warn("{} -- {} -- {} -- {}", request.getAgentId(), logEvent.getLevel(),
                            logEvent.getLoggerName(), logEvent.getMessage());
                } else {
                    logger.warn("{} -- {} -- {} -- {}\n{}", request.getAgentId(),
                            logEvent.getLevel(), logEvent.getLoggerName(),
                            logEvent.getMessage(), t);
                }
            } catch (Throwable t) {
                responseObserver.onError(t);
                return;
            }
            responseObserver.onNext(EmptyMessage.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }
}
