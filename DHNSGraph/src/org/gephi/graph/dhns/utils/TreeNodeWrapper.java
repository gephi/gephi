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
package org.gephi.graph.dhns.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.gephi.utils.collection.avl.AVLItem;
import org.gephi.utils.collection.avl.SimpleAVLTree;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class TreeNodeWrapper {

    private SimpleAVLTree nodeTree;
    private TreeStructure treeStructure;

    public TreeNodeWrapper(TreeStructure treeStructure) {
        this.treeStructure = treeStructure;
    }

    public ImmutableTreeNode wrap(AbstractNodeIterator iterator) {
        nodeTree = new SimpleAVLTree();
        TreeNodeImpl root = new TreeNodeImpl(treeStructure.getRoot());
        nodeTree.add(root);

        for (; iterator.hasNext();) {
            AbstractNode node = iterator.next();
            TreeNodeImpl n = new TreeNodeImpl(node);
            if (node.parent != null) {
                TreeNodeImpl parent = (TreeNodeImpl) nodeTree.get(node.parent.getNumber());
                if (parent != null) {
                    n.parent = parent;
                    parent.children.add(n);
                } else {
                    n.parent = root;
                    root.children.add(n);
                }
            }
            nodeTree.add(n);
        }

        //To array
        for (AVLItem item : nodeTree) {
            TreeNodeImpl node = (TreeNodeImpl) item;
            node.toArray();
        }
        return root;
    }

    private static class TreeNodeImpl implements ImmutableTreeNode, AVLItem {

        private TreeNodeImpl parent;
        private AbstractNode node;
        private List<TreeNodeImpl> children;
        private TreeNodeImpl[] childrenArray;

        public TreeNodeImpl(AbstractNode node) {
            this.node = node;
            this.children = new ArrayList<TreeNodeImpl>();
        }

        public TreeNode getChildAt(int childIndex) {
            return childrenArray[childIndex];
        }

        public int getChildCount() {
            return childrenArray.length;
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getIndex(TreeNode node) {
            for (int i = 0; i < childrenArray.length; i++) {
                if (childrenArray[i] == node) {
                    return i;
                }
            }
            return -1;
        }

        public boolean getAllowsChildren() {
            return false;
        }

        public boolean isLeaf() {
            return childrenArray == null;
        }

        public Enumeration children() {
            return new IteratorEnumeration(childrenArray);
        }

        public int getNumber() {
            return node.getNumber();
        }

        public void toArray() {
            if (!children.isEmpty()) {
                childrenArray = children.toArray(new TreeNodeImpl[0]);
            }
            children = null;
        }

        public Node getNode() {
            return node;
        }

        @Override
        public String toString() {
            return node.toString();
        }
    }

    private static class IteratorEnumeration implements Enumeration {

        Object[] array;
        int index = 0;

        public IteratorEnumeration(Object[] array) {
            this.array = array;
        }

        public boolean hasMoreElements() {
            return index < array.length;
        }

        public Object nextElement() {
            return array[index++];
        }
    }
}
