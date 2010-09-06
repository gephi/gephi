/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.graph.dhns.core;

import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/**
 * Holds nodes tree and manage basic operations.
 *
 * @author Mathieu Bastian
 */
public class TreeStructure {

    protected DurableTreeList tree;
    protected AbstractNode root;
    protected final int viewId;

    public TreeStructure(int viewId) {
        tree = new DurableTreeList();
        this.viewId = viewId;
        initRoot();
    }

    private void initRoot() {
        root = new AbstractNode(-1, viewId, 0, 0, 0, null);
        tree.add(root);
    }

    public AbstractNode getNodeAt(int pre) {
        return tree.get(pre);
    }

    public AbstractNode getEnabledAncestorOrSelf(AbstractNode node) {
        AbstractNode parent = node;
        while (!parent.isEnabled()) {
            parent = parent.parent;
            if (parent == null || parent.getPre() == 0) {
                return null;
            }
        }
        return parent;
    }

    public AbstractNode[] getEnabledAncestorsOrSelf(AbstractNode node) {
        AbstractNode enabled = getEnabledAncestorOrSelf(node);
        if (enabled != null) {
            return new AbstractNode[]{enabled};
        } else {
            return null;
        }
    }

    /*public AbstractNode[] getEnabledAncestorsOrSelf(AbstractNode node) {
    PreNode preNode = node.getOriginalNode();
    if (preNode.getClones() == null) {
    AbstractNode enabled = getEnabledAncestorOrSelf(preNode);
    if (enabled != null) {
    return new AbstractNode[]{enabled};
    } else {
    return null;
    }
    } else {
    List<AbstractNode> nodeList = new ArrayList<AbstractNode>();
    AbstractNode enabled = getEnabledAncestorOrSelf(preNode);
    if (enabled != null) {
    nodeList.add(enabled);
    }
    CloneNode cn = preNode.getClones();
    while (cn != null) {
    enabled = getEnabledAncestorOrSelf(cn);
    if (enabled != null && !nodeList.contains(enabled)) {
    nodeList.add(enabled.getOriginalNode());
    }
    cn = cn.getNext();
    }
    return nodeList.toArray(new AbstractNode[0]);
    }
    }*/
    public AbstractNode getEnabledAncestor(AbstractNode node) {
        AbstractNode parent = node.parent;
        while (!parent.isEnabled()) {
            if (parent.getPre() == 0) {
                return null;
            }
            parent = parent.parent;
        }
        return parent;
    }

    public void insertAtEnd(AbstractNode node) {
        node.pre = tree.size();
        tree.add(node);
    }

    public void insertAsChild(AbstractNode node, AbstractNode parent) {
        node.parent = parent;
        node.pre = parent.getPre() + parent.size + 1;
        node.level = parent.level + 1;
        tree.add(node.pre, node);
        incrementAncestorsSize(node);
    }

    public void resetLevelSize(int firstLevel) {
        tree.levelsSize = new int[1 + (firstLevel > 0 ? 1 : 0)];
        if (firstLevel > 0) {
            tree.levelsSize[1] = firstLevel;
        }
    }

    public void move(AbstractNode node, AbstractNode newParent) {

        AbstractNode sourceParent = node.parent;
        int sourceSize = 1 + node.size;

        tree.move(node.getPre(), newParent.getPre());

        if (sourceParent != null) {
            decrementAncestorAndSelfSize(sourceParent, sourceSize);
        }

        //Increment ancestor & self
        incrementAncestorsAndSelfSize(newParent, sourceSize);

        /* nodeSize = node.size;
        int nodePre = node.getPre();
        boolean forward = newParent.getPre()+newParent.size+1 > nodePre;

        //Move node itself
        decrementAncestorSize(node, 1);
        tree.removeAndKeepParent(nodePre);
        insertAsChild(node, newParent);
        node.size = 0;

        //showTreeAsTable();

        if(nodeSize>0) {
        //Move descendants
        for(int i=0;i<nodeSize;i++) {
        int descPre = nodePre;
        if(!forward) {
        descPre += i + 1;
        }
        decrementAncestorSize(node, 1);
        PreNode descendant = tree.removeAndKeepParent(descPre);
        //System.out.println("descendant "+descendant.getId());
        PreNode parent = descendant.parent;
        insertAsChild(descendant, parent);
        descendant.size = 0;
        }
        }*/
    }

    public void deleteAtPre(AbstractNode node) {
        int pre = node.getPre();
        AbstractNode n = tree.remove(pre);
        n.removeFromView(viewId);
        for (int i = 0; i < node.size; i++) {
            n = tree.remove(pre);
            n.removeFromView(viewId);
        }
    }

    public void deleteDescendantAndSelf(AbstractNode node) {
        decrementAncestorSize(node, node.size + 1);
        deleteAtPre(node);
    }

    public void deleteOnlySelf(AbstractNode node) {
        int pre = node.getPre();
        AbstractNode n = tree.remove(pre);
        n.removeFromView(viewId);
    }

    public void incrementAncestorsSize(AbstractNode node) {
        incrementAncestorsSize(node, 1);
    }

    public void incrementAncestorsSize(AbstractNode node, int shift) {
        while (node.parent != null) {
            node = node.parent;
            node.size += shift;
            node.getPost();
        }
    }

    public void incrementAncestorsAndSelfSize(AbstractNode node, int shift) {
        while (node != null) {
            node.size += shift;
            node.getPost();
            node = node.parent;
        }
    }

    public void decrementAncestorSize(AbstractNode node, int shift) {
        while (node.parent != null) {
            node = node.parent;
            node.size -= shift;
            node.getPost();
        }
    }

    public void decrementAncestorAndSelfSize(AbstractNode node, int shift) {
        while (node != null) {
            node.size -= shift;
            node.getPost();
            node = node.parent;
        }
    }

    public boolean hasEnabledDescendant(AbstractNode node) {
        for (int i = node.getPre() + 1; i <= node.pre + node.size; i++) {
            AbstractNode descendant = tree.get(i);
            if (descendant.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void showTreeAsTable() {
        System.out.println("pre\tsize\tlevel\tparent\tpost\tenabled\tid");
        System.out.println("-----------------------------------------------------------------");
        int pre = 0;
        for (AbstractNode p : tree) {
            System.out.println(p.pre + "\t" + p.size + "\t" + p.level + "\t" + (p.parent == null ? "null" : p.parent.getPre()) + "\t" + p.post + "\t" + p.isEnabled() + "\t" + p.getId());
            pre++;
        }
    }

    public void clear() {
        //Clean nodes
        for (TreeListIterator itr = new TreeListIterator(tree); itr.hasNext();) {
            AbstractNode preNode = itr.next();
            preNode.avlNode = null;
            preNode.parent = null;
        }
        tree.clear();
        root = null;
        initRoot();
    }

    public int getTreeSize() {
        return tree.size();
    }

    public int getTreeHeight() {
        int[] levelsSize = tree.levelsSize;
        for (int i = levelsSize.length - 1; i >= 0; i--) {
            if (levelsSize[i] > 0) {
                return i;
            }
        }
        return 0;
    }

    public int getLevelSize(int level) {
        return tree.levelsSize[level];
    }

    public DurableTreeList getTree() {
        return tree;
    }

    public AbstractNode getRoot() {
        return root;
    }
}
