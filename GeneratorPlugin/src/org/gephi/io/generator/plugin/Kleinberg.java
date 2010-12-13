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
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.117.7097&rep=rep1&type=pdf
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.83.381&rep=rep1&type=pdf
 *
 * n >= 2
 * p >= 1
 * p <= 2n - 2
 * q >= 0
 * q <= n^2 - p * (p + 3) / 2 - 1 for p < n
 * q <= (2n - p - 3) * (2n - p) / 2 + 1 for p >= n
 * r >= 0
 *
 * Ω(n^4 * q)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class Kleinberg implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private int n = 10;
	private int p = 2;
	private int q = 2;
	private int r = 0;

	public void generate(ContainerLoader container) {
		Progress.start(progressTicket, n * n + n * n * (2 * p + 1) * (2 * p + 1) +
				(int)Math.pow(n, 4) + n * n * q);
		Random random = new Random();

		// Creating lattice n x n
		NodeDraft[][] nodes = new NodeDraft[n][n];
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j) {
				NodeDraft node = container.factory().newNodeDraft();
				node.setLabel("Node " + i + " " + j);
				nodes[i][j] = node;
				container.addNode(node);
				Progress.progress(progressTicket);
			}

		// Creating edges from each node to p local contacts
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j)
				for (int k = i - p; k <= i + p && !cancel; ++k)
					for (int l = j - p; l <= j + p && !cancel; ++l) {
						if (k >= 0 && k < n && l >= 0 && l < n && d(i, j, k, l) <= p && nodes[i][j] != nodes[k][l]) {
							EdgeDraft edge = container.factory().newEdgeDraft();
							edge.setSource(nodes[i][j]);
							edge.setTarget(nodes[k][l]);
							container.addEdge(edge);
						}
						Progress.progress(progressTicket);
					}

		// Creating edges from each node to q long-range contacts
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j) {
				double sum = 0.0;
				for (int k = 0; k < n && !cancel; ++k)
					for (int l = 0; l < n && !cancel; ++l) {
						if (d(i, j, k, l) > p)
							sum += Math.pow(d(i, j, k, l), -r);
						Progress.progress(progressTicket);
					}
				for (int m = 0; m < q && !cancel; ++m) {
					double  b = random.nextDouble();
					boolean e = false;
					while (!e && !cancel) {
						double pki = 0.0;
						for (int k = 0; k < n && !e && !cancel; ++k)
							for (int l = 0; l < n && !e && !cancel; ++l)
								if (d(i, j, k, l) > p) {
									pki += Math.pow(d(i, j, k, l), -r) / sum;

									if (b <= pki && !container.edgeExists(nodes[i][j], nodes[k][l])) {
										EdgeDraft edge = container.factory().newEdgeDraft();
										edge.setSource(nodes[i][j]);
										edge.setTarget(nodes[k][l]);
										container.addEdge(edge);

										e = true;
									}
								}
						b = random.nextDouble();
					}
					Progress.progress(progressTicket);
				}
			}

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	private int d(int i, int j, int k, int l) {
		return Math.abs(k - i) + Math.abs(l - j);
	}

	public int getn() {
		return n;
	}

	public int getp() {
		return p;
	}

	public int getq() {
		return q;
	}

	public int getr() {
		return r;
	}

	public void setn(int n) {
		this.n = n;
	}

	public void setp(int p) {
		this.p = p;
	}

	public void setq(int q) {
		this.q = q;
	}

	public void setr(int r) {
		this.r = r;
	}

	public String getName() {
		return "Kleinberg Small World model";
	}

	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(KleinbergUI.class);
	}

	public boolean cancel() {
		cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
