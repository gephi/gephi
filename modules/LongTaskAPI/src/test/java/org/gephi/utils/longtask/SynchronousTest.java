package org.gephi.utils.longtask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    }

    @Test
    public void testExecuteRunnableException() {
        Mockito.doThrow(new RuntimeException()).when(runnable).run();
        executor.execute(longTask, runnable, "", errorHandler);
        Mockito.verify(errorHandler).fatalError(Mockito.any(RuntimeException.class));
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

    public static class MockProgressTicketProvider implements ProgressTicketProvider {

        public MockProgressTicketProvider() {
        }

        @Override
        public ProgressTicket createTicket(String taskName, Cancellable cancellable) {
            return progressTicket;
        }
    }
}
