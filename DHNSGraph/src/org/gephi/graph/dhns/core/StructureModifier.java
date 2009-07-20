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
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.CloneNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.DescendantAndSelfIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.PreNodeTreeListIterator;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.view.View;

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
        this.treeStructure = dhns.getTreeStructure();
        this.graphVersion = dhns.getGraphVersion();
        edgeProcessor = new EdgeProcessor(dhns);
    }

    public void expand(final View view, final Node node) {
        expandBlock(view, node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                expandBlock(node);
//            }
//        });
    }

    public void expandBlock(View view, Node node) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        if (absNode.level < treeStructure.treeHeight) {
            expand(view, absNode);
        //sightManager.updateSight((SightImpl) sight);
        }
        graphVersion.incNodeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void retract(final View view, final Node node) {
        retractBlock(view, node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                retractBlock(node);
//            }
//        });
    }

    public void retractBlock(View view, Node node) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        if (absNode.level < treeStructure.treeHeight) {
            retract(view, ((AbstractNode) node));
        //sightManager.updateSight((SightImpl)sight);
        }
        graphVersion.incNodeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void addNode(final View view, final Node node, final Node parent) {
        addNodeBlock(view, node, parent);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                addNodeBlock(node, parent);
//            }
//        });
    }

    public void addNodeBlock(View view, Node node, Node parent) {
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
            boolean enabled = (treeStructure.getEnabledAncestor(view, absNode) == null);
            absNode.addView(view, enabled);
            addNode(absNode);
        }

        graphVersion.incNodeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_UPDATED);
    }

    public void deleteNode(final View view, final Node node) {
        deleteNodeBlock(view, node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                deleteNodeBlock(node);
//            }
//        });
    }

    public void deleteNodeBlock(View view, Node node) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        PreNode preNode = absNode.getOriginalNode();
        deleteNode(view, preNode);
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

    public void clearMetaEdges(final View view, final Node node) {
        clearMetaEdgesBlock(view, node);
//        executor.execute(new Runnable() {
//
//            public void run() {
//                clearMetaEdgesBlock(node);
//            }
//        });
    }

    public void clearMetaEdgesBlock(View view, Node node) {
        dhns.getWriteLock().lock();
        clearMetaEdges(view, (AbstractNode) node);
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
    }

    public void resetView(View view) {
        dhns.getWriteLock().lock();
        edgeProcessor.clearAllMetaEdges(view);
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (node.isInView(view)) {
                node.setEnabled(view, node.size == 0);
            }
        }
        graphVersion.incEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.EDGES_UPDATED);
    }

    public void moveToGroup(View view, Node node, Node nodeGroup) {
        dhns.getWriteLock().lock();
        AbstractNode absNode = (AbstractNode) node;
        AbstractNode preGroup = (AbstractNode) nodeGroup;
        moveToGroup(view, absNode, preGroup);
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public Node group(View view, Node[] nodes) {
        dhns.getWriteLock().lock();
        AbstractNode group = dhns.getGraphFactory().newNode();
        group.addView(view, true);
        AbstractNode parent = ((AbstractNode) nodes[0]).parent;
        group.parent = parent;
        addNode(group);
        for (int i = 0; i < nodes.length; i++) {
            AbstractNode nodeToGroup = (AbstractNode) nodes[i];
            moveToGroup(view, nodeToGroup, group);
        }
        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
        return group;
    }

    public void ungroup(View view, AbstractNode nodeGroup) {
        dhns.getWriteLock().lock();
        //TODO Better implementation. Just remove nodeGroup from the treelist and lower level of children
        int count = 0;
        for (ChildrenIterator itr = new ChildrenIterator(treeStructure, nodeGroup, null); itr.hasNext();) {
            itr.next();
            count++;
        }

        if (nodeGroup.isEnabled(view)) {
            expand(view, nodeGroup);
        }
        for (int i = 0; i < count; i++) {
            AbstractNode node = treeStructure.getNodeAt(nodeGroup.getPre() + 1);
            dhns.getStructureModifier().moveToGroup(view, node, nodeGroup.parent);
        }

        deleteNode(view, nodeGroup);

        graphVersion.incNodeAndEdgeVersion();
        dhns.getWriteLock().unlock();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    //------------------------------------------
    private void expand(View view, AbstractNode absNode) {

        //Disable parent
        absNode.setEnabled(view, false);
        edgeProcessor.clearMetaEdges(view, absNode);

        //Enable children
        for (ChildrenIterator itr = new ChildrenIterator(treeStructure, absNode, null); itr.hasNext();) {
            AbstractNode child = itr.next();

            child.setEnabled(view, true);
            edgeProcessor.computeMetaEdges(view, child, child);
        }
    }

    private void retract(View view, AbstractNode parent) {

        //Disable children
        for (ChildrenIterator itr = new ChildrenIterator(treeStructure, parent, null); itr.hasNext();) {
            AbstractNode child = itr.next();
            child.setEnabled(view, false);
            edgeProcessor.clearMetaEdges(view, child);
        }

        //Enable node
        parent.setEnabled(view, true);
        edgeProcessor.computeMetaEdges(view, parent, parent);
    }

    private void addNode(AbstractNode node) {
        treeStructure.insertAsChild(node, node.parent);
    }

    private void addEdge(AbstractEdge edge) {
        AbstractNode sourceNode = edge.getSource();
        AbstractNode targetNode = edge.getTarget();

        //Add Edges
        sourceNode.getEdgesOutTree().add(edge);
        targetNode.getEdgesInTree().add(edge);

        //Add Meta Edge
        if (!edge.isSelfLoop()) {
            edgeProcessor.createMetaEdge(edge);
        }
    }

    private void deleteNode(View view, PreNode node) {

        for (DescendantAndSelfIterator itr = new DescendantAndSelfIterator(treeStructure, node, null); itr.hasNext();) {
            AbstractNode descendant = itr.next();
            if (descendant.isEnabled(view)) {
                edgeProcessor.clearMetaEdges(view, descendant);
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

        //Remove edge from possible metaEdge
        edgeProcessor.removeEdgeFromMetaEdge(edge);
        return res;
    }

    private void clearAllEdges() {
        edgeProcessor.clearAllEdges();
    }

    private void clearAllNodes() {
        treeStructure.clear();
    }

    private void clearEdges(AbstractNode node) {
        edgeProcessor.clearEdges(node);
    }

    private void clearMetaEdges(View view, AbstractNode node) {
        edgeProcessor.clearMetaEdges(view, node);
    }

    private void moveToGroup(View view, AbstractNode node, AbstractNode nodeGroup) {

        AbstractNode toMoveAncestor = treeStructure.getEnabledAncestor(view, node);
        AbstractNode destinationAncestor = treeStructure.getEnabledAncestorOrSelf(view, nodeGroup);

        if (toMoveAncestor != destinationAncestor) {
            if (toMoveAncestor != null) {
                //The node has an enabled ancestor
                //We delete edges from potential meta edges
                if (node.size > 0) {
                    for (DescendantAndSelfIterator itr = new DescendantAndSelfIterator(treeStructure, node, null); itr.hasNext();) {
                        AbstractNode descendant = itr.next();
                        edgeProcessor.clearEdgesWithoutRemove(descendant);
                    }
                } else {
                    edgeProcessor.clearEdgesWithoutRemove(node);
                }
            } else if (node.isEnabled(view)) {
                //The node is enabled
                if (destinationAncestor != null) {
                    //The destination is enabled or has enabled ancestor
                    //Node is thus disabled
                    edgeProcessor.clearMetaEdges(view, node);
                    node.setEnabled(view, false);
                //DO
                } else {
                    //The node is kept enabled
                    //Meta edges are still valid only if their target is out of the dest cluster
                    edgeProcessor.clearMetaEdgesOutOfRange(view, node, nodeGroup);
                }
            } else if (node.size > 0) {
                if (destinationAncestor != null) {
                    //The node may have some enabled descendants and we set them disabled
                    for (DescendantIterator itr = new DescendantIterator(treeStructure, node, null); itr.hasNext();) {
                        AbstractNode descendant = itr.next();
                        if (descendant.isEnabled(view)) {
                            edgeProcessor.clearMetaEdges(view, descendant);
                            descendant.setEnabled(view, false);
                        }
                    }
                //DO
                } else {
                    //The node may have some enabled descendants and we keep them enabled
                    for (DescendantIterator itr = new DescendantIterator(treeStructure, node, null); itr.hasNext();) {
                        AbstractNode descendant = itr.next();
                        if (descendant.isEnabled(view)) {
                            //Enabled descendants meta edges are still valid only if their target is out of
                            //the destination cluster
                            edgeProcessor.clearMetaEdgesOutOfRange(view, node, nodeGroup);
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
            edgeProcessor.computeMetaEdges(view, node, destinationAncestor);
        }
    }

    private void cloneDescedantAndSelft(AbstractNode node, AbstractNode parentNode) {
        if (node.size > 0) {
            DescendantAndSelfIterator itr = new DescendantAndSelfIterator(treeStructure, node, null);
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
