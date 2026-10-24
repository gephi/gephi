package org.gephi.datalab.impl;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class GraphElementsControllerImplTest {

    private static final Interval FIRST = new Interval(2000, 2001);
    private static final Interval SECOND = new Interval(2002, 2003);

    private GraphModel graphModel;

    @Before
    public void setUp() {
        Lookup.getDefault().lookup(ProjectController.class).newProject();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    @After
    public void tearDown() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
    }

    @Test
    public void testDuplicateNodeWithDynamicValue() {
        Column column = graphModel.getNodeTable().addColumn("price", IntervalDoubleMap.class);
        Node node = graphModel.factory().newNode("1");
        graphModel.getGraph().addNode(node);
        node.setAttribute(column, 1.0, FIRST);

        Node copy = new GraphElementsControllerImpl().duplicateNode(node);
        copy.setAttribute(column, 2.0, SECOND);

        IntervalDoubleMap original = (IntervalDoubleMap) node.getAttribute(column);
        Assert.assertEquals(1, original.size());
        Assert.assertEquals(1.0, original.getDouble(FIRST), 0.0);

        graphModel.getGraph().removeNode(copy);
        Interval bounds = graphModel.getTimeBounds();
        for (Interval interval : original.toKeysArray()) {
            Assert.assertTrue(bounds.getLow() <= interval.getLow() && interval.getHigh() <= bounds.getHigh());
        }
    }
}
