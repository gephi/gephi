package org.gephi.desktop.attributes;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * The eligible columns are read on the EDT and must not depend on the graph lock: a long running reader plus a
 * queued writer would otherwise block every new reader, including the EDT. The dynamic flag they depend on is cached
 * and refreshed off the EDT through {@link AttributesUIModelImpl#refreshDynamic()}.
 */
public class AttributesUIModelImplGraphLockTest {

    private static final long TIMEOUT_SECONDS = 5;

    private AttributesUIModelImpl model;
    private GraphModel graphModel;

    @Before
    public void setUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.newProject();
        Workspace workspace = project.getCurrentWorkspace();
        graphModel = GraphModel.Factory.newInstance();
        workspace.add(graphModel);
        model = new AttributesUIModelImpl(workspace);
        Assert.assertSame(graphModel, model.getGraphModel());
    }

    @After
    public void tearDown() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
    }

    @Test
    public void testEligibleColumnsDoNotBlockOnQueuedWriter() throws Exception {
        Graph graph = graphModel.getGraph();
        CountDownLatch readerLocked = new CountDownLatch(1);
        CountDownLatch releaseReader = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            graph.readLock();
            try {
                readerLocked.countDown();
                releaseReader.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                graph.readUnlock();
            }
        });
        reader.setDaemon(true);
        Thread writer = new Thread(() -> {
            graph.writeLock();
            graph.writeUnlock();
        });
        writer.setDaemon(true);
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            reader.start();
            Assert.assertTrue(readerLocked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            writer.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
            while (writer.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
                Thread.yield();
            }
            Assert.assertEquals(Thread.State.WAITING, writer.getState());

            // Reading graphModel.isDynamic() here would take a read lock queued behind the writer
            Future<Integer> nodeCols = executor.submit(() -> model.getEligibleNodeColumns().size());
            Future<Integer> edgeCols = executor.submit(() -> model.getEligibleEdgeColumns().size());
            Future<Integer> cols = executor.submit(() -> model.getEligibleColumns().size());
            Assert.assertTrue(nodeCols.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) > 0);
            Assert.assertNotNull(edgeCols.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            Assert.assertTrue(cols.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) > 0);
        } finally {
            releaseReader.countDown();
            reader.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            writer.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            executor.shutdownNow();
        }
    }

    @Test
    public void testRefreshDynamicUpdatesEligibleColumns() {
        Column inDegree = graphModel.defaultColumns().inDegree();
        Column outDegree = graphModel.defaultColumns().outDegree();
        Column nodeTimeSet = graphModel.defaultColumns().nodeTimeSet();

        // Cached default: not dynamic
        List<Column> initial = model.getEligibleNodeColumns();
        Assert.assertTrue(initial.contains(inDegree));
        Assert.assertTrue(initial.contains(outDegree));
        Assert.assertFalse(initial.contains(nodeTimeSet));
        Assert.assertFalse(model.refreshDynamic());

        // An undirected edge makes the graph not directed, which is read live
        Graph graph = graphModel.getGraph();
        Node n1 = graphModel.factory().newNode("n1");
        Node n2 = graphModel.factory().newNode("n2");
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(graphModel.factory().newEdge(n1, n2, 0, 1.0, false));
        Assert.assertFalse(graphModel.isDirected());

        List<Column> undirected = model.getEligibleNodeColumns();
        Assert.assertFalse(undirected.contains(inDegree));
        Assert.assertFalse(undirected.contains(outDegree));
        Assert.assertTrue(undirected.contains(graphModel.defaultColumns().degree()));
        Assert.assertFalse(model.refreshDynamic());

        // A timestamp makes the graph dynamic, which the columns only follow once refreshed
        n1.addTimestamp(1.0);
        Assert.assertTrue(graphModel.isDynamic());
        Assert.assertFalse(model.getEligibleNodeColumns().contains(nodeTimeSet));
        Assert.assertTrue(model.refreshDynamic());
        Assert.assertTrue(model.getEligibleNodeColumns().contains(nodeTimeSet));
        Assert.assertFalse(model.refreshDynamic());
    }
}
