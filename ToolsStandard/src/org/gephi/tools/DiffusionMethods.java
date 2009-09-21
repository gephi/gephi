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
package org.gephi.tools;

import java.util.ArrayList;
import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class DiffusionMethods {

    public static Node[] getNeighbors(Node[] nodes, boolean withDoubles) {
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        DirectedGraph graph = gc.getVisibleDirectedGraph();
        if (withDoubles) {
            ArrayList<Node> nodeList = new ArrayList<Node>();
            for (Node n : nodes) {
                for (Node neighbor : graph.getNeighbors(n).toArray()) {
                    nodeList.add(neighbor);
                }
            }
            return nodeList.toArray(new Node[0]);
        } else {
            NodeTree nodeTree = new NodeTree();
            for (Node n : nodes) {
                for (Node neighbor : graph.getNeighbors(n).toArray()) {
                    nodeTree.add(neighbor);
                }
            }
            return nodeTree.toArray(new Node[0]);
        }
    }

    public static Node[] getPredecessors(Node[] nodes) {
        NodeTree nodeTree = new NodeTree();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        DirectedGraph graph = gc.getVisibleDirectedGraph();
        for (Node n : nodes) {
            for (Node neighbor : graph.getPredecessors(n).toArray()) {
                nodeTree.add(neighbor);
            }
        }
        return nodeTree.toArray(new Node[0]);
    }

    public static Node[] getSuccessors(Node[] nodes) {
        NodeTree nodeTree = new NodeTree();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        DirectedGraph graph = gc.getVisibleDirectedGraph();
        for (Node n : nodes) {
            for (Node neighbor : graph.getSuccessors(n).toArray()) {
                nodeTree.add(neighbor);
            }
        }
        return nodeTree.toArray(new Node[0]);
    }

    public static enum DiffusionMethod {

        NEIGHBORS("DiffusionMethod.Neighbors"),
        NEIGHBORS_OF_NEIGHBORS("DiffusionMethod.NeighborsOfNeighbors"),
        PREDECESSORS("DiffusionMethod.Predecessors"),
        SUCCESSORS("DiffusionMethod.Successors");
        private final String name;

        DiffusionMethod(String name) {
            this.name = name;
        }

        public String getName() {
            return NbBundle.getMessage(DiffusionMethods.class, name);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    //NodeTree
    private static class NodeTree extends ParamAVLTree<Node> {

        public NodeTree(Node[] initialNodes) {
            this();
            for (int i = 0; i < initialNodes.length; i++) {
                add(initialNodes[i]);
            }
        }

        public NodeTree() {
            super(new AVLItemAccessor<Node>() {

                public int getNumber(Node item) {
                    return item.getId();
                }
            });
        }
    }
}
