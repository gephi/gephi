package org.gephi.desktop.search.filter;

import org.gephi.desktop.search.impl.SearchCategoryImpl;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Subgraph;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class SearchFilterTest {

    private Project project;

    @Before
    public void setUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        project = pc.newProject();
    }

    @After
    public void cleanUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        project = null;
    }

    @Test
    public void testNodeFilter() {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace(project.getCurrentWorkspace()).generateTinyGraph();
        Graph graph = graphGenerator.getGraph();

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        FilterBuilder filterBuilder = Lookup.getDefault().lookup(SearchFilterBuilder.class);
        Assert.assertNotNull(filterBuilder);

        Query query = filterController.createQuery(filterBuilder);
        SearchFilterBuilder.SearchFilter filter = (SearchFilterBuilder.SearchFilter) query.getFilter();
        filter.setQuery(GraphGenerator.FIRST_NODE);
        GraphView view = filterController.filter(query);
        Subgraph subgraph = graph.getModel().getGraph(view);
        Assert.assertTrue(subgraph.contains(graph.getNode(GraphGenerator.FIRST_NODE)));
        Assert.assertEquals(1, subgraph.getNodeCount());
    }

    @Test
    public void testEdgeFilter() {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace(project.getCurrentWorkspace()).generateTinyMultiGraph();
        Graph graph = graphGenerator.getGraph();

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

        Query query = filterController.createQuery(Lookup.getDefault().lookup(SearchFilterBuilder.class));
        SearchFilterBuilder.SearchFilter filter = (SearchFilterBuilder.SearchFilter) query.getFilter();
        filter.setType(SearchCategoryImpl.EDGES().getId());
        filter.setQuery(GraphGenerator.FIRST_EDGE);

        GraphView view = filterController.filter(query);
        Subgraph subgraph = graph.getModel().getGraph(view);
        Assert.assertTrue(subgraph.contains(graph.getEdge(GraphGenerator.FIRST_EDGE)));
        Assert.assertEquals(1, subgraph.getEdgeCount());
    }
}
