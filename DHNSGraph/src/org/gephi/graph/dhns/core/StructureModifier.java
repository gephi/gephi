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
package org.gephi.graph.dhns.core;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.filter.Tautology;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.CloneNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.DescendantAndSelfIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.PreNodeTreeListIterator;

/**
 * Business class for external operations on the data structure. Propose blocking mechanism.
 *
 * @author Mathieu Bastian
 */
public class StructureModifier {

    private Dhns dhns;
    private GraphVersion graphVersion;
    private TreeStructure treeStructure;
    private EdgeProcessor edgeProcessor;

    //Executor
    //ExecutorService executor = Executors.newSingleThreadExecutor();
    public StructureModifier(Dhns dhns) {
        this.dhns = dhns;
        this.treeStructure = dhns.getGraphStructure().getStructure();
        this.graphVersion = dhns.getGraphVersion();
        edgeProcessor = new EdgeProcessor(dhns);
    }

    public EdgeProcessor getEdgeProcessor() {
        return edgeProcessor;
    }

    public void expand(final Node node) {
        expandBlock(node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                expandBlock(node);
//            }
//        });
    }

    public void expandBlock(Node node) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        if (absNode.level < treeStructure.treeHeight) {
            expand(absNode);
            //sightManager.updateSight((SightImpl) sight);
        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void retract(final Node node) {
        retractBlock(node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                retractBlock(node);
//            }
//        });
    }

    public void retractBlock(Node node) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        if (absNode.level < treeStructure.treeHeight) {
            retract(((AbstractNode) node));
            //sightManager.updateSight((SightImpl)sight);
        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void addNode(final Node node, final Node parent) {
        addNodeBlock(node, parent);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                addNodeBlock(node, parent);
//            }
//        });
    }

    public void addNodeBlock(Node node, Node parent) {
        dhns.getWriteLock().lock();
        AbstractNode parentNode;
        if (parent == null) {
            parentNode = treeStructure.getRoot();
        } else {
            parentNode = ((AbstractNode) parent);
        }
        AbstractNode absNode = (AbstractNode) node;
        if (absNode.isValid()) {
            //Node already exist, clone descendant & self
            cloneDescedantAndSelft(absNode, parentNode);
        } else {
            absNode.parent = parentNode;
            boolean enabled = (treeStructure.getEnabledAncestor(absNode) == null);
            absNode.setEnabled(enabled);
            addNode(absNode);
        }

        graphVersion.incNodeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_UPDATED);
    }

    public void deleteNode(final Node node) {
        deleteNodeBlock(node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                deleteNodeBlock(node);
//            }
//        });
    }

    public void deleteNodeBlock(Node node) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        PreNode preNode = absNode.getOriginalNode();
        deleteNode(preNode);
        //dhns.getDictionary().removeNode(absNode);      //Dico
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void addEdge(final Edge edge) {
        addEdgeBlock(edge);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                addEdgeBlock(edge);
//            }
//        });
    }

    public void addEdgeBlock(Edge edge) {
        dhns.getWriteLock().lock();
        AbstractEdge abstractEdge = (AbstractEdge) edge;

        addEdge(abstractEdge);
        //dhns.getDictionary().addEdge(preEdge);     //Dico
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
    }

    public boolean deleteEdge(final Edge edge) {
        return deleteEdgeBlock(edge);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                deleteEdgeBlock(edge);
//            }
//        });
    }

    public boolean deleteEdgeBlock(Edge edge) {
        dhns.getWriteLock().lock();
        AbstractEdge edgeImpl = (AbstractEdge) edge;
        //dhns.getDictionary().removeEdge(edge);
        boolean res = delEdge(edgeImpl);
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
        return res;
    }

    public void clear() {
        clearBlock();
//        executor.execute(new Runnable() {
//
//            public void run() {
//                clearBlock();
//            }
//        });
    }

    public void clearBlock() {
        dhns.getWriteLock().lock();
        clearAllEdges();
        clearAllNodes();
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void clearEdges() {
        clearEdgesBlock();
//        executor.execute(new Runnable() {
//
//            public void run() {
//                clearEdgesBlock();
//            }
//        });
    }

    public void clearEdgesBlock() {
        dhns.getWriteLock().lock();
        clearAllEdges();
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
    }

    public void clearEdges(final Node node) {
        clearEdgesBlock(node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                clearEdgesBlock(node);
//            }
//        });
    }

    public void clearEdgesBlock(Node node) {
        dhns.getWriteLock().lock();
        clearEdges((AbstractNode) node);
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
    }

    public void clearMetaEdges(final Node node) {
        clearMetaEdgesBlock(node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                clearMetaEdgesBlock(node);
//            }
//        });
    }

    public void clearMetaEdgesBlock(Node node) {
        dhns.getWriteLock().lock();
        clearMetaEdges((AbstractNode) node);
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
    }

    public void resetViewToLeaves() {
        dhns.getWriteLock().lock();
        edgeProcessor.clearAllMetaEdges();
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.setEnabled(node.size == 0);

        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void resetViewToTopNodes() {
        dhns.getWriteLock().lock();
        edgeProcessor.clearAllMetaEdges();
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.setEnabled(node.parent == treeStructure.root);

        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void resetViewToLevel(int level) {
        dhns.getWriteLock().lock();
        edgeProcessor.clearAllMetaEdges();
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.setEnabled(node.level == level);
        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void moveToGroup(Node node, Node nodeGroup) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        AbstractNode preGroup = (AbstractNode) nodeGroup;
        moveToGroup(absNode, preGroup);
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public Node group(Node[] nodes) {
        dhns.getWriteLock().lock();
        AbstractNode group = dhns.factory().newNode();
        group.setEnabled(true);
        AbstractNode parent = ((AbstractNode) nodes[0]).getRootNode().parent;
        group.parent = parent;
        addNode(group);
        for (int i = 0; i < nodes.length; i++) {
            AbstractNode nodeToGroup = ((AbstractNode) nodes[i]).getRootNode();
            moveToGroup(nodeToGroup, group);
        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
        return group;
    }

    public void ungroup(AbstractNode nodeGroup) {
        dhns.getWriteLock().lock();
        nodeGroup = nodeGroup.getRootNode();
        //TODO Better implementation. Just remove nodeGroup from the treelist and lower level of children
        int count = 0;
        for (ChildrenIterator itr = new ChildrenIterator(treeStructure, nodeGroup, Tautology.instance); itr.hasNext();) {
            itr.next();
            count++;
        }

        if (nodeGroup.isEnabled()) {
            expand(nodeGroup);
        }
        for (int i = 0; i < count; i++) {
            AbstractNode node = treeStructure.getNodeAt(nodeGroup.getPre() + 1);
            dhns.getStructureModifier().moveToGroup(node, nodeGroup.parent);
        }

        deleteNode(nodeGroup);

        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    //------------------------------------------
    private void expand(AbstractNode absNode) {

        absNode = absNode.getRootNode();

        //Disable parent
        absNode.setEnabled(false);
        edgeProcessor.clearMetaEdges(absNode);

        //Enable children
        for (ChildrenIterator itr = new ChildrenIterator(treeStructure, absNode, Tautology.instance); itr.hasNext();) {
            AbstractNode child = itr.next();

            child.setEnabled(true);
            edgeProcessor.computeMetaEdges(child, child);
        }
    }

    private void retract(AbstractNode parent) {

        parent = parent.getRootNode();

        //Disable children
        for (ChildrenIterator itr = new ChildrenIterator(treeStructure, parent, Tautology.instance); itr.hasNext();) {
            AbstractNode child = itr.next();
            child.setEnabled(false);
            edgeProcessor.clearMetaEdges(child);
        }

        //Enable node
        parent.setEnabled(true);
        edgeProcessor.computeMetaEdges(parent, parent);
    }

    private void addNode(AbstractNode node) {
        treeStructure.insertAsChild(node, node.parent);
        dhns.getGraphStructure().getNodeDictionnary().add(node.getOriginalNode());
    }

    private void addEdge(AbstractEdge edge) {
        AbstractNode sourceNode = edge.getSource();
        AbstractNode targetNode = edge.getTarget();

        //Add Edges
        sourceNode.getEdgesOutTree().add(edge);
        targetNode.getEdgesInTree().add(edge);

        dhns.getGraphStructure().getEdgeDictionnary().add(edge);

        //Add Meta Edge
        if (!edge.isSelfLoop()) {
            edgeProcessor.createMetaEdge(edge);
        }
    }

    private void deleteNode(PreNode node) {

        for (DescendantAndSelfIterator itr = new DescendantAndSelfIterator(treeStructure, node, Tautology.instance); itr.hasNext();) {
            AbstractNode descendant = itr.next();
            if (descendant.isEnabled()) {
                edgeProcessor.clearMetaEdges(descendant);
            }
            edgeProcessor.clearEdges(descendant);

            //Delete clones or original (if descendant is a clone)
            if (descendant == descendant.getOriginalNode()) {
                CloneNode cn = descendant.getOriginalNode().getClones();
                while (cn != null) {
                    //Unattach clones
                    unAttachClones(cn);
                    treeStructure.deleteDescendantAndSelf(cn);
                    cn = cn.getNext();
                }
            } else {
                treeStructure.deleteDescendantAndSelf(descendant.getOriginalNode());
                dhns.getGraphStructure().getNodeDictionnary().remove(descendant.getOriginalNode());
                CloneNode cn = descendant.getOriginalNode().getClones();
                while (cn != null) {
                    if (cn != descendant) {
                        treeStructure.deleteDescendantAndSelf(cn);
                    }
                    cn = cn.getNext();
                }
            }

        }

        treeStructure.deleteDescendantAndSelf(node);
    }

    private boolean delEdge(AbstractEdge edge) {
        //Remove edge
        boolean res = edge.getSource().getEdgesOutTree().remove(edge);
        res = res && edge.getTarget().getEdgesInTree().remove(edge);

        dhns.getGraphStructure().getEdgeDictionnary().remove(edge);

        //Remove edge from possible metaEdge
        edgeProcessor.removeEdgeFromMetaEdge(edge);
        return res;
    }

    private void clearAllEdges() {
        edgeProcessor.clearAllEdges();
    }

    private void clearAllNodes() {
        treeStructure.clear();
        dhns.getGraphStructure().getNodeDictionnary().clear();
    }

    private void clearEdges(AbstractNode node) {
        edgeProcessor.clearEdges(node);
    }

    private void clearMetaEdges(AbstractNode node) {
        edgeProcessor.clearMetaEdges(node);
    }

    private void moveToGroup(AbstractNode node, AbstractNode nodeGroup) {

        AbstractNode toMoveAncestor = treeStructure.getEnabledAncestor(node);
        AbstractNode destinationAncestor = treeStructure.getEnabledAncestorOrSelf(nodeGroup);

        if (toMoveAncestor != destinationAncestor) {
            if (toMoveAncestor != null) {
                //The node has an enabled ancestor
                //We delete edges from potential meta edges
                if (node.size > 0) {
                    for (DescendantAndSelfIterator itr = new DescendantAndSelfIterator(treeStructure, node, Tautology.instance); itr.hasNext();) {
                        AbstractNode descendant = itr.next();
                        edgeProcessor.clearEdgesWithoutRemove(descendant);
                    }
                } else {
                    edgeProcessor.clearEdgesWithoutRemove(node);
                }
            } else if (node.isEnabled()) {
                //The node is enabled
                if (destinationAncestor != null) {
                    //The destination is enabled or has enabled ancestor
                    //Node is thus disabled
                    edgeProcessor.clearMetaEdges(node);
                    node.setEnabled(false);
                    //DO
                } else {
                    //The node is kept enabled
                    //Meta edges are still valid only if their target is out of the dest cluster
                    edgeProcessor.clearMetaEdgesOutOfRange(node, nodeGroup);
                }
            } else if (node.size > 0) {
                if (destinationAncestor != null) {
                    //The node may have some enabled descendants and we set them disabled
                    for (DescendantIterator itr = new DescendantIterator(treeStructure, node, Tautology.instance); itr.hasNext();) {
                        AbstractNode descendant = itr.next();
                        if (descendant.isEnabled()) {
                            edgeProcessor.clearMetaEdges(descendant);
                            descendant.setEnabled(false);
                        }
                    }
                    //DO
                } else {
                    //The node may have some enabled descendants and we keep them enabled
                    for (DescendantIterator itr = new DescendantIterator(treeStructure, node, Tautology.instance); itr.hasNext();) {
                        AbstractNode descendant = itr.next();
                        if (descendant.isEnabled()) {
                            //Enabled descendants meta edges are still valid only if their target is out of
                            //the destination cluster
                            edgeProcessor.clearMetaEdgesOutOfRange(node, nodeGroup);
                        }
                    }
                }

            }
        }

        treeStructure.move(node, nodeGroup);

        if (destinationAncestor != null) {
            destinationAncestor.getPre();
            //Compute all meta edges for the descendants of node and append them to the enabled
            //destinationAncestor
            edgeProcessor.computeMetaEdges(node, destinationAncestor);
        }
    }

    private void cloneDescedantAndSelft(AbstractNode node, AbstractNode parentNode) {
        if (node.size > 0) {
            DescendantAndSelfIterator itr = new DescendantAndSelfIterator(treeStructure, node, Tautology.instance);
            for (; itr.hasNext();) {
                AbstractNode desc = itr.next();
                CloneNode clone = new CloneNode(desc);
                if (desc == node) {
                    //Parent is the given parentNode
                    clone.parent = parentNode;
                } else {
                    clone.parent = desc.parent.getOriginalNode().getClones();       //The last clone added
                }
                addNode(clone);
            }
        } else {
            CloneNode clone = new CloneNode(node);
            clone.parent = parentNode;
            addNode(clone);
        }
    }

    private void unAttachClones(CloneNode node) {
        for (int i = node.getPre(); i <= node.pre + node.size; i++) {
            AbstractNode n = treeStructure.getNodeAt(i);
            if (n.isClone()) {
                n.getOriginalNode().removeClone((CloneNode) n);
            }
        }
    }
}
