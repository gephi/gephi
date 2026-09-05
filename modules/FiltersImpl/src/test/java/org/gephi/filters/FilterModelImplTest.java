package org.gephi.filters;

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.GraphView;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterModelImplTest {

    /**
     * Regression test: setCurrentResult() used to be the only mutator in this class
     * that never fired the model's ChangeListener, so listeners had no way to know
     * when an async filter/select operation actually finished (only that it started).
     */
    @Test
    public void testSetCurrentResultFiresChangeEvent() {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        AtomicInteger fireCount = new AtomicInteger();
        filterModel.addChangeListener(e -> fireCount.incrementAndGet());

        GraphView viewA = filterModel.getGraphModel().getGraph().getView();
        filterModel.setCurrentResult(viewA);
        assertEquals(1, fireCount.get());

        //Setting the same reference again must not fire a redundant event
        filterModel.setCurrentResult(viewA);
        assertEquals(1, fireCount.get());

        GraphView viewB = filterModel.getGraphModel().copyView(viewA);
        filterModel.setCurrentResult(viewB);
        assertEquals(2, fireCount.get());

        filterModel.setCurrentResult(null);
        assertEquals(3, fireCount.get());
    }
}
