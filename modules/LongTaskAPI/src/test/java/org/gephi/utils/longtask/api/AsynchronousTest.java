package org.gephi.utils.longtask.api;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public void testExecuteCallableExceptionListenerFatalError() throws Exception {
        RuntimeException exception = new RuntimeException();
        executor.setLongTaskListener(listener);
        Mockito.doThrow(exception).when(callable).call();
        executor.execute(longTask, callable, "", errorHandler);
        // The callbacks are awaited directly: isRunning() is already false between the
        // submission and the moment the pool thread picks the task up
        Mockito.verify(errorHandler, Mockito.timeout(30000)).fatalError(exception);
        // The listener's fatalError() complements the error handler, it doesn't replace it
        Mockito.verify(listener, Mockito.timeout(30000)).fatalError(exception);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executor::isRunning, t -> !t);
        Mockito.verify(listener, Mockito.never()).taskFinished(Mockito.any());
    }

    @Test
    public void testExecuteCallableExceptionListenerWithoutFatalError() throws Exception {
        RuntimeException exception = new RuntimeException();
        AtomicBoolean taskFinished = new AtomicBoolean();
        // A listener which relies on the default, do-nothing fatalError() implementation
        executor.setLongTaskListener(task -> taskFinished.set(true));
        Mockito.doThrow(exception).when(callable).call();
        executor.execute(longTask, callable, "", errorHandler);
        Mockito.verify(errorHandler, Mockito.timeout(30000)).fatalError(exception);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executor::isRunning, t -> !t);
        Assert.assertFalse(taskFinished.get());
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
    public void testCancelRunnableInterruptFailingTask() {
        Mockito.when(longTask.cancel()).thenReturn(false);
        Mockito.doAnswer(invocation -> {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // The task fails on interruption, that is after the cancel timer already
                // claimed its completion and reported it to the listener
                throw new RuntimeException(e);
            }
            return null;
        }).when(runnable).run();
        executorWithInterruption.setLongTaskListener(listener);
        executorWithInterruption.setDefaultErrorHandler(errorHandler);
        executorWithInterruption.execute(longTask, runnable);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(executorWithInterruption::isRunning);
        executorWithInterruption.cancel();
        Mockito.verify(errorHandler, Mockito.timeout(30000)).fatalError(Mockito.any(RuntimeException.class));
        Mockito.verify(listener, Mockito.timeout(30000)).taskFinished(Mockito.any());
        // Exactly one of the two listener methods is called per task
        Mockito.verify(listener, Mockito.after(500).never()).fatalError(Mockito.any());
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
