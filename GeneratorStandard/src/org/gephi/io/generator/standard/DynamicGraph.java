/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.generator.standard;

import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.generator.Generator;
import org.gephi.ui.generator.GeneratorUI;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicGraph implements Generator {

    protected int numberOfNodes = 500;
    protected int numberOfEdges = 2000;

    public void generate(ContainerLoader container) {

        ContainerLoader.ContainerFactory factory = container.factory();

        int maximum = 1000;

        for (int i = 0; i < numberOfNodes; i++) {
            NodeDraft nodeDraft = factory.newNodeDraft();
            nodeDraft.setLabel("Node " + i);
            nodeDraft.setId("Node " + i);
            int from = (int) (Math.random() * (maximum - 100));
            int to = from + (int) Math.random() * (maximum - from);
            nodeDraft.setDynamicFrom(from);
            nodeDraft.setDynamicTo(to);
            container.addNode(nodeDraft);
        }

        for (int i = 0; i < numberOfEdges; i++) {
            EdgeDraft edgeDraft = factory.newEdgeDraft();
            int source = (int) (Math.random() * (numberOfNodes));
            int target = (int) (Math.random() * (numberOfNodes));
            edgeDraft.setSource(container.getNode("Node " + source));
            edgeDraft.setTarget(container.getNode("Node " + target));
            container.addEdge(edgeDraft);
        }
    }

    public String getName() {
        return "Dynamic Graph";
    }

    public GeneratorUI getUI() {
        return null;
    }

    public boolean cancel() {
        return false;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
    }
}
