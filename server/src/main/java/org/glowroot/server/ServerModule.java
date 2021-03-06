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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import org.glowroot.common.config.ImmutableWebConfig;
import org.glowroot.common.config.WebConfig;
import org.glowroot.common.live.LiveAggregateRepository.LiveAggregateRepositoryNop;
import org.glowroot.common.repo.RepoAdmin;
import org.glowroot.common.repo.util.AlertingService;
import org.glowroot.common.repo.util.MailService;
import org.glowroot.common.repo.util.RollupLevelService;
import org.glowroot.common.util.Clock;
import org.glowroot.common.util.Version;
import org.glowroot.server.storage.AgentDao;
import org.glowroot.server.storage.AggregateDao;
import org.glowroot.server.storage.ConfigRepositoryImpl;
import org.glowroot.server.storage.ConfigRepositoryImpl.ConfigListener;
import org.glowroot.server.storage.GaugeValueDao;
import org.glowroot.server.storage.RoleDao;
import org.glowroot.server.storage.SchemaUpgrade;
import org.glowroot.server.storage.ServerConfigDao;
import org.glowroot.server.storage.TraceDao;
import org.glowroot.server.storage.TransactionTypeDao;
import org.glowroot.server.storage.TriggeredAlertDao;
import org.glowroot.server.storage.UserDao;
import org.glowroot.ui.CreateUiModuleBuilder;
import org.glowroot.ui.UiModule;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MINUTES;

class ServerModule {

    private static final Logger logger;

    static {
        CodeSource codeSource = ServerModule.class.getProtectionDomain().getCodeSource();
        File glowrootServerJarFile = null;
        Exception exception = null;
        try {
            glowrootServerJarFile = getGlowrootServerJarFile(codeSource);
        } catch (URISyntaxException e) {
            exception = e;
        }
        if (glowrootServerJarFile != null) {
            File logbackXmlOverride =
                    new File(glowrootServerJarFile.getParentFile(), "logback.xml");
            if (logbackXmlOverride.exists()) {
                System.setProperty("logback.configurationFile",
                        logbackXmlOverride.getAbsolutePath());
            }
        }
        logger = LoggerFactory.getLogger(ServerModule.class);
        if (exception != null) {
            logger.error(exception.getMessage(), exception);
        }
    }

    private final Cluster cluster;
    private final Session session;
    private final RollupService rollupService;
    private final GrpcServer server;
    private final UiModule uiModule;

