package org.gephi.desktop.attributes.selection;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import org.gephi.desktop.attributes.AttributesUIModelImpl;
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
 * The degrees of a filtered view need the graph read lock. The selection panel runs on the EDT and must not wait for
 * it: a long running reader plus a queued writer would otherwise block every new reader.
 */
public class SelectionPanelGraphLockTest {

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

        Graph graph = graphModel.getGraph();
        Node n1 = graphModel.factory().newNode("n1");
        Node n2 = graphModel.factory().newNode("n2");
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(graphModel.factory().newEdge(n1, n2, 0, 1.0, true));
        graphModel.setVisibleView(graphModel.createView());

        model = new AttributesUIModelImpl(workspace);
        model.setColumnHidden(graphModel.defaultColumns().degree(), false);
        model.setSelectedNodes(new Node[] {n1});
    }

    @After
    public void tearDown() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
    }

    @Test
    public void testDegreesAreShownWhenTheLockIsFree() {
        SelectionPanel panel = new SelectionPanel();
        panel.refreshSelectedNodes(model);
        Assert.assertTrue(labelTexts(panel).contains("0"));
    }

    @Test
    public void testDegreesDoNotBlockOnQueuedWriter() throws Exception {
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

            SelectionPanel panel = new SelectionPanel();
            executor.submit(() -> panel.refreshSelectedNodes(model)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assert.assertTrue(labelTexts(panel).contains("..."));
        } finally {
            releaseReader.countDown();
            reader.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            writer.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            executor.shutdownNow();
        }
    }

    private static List<String> labelTexts(Container container) {
        List<String> texts = new ArrayList<>();
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label) {
                texts.add(label.getText());
            }
            if (component instanceof Container child) {
                texts.addAll(labelTexts(child));
            }
        }
        return texts;
    }
}
