/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.graph.dhns.graph;

import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.BiEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeAndMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.predicate.Tautology;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalDirectedGraphImpl extends HierarchicalGraphImpl implements HierarchicalDirectedGraph {

    public HierarchicalDirectedGraphImpl(Dhns dhns, GraphViewImpl view) {
        super(dhns, view);
    }

    //Graph
    public boolean addEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        if (!edge.isDirected()) {
            throw new IllegalArgumentException("Can't add an undirected egde");
        }
        if (checkEdgeExist(absEdge.getSource(view.getViewId()), absEdge.getTarget(view.getViewId()))) {
            //Edge already exist
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.factory().newEdgeAttributes(edge.getEdgeData()));
        }
        view.getStructureModifier().addEdge(absEdge);
        dhns.touchDirected();
        return true;
    }

    //Directed
    public boolean addEdge(Node source, Node target) {
        AbstractNode absSource = checkNode(source);
        AbstractNode absTarget = checkNode(target);
        if (checkEdgeExist(absSource, absTarget)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.factory().newEdge(source, target);
        view.getStructureModifier().addEdge(edge);
        dhns.touchDirected();
        return true;
    }

    //Directed
    public boolean removeEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        return view.getStructureModifier().deleteEdge(absEdge);
    }

    //Directed
    public NodeIterable getSuccessors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, enabledNodePredicate, Tautology.instance), absNode, Tautology.instance));
    }

    //Directed
    public NodeIterable getPredecessors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, enabledNodePredicate, Tautology.instance), absNode, Tautology.instance));

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
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEnabledInDegree();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEnabledOutDegree();
        return count;
    }

    public int getMutualDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEnabledMutualDegree();
        return count;
    }

    //Graph
    public boolean contains(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        return getEdge(absEdge.getSource(view.getViewId()), absEdge.getTarget(view.getViewId())) != null;
    }

    //Graph
    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), false, enabledNodePredicate, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getEdgesTree() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), false, Tautology.instance, Tautology.instance));
    }

    //Directed
    public EdgeIterable getInEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, enabledNodePredicate, Tautology.instance));
    }

    //Directed
    public EdgeIterable getOutEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, enabledNodePredicate, Tautology.instance));
    }

    //Graph
    public EdgeIterable getEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, enabledNodePredicate, Tautology.instance));
    }

    //Graph
    public NodeIterable getNeighbors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, enabledNodePredicate, Tautology.instance), absNode, Tautology.instance));
    }

    //Directed
    public Edge getEdge(Node source, Node target) {
        if (source == null || target == null) {
            return null;
        }
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        AbstractEdge res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        return res;
    }

    //Graph
    public int getEdgeCount() {
        return view.getEdgesCountEnabled();
    }

    //ClusteredGraph
    public int getTotalEdgeCount() {
        return view.getEdgesCountEnabled() + view.getMetaEdgesCountTotal();
    }

    //Graph
    public int getDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount() + absNode.getEdgesOutTree().getCount();
        return count;
    }

    //Graph
    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return isSuccessor(node1, node2) || isPredecessor(node1, node2);
    }

    //Graph
    public boolean isDirected(Edge edge) {
        checkEdgeOrMetaEdge(edge);
        return true;
    }

    //ClusteredGraph
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure, view.getViewId(), absNode, absNode, true, false, Tautology.instance, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure, view.getViewId(), absNode, absNode, false, false, Tautology.instance, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), false));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), false, enabledNodePredicate, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
    }

    public EdgeIterable getEdgesAndMetaEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        EdgeNodeIterator std = new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, enabledNodePredicate, Tautology.instance);
        MetaEdgeNodeIterator meta = new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false);
        return dhns.newEdgeIterable(new BiEdgeIterator(std, meta));
    }

    public boolean removeMetaEdge(Edge edge) {
        AbstractEdge absEdge = checkMetaEdge(edge);
        return view.getStructureModifier().deleteMetaEdge(absEdge);
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(null, absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
    }

    public EdgeIterable getInEdgesAndMetaInEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        EdgeNodeIterator std = new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, enabledNodePredicate, Tautology.instance);
        MetaEdgeNodeIterator meta = new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false);
        return dhns.newEdgeIterable(new BiEdgeIterator(std, meta));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), null, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
    }

    public EdgeIterable getOutEdgesAndMetaOutEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        EdgeNodeIterator std = new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, enabledNodePredicate, Tautology.instance);
        MetaEdgeNodeIterator meta = new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false);
        return dhns.newEdgeIterable(new BiEdgeIterator(std, meta));
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount();
        return count;
    }

    public int getTotalInDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEnabledInDegree() + absNode.getMetaEdgesInTree().getCount();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesOutTree().getCount();
        return count;
    }

    public int getTotalOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEnabledOutDegree() + absNode.getMetaEdgesOutTree().getCount();
        return count;
    }

    //ClusteredGraph
    public int getMetaDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount() + absNode.getMetaEdgesOutTree().getCount();
        return count;
    }

    public int getTotalDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount()
                + absNode.getEdgesOutTree().getCount()
                + absNode.getMetaEdgesInTree().getCount()
                + absNode.getMetaEdgesOutTree().getCount();
        return count;
    }

    //ClusteredDirected
    public MetaEdge getMetaEdge(Node source, Node target) {
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        return sourceNode.getMetaEdgesOutTree().getItem(targetNode.getNumber());
    }

    @Override
    public HierarchicalDirectedGraphImpl copy(Dhns dhns, GraphViewImpl view) {
        return new HierarchicalDirectedGraphImpl(dhns, view);
    }

    public EdgeIterable getHierarchyEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
