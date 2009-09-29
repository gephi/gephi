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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgePredicate;
import org.gephi.graph.api.FilteredGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.filter.FilterControl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.proposition.PropositionImpl;
import org.gephi.graph.dhns.view.View;

/**
 * Utilities methods for managing graphs implementation like locking of non-null checking
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractGraphImpl implements FilteredGraph {

    protected Dhns dhns;

    //Proposition
    protected PropositionImpl<AbstractNode> nodeProposition;
    protected PropositionImpl<AbstractEdge> edgeProposition;

    //Config
    protected boolean allowMultilevel = true;

    //View
    protected View view;

    //Filter
    protected boolean filtered = false;
    protected FilterControl filterControl;

    public FilterControl getFilterControl() {
        return filterControl;
    }

    public void addNodePredicate(NodePredicate nodePredicate) {
        nodeProposition.addPredicate(nodePredicate);
    }

    public void addEdgePredicate(EdgePredicate edgePredicate) {
        edgeProposition.addPredicate(edgePredicate);
    }

    public void removeEdgePredicate(EdgePredicate edgePredicate) {
        edgeProposition.removePredicate(edgePredicate);
    }

    public void removeNodePredicate(NodePredicate nodePredicate) {
        nodeProposition.removePredicate(nodePredicate);
    }

    public NodePredicate[] getNodePredicates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgePredicate[] getEdgePredicates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNodeProposition(PropositionImpl<AbstractNode> nodeProposition) {
        this.nodeProposition = nodeProposition;
    }

    public void setEdgeProposition(PropositionImpl<AbstractEdge> edgeProposition) {
        this.edgeProposition = edgeProposition;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setAllowMultilevel(boolean allowMultilevel) {
        this.allowMultilevel = allowMultilevel;
    }

    public PropositionImpl<AbstractNode> getNodeProposition() {
        return nodeProposition;
    }

    public PropositionImpl<AbstractEdge> getEdgeProposition() {
        return edgeProposition;
    }

    public boolean isAllowMultilevel() {
        return allowMultilevel;
    }

    public View getView() {
        return view;
    }

    public abstract Graph getGraph();

    public void addGraphListener(GraphListener graphListener) {
        dhns.getEventManager().addListener(graphListener);
    }

    public void removeGraphListener(GraphListener graphListener) {
        dhns.getEventManager().removeListener(graphListener);
    }

    public Dhns getSubGraph(AbstractNodeIterator nodeIterator) {
        //Dhns newDhns = dhns.getController().newDhns();

        /*TreeStructure tree = newDhns.getTreeStructure();
        PreNodeTree nodeIdTree = new PreNodeTree();
        GraphFactoryImpl factory = newDhns.getGraphFactory();
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();

        for (; nodeIterator.hasNext();) {
        PreNode node = nodeIterator.next();
        PreNode duplicate = factory.duplicateNode(node);
        nodeIdTree.add(duplicate);
        }

        for (TreeListIterator itr = new TreeListIterator(tree.getTree(), 1); itr.hasNext();) {
        PreNode newNode = itr.next();

        //New edge OUT tree
        EdgeOppositeTree edgeOutTree = new EdgeOppositeTree(newNode);
        if (!newNode.getEdgesOutTree().isEmpty()) {
        for (edgeIterator.setNode(newNode.getEdgesOutTree()); edgeIterator.hasNext();) {
        AbstractEdge edge = edgeIterator.next();
        PreNode target = nodeIdTree.getItem(edge.getTarget().getNumber());
        if (target != null) {
        //The edge target is also in the subgraph
        AbstractEdge duplicate = factory.duplicateEdge(edge, newNode, target);
        edgeOutTree.add(duplicate);
        }
        }
        }
        newNode.setEdgesOutTree(edgeOutTree);

        //New edge IN tree
        EdgeOppositeTree edgeInTree = new EdgeOppositeTree(newNode);
        if (!newNode.getEdgesInTree().isEmpty()) {
        for (edgeIterator.setNode(newNode.getEdgesInTree()); edgeIterator.hasNext();) {
        AbstractEdge edge = edgeIterator.next();
        PreNode source = nodeIdTree.getItem(edge.getSource().getNumber());
        if (source != null) {
        //The edge source is also in the subgraph
        AbstractEdge duplicate = factory.duplicateEdge(edge, source, newNode);
        edgeInTree.add(duplicate);
        }
        }
        }
        newNode.setEdgesInTree(edgeInTree);

        //Clear meta edges
        newNode.getMetaEdgesInTree().clear();
        newNode.getMetaEdgesOutTree().clear();

        }
         */
        //return newDhns;
        return null;
    }

    public void readLock() {
        //System.out.println(Thread.currentThread()+ "read lock");
        dhns.getReadLock().lock();
    }

    public void readUnlock() {
        //System.out.println(Thread.currentThread()+ "read unlock");
        dhns.getReadLock().unlock();
    }

    public void writeLock() {
        //System.out.println(Thread.currentThread()+ "write lock");
        dhns.getWriteLock().lock();
    }

    public void writeUnlock() {
        //System.out.println(Thread.currentThread()+ "write lock");
        dhns.getWriteLock().unlock();
    }

    public int getNodeVersion() {
        return dhns.getGraphVersion().getNodeVersion();
    }

    public int getEdgeVersion() {
        return dhns.getGraphVersion().getEdgeVersion();
    }

    protected AbstractNode checkNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node can't be null");
        }
        AbstractNode preNode = (AbstractNode) node;
        if (!preNode.isValid()) {
            throw new IllegalArgumentException("Node must be in the graph");
        }
        return preNode;
    }

    protected AbstractEdge checkEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge abstractEdge = (AbstractEdge) edge;
        if (!abstractEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");
        }
        if (abstractEdge.isMetaEdge()) {
            throw new IllegalArgumentException("Edge can't be a meta edge");
        }
        return abstractEdge;
    }

    protected MetaEdgeImpl checkMetaEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        if (!absEdge.isMetaEdge()) {
            throw new IllegalArgumentException("edge must be a meta edge");
        }
        if (!absEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");
        }
        return (MetaEdgeImpl) absEdge;
    }

    protected boolean checkEdgeExist(AbstractNode source, AbstractNode target) {
        return source.getEdgesOutTree().hasNeighbour(target);
    }

    protected AbstractEdge getSymmetricEdge(AbstractEdge edge) {
        return edge.getTarget().getEdgesOutTree().getItem(edge.getSource().getNumber());
    }
}
