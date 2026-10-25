package org.gephi.releasetests;

import java.awt.Color;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

/**
 * Several threads change and read a graph with the Overview showing and ForceAtlas 2 running, while a thread runs the
 * statistics, filter, screenshot, export and workspace switch a user would, one after the other. It checks that none
 * of these fails, logs an error, deadlocks or leaves the Event Dispatch Thread, the Overview or the graph lock
 * waiting, and that the graph is as it was once every thread is done.
 */
final class SoakSuite implements Suite {

    static final Duration DURATION = Duration.ofSeconds(90);

    private static final int NODES = 2_000;
    private static final int BLOCKS = 8;
    private static final int EDGES_PER_NODE = 5;
    private static final int WRITERS = 5;

    private final Random random = new Random(1);

    @Override
    public void run(SuiteContext c) throws Exception {
        Workspace workspace = c.step("generate", () -> generate(c));
        GraphModel graphModel = c.graphController.getGraphModel(workspace);
        Graph graph = graphModel.getGraph();
        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();
        c.expectRendering(true);

        c.step("layout", () -> c.startLayout(ForceAtlas2Builder.class));

        Workspace other = c.step("duplicate workspace", () -> {
            Workspace duplicate = c.projectController.duplicateWorkspace(workspace);
            // The statistics and filters run on the current workspace
            SuiteContext.onEdt(() -> c.projectController.openWorkspace(workspace));
            SuiteContext.check(c.projectController.getCurrentWorkspace() == workspace,
                "The workspace isn't the current one");
            return duplicate;
        });

        c.step("load", () -> new Load(c, workspace, other).run(DURATION));

        c.step("end state", () -> {
            c.stopLayout();
            c.checkLocksReleased(graph);
            SuiteContext.check(graph.getNodeCount() == nodeCount && graph.getEdgeCount() == edgeCount,
                "The graph has " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges instead of "
                    + nodeCount + " and " + edgeCount + " once the load is done");
            SuiteContext.checkNotBlank(c.overviewScreenshot("soak-end.png"), "The Overview screenshot");
        });

        c.step("close", () -> {
            c.expectRendering(false);
            c.projectController.closeCurrentProject();
        });
    }

    /**
     * A new project with a graph of blocks, denser inside than between them, where each node has a block and a score.
     */
    private Workspace generate(SuiteContext c) {
        Workspace workspace = c.projectController.newProject().getCurrentWorkspace();
        GraphModel graphModel = c.graphController.getGraphModel(workspace);
        graphModel.getNodeTable().addColumn(Load.BLOCK, String.class);
        graphModel.getNodeTable().addColumn(Load.SCORE, Double.class);
        GraphFactory factory = graphModel.factory();
        Graph graph = graphModel.getGraph();
        Node[] nodes = new Node[NODES];
        graph.writeLock();
        try {
            for (int i = 0; i < NODES; i++) {
                Node node = factory.newNode("n" + i);
                node.setLabel("Node " + i);
                node.setAttribute(Load.BLOCK, "b" + i % BLOCKS);
                node.setAttribute(Load.SCORE, random.nextDouble());
                node.setX(random.nextFloat() * 1000 - 500);
                node.setY(random.nextFloat() * 1000 - 500);
                graph.addNode(node);
                nodes[i] = node;
            }
            for (int i = 0; i < NODES; i++) {
                for (int e = 0; e < EDGES_PER_NODE; e++) {
                    // Four edges in five stay in the node's block
                    int target = random.nextInt(5) != 0
                        ? (random.nextInt(NODES / BLOCKS) * BLOCKS + i % BLOCKS)
                        : random.nextInt(NODES);
                    if (target != i && graph.getEdge(nodes[i], nodes[target]) == null) {
                        graph.addEdge(factory.newEdge(nodes[i], nodes[target], false));
                    }
                }
            }
        } finally {
            graph.writeUnlock();
        }
        return workspace;
    }

    /**
     * What the threads do for a given time, started together and joined at the end. The first failure ends the load
     * and fails it.
     */
    private static final class Load {

        static final String BLOCK = "block";
        static final String SCORE = "score";

        private final SuiteContext c;
        private final Workspace workspace;
        private final Workspace other;
        private final Graph graph;
        private final GraphFactory factory;
        private final Node[] nodes;
        private final AtomicReference<Throwable> failure = new AtomicReference<>();
        private final AtomicLong operations = new AtomicLong();
        private volatile boolean running;

        Load(SuiteContext c, Workspace workspace, Workspace other) {
            this.c = c;
            this.workspace = workspace;
            this.other = other;
            GraphModel graphModel = c.graphController.getGraphModel(workspace);
            this.graph = graphModel.getGraph();
            this.factory = graphModel.factory();
            this.nodes = graph.getNodes().toArray();
        }

        void run(Duration duration) throws Exception {
            running = true;
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < WRITERS; i++) {
                int seed = i;
                threads.add(thread("Release Tests Soak Writer " + i, () -> write(new Random(seed))));
            }
            threads.add(thread("Release Tests Soak Reader", () -> read(new Random(100))));
            threads.add(thread("Release Tests Soak User", this::useFeatures));
            threads.forEach(Thread::start);

