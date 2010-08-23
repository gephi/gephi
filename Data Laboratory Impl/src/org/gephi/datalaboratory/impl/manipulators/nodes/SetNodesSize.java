/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.nodes;

import javax.swing.Icon;
import org.gephi.datalaboratory.impl.manipulators.nodes.ui.SetNodesSizeUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that sets a given size for all the selected nodes.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SetNodesSize implements NodesManipulator {

    private Node[] nodes;
    private float size = 1.0f;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
        size=clickedNode.getNodeData().getSize();//Show size of the clicked node in UI
    }

    public void execute() {
        for(Node node:nodes){
            node.getNodeData().setSize(size);
        }
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(SetNodesSize.class, "SetNodesSize.name.multiple");
        } else {
            return NbBundle.getMessage(SetNodesSize.class, "SetNodesSize.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return new SetNodesSizeUI();
    }

    public int getType() {
        return 400;
    }

    public int getPosition() {
        return 2112;//We are the priests of the temples of syrinx!
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/size.png", true);
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
