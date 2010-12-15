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
 * N  > 0
 * m0 > 0
 * M  > 0 && M <= m0
 * 0 <= p < 1
 * 0 <= q < 1 - p
 *
 * Ω(N^2 * M)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class BarabasiAlbertGeneralized implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private int    N  = 50;
	private int    m0 = 1;
	private int    M  = 1;
	private double p  = 0.25;
	private double q  = 0.25;

	public void generate(ContainerLoader container) {
		Progress.start(progressTicket, N);
		Random random = new Random();
		container.setEdgeDefault(EdgeDefault.UNDIRECTED);

		NodeDraft[] nodes = new NodeDraft[N + 1];
		int[] degrees = new int[N + 1];

		// Creating m0 isolated nodes
		for (int i = 0; i < m0 && !cancel; ++i) {
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + i);
			nodes[i] = node;
			degrees[i] = 0;
			container.addNode(node);
		}

		// Performing N steps of the algorithm
		int n  = m0; // the number of existing nodes
		int ec = 0;  // the number of existing edges
		for (int i = 0; i < N && !cancel; ++i) {
			double r = random.nextDouble();

			if (r <= p) { // adding M edges
				if (ec == n * (n - 1) / 2)
					continue;

				double sum = 0.0;
				for (int j = 0; j < n && !cancel; ++j)
					sum += degrees[j] + 1;
				for (int m = 0; m < M && !cancel; ++m) {
					int a = random.nextInt(n);
					while (degrees[a] == n - 1 && !cancel)
						a = random.nextInt(n);
					double  b = random.nextDouble();
					boolean e = false;
					while (!e && !cancel) {
						double pki = 0.0;
						for (int j = 0; j < n && !e && !cancel; ++j) {
							pki += (degrees[j] + 1) / sum;

							if (b <= pki && a != j && !edgeExists(container, nodes[a], nodes[j])) {
								EdgeDraft edge = container.factory().newEdgeDraft();
								edge.setSource(nodes[a]);
								edge.setTarget(nodes[j]);
								degrees[a]++;
								degrees[j]++;
								sum += 2.0;
								container.addEdge(edge);
								ec++;
								
								e = true;
							}
							else if (ec == n * (n - 1) / 2)
								e = true;
						}
						b = random.nextDouble();
					}
				}
			}
			else if (r <= p + q) { // rewiring M edges
				if (ec == 0 || ec == n * (n - 1) / 2)
					continue;

				double sum = 0.0;
				for (int j = 0; j < n && !cancel; ++j)
					sum += degrees[j] + 1;
				for (int m = 0; m < M && !cancel; ++m) {
					int a = random.nextInt(n);
					while ((degrees[a] == 0 || degrees[a] == n - 1) && !cancel)
						a = random.nextInt(n);
					int l = random.nextInt(n);
					while (!edgeExists(container, nodes[l], nodes[a]) && !cancel)
						l = random.nextInt(n);
					double  b = random.nextDouble();
					boolean e = false;
					while (!e && !cancel) {
						double pki = 0.0;
						for (int j = 0; j < n && !e && !cancel; ++j) {
							pki += (degrees[j] + 1) / sum;

							if (b <= pki && a != j && !edgeExists(container, nodes[a], nodes[j])) {
								container.removeEdge(getEdge(container, nodes[a], nodes[l]));
								degrees[l]--;

								EdgeDraft edge = container.factory().newEdgeDraft();
								edge.setSource(nodes[a]);
								edge.setTarget(nodes[j]);
								degrees[j]++;
								container.addEdge(edge);

								e = true;
							}
						}
						b = random.nextDouble();
					}
				}
			}
			else { // adding a new node with M edges
				NodeDraft node = container.factory().newNodeDraft();
				node.setLabel("Node " + n);
				nodes[n] = node;
				degrees[n] = 0;
				container.addNode(node);

				// Adding M edges out of the new node
				double sum = 0.0;
				for (int j = 0; j < n && !cancel; ++j)
					sum += degrees[j];
				double s = 0.0;
				for (int m = 0; m < M && !cancel; ++m) {
					r = random.nextDouble();
					double p = 0.0;
					for (int j = 0; j < n && !cancel; ++j) {
						if (edgeExists(container, nodes[n], nodes[j]))
							continue;

						if (n == 1)
							p = 1.0;
						else p += degrees[j] / sum + s / (n - m);

						if (r <= p) {
							s += degrees[j] / sum;

							EdgeDraft edge = container.factory().newEdgeDraft();
							edge.setSource(nodes[n]);
							edge.setTarget(nodes[j]);
							degrees[n]++;
							degrees[j]++;
							container.addEdge(edge);
							ec++;

							break;
						}
					}
				}
				
				n++;
			}

			Progress.progress(progressTicket);
		}

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	private boolean edgeExists(ContainerLoader container, NodeDraft node1, NodeDraft node2) {
		return container.edgeExists(node1, node2) || container.edgeExists(node2, node1);
	}

	private EdgeDraft getEdge(ContainerLoader container, NodeDraft node1, NodeDraft node2) {
		EdgeDraft edge = container.getEdge(node1, node2);
		if (edge == null)
			edge = container.getEdge(node2, node1);
		return edge;
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

	public double getp() {
		return p;
	}

	public double getq() {
		return q;
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

	public void setp(double p) {
		this.p = p;
	}

	public void setq(double q) {
		this.q = q;
	}

	public String getName() {
		return "Generalized Barabasi-Albert Scale Free model";
	}

	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(BarabasiAlbertGeneralizedUI.class);
	}

	public boolean cancel() {
		cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
