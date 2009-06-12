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
package org.gephi.graph.dhns.graph;

import java.util.Iterator;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeContentIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleMetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleRangeEdgeIterator;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.CompleteTreeIterator;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.VisibleTreeIterator;

/**
 * Implementation of clustered directed graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredDirectedGraphImpl extends ClusteredGraphImpl implements ClusteredDirectedGraph {

    public ClusteredDirectedGraphImpl(Dhns dhns, boolean visible) {
        super(dhns, visible);
    }

    //Directed
    public boolean addEdge(Node source, Node target) {
        PreNode preSource = checkNode(source);
        PreNode preTarget = checkNode(target);
        if (checkEdgeExist(preSource, preTarget)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.getGraphFactory().newEdge(source, target);
        dhns.getStructureModifier().addEdge(edge);
        return true;
    }

    //Directed
    public boolean removeEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        AbstractEdge undirected = absEdge.getUndirected();      //Ensure that the edge with the min id is removed before his mutual with a greater id
        return dhns.getStructureModifier().deleteEdge(undirected);
    }

    //Directed
    public NodeIterable getSuccessors(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false), preNode));
        }
    }

    //Directed
    public NodeIterable getPredecessors(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.IN, false), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false), preNode));
        }
    }

    //Directed
    public boolean isSuccessor(Node node, Node successor) {
        return getEdge(node, successor) != null;
    }

    //Directed
    public boolean isPredecessor(Node node, Node predecessor) {
        return getEdge(predecessor, node) != null;
    }

    //Directed
    public int getInDegree(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        int count = 0;
        if (visible && !preNode.getEdgesInTree().isEmpty()) {
            for (Iterator<AbstractEdge> itr = preNode.getEdgesInTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getEdgesInTree().getCount();
        }
        readUnlock();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        int count = 0;
        if (visible && !preNode.getEdgesInTree().isEmpty()) {
            for (Iterator<AbstractEdge> itr = preNode.getEdgesOutTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getEdgesOutTree().getCount();
        }
        readUnlock();
        return count;
    }

    //Graph
    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    //Graph
    public EdgeIterable getEdges() {
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure()), false));
        } else {
            return dhns.newEdgeIterable(new EdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure()), false));
        }
    }

    //Directed
    public EdgeIterable getInEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
        }
    }

    //Directed
    public EdgeIterable getOutEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
        }
    }

    //Graph
    public EdgeIterable getEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
        }
    }

    //Graph
    public NodeIterable getNeighbors(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true), preNode));
        }
    }

    //Directed
    public Edge getEdge(Node source, Node target) {
        PreNode sourceNode = checkNode(source);
        PreNode targetNode = checkNode(target);
        readLock();
        AbstractEdge res = null;
        if (visible) {
            AbstractEdge edge = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
            if (edge != null && edge.isVisible()) {
                res = edge;
            }
        } else {
            res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        }
        readUnlock();
        return res;
    }

    //Graph
    public int getEdgeCount() {
        readLock();
        int count = 0;
        if (visible) {
            for (VisibleEdgeIterator itr = new VisibleEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure()), false); itr.hasNext();) {
                itr.next();
                count++;
            }
        } else {
            for (EdgeIterator itr = new EdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure()), false); itr.hasNext();) {
                itr.next();
                count++;
            }
        }
        readUnlock();
        return count;
    }

    //Graph
    public int getDegree(Node node) {
        return getInDegree(node) + getOutDegree(node);
    }

    //Graph
    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return isSuccessor(node1, node2) || isPredecessor(node1, node2);
    }

    //ClusteredGraph
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        PreNode preNode = checkNode(nodeGroup);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleRangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, true, false));
        } else {
            return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, true, false));
        }
    }

    //ClusteredGraph
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        PreNode preNode = checkNode(nodeGroup);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleRangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, false, false));
        } else {
            return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, false, false));
        }
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure()), false));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure()), false));
        }
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeNodeIterator(preNode, VisibleMetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeNodeIterator(preNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
        }
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeNodeIterator(preNode, VisibleMetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeNodeIterator(preNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
        }
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeNodeIterator(preNode, VisibleMetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeNodeIterator(preNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
        }
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        int count = 0;
        if (visible && !preNode.getMetaEdgesInTree().isEmpty()) {
            for (Iterator<MetaEdgeImpl> itr = preNode.getMetaEdgesInTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getMetaEdgesInTree().getCount();
        }
        readUnlock();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        int count = 0;
        if (visible && !preNode.getMetaEdgesOutTree().isEmpty()) {
            for (Iterator<MetaEdgeImpl> itr = preNode.getMetaEdgesOutTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getMetaEdgesOutTree().getCount();
        }
        readUnlock();
        return count;
    }

    //ClusteredGraph
    public int getMetaDegree(Node node) {
        return getMetaInDegree(node) + getMetaOutDegree(node);
    }

    public EdgeIterable getMetaEdgeContent(Edge metaEdge) {
        MetaEdgeImpl metaEdgeImpl = checkMetaEdge(metaEdge);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new MetaEdgeContentIterator(metaEdgeImpl, true, false));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeContentIterator(metaEdgeImpl, false, false));
        }
    }
}
