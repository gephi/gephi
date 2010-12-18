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

import java.util.ArrayList;
import java.util.List;
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
 * Generates a perfectly balanced r-tree of height h (edges are undirected).
 *
 * r >= 2
 * h >= 1
 *
 * O(r^h)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class BalancedTree implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private int r = 2;
	private int h = 4;

	public void generate(ContainerLoader container) {
		int n = ((int)Math.pow(r, h + 1) - 1) / (r - 1);

		Progress.start(progressTicket, n - 1);
		container.setEdgeDefault(EdgeDefault.UNDIRECTED);

		// Creating a root of degree r
		NodeDraft root = container.factory().newNodeDraft();
		root.setLabel("Node 0");
		container.addNode(root);
		List<NodeDraft> newLeaves = new ArrayList<NodeDraft>();
		int v = 1;
		for (int i = 0; i < r && !cancel; ++i) {
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + v++);
			newLeaves.add(node);
			container.addNode(node);

			EdgeDraft edge = container.factory().newEdgeDraft();
			edge.setSource(root);
			edge.setTarget(node);
			container.addEdge(edge);
			
			Progress.progress(progressTicket);
		}

		// Creating internal nodes
		for (int height = 1; height < h && !cancel; ++height) {
			List<NodeDraft> leaves = newLeaves;
			newLeaves = new ArrayList<NodeDraft>();
			for (NodeDraft leave : leaves)
				for (int i = 0; i < r; ++i) {
					NodeDraft node = container.factory().newNodeDraft();
					node.setLabel("Node " + v++);
					newLeaves.add(node);
					container.addNode(node);

					EdgeDraft edge = container.factory().newEdgeDraft();
					edge.setSource(leave);
					edge.setTarget(node);
					container.addEdge(edge);

					Progress.progress(progressTicket);
				}
		}

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	public int getr() {
		return r;
	}

	public int geth() {
		return h;
	}

	public void setr(int r) {
		this.r = r;
	}

	public void seth(int h) {
		this.h = h;
	}

	public String getName() {
		return "Balanced Tree";
	}

	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(BalancedTreeUI.class);
	}

	public boolean cancel() {
		cancel = true;
		return true;
	}

	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
