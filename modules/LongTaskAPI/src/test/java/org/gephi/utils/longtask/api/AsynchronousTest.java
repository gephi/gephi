package org.gephi.utils.longtask.api;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.gephi.utils.longtask.spi.LongTask;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AsynchronousTest {

    @Mock
    Callable<Integer> callable;

    @Mock
    Runnable runnable;

    @Mock
    LongTask longTask;

    @Mock
    LongTaskErrorHandler errorHandler;

    @Mock
    LongTaskListener listener;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LongTaskExecutor executor;

    private LongTaskExecutor executorWithInterruption;

    @Before
    public void setUp() {
        executor = new LongTaskExecutor(true);
        executorWithInterruption = new LongTaskExecutor(true, "", 1);
    }

    @Test
    public void testExecuteCallable() throws Exception {
        Mockito.doReturn(42).when(callable).call();
        Future<Integer> res = executor.execute(null, callable);
        Assert.assertEquals(42, res.get().intValue());
    }

    @Test
    public void testExecuteCallableException() throws Exception {
        executor.setLongTaskListener(listener);
        Mockito.doThrow(new RuntimeException()).when(callable).call();
        executor.execute(longTask, callable, "", errorHandler);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executor::isRunning, t -> !t);
        Mockito.verify(errorHandler).fatalError(Mockito.any(RuntimeException.class));
        Mockito.verify(listener, Mockito.never()).taskFinished(Mockito.any());
    }

    @Test
    public void testExecuteCallableExceptionFuture() throws Exception {
        Mockito.doThrow(new RuntimeException()).when(callable).call();
        Future<Integer> future = executor.execute(longTask, callable, "", errorHandler);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executor::isRunning, t -> !t);

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(Is.isA(RuntimeException.class));
        future.get();
    }

    @Test
    public void testCancel() throws Exception {
        executor.setLongTaskListener(listener);
        // The task blocks until the test releases it, so it is guaranteed to be
        // observed as running instead of depending on a fixed delay
        CountDownLatch release = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            release.await();
            return 42;
        }).when(callable).call();
        Future<Integer> future = executor.execute(longTask, callable);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executor::isRunning);
        executor.cancel();
        release.countDown();
        future.get();
        Mockito.verify(longTask).cancel();
        Mockito.verify(listener).taskFinished(Mockito.any());
    }

    @Test
    public void testExecuteTwice() throws Exception {
        executor.execute(longTask, callable);
        executor.execute(longTask, callable);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executor::isRunning, t -> !t);
        Mockito.verify(callable, Mockito.times(2)).call();
    }

    @Test
    public void testCancelRunnableInterrupt() {
        Mockito.when(longTask.cancel()).thenReturn(false);
        Mockito.doAnswer(new AnswersWithDelay(2000, invocation -> null)).when(runnable).run();
        executorWithInterruption.setLongTaskListener(listener);
        executorWithInterruption.execute(longTask, runnable);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executorWithInterruption::isRunning);
        executorWithInterruption.cancel();
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executorWithInterruption::isRunning, t -> !t);
        Mockito.verify(listener).taskFinished(Mockito.any());
    }

    @Test
    public void testCancelRunnableIgnoresInterrupt() {
        Mockito.when(longTask.cancel()).thenReturn(false);
        Mockito.doAnswer(invocation -> {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // The task swallows the interruption and completes normally, so both the
                // interrupt timer and the task thread reach the completion code
            }
            return null;
        }).when(runnable).run();
        executorWithInterruption.setLongTaskListener(listener);
        executorWithInterruption.execute(longTask, runnable);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executorWithInterruption::isRunning);
        executorWithInterruption.cancel();
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executorWithInterruption::isRunning, t -> !t);
        Mockito.verify(listener, Mockito.after(500).times(1)).taskFinished(Mockito.any());
    }
}
