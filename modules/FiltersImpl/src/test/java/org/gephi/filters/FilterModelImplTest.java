package org.gephi.filters;

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.GraphView;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterModelImplTest {

    /**
     * setCurrentResult() must fire the model's ChangeListener whenever the result
     * actually changes, so listeners can tell when a filter/select pass finishes.
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
