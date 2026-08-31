package org.gephi.desktop.context;

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.GraphModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ContextRefreshThreadTest {

    private ContextRefreshThread thread;

    @After
    public void tearDown() {
        if (thread != null) {
            thread.shutdown();
        }
    }

    @Test
    public void testShutdownPreventsFurtherListenerInvocations() {
        GraphModel graphModel = GraphGenerator.build().generateTinyGraph().getGraphModel();
        AtomicInteger invocationCount = new AtomicInteger();
        thread = new ContextRefreshThread(graphModel, invocationCount::incrementAndGet);

        thread.shutdown();
        thread.run();

        Assert.assertEquals(0, invocationCount.get());
    }

    @Test
    public void testRunInvokesListenerOnlyWhenGraphVersionChanges() {
        GraphModel graphModel = GraphGenerator.build().generateTinyGraph().getGraphModel();
        AtomicInteger invocationCount = new AtomicInteger();
        thread = new ContextRefreshThread(graphModel, invocationCount::incrementAndGet);

        thread.run();
        Assert.assertEquals(1, invocationCount.get());

        thread.run();
        Assert.assertEquals(1, invocationCount.get());
    }
}
