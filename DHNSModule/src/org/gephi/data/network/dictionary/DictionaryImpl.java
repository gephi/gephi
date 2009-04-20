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

package org.gephi.data.network.dictionary;

import java.util.concurrent.ConcurrentHashMap;
import org.gephi.data.network.api.Dictionary;
import org.gephi.data.network.edge.PreEdge;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class DictionaryImpl implements Dictionary {
    private ConcurrentHashMap<String, PreNode> nodeMap;
    private ConcurrentHashMap<String, PreEdge> edgeMap;

    public DictionaryImpl()
    {
        nodeMap = new ConcurrentHashMap<String, PreNode>();
        edgeMap = new ConcurrentHashMap<String, PreEdge>();
    }

    public void addNode(PreNode node)
    {
        nodeMap.put(node.getNode().getLabel(), node);
    }

    public void removeNode(PreNode node)
    {
        nodeMap.remove(node);
    }

    public Node getNode(String label)
    {
        PreNode node = nodeMap.get(label);
        if(node==null)
            return null;
        return node.getNode();
    }

    public void addEdge(PreEdge edge)
    {
        NodeImpl nSource = (NodeImpl)edge.getEdge().getSource();
        NodeImpl nTarget = (NodeImpl)edge.getEdge().getTarget();
        String key = String.valueOf(nSource.getPreNode().getID()) +
                String.valueOf(nTarget.getPreNode().getID());
        edgeMap.put(key, edge);
    }

    public void removeEdge(PreEdge edge)
    {
        NodeImpl nSource = (NodeImpl)edge.getEdge().getSource();
        NodeImpl nTarget = (NodeImpl)edge.getEdge().getTarget();
        String key = String.valueOf(nSource.getPreNode().getID()) +
                String.valueOf(nTarget.getPreNode().getID());
        edgeMap.remove(key);
    }

    public Edge getEdge(Node source, Node target) {
        NodeImpl nSource = (NodeImpl)source;
        NodeImpl nTarget = (NodeImpl)target;
        String key = String.valueOf(nSource.getPreNode().getID()) +
                String.valueOf(nTarget.getPreNode().getID());
        PreEdge edge = edgeMap.get(key);
        if(edge==null)
            return null;
        return edge.getEdge();
    }
}