    ServerModule() throws Exception {
        Cluster cluster = null;
        Session session = null;
        RollupService rollupService = null;
        GrpcServer server = null;
        UiModule uiModule = null;
        try {
            // install jul-to-slf4j bridge for protobuf which logs to jul
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            Clock clock = Clock.systemClock();
            String version = Version.getVersion(Bootstrap.class);

            ServerConfiguration serverConfig = getServerConfiguration();
            session = connect(serverConfig);
            cluster = session.getCluster();
            session.execute("create keyspace if not exists " + serverConfig.cassandraKeyspace()
                    + " with replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
            session.execute("use " + serverConfig.cassandraKeyspace());

            KeyspaceMetadata keyspace =
                    cluster.getMetadata().getKeyspace(serverConfig.cassandraKeyspace());
            SchemaUpgrade schemaUpgrade = new SchemaUpgrade(session, keyspace);
            Integer initialSchemaVersion = schemaUpgrade.getInitialSchemaVersion();
            if (initialSchemaVersion != null) {
                schemaUpgrade.upgrade();
            }
            ServerConfigDao serverConfigDao = new ServerConfigDao(session);
            AgentDao agentDao = new AgentDao(session);
            UserDao userDao = new UserDao(session, keyspace);
            RoleDao roleDao = new RoleDao(session, keyspace);
            ConfigRepositoryImpl configRepository =
                    new ConfigRepositoryImpl(serverConfigDao, agentDao, userDao, roleDao);
            Integer uiPortOverride = serverConfig.uiPortOverride();
            if (uiPortOverride != null) {
                // TODO supplying ui.port in glowroot-server.properties should make the port
                // non-editable in admin UI
                WebConfig webConfig = configRepository.getWebConfig();
                WebConfig updatedWebConfig =
                        ImmutableWebConfig.copyOf(webConfig).withPort(uiPortOverride);
                configRepository.updateWebConfig(updatedWebConfig, webConfig.version());
            }
            serverConfigDao.setConfigRepository(configRepository);
            agentDao.setConfigRepository(configRepository);

            TransactionTypeDao transactionTypeDao =
                    new TransactionTypeDao(session, configRepository);
            AggregateDao aggregateDao =
                    new AggregateDao(session, transactionTypeDao, configRepository);
            TraceDao traceDao = new TraceDao(session, transactionTypeDao, configRepository);
            GaugeValueDao gaugeValueDao = new GaugeValueDao(session, configRepository);
            TriggeredAlertDao triggeredAlertDao = new TriggeredAlertDao(session, configRepository);
            RollupLevelService rollupLevelService = new RollupLevelService(configRepository, clock);
            AlertingService alertingService = new AlertingService(configRepository,
                    triggeredAlertDao, aggregateDao, gaugeValueDao, rollupLevelService,
                    new MailService());
            server = new GrpcServer(serverConfig.grpcPort(), agentDao, aggregateDao,
                    gaugeValueDao, traceDao, alertingService);
            DownstreamServiceImpl downstreamService = server.getDownstreamService();
            configRepository.addConfigListener(new ConfigListener() {
                @Override
                public void onChange(String agentId) throws Exception {
                    downstreamService.updateAgentConfigIfConnectedAndNeeded(agentId);
                }
            });
            rollupService = new RollupService(agentDao, aggregateDao, gaugeValueDao,
                    downstreamService, clock);

            if (initialSchemaVersion == null) {
                schemaUpgrade.updateSchemaVersionToCurent();
            }

            uiModule = new CreateUiModuleBuilder()
                    .fat(false)
                    .offlineViewer(false)
                    .clock(clock)
                    .logDir(new File("."))
                    .liveJvmService(new LiveJvmServiceImpl(downstreamService))
                    .configRepository(configRepository)
                    .agentRepository(agentDao)
                    .transactionTypeRepository(transactionTypeDao)
                    .traceRepository(traceDao)
                    .aggregateRepository(aggregateDao)
                    .gaugeValueRepository(gaugeValueDao)
                    .repoAdmin(new NopRepoAdmin())
                    .rollupLevelService(rollupLevelService)
                    .liveTraceRepository(new LiveTraceRepositoryImpl(downstreamService))
                    .liveAggregateRepository(new LiveAggregateRepositoryNop())
                    .liveWeavingService(new LiveWeavingServiceImpl(downstreamService))
                    .numWorkerThreads(50)
                    .version(version)
                    .build();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            // try to shut down cleanly, otherwise apache commons daemon (via Bootstrap) doesn't
            // know service failed to start up
            if (uiModule != null) {
                uiModule.close();
            }
            if (server != null) {
                server.close();
            }
            if (rollupService != null) {
                rollupService.close();
            }
            if (session != null) {
                session.close();
            }
            if (cluster != null) {
                cluster.close();
            }
            throw Throwables.propagate(t);
        }
        this.cluster = cluster;
        this.session = session;
        this.rollupService = rollupService;
        this.server = server;
        this.uiModule = uiModule;
    }

    @VisibleForTesting
    static @Nullable File getGlowrootServerJarFile(@Nullable CodeSource codeSource)
            throws URISyntaxException {
        if (codeSource == null) {
            return null;
        }
        File codeSourceFile = new File(codeSource.getLocation().toURI());
        if (codeSourceFile.getName().endsWith(".jar")) {
            return codeSourceFile;
        }
        return null;
    }

    private static Session connect(ServerConfiguration serverConfig) throws InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean waitingForCassandraLogged = false;
        NoHostAvailableException lastException = null;
        while (stopwatch.elapsed(MINUTES) < 10) {
            try {
                Cluster cluster = Cluster.builder()
                        .addContactPoints(
                                serverConfig.cassandraContactPoint().toArray(new String[0]))
                        // aggressive reconnect policy seems ok since not many clients
                        .withReconnectionPolicy(new ConstantReconnectionPolicy(1000))
                        // let driver know that only idempotent queries are used so it will retry on
                        // timeout
                        .withQueryOptions(new QueryOptions().setDefaultIdempotence(true))
                        .build();
                return cluster.connect();
            } catch (NoHostAvailableException e) {
                logger.debug(e.getMessage(), e);
                lastException = e;
                if (!waitingForCassandraLogged) {
                    logger.info("waiting for cassandra ({}) ...",
                            Joiner.on(",").join(serverConfig.cassandraContactPoint()));
                }
                waitingForCassandraLogged = true;
                Thread.sleep(1000);
            }
        }
        checkNotNull(lastException);
        throw lastException;
    }

    void close() throws InterruptedException {
        uiModule.close();
        server.close();
        rollupService.close();
        session.close();
        cluster.close();
    }

    private static ServerConfiguration getServerConfiguration() throws IOException {
        ImmutableServerConfiguration.Builder builder = ImmutableServerConfiguration.builder();
        File propFile = new File("glowroot-server.properties");
        if (!propFile.exists()) {
            return builder.build();
        }
        Properties props = new Properties();
        InputStream in = new FileInputStream(propFile);
        try {
            props.load(in);
        } finally {
            in.close();
        }
        String cassandraContactPoints = props.getProperty("cassandra.contact.points");
        if (!Strings.isNullOrEmpty(cassandraContactPoints)) {
            builder.cassandraContactPoint(Splitter.on(',').trimResults().omitEmptyStrings()
                    .splitToList(cassandraContactPoints));
        }
        String cassandraKeyspace = props.getProperty("cassandra.keyspace");
        if (!Strings.isNullOrEmpty(cassandraKeyspace)) {
            builder.cassandraKeyspace(cassandraKeyspace);
        }
        String grpcPortText = props.getProperty("grpc.port");
        if (!Strings.isNullOrEmpty(grpcPortText)) {
            builder.grpcPort(Integer.parseInt(grpcPortText));
        }
        String uiPortText = props.getProperty("ui.port");
        if (!Strings.isNullOrEmpty(uiPortText)) {
            builder.uiPortOverride(Integer.parseInt(uiPortText));
        }
        return builder.build();
    }

    @Value.Immutable
    static abstract class ServerConfiguration {
        @Value.Default
        @SuppressWarnings("immutables")
        List<String> cassandraContactPoint() {
            return ImmutableList.of("127.0.0.1");
        }
        @Value.Default
        String cassandraKeyspace() {
            return "glowroot";
        }
        @Value.Default
        int grpcPort() {
            return 8181;
        }
        abstract @Nullable Integer uiPortOverride();
    }

    private static class NopRepoAdmin implements RepoAdmin {
        @Override
        public void deleteAllData() throws Exception {}
        @Override
        public void defrag() throws Exception {}
        @Override
        public void resizeIfNecessary() throws Exception {}
    }
}
