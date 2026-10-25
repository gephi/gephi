package org.gephi.desktop.attributes.edit;

import java.time.ZoneId;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class MultipleRowsAttributeValueWrapperTest {

    private static final Interval FIRST = new Interval(2000, 2001);
    private static final Interval SECOND = new Interval(2002, 2003);
    private static final ZoneId ZONE = ZoneId.of("UTC");

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
    public void testSetDynamicValueDoesNotShareInstanceBetweenRows() {
        Column column = graphModel.getNodeTable().addColumn("price", IntervalDoubleMap.class);
        Node first = graphModel.factory().newNode("1");
        Node second = graphModel.factory().newNode("2");
        graphModel.getGraph().addNode(first);
        graphModel.getGraph().addNode(second);

        IntervalDoubleMap value = new IntervalDoubleMap();
        value.put(FIRST, 1.0);
        String text = AttributeUtils.print(value, TimeFormat.DOUBLE, ZONE);

        new MultipleRowsAttributeValueWrapper(new Element[] {first, second}, column, TimeFormat.DOUBLE, ZONE)
            .setValueAsString(text);
        first.setAttribute(column, 2.0, SECOND);

        IntervalDoubleMap other = (IntervalDoubleMap) second.getAttribute(column);
        Assert.assertEquals(1, other.size());
        Assert.assertEquals(1.0, other.getDouble(FIRST), 0.0);
    }
}
