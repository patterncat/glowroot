/*
 * Copyright 2016 the original author or authors.
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
package org.glowroot.agent.plugin.executor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.glowroot.agent.it.harness.AppUnderTest;
import org.glowroot.agent.it.harness.Container;
import org.glowroot.agent.it.harness.Containers;
import org.glowroot.agent.it.harness.TraceEntryMarker;
import org.glowroot.agent.it.harness.TransactionMarker;
import org.glowroot.wire.api.model.TraceOuterClass.Trace;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class ForkJoinPoolIT {

    private static Container container;

    @BeforeClass
    public static void setUp() throws Exception {
        // tests only work with javaagent container because they need to weave bootstrap classes
        // that implement ForkJoinPool
        container = Containers.createJavaagent();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        container.close();
    }

    @After
    public void afterEachTest() throws Exception {
        container.checkAndReset();
    }

    @Test
    public void shouldCaptureSubmitCallable() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitCallable.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureSubmitRunnable() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitRunnable.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureSubmitRunnableWithReturnValue() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitRunnableWithReturnValue.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureSubmitForkJoinTask() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitForkJoinTask.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureSubmitCallableAsForkJoinTask() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitCallableAsForkJoinTask.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureSubmitRunnableAsForkJoinTask() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitRunnableAsForkJoinTask.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureSubmitRunnableAsForkJoinTaskWithReturnValue() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolSubmitRunnableAsForkJoinTaskWithReturnValue.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureExecuteRunnable() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolExecuteRunnable.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureExecuteForkJoinTask() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolExecuteForkJoinTask.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureInvokeForkJoinTask() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolInvokeForkJoinTask.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isEqualTo(2);
    }

    @Test
    public void shouldCaptureInvokeAll() throws Exception {
        // given
        // when
        Trace trace = container.execute(DoPoolInvokeAll.class);
        // then
        assertThat(trace.getHeader().getEntryCount()).isBetween(4, 6);
        int count = 0;
        for (Trace.Entry entry : trace.getEntryList()) {
            if (entry.getMessage().equals("trace entry marker / CreateTraceEntry")) {
                count++;
            }
        }
        assertThat(count).isEqualTo(3);
    }

    public static class DoPoolSubmitCallable implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<Void> future = pool.submit(new SimpleCallable());
            future.get();
        }
    }

    public static class DoPoolSubmitRunnable implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<?> future = pool.submit(new SimpleRunnable());
            future.get();
        }
    }

    public static class DoPoolSubmitRunnableWithReturnValue
            implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<Integer> future = pool.submit(new SimpleRunnable(), 5);
            future.get();
        }
    }

    public static class DoPoolSubmitForkJoinTask implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<Integer> future = pool.submit(new SimpleTask());
            future.get();
        }
    }

    public static class DoPoolSubmitCallableAsForkJoinTask
            implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<?> future = pool.submit(ForkJoinTask.adapt(new SimpleCallable()));
            future.get();
        }
    }

    public static class DoPoolSubmitRunnableAsForkJoinTask
            implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<?> future = pool.submit(ForkJoinTask.adapt(new SimpleRunnable()));
            future.get();
        }
    }

    public static class DoPoolSubmitRunnableAsForkJoinTaskWithReturnValue
            implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            Future<Integer> future = pool.submit(ForkJoinTask.adapt(new SimpleRunnable(), 5));
            future.get();
        }
    }

    public static class DoPoolExecuteRunnable implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            SimpleRunnable simpleRunnable = new SimpleRunnable();
            pool.execute(simpleRunnable);
            simpleRunnable.latch.await();
            pool.shutdown();
            pool.awaitTermination(10, SECONDS);
        }
    }

    public static class DoPoolExecuteForkJoinTask implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            SimpleTask simpleTask = new SimpleTask();
            pool.execute(simpleTask);
            simpleTask.latch.await();
            pool.shutdown();
            pool.awaitTermination(10, SECONDS);
        }
    }

    public static class DoPoolInvokeForkJoinTask implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            SimpleTask simpleTask = new SimpleTask();
            pool.invoke(simpleTask);
        }
    }

    public static class DoPoolInvokeAll implements AppUnderTest, TransactionMarker {

        @Override
        public void executeApp() throws Exception {
            transactionMarker();
        }

        @Override
        public void transactionMarker() throws Exception {
            ForkJoinPool pool = new ForkJoinPool();
            List<Callable<Void>> callables = Lists.newArrayList();
            callables.add(new SimpleCallable());
            callables.add(new SimpleCallable());
            callables.add(new SimpleCallable());
            for (Future<Void> future : pool.invokeAll(callables)) {
                future.get();
            }
        }
    }

    private static class SimpleCallable implements Callable<Void> {
        @Override
        public Void call() {
            new CreateTraceEntry().traceEntryMarker();
            return null;
        }
    }

    private static class SimpleRunnable implements Runnable {

        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void run() {
            new CreateTraceEntry().traceEntryMarker();
            latch.countDown();
        }
    }

    @SuppressWarnings("serial")
    private static class SimpleTask extends ForkJoinTask<Integer> {

        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public Integer getRawResult() {
            return null;
        }

        @Override
        protected void setRawResult(Integer value) {}

        @Override
        protected boolean exec() {
            new CreateTraceEntry().traceEntryMarker();
            latch.countDown();
            return true;
        }
    }

    private static class CreateTraceEntry implements TraceEntryMarker {
        @Override
        public void traceEntryMarker() {}
    }
}
