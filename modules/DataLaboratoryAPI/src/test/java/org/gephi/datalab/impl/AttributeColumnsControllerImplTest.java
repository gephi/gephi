package org.gephi.datalab.impl;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class AttributeColumnsControllerImplTest {

    private static final Interval FIRST = new Interval(2000, 2001);
    private static final Interval SECOND = new Interval(2002, 2003);

    private GraphModel graphModel;
    private Table table;
    private Column column;
    private Node node;
    private AttributeColumnsControllerImpl controller;

    @Before
    public void setUp() {
        Lookup.getDefault().lookup(ProjectController.class).newProject();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        table = graphModel.getNodeTable();
        column = table.addColumn("price", IntervalDoubleMap.class);
        node = graphModel.factory().newNode("1");
        graphModel.getGraph().addNode(node);
        node.setAttribute(column, 1.0, FIRST);
        controller = new AttributeColumnsControllerImpl();
    }

    @After
    public void tearDown() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
    }

    @Test
    public void testDuplicateDynamicColumn() {
        Column copy = controller.duplicateColumn(table, column, "price copy", IntervalDoubleMap.class);
        node.setAttribute(copy, 2.0, SECOND);

        IntervalDoubleMap original = (IntervalDoubleMap) node.getAttribute(column);
        Assert.assertEquals(1, original.size());
        Assert.assertEquals(1.0, original.getDouble(FIRST), 0.0);

        controller.deleteAttributeColumn(table, copy);
        Interval bounds = graphModel.getTimeBounds();
        for (Interval interval : original.toKeysArray()) {
            Assert.assertTrue(bounds.getLow() <= interval.getLow() && interval.getHigh() <= bounds.getHigh());
        }
    }

    @Test
    public void testCopyDynamicValueToOtherNode() {
        Node other = graphModel.factory().newNode("2");
        graphModel.getGraph().addNode(other);
        controller.copyNodeDataToOtherNodes(node, new Node[] {other}, new Column[] {column});
        other.setAttribute(column, 2.0, SECOND);

        IntervalDoubleMap original = (IntervalDoubleMap) node.getAttribute(column);
        Assert.assertEquals(1, original.size());
        Assert.assertEquals(1.0, original.getDouble(FIRST), 0.0);
    }
}
