package org.gephi.io.generator.plugin;

import java.util.Random;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Scale-free graph generator based on the Barabási–Albert preferential attachment model.
 * <p>
 * The graph starts from a star of <code>m + 1</code> nodes. Each new node is then connected to <code>m</code>
 * distinct existing nodes, each picked with a probability proportional to its degree. The result is a simple
 * connected graph with <code>m + (n - m - 1) * m</code> edges and a degree distribution following P(k) ~ k^-3.
 * <p>
 * Barabási, A.-L. and Albert, R. "Emergence of scaling in random networks", Science 286, 1999.
 */
@ServiceProvider(service = Generator.class)
public class ScaleFreeGraph implements Generator {

    protected int numberOfNodes = 100;
    protected int edgesPerNode = 2;
    protected Long seed = null;
    protected ProgressTicket progress;
    protected volatile boolean cancel = false;

    @Override
    public void generate(ContainerLoader container) {
        cancel = false;

        if (edgesPerNode >= numberOfNodes) {
            throw new IllegalStateException("# of edges per node must be lower than the # of nodes");
        }

        int m = edgesPerNode;
        long listSize = 2L * (m + ((long) numberOfNodes - m - 1) * m);
        if (listSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The graph is too large, reduce the # of nodes or edges per node");
        }
        Progress.start(progress, 2 * numberOfNodes - m - 1);
        int progressUnit = 0;
        Random random = seed != null ? new Random(seed) : new Random();

        NodeDraft[] nodeArray = new NodeDraft[numberOfNodes];
        for (int i = 0; i < numberOfNodes && !cancel; i++) {
            NodeDraft nodeDraft = container.factory().newNodeDraft();
            container.addNode(nodeDraft);
            nodeArray[i] = nodeDraft;
            Progress.progress(progress, ++progressUnit);
        }

        // Each node appears once per incident edge, so drawing uniformly from this list is the same as drawing
        // a node with a probability proportional to its degree.
        int[] repeatedNodes = new int[(int) listSize];
        int size = 0;

        // Initial star: node 0 linked to nodes 1..m
        for (int i = 1; i <= m && !cancel; i++) {
            addEdge(container, nodeArray[0], nodeArray[i]);
            repeatedNodes[size++] = 0;
            repeatedNodes[size++] = i;
        }

        int[] targets = new int[m];
        for (int source = m + 1; source < numberOfNodes && !cancel; source++) {
            // Draw m distinct targets, redrawing duplicates
            int count = 0;
            while (count < m) {
                int candidate = repeatedNodes[random.nextInt(size)];
                boolean duplicate = false;
                for (int j = 0; j < count; j++) {
                    if (targets[j] == candidate) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    targets[count++] = candidate;
                }
            }
            for (int j = 0; j < m; j++) {
                addEdge(container, nodeArray[source], nodeArray[targets[j]]);
                repeatedNodes[size++] = targets[j];
                repeatedNodes[size++] = source;
            }
            Progress.progress(progress, ++progressUnit);
        }

        Progress.finish(progress);
        progress = null;
    }

    private void addEdge(ContainerLoader container, NodeDraft source, NodeDraft target) {
        EdgeDraft edgeDraft = container.factory().newEdgeDraft();
        edgeDraft.setSource(source);
        edgeDraft.setTarget(target);
        container.addEdge(edgeDraft);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ScaleFreeGraph.class, "ScaleFreeGraph.name");
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(ScaleFreeGraphUI.class);
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 2) {
            throw new IllegalArgumentException("# of nodes must be greater than 1");
        }
        this.numberOfNodes = numberOfNodes;
    }

    public int getEdgesPerNode() {
        return edgesPerNode;
    }

    public void setEdgesPerNode(int edgesPerNode) {
        if (edgesPerNode < 1) {
            throw new IllegalArgumentException("# of edges per node must be greater than 0");
        }
        this.edgesPerNode = edgesPerNode;
    }

    /**
     * Returns the random seed, or <code>null</code> if each generation should be random.
     *
     * @return the random seed or null
     */
    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