            long end = System.nanoTime() + duration.toNanos();
            while (System.nanoTime() < end && failure.get() == null) {
                Thread.sleep(100);
            }
            running = false;
            for (Thread thread : threads) {
                thread.join(SuiteContext.TIMEOUT.toMillis() * 2);
                SuiteContext.check(!thread.isAlive(), thread.getName() + " didn't end");
            }
            c.metric("soakOperations", operations.get());
            Throwable t = failure.get();
            if (t != null) {
                throw new AssertionError("The load failed: " + t, t);
            }
        }

        /**
         * Leaves the others a turn, as calls coming from outside the process would.
         */
        private static void pause() throws InterruptedException {
            Thread.sleep(1);
        }

        private Thread thread(String name, SuiteContext.Body body) {
            Thread thread = new Thread(() -> {
                try {
                    body.run();
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }, name);
            thread.setDaemon(true);
            return thread;
        }

        /**
         * Changes the attributes, colours, sizes and positions of the nodes there from the start, and adds nodes
         * then removes them.
         */
        private void write(Random random) throws InterruptedException {
            List<Node> added = new ArrayList<>();
            while (running) {
                Node node = nodes[random.nextInt(nodes.length)];
                graph.writeLock();
                try {
                    switch (random.nextInt(6)) {
                        case 0 -> node.setLabel("Label " + random.nextInt());
                        case 1 -> node.setAttribute(SCORE, random.nextDouble());
                        case 2 -> node.setColor(new Color(random.nextInt(0x1000000)));
                        case 3 -> node.setSize(5 + random.nextFloat() * 25);
                        case 4 -> {
                            for (int i = 0; i < 50; i++) {
                                Node moved = nodes[random.nextInt(nodes.length)];
                                moved.setX(random.nextFloat() * 1000 - 500);
                                moved.setY(random.nextFloat() * 1000 - 500);
                            }
                        }
                        default -> {
                            if (added.size() < 20) {
                                Node created = factory.newNode();
                                graph.addNode(created);
                                graph.addEdge(factory.newEdge(created, node, false));
                                added.add(created);
                            } else {
                                added.forEach(graph::removeNode);
                                added.clear();
                            }
                        }
                    }
                } finally {
                    graph.writeUnlock();
                }
                operations.incrementAndGet();
                pause();
            }
            graph.writeLock();
            try {
                added.forEach(graph::removeNode);
            } finally {
                graph.writeUnlock();
            }
        }

        /**
         * Reads the graph the ways the interface does, including walking some nodes without finishing the iteration.
         */
        private void read(Random random) throws InterruptedException {
            while (running) {
                Node node = nodes[random.nextInt(nodes.length)];
                switch (random.nextInt(3)) {
                    case 0 -> {
                        NodeIterable iterable = graph.getNodes();
                        int seen = 0;
                        for (Node ignored : iterable) {
                            if (++seen == 50) {
                                iterable.doBreak();
                                break;
                            }
                        }
                    }
                    case 1 -> graph.getNeighbors(node).toArray();
                    default -> graph.getDegree(node);
                }
                operations.incrementAndGet();
                pause();
            }
        }

        /**
         * Does what a user does from the panels, in turn, none of it twice at once.
         */
        private void useFeatures() throws Exception {
            AppearanceController appearance = Lookup.getDefault().lookup(AppearanceController.class);
            AppearanceModel appearanceModel = appearance.getModel(workspace);
            GraphModel graphModel = c.graphController.getGraphModel(workspace);
            Column block = graphModel.getNodeTable().getColumn(BLOCK);
            Column score = graphModel.getNodeTable().getColumn(SCORE);
            int round = 0;
            while (running) {
                appearance.transform(appearanceModel.getNodeFunction(block, PartitionElementColorTransformer.class));
                appearance.transform(appearanceModel.getNodeFunction(score, RankingNodeSizeTransformer.class));

                c.runStatistics(new Degree());
                if (round % 2 == 0) {
                    c.runStatistics(new Modularity());
                }

                DegreeRangeBuilder.DegreeRangeFilter filter = new DegreeRangeBuilder.DegreeRangeFilter();
                filter.init(graph);
                filter.setRange(new Range(5, Integer.MAX_VALUE));
                Query query = c.filterController.createQuery(filter);
                SuiteContext.onEdt(() -> c.filterController.add(query));
                c.filterVisible(query);
                c.filterVisible(null);
                SuiteContext.onEdt(() -> c.filterController.remove(query));

                SuiteContext.checkNotBlank(c.overviewScreenshot("soak.png"), "The Overview screenshot");
                File png = c.file("soak-export.png");
                c.exportController.exportFile(png);
                SuiteContext.checkNotBlank(ImageIO.read(png), "The PNG export");

                SuiteContext.onEdt(() -> c.projectController.openWorkspace(other));
                Thread.sleep(2_000);
                SuiteContext.onEdt(() -> c.projectController.openWorkspace(workspace));
                round++;
                operations.incrementAndGet();
            }
        }
    }
}
