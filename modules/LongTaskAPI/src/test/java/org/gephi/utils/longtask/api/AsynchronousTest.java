package org.gephi.utils.longtask.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.awaitility.Awaitility;
import org.gephi.utils.longtask.spi.LongTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    LongTask longTask;

    private LongTaskExecutor executor;

    @Before
    public void setUp() {
        executor = new LongTaskExecutor(true);
    }

    @Test
    public void testExecuteCallable() throws Exception {
        Mockito.doReturn(42).when(callable).call();
        Future<Integer> res = executor.execute(null, callable);
        Assert.assertEquals(42, res.get().intValue());
    }

    @Test
    public void testCancel() throws Exception {
        Mockito.doAnswer(new AnswersWithDelay(200, invocation -> 42)).when(callable).call();
        Future<Integer> future = executor.execute(longTask, callable);
        Awaitility.await().until(executor::isRunning);
        executor.cancel();
        future.get();
        Mockito.verify(longTask).cancel();
    }
}
