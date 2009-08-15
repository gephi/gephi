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
import org.gephi.io.container.NodeDraft;
import org.gephi.io.generator.Generator;
import org.gephi.ui.generator.GeneratorUI;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalGraph implements Generator {

    public void generate(ContainerLoader container) {

        ContainerLoader.DraftFactory factory = container.factory();

        NodeDraft nodeA = factory.newNodeDraft();
        nodeA.setLabel("Node A");
        NodeDraft nodeB = factory.newNodeDraft();
        nodeB.setLabel("Node B");
        NodeDraft nodeC = factory.newNodeDraft();
        nodeC.setLabel("Node C");
        NodeDraft nodeD = factory.newNodeDraft();
        nodeD.setLabel("Node D");
        NodeDraft nodeE = factory.newNodeDraft();
        nodeE.setLabel("Node E");
        NodeDraft nodeF = factory.newNodeDraft();
        nodeF.setLabel("Node F");
        NodeDraft nodeG = factory.newNodeDraft();
        nodeG.setLabel("Node G");
        NodeDraft nodeH = factory.newNodeDraft();
        nodeH.setLabel("Node H");

        nodeA.addChild(nodeC);
        nodeA.addChild(nodeD);
        nodeA.addChild(nodeH);
        nodeB.addChild(nodeD);
        nodeB.addChild(nodeE);
        nodeB.addChild(nodeF);
        nodeB.addChild(nodeG);

        container.addNode(nodeA);
        container.addNode(nodeB);
        container.addNode(nodeC);
        container.addNode(nodeD);
        container.addNode(nodeE);
        container.addNode(nodeF);
        container.addNode(nodeG);
        container.addNode(nodeH);
    }

    public String getName() {
        return "Hierarchical Graph";
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
