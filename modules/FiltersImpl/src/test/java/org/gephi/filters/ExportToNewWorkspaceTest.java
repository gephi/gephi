package org.gephi.filters;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Regression tests for {@link FilterControllerImpl#exportToNewWorkspace(Query)}, which
 * used to read the source filtered view (copyNodes/hasEdge) with no lock at all while
 * mutating the destination graph, racing against a concurrent filter pass replacing the
 * exact same view.
 */
public class ExportToNewWorkspaceTest {

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
     * Builds two connected components: a chain of 3 nodes ("1"-"2"-"3") and an isolated
     * node ("4"). The Giant Component filter must keep only the 3-node component.
     */
    private Query buildGiantComponentQuery() {
        GraphGenerator generator = GraphGenerator.build(workspace);
        GraphModel graphModel = generator.getGraphModel();

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        Node n4 = graphModel.factory().newNode("4");
        Edge e1 = graphModel.factory().newEdge("1", n1, n2, 0, 1.0, false);
        Edge e2 = graphModel.factory().newEdge("2", n2, n3, 0, 1.0, false);

        Graph graph = graphModel.getGraph();
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addNode(n4);
        graph.addEdge(e1);
        graph.addEdge(e2);

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        GiantComponentBuilder builder = Lookup.getDefault().lookup(GiantComponentBuilder.class);
        return filterController.createQuery(builder);
    }

    private Workspace awaitNewWorkspace(Collection<Workspace> before) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> project.getWorkspaces().size() > before.size());
        for (Workspace w : project.getWorkspaces()) {
            if (!before.contains(w)) {
                return w;
            }
        }
        throw new AssertionError("New workspace not found");
    }

    private void assertGiantComponentExported(GraphModel newGraphModel) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> newGraphModel.getGraph().getNodeCount() > 0);
        Graph newGraph = newGraphModel.getGraph();
        assertEquals(3, newGraph.getNodeCount());
        assertEquals(2, newGraph.getEdgeCount());
        assertNotNull(newGraph.getNode("1"));
        assertNotNull(newGraph.getNode("2"));
        assertNotNull(newGraph.getNode("3"));
        assertNull(newGraph.getNode("4"));
    }

    @Test
    public void testExportProducesFilteredSubgraph() {
        //Query is never made "current", so exportToNewWorkspace processes it directly
        //rather than reusing a cached FilterModel result.
        Query query = buildGiantComponentQuery();
        Collection<Workspace> before = new ArrayList<>(project.getWorkspaces());

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        filterController.exportToNewWorkspace(query);

        Workspace newWorkspace = awaitNewWorkspace(before);
        GraphModel newGraphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(newWorkspace);
        assertGiantComponentExported(newGraphModel);
    }

    /**
     * Stress test exercising the locking fix: repeatedly re-runs the model's live
     * FilterThread for the query that exportToNewWorkspace treats as "current" (so the
     * export reuses the model's cached GraphView), forcing many destroyView() calls on
     * that exact view concurrently with the export reading it.
     */
    @Test
    public void testConcurrentFilterChurnDoesNotCorruptExport() throws Exception {
        Query query = buildGiantComponentQuery();
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        FilterModelImpl filterModel = (FilterModelImpl) workspace.getLookup().lookup(FilterModel.class);

        filterController.add(query);
        filterController.filterVisible(query);
        await().atMost(10, TimeUnit.SECONDS).until(() -> filterModel.getCurrentResult() != null);
        FilterThread filterThread = filterModel.getFilterThread();

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean stopChurn = new AtomicBoolean(false);
        Thread churnThread = new Thread(() -> {
            try {
                //No sleep: churns as fast as possible for the whole export loop below,
                //rather than a fixed iteration count that could finish before the export
                //even starts and leave the two operations never actually overlapping.
                while (!stopChurn.get()) {
                    filterThread.setRootQuery((AbstractQueryImpl) query);
                }
            } catch (Throwable ex) {
                failure.compareAndSet(null, ex);
            }
        });
        churnThread.start();

        try {
            int exportRounds = 20;
            for (int i = 0; i < exportRounds; i++) {
                Collection<Workspace> before = new ArrayList<>(project.getWorkspaces());
                filterController.exportToNewWorkspace(query);
                Workspace newWorkspace = awaitNewWorkspace(before);
                GraphModel newGraphModel = graphController.getGraphModel(newWorkspace);
                assertGiantComponentExported(newGraphModel);
            }
        } finally {
            stopChurn.set(true);
            churnThread.join(20_000);
            filterThread.setRunning(false);
            filterThread.join(10_000);
        }

        if (failure.get() != null) {
            throw new AssertionError("Concurrent filter churn / export threw", failure.get());
        }
    }
}
