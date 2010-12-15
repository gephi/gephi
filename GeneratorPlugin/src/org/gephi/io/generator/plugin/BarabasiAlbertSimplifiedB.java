/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
 * http://en.wikipedia.org/wiki/Barabási–Albert_model
 * http://www.barabasilab.com/pubs/CCNR-ALB_Publications/199910-15_Science-Emergence/199910-15_Science-Emergence.pdf
 * http://www.facweb.iitkgp.ernet.in/~niloy/COURSE/Spring2006/CNT/Resource/ba-model-2.pdf
 *
 * N > 0
 * M > 0
 * M <= N * (N - 1) / 2
 *
 * Ω(N * M)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class BarabasiAlbertSimplifiedB implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private int N = 50;
	private int M = 50;

	public void generate(ContainerLoader container) {
		Progress.start(progressTicket, N + M);
		Random random = new Random();
		container.setEdgeDefault(EdgeDefault.UNDIRECTED);

		NodeDraft[] nodes = new NodeDraft[N];
		int[] degrees = new int[N];

		// Creating N nodes
		for (int i = 0; i < N && !cancel; ++i) {
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + i);
			nodes[i] = node;
			degrees[i] = 0;
			container.addNode(node);
			Progress.progress(progressTicket);
		}

		// Creating M edges
		for (int m = 0; m < M && !cancel; ++m) {
			double sum = 0.0; // sum of all nodes degrees
			for (int j = 0; j < N && !cancel; ++j)
				sum += degrees[j] + 1;

			// Selecting a node randomly
			int i = random.nextInt(N);
			while (degrees[i] == N - 1 && !cancel)
				i = random.nextInt(N);

			double  b = random.nextDouble();
			boolean e = false;
			while (!e && !cancel) {
				double pki = 0.0;
				for (int j = 0; j < N && !e && !cancel; ++j) {
					pki += (degrees[j] + 1) / sum;

					if (b <= pki && i != j && !edgeExists(container, nodes[i], nodes[j])) {
						EdgeDraft edge = container.factory().newEdgeDraft();
						edge.setSource(nodes[i]);
						edge.setTarget(nodes[j]);
						degrees[i]++;
						degrees[j]++;
						container.addEdge(edge);

						e = true;
					}
				}
				b = random.nextDouble();
			}

			Progress.progress(progressTicket);
		}

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	private boolean edgeExists(ContainerLoader container, NodeDraft node1, NodeDraft node2) {
		return container.edgeExists(node1, node2) || container.edgeExists(node2, node1);
	}

	public int getN() {
		return N;
	}

	public int getM() {
		return M;
	}

	public void setN(int N) {
		this.N = N;
	}

	public void setM(int M) {
		this.M = M;
	}

	public String getName() {
		return "Barabasi-Albert Scale Free model B (no growth)";
	}

	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(BarabasiAlbertSimplifiedBUI.class);
	}

	public boolean cancel() {
		cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
