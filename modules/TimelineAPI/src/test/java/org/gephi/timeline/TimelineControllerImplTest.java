package org.gephi.timeline;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder.DynamicRangeFilter;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.timeline.api.TimelineController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class TimelineControllerImplTest {

    private Project project;
    private Workspace workspace;

    @Before
    public void setUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        project = pc.newProject();
        workspace = project.getCurrentWorkspace();
    }

    @After
    public void cleanUp() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
        project = null;
    }

    /**
     * Regression test for #3025: playing the timeline while the "Time Interval" (Dynamic Range)
     * filter is a subquery of another filter (here Degree Range) used to corrupt the parent
     * filter's range property instead of the Dynamic Range filter's own property, crashing
     * FilterProcessor with "Lower and upper must be the same class".
     */
    @Test
    public void testSetIntervalWithDynamicRangeAsSubQuery() {
        GraphGenerator generator = GraphGenerator.build(workspace);
        GraphModel graphModel = generator.getGraphModel();

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        n1.addInterval(new Interval(0.0, 10.0));
        n2.addInterval(new Interval(0.0, 10.0));
        Edge e1 = graphModel.factory().newEdge("1", n1, n2, 0, 1.0, true);
        e1.addInterval(new Interval(0.0, 10.0));

        Graph graph = graphModel.getGraph();
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

        DegreeRangeBuilder degreeRangeBuilder = Lookup.getDefault().lookup(DegreeRangeBuilder.class);
        Query degreeQuery = filterController.createQuery(degreeRangeBuilder);
        filterController.add(degreeQuery);

        DynamicRangeBuilder dynamicRangeBuilder = Lookup.getDefault().lookup(DynamicRangeBuilder.class);
        FilterBuilder[] dynamicBuilders = dynamicRangeBuilder.getBuilders(workspace);
        Assert.assertTrue(dynamicBuilders.length > 0);
        Query dynamicQuery = filterController.createQuery(dynamicBuilders[0]);
        filterController.setSubQuery(degreeQuery, dynamicQuery);

        FilterModel filterModel = filterController.getModel(workspace);
        Assert.assertSame(degreeQuery, filterModel.getCurrentQuery());

        TimelineController timelineController = Lookup.getDefault().lookup(TimelineController.class);
        timelineController.setCustomBounds(0.0, 10.0);

        //Simulate the timeline playing through the interval
        timelineController.setInterval(0.0, 4.0);

        //The Dynamic Range filter's own range must have been updated...
        DynamicRangeFilter dynamicRangeFilter = (DynamicRangeFilter) dynamicQuery.getFilter();
        Assert.assertEquals(0.0, dynamicRangeFilter.getRange().getLowerDouble(), 0.0);
        Assert.assertEquals(4.0, dynamicRangeFilter.getRange().getUpperDouble(), 0.0);

        //...and NOT the Degree Range filter's range, which must stay Integer-typed
        DegreeRangeFilter degreeRangeFilter = (DegreeRangeFilter) degreeQuery.getFilter();
        Assert.assertEquals(Integer.class, degreeRangeFilter.getRange().getRangeType());

        //Must not throw IllegalArgumentException: Lower and upper must be the same class
        GraphView view1 = filterController.filter(degreeQuery);
        Assert.assertNotNull(view1);

        //Advance the timeline further, still without crashing
        timelineController.setInterval(4.0, 8.0);
        Assert.assertEquals(Integer.class, degreeRangeFilter.getRange().getRangeType());
        GraphView view2 = filterController.filter(degreeQuery);
        Assert.assertNotNull(view2);

        //Stopping the timeline should only remove the Dynamic Range subquery, not the whole query
        timelineController.setInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Assert.assertSame(degreeQuery, filterModel.getCurrentQuery());
        Assert.assertEquals(0, degreeQuery.getChildren().length);

        filterController.filterVisible(null);
    }
}
