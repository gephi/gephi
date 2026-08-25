package org.gephi.utils.longtask.api;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.awaitility.Awaitility;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
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
import org.netbeans.junit.MockServices;
import org.openide.util.Cancellable;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SynchronousTest {

    @Mock
    Runnable runnable;

    @Mock
    Callable<Integer> callable;

    @Mock
    LongTask longTask;

    @Mock
    LongTaskErrorHandler errorHandler;

    @Mock
    LongTaskListener listener;

    @Mock
    static ProgressTicket progressTicket;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LongTaskExecutor executor;

    @Before
    public void setUp() {
        executor = new LongTaskExecutor(false);
        MockServices.setServices(MockProgressTicketProvider.class);
    }

    @Test
    public void testExecuteRunnable() {
        executor.execute(null, runnable);
        Mockito.verify(runnable).run();
    }

    @Test
    public void testExecuteRunnableWithProgress() {
        executor.execute(longTask, runnable);
        Mockito.verify(longTask).setProgressTicket(Mockito.any(ProgressTicket.class));
        Mockito.verify(progressTicket).start();
        Mockito.verify(progressTicket).finish();
    }

    @Test
    public void testExecuteRunnableException() {
        executor.setLongTaskListener(listener);
        Mockito.doThrow(new RuntimeException()).when(runnable).run();
        executor.execute(longTask, runnable, "", errorHandler);
        Mockito.verify(errorHandler).fatalError(Mockito.any(RuntimeException.class));
        Mockito.verify(listener, Mockito.never()).taskFinished(Mockito.any());
    }

    @Test
    public void testExecuteRunnableProgressWithException() {
        Mockito.doThrow(new RuntimeException()).when(runnable).run();
        executor.execute(longTask, runnable);
        Mockito.verify(progressTicket).finish();
    }

    @Test
    public void testExecuteRunnableListener() {
        executor.setLongTaskListener(listener);
        executor.execute(longTask, runnable);
        Mockito.verify(listener).taskFinished(Mockito.eq(longTask));
    }

    @Test
    public void testExecuteCallable() throws Exception {
        Mockito.doReturn(42).when(callable).call();
        Future<Integer> res = executor.execute(null, callable);
        Mockito.verify(callable).call();
        Assert.assertEquals(42, res.get().intValue());
    }

    @Test
    public void testExecuteCallableException() throws Exception {
        Mockito.doThrow(new RuntimeException()).when(callable).call();
        Future<Integer> res = executor.execute(null, callable, "", errorHandler);
        Mockito.verify(errorHandler).fatalError(Mockito.any(RuntimeException.class));

        expectedException.expect(ExecutionException.class);
        expectedException.expectCause(Is.isA(RuntimeException.class));
        res.get();
    }

    @Test(timeout = 30000)
    public void testCancelNotBlockedByRunningTask() throws Exception {
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch cancelReturned = new CountDownLatch(1);
        final AtomicBoolean cancelledWhileRunning = new AtomicBoolean();

        // The task body only completes once cancel() has returned on the other thread,
        // so the executor's monitor can't be held while the task is running.
        Mockito.doAnswer(invocation -> {
            taskStarted.countDown();
            cancelledWhileRunning.set(cancelReturned.await(5, TimeUnit.SECONDS));
            return null;
        }).when(runnable).run();

        Thread canceller = new Thread(() -> {
            try {
                if (taskStarted.await(10, TimeUnit.SECONDS)) {
                    executor.cancel();
                    cancelReturned.countDown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "cancel-caller");
        canceller.start();

        executor.execute(longTask, runnable);
        canceller.join();

        Assert.assertTrue("cancel() must not wait for the running synchronous task",
            cancelledWhileRunning.get());
        Mockito.verify(longTask).cancel();
    }

    public static class MockProgressTicketProvider implements ProgressTicketProvider {

        public MockProgressTicketProvider() {
        }

        @Override
        public ProgressTicket createTicket(String taskName, Cancellable cancellable) {
            return progressTicket;
        }
    }
}
