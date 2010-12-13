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
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Generates a directed connected graph.
 *
 * http://en.wikipedia.org/wiki/Barabási–Albert_model
 * http://www.barabasilab.com/pubs/CCNR-ALB_Publications/199910-15_Science-Emergence/199910-15_Science-Emergence.pdf
 * http://www.facweb.iitkgp.ernet.in/~niloy/COURSE/Spring2006/CNT/Resource/ba-model-2.pdf
 *
 * N  > 0
 * m0 > 0 && m0 <  N
 * M  > 0 && M  <= m0
 *
 * O(N^2 * M)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class BarabasiAlbert implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private int N  = 50;
	private int m0 = 1;
	private int M  = 1;

	public void generate(ContainerLoader container) {
		Progress.start(progressTicket, N + M);
		Random random = new Random();

		NodeDraft[] nodes = new NodeDraft[N];
		int[] degrees = new int[N];

		// Creating m0 nodes
		for (int i = 0; i < m0 && !cancel; ++i) {
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + i);
			nodes[i] = node;
			degrees[i] = 0;
			container.addNode(node);
			Progress.progress(progressTicket);
		}

		// Linking every node with each other (no self-loops)
		for (int i = 0; i < m0 && !cancel; ++i)
			for (int j = i + 1; j < m0 && !cancel; ++j) {
				EdgeDraft edge = container.factory().newEdgeDraft();
				edge.setSource(nodes[i]);
				edge.setTarget(nodes[j]);
				degrees[i]++;
				degrees[j]++;
				container.addEdge(edge);
				Progress.progress(progressTicket);
			}

		// Adding N - m0 nodes, each with M edges
		for (int i = m0; i < N && !cancel; ++i) {
			// Adding new node
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + i);
			nodes[i] = node;
			degrees[i] = 0;
			container.addNode(node);

			// Adding M edges out of the new node
			double sum = 0.0; // sum of all nodes degrees
			for (int j = 0; j < i && !cancel; ++j)
				sum += degrees[j];
			double s = 0.0;
			for (int m = 0; m < M && !cancel; ++m) {
				double r = random.nextDouble();
				double p = 0.0;
				for (int j = 0; j < i && !cancel; ++j) {
					if (container.edgeExists(nodes[i], nodes[j]) || container.edgeExists(nodes[j], nodes[i]))
						continue;

					if (i == 1)
						p = 1.0;
					else p += degrees[j] / sum + s / (i - m);

					if (r <= p) {
						s += degrees[j] / sum;

						EdgeDraft edge = container.factory().newEdgeDraft();
						edge.setSource(nodes[i]);
						edge.setTarget(nodes[j]);
						degrees[i]++;
						degrees[j]++;
						container.addEdge(edge);
						Progress.progress(progressTicket);

						break;
					}
				}
			}

			Progress.progress(progressTicket);
		}

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	public int getN() {
		return N;
	}

	public int getm0() {
		return m0;
	}

	public int getM() {
		return M;
	}

	public void setN(int N) {
		this.N = N;
	}

	public void setm0(int m0) {
		this.m0 = m0;
	}

	public void setM(int M) {
		this.M = M;
	}

	public String getName() {
		return "Barabasi-Albert Scale Free model";
	}

	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(BarabasiAlbertUI.class);
	}

	public boolean cancel() {
		cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
