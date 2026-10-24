package org.gephi.integration;

import org.gephi.appearance.api.AppearanceModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.filters.plugin.operator.INTERSECTIONBuilder;
import org.gephi.filters.plugin.partition.PartitionBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;

public class FilteringTest extends AbstractIntegrationTest {

    @Test
    public void testDegreeFilter() {
        Project project = projectController.newProject();
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().getGraph();
        Node extraNode = graph.getModel().factory().newNode();
        Assert.assertTrue(graph.addNode(extraNode));

        DegreeRangeBuilder.DegreeRangeFilter degreeFilter = new DegreeRangeBuilder.DegreeRangeFilter();
        degreeFilter.init(graph);
        degreeFilter.setRange(new Range(1, Integer.MAX_VALUE));
        GraphView view = filterController.filter(filterController.createQuery(degreeFilter));

        Graph filteredGraph = graph.getModel().getGraph(view);
        Assert.assertEquals(2, filteredGraph.getNodeCount());
        Assert.assertFalse(filteredGraph.contains(extraNode));
        Assert.assertEquals(1, filteredGraph.getEdgeCount());
    }

    @Test
    public void testPartitionFilter() {
        Project project = projectController.newProject();
        Workspace workspace = project.getCurrentWorkspace();
        Graph graph = GraphGenerator.build(workspace).generateTinyGraph().addStringNodeColumn().getGraph();

        AppearanceModel appearanceModel = appearanceController.getModel(workspace);
        PartitionBuilder.NodePartitionFilter partitionFilter = new PartitionBuilder.NodePartitionFilter(appearanceModel,
            appearanceModel.getNodePartition(graph.getModel().getNodeTable().getColumn(GraphGenerator.STRING_COLUMN)));
        partitionFilter.unselectAll();
        partitionFilter.addPart(GraphGenerator.STRING_COLUMN_VALUES[0]);
        GraphView view = filterController.filter(filterController.createQuery(partitionFilter));

        Graph filteredGraph = graph.getModel().getGraph(view);
        Assert.assertEquals(1, filteredGraph.getNodeCount());
        Assert.assertTrue(filteredGraph.contains(graph.getNode(GraphGenerator.FIRST_NODE)));

        partitionFilter.selectAll();
        partitionFilter.removePart(GraphGenerator.STRING_COLUMN_VALUES[0]);
        GraphView view2 = filterController.filter(filterController.createQuery(partitionFilter));

        filteredGraph = graph.getModel().getGraph(view2);
        Assert.assertEquals(1, filteredGraph.getNodeCount());
        Assert.assertTrue(filteredGraph.contains(graph.getNode(GraphGenerator.SECOND_NODE)));
    }

    @Test
    public void testExportToLabelVisible() {
        // gephi/gephi-toolkit-demos#5: exportToLabelVisible(query) should show the labels of the nodes matching the
        // query and hide the others
        Project project = projectController.newProject();
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().getGraph();
        Node isolated = graph.getModel().factory().newNode("isolated");
        graph.addNode(isolated);
        for (Node n : graph.getNodes()) {
            n.getTextProperties().setVisible(true);
        }

        DegreeRangeBuilder.DegreeRangeFilter degreeFilter = new DegreeRangeBuilder.DegreeRangeFilter();
        degreeFilter.init(graph);
        degreeFilter.setRange(new Range(1, Integer.MAX_VALUE));
        Query query = filterController.createQuery(degreeFilter);
        filterController.exportToLabelVisible(query);

        Assert.assertFalse("Isolated node's label should be hidden", isolated.getTextProperties().isVisible());
        Assert.assertTrue("Connected node's label should remain visible",
            graph.getNode(GraphGenerator.FIRST_NODE).getTextProperties().isVisible());
        Assert.assertTrue("Connected node's label should remain visible",
            graph.getNode(GraphGenerator.SECOND_NODE).getTextProperties().isVisible());
    }

    @Test
    public void testAndOperator() {
        Project project = projectController.newProject();
        Graph graph =
            GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().addStringNodeColumn().getGraph();

        DegreeRangeBuilder.DegreeRangeFilter degreeFilter = new DegreeRangeBuilder.DegreeRangeFilter();
        degreeFilter.init(graph);
        degreeFilter.setRange(new Range(1, 1));
        Query degreeQuery = filterController.createQuery(degreeFilter);

        AttributeEqualBuilder.EqualStringFilter.Node equalFilter = new AttributeEqualBuilder.EqualStringFilter.Node(
            graph.getModel().getNodeTable().getColumn(GraphGenerator.STRING_COLUMN));
        equalFilter.init(graph);
        equalFilter.setPattern(GraphGenerator.STRING_COLUMN_VALUES[0]);
        Query equalQuery = filterController.createQuery(equalFilter);

        Query andQuery = filterController.createQuery(new INTERSECTIONBuilder.IntersectionOperator());
        filterController.setSubQuery(andQuery, degreeQuery);
        filterController.setSubQuery(andQuery, equalQuery);
        GraphView view = filterController.filter(andQuery);

        Graph filteredGraph = graph.getModel().getGraph(view);
        Assert.assertEquals(1, filteredGraph.getNodeCount());
        Assert.assertTrue(filteredGraph.contains(graph.getNode(GraphGenerator.FIRST_NODE)));
    }
}
