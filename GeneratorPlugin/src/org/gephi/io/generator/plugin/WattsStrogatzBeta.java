/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.generator.plugin;

import java.util.Random;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Generates an undirected not necessarily connected graph.
 *
 * http://en.wikipedia.org/wiki/Watts_and_Strogatz_model
 * http://tam.cornell.edu/tam/cms/manage/upload/SS_nature_smallworld.pdf
 * http://www.bsos.umd.edu/socy/alan/stats/network-grad/summaries/Watts-Six%20Degrees-Ghosh.pdf
 * http://www.cc.gatech.edu/~mihail/D.8802readings/watts-swp.pdf
 *
 * N > K >= ln(N) >= 1
 * K % 2 == 0
 * 0 <= beta <= 1
 *
 * Ω(N * K)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class WattsStrogatzBeta implements Generator {

    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private int N = 20;
    private int K = 4;
    private double beta = 0.2;

    public void generate(ContainerLoader container) {
        Progress.start(progressTicket, N + N * K);
        Random random = new Random();
        container.setEdgeDefault(EdgeDefault.UNDIRECTED);

        NodeDraft[] nodes = new NodeDraft[N];

        // Creating a regular ring lattice
        for (int i = 0; i < N && !cancel; ++i) {
            NodeDraft node = container.factory().newNodeDraft();
            node.setLabel("Node " + i);
            nodes[i] = node;
            container.addNode(node);
            Progress.progress(progressTicket);
        }
        for (int i = 0; i < N && !cancel; ++i) {
            for (int j = 1; j <= K / 2 && !cancel; ++j) {
                EdgeDraft edge = container.factory().newEdgeDraft();
                edge.setSource(nodes[i]);
                edge.setTarget(nodes[(i + j) % N]);
                container.addEdge(edge);
                Progress.progress(progressTicket);
            }
        }

        // Rewiring edges
        for (int i = 0; i < N && !cancel; ++i) {
            for (int j = 1; j <= K / 2 && !cancel; ++j) {
                if (random.nextDouble() <= beta) {
                    container.removeEdge(getEdge(container, nodes[i], nodes[(i + j) % N]));

                    int k = random.nextInt(N);
                    while ((k == i || edgeExists(container, nodes[i], nodes[k])) && !cancel) {
                        k = random.nextInt(N);
                    }

                    EdgeDraft edge = container.factory().newEdgeDraft();
                    edge.setSource(nodes[i]);
                    edge.setTarget(nodes[k]);
                    container.addEdge(edge);

                    Progress.progress(progressTicket);
                }
            }
        }

        Progress.finish(progressTicket);
        progressTicket = null;
    }

    private boolean edgeExists(ContainerLoader container, NodeDraft node1, NodeDraft node2) {
        return container.edgeExists(node1, node2) || container.edgeExists(node2, node1);
    }

    private EdgeDraft getEdge(ContainerLoader container, NodeDraft node1, NodeDraft node2) {
        EdgeDraft edge = container.getEdge(node1, node2);
        if (edge == null) {
            edge = container.getEdge(node2, node1);
        }
        return edge;
    }

    public int getN() {
        return N;
    }

    public int getK() {
        return K;
    }

    public double getbeta() {
        return beta;
    }

    public void setN(int N) {
        this.N = N;
    }

    public void setK(int K) {
        this.K = K;
    }

    public void setbeta(double beta) {
        this.beta = beta;
    }

    public String getName() {
        return "Watts-Strogatz Small World model Beta";
    }

    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(WattsStrogatzBetaUI.class);
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
