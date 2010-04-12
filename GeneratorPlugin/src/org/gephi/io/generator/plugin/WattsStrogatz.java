/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Generator.class)
public class WattsStrogatz implements Generator {

    protected int numberOfNodes = 50;
    protected int numberOfNeighbors = 3;
    protected double rewiringProbability = 0.5;
    protected ProgressTicket progress;
    protected boolean cancel = false;

    public void generate(ContainerLoader container) {
        Progress.start(progress, numberOfNodes);
        Random random = new Random();

        //Create ring lattice
        for (int i = 0; i < numberOfNodes && !cancel; i++) {
            NodeDraft node = container.factory().newNodeDraft();
            node.setLabel("Node " + i);
            node.setId("" + i);
            container.addNode(node);
        }
        for (int i = 0; i < numberOfNodes && !cancel; i++) {
            for (int j = 0; j < numberOfNeighbors; j++) {
                EdgeDraft edge = container.factory().newEdgeDraft();
                edge.setSource(container.getNode("" + i));
                edge.setTarget(container.getNode("" + ((i + (numberOfNeighbors - j)) % numberOfNodes)));
                container.addEdge(edge);
            }
        }

        //Rewire edges
        for (int i = 0; i < numberOfNodes && !cancel; i++) {
            for (int s = 1; s <= numberOfNeighbors && !cancel; s++) {
                while (true) {
                    // randomly rewire a proportion, beta, of the edges in the graph.
                    double r = random.nextDouble();
                    if (r < rewiringProbability) {
                        int v = random.nextInt(numberOfNeighbors);

                        NodeDraft vthNode = container.getNode("" + v);
                        NodeDraft ithNode = container.getNode("" + i);
                        NodeDraft kthNode = container.getNode("" + ((i + s) % numberOfNodes));//upIndex(i, s));
                        EdgeDraft e = container.getEdge(ithNode, kthNode);

                        if (kthNode != vthNode && container.getEdge(kthNode, vthNode) == null) {
                            container.removeEdge(e);
                            EdgeDraft edgeDraft = container.factory().newEdgeDraft();
                            edgeDraft.setSource(kthNode);
                            edgeDraft.setTarget(vthNode);
                            container.addEdge(edgeDraft);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            Progress.progress(progress);
        }

        Progress.finish(progress);
        progress = null;
    }

    public int getNumberOfNeighbors() {
        return numberOfNeighbors;
    }

    public void setNumberOfNeighbors(int numberOfNeighbors) {
        if (numberOfNeighbors < 2 || numberOfNeighbors > numberOfNodes / 2) {
            throw new IllegalArgumentException("Neighbors must be between 2 and numberOfNodes / 2");
        }
        this.numberOfNeighbors = numberOfNeighbors;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public double getRewiringProbability() {
        return rewiringProbability;
    }

    public void setRewiringProbability(double rewiringProbability) {
        if (rewiringProbability < 0 || rewiringProbability > 1) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        this.rewiringProbability = rewiringProbability;
    }

    public String getName() {
        return "Watts-Strogatz Small World";
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(WattsStrogatzUI.class);
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
