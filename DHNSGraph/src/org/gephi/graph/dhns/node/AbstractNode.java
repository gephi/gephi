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
package org.gephi.graph.dhns.node;

import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;

/**
 * Implementation of node with default behaviour.
 *
 * @author Mathieu Bastian
 */
public class AbstractNode implements Node, AVLItem {

    private static int IDGen = 0;
    protected final int ID;
    protected NodeDataImpl nodeData;

    public AbstractNode() {
        ID = IDGen++;
        nodeData = new NodeDataImpl(this);
    }

    public int getNumber() {
        return ID;
    }

    public NodeData getNodeData() {
        return nodeData;
    }

    public boolean hasAttributes() {
        return nodeData.getAttributes() == null;
    }

    public void setAttributes(Attributes attributes) {
        if (attributes != null) {
            nodeData.setAttributes(attributes);
        }
    }

    public int getId() {
        return ID;
    }
}