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

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * This class is a modification of the <b><code>TreeList</code></b> from Apache Commons Collections 3.1.
 * <p>Basically
 * the <code>TreeList</code> is a <code>List</code> implementation that is optimised for fast insertions and
 * removals at any index in the list.
 * <p>
 * This list implementation uses a tree structure internally to ensure that
 * all insertions and removals are O(ln(n)). This provides much faster performance
 * than both an <code>ArrayList</code> and a <code>LinkedList</code> where elements
 * are inserted and removed repeatedly from anywhere in the list.
 * <p>
 * The class has been modified in the way any modification avoid renumbering of the <b>pre</b> order.
 * <ul><li>Tuned for only store {@link PreNode}. And <code>PreNode</code> knows his {@link DurableAVLNode}.</li>
 * <li>The class know if the <b>pre</b> number of items is synchronized with indexes or not. See
 * <code>preConsistent</code> integer.</li>
 * <li>When index are not synchronized the real index of <code>DurableAVLNode</code> has to be retrieved.</li>
 * <li>That's why the parent node has been added to <code>DurableAVLNode</code>. In that way retrieving a
 *  node index can be performed in O(H) where H is the height of the tree.</li></ul>
 * @author Joerg Schmuecker
 * @author Stephen Colebourne
 * @author Mathieu Bastian
 */
public class DurableTreeList extends AbstractList<PreNode> implements Iterable<PreNode> {
//    add; toArray; iterator; insert; get; indexOf; remove
//    TreeList = 1260;7360;3080;  160;   170;3400;  170;
//   ArrayList =  220;1480;1760; 6870;    50;1540; 7200;
//  LinkedList =  270;7360;3350;55860;290720;2910;55200;

    /** The root node in the AVL tree */
    DurableAVLNode root;
    /** The current size of the list */
    int size = 0;
    private int preConsistent = 0;

    //-----------------------------------------------------------------------
    /**
     * Constructs a new empty list.
     */
    public DurableTreeList() {
        super();
    }

    /**
     * Constructs a new empty list that copies the specified list.
     * 
     * @param coll  the collection to copy
     * @throws NullPointerException if the collection is null
     */
    public DurableTreeList(Collection<PreNode> coll) {
        super();
        addAll(coll);
    }

    public void incPreConsistent() {
        preConsistent++;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the element at the specified index.
     * 
     * @param index  the index to retrieve
     * @return the element at the specified index
     */
    public PreNode get(int index) {
        checkInterval(index, 0, size() - 1);
        return root.get(index).getValue();
    }

    public DurableAVLNode getNode(int index) {
        checkInterval(index, 0, size() - 1);
        return root.get(index);
    }

    /**
     * Gets the current size of the list.
     * 
     * @return the current size
     */
    public int size() {
        return size;
    }

    /**
     * Gets an iterator over the list.
     * 
     * @return an iterator over the list
     */
    @Override
    public Iterator<PreNode> iterator() {
        // override to go 75% faster
        return new TreeListIterator(this);
    }

    public Iterator<PreNode> iterator(int fromIndex) {
        // override to go 75% faster
        return new TreeListIterator(this, fromIndex);
    }

    /**
     * Searches for the index of an object in the list.
     * 
     * @return the index of the object, -1 if not found
     */
    public int indexOf(PreNode object) {
        // override to go 75% faster
        if (root == null) {
            return -1;
        }

        return root.indexOf(object, root.relativePosition);
    }

    /**
     * Searches for the presence of an object in the list.
     * 
     * @return true if the object is found
     */
    public boolean contains(PreNode object) {
        return (indexOf(object) >= 0);
    }

    /**
     * Converts the list into an array.
     * 
     * @return the list as an array
     */
    @Override
    public PreNode[] toArray() {
        // override to go 20% faster
        PreNode[] array = new PreNode[size()];
        if (root != null) {
            root.toArray(array, root.relativePosition);
        }
        return array;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a new element to the list.
     * 
     * @param index  the index to add before
     * @param obj  the element to add
     */
    @Override
    public void add(int index, PreNode obj) {
        modCount++;
        checkInterval(index, 0, size());
        incPreConsistent();
        if (root == null) {
            root = new DurableAVLNode(this, index, obj, null, null, null);
        } else {
            root = root.insert(index, obj);
            root.parent = null;
        }
        size++;
    }

    @Override
    public boolean add(PreNode e) {
        add(size, e);
        return true;
    }

    /**
     * Sets the element at the specified index.
     * 
     * @param index  the index to set
     * @param obj  the object to store at the specified index
     * @return the previous object at that index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public PreNode set(int index, PreNode obj) {
        checkInterval(index, 0, size() - 1);
        DurableAVLNode node = root.get(index);
        PreNode result = node.value;
        node.setValue(obj);
        return result;
    }

    /**
     * Removes the element at the specified index.
     * 
     * @param index  the index to remove
     * @return the previous object at that index
     */
    @Override
    public PreNode remove(int index) {
        modCount++;
        checkInterval(index, 0, size() - 1);
        PreNode result = get(index);
        result.avlNode.setIndex(index);
        root = root.remove(index);
        result.avlNode = null;
        result.parent = null;
        size--;
        incPreConsistent();
        return result;
    }

    public PreNode removeAndKeepParent(int index) {
        checkInterval(index, 0, size() - 1);

        //Remove without setting null parent
        PreNode node = get(index);
        root = root.remove(index);
        node.avlNode = null;
        node.size = 0;
        size--;
        incPreConsistent();
        return node;
    }

    public void move(int index, int destination) {
        checkInterval(index, 0, size() - 1);

        PreNode node = get(index);
        PreNode parent = get(destination);
        int destinationPre = parent.pre + parent.size + 1;
        int nodeLimit = node.pre+node.size;
        boolean forward = destinationPre > node.pre;
        int difflevel = 0;
        
        //Move descendant & self
        int count = 0;
        for(int i=node.pre;i<=nodeLimit;i++) {
            int sourcePre = i;
            int destPre = destinationPre + count;
            if(forward) {
                sourcePre-=count;
                destPre-=count+1;
            }

            PreNode sourceNode = get(sourcePre);
            root = root.remove(sourcePre);      //Remove
            sourceNode.avlNode = null;          //Remove
            size--;                             //Remove
            //System.out.println("add "+(destPre)+"   remove "+sourceNode.getId());
            add(destPre, sourceNode);
                   
            if(count==0) {
                sourceNode.parent = parent;
                difflevel = node.parent.level - node.level + 1;
            }
            sourceNode.level += difflevel;

            count++;
        }

        incPreConsistent();
    }

    /**
     * Clears the list, removing all entries.
     */
    @Override
    public void clear() {
        modCount++;
        root = null;
        size = 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether the index is valid.
     * 
     * @param index  the index to check
     * @param startIndex  the first allowed index
     * @param endIndex  the last allowed index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    private void checkInterval(int index, int startIndex, int endIndex) {
        if (index < startIndex || index > endIndex) {
            throw new IndexOutOfBoundsException("Invalid index:" + index + ", size=" + size());
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Implements an DurableAVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.List}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    public static class DurableAVLNode {

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        private DurableAVLNode left;
        /** Flag indicating that left reference is not a subtree but the predecessor. */
        private boolean leftIsPrevious;
        /** The right child node or the successor if {@link #rightIsNext}. */
        private DurableAVLNode right;
        /** Flag indicating that right reference is not a subtree but the successor. */
        private boolean rightIsNext;
        /** How many levels of left/right are below this one. */
        private int height;
        /** The relative position, root holds absolute position. */
        private int relativePosition;
        /** The stored element. */
        PreNode value;
        private DurableAVLNode parent;
        private int preConsistent;
        private DurableTreeList tree;

        /**
         * Constructs a new node with a relative position.
         * 
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
        private DurableAVLNode(DurableTreeList treeParent, int relativePosition, PreNode obj, DurableAVLNode rightFollower, DurableAVLNode leftFollower, DurableAVLNode parentNode) {
            this.relativePosition = relativePosition;
            value = obj;
            obj.avlNode = this;
            tree = treeParent;
            rightIsNext = true;
            leftIsPrevious = true;
            right = rightFollower;
            left = leftFollower;
            parent = parentNode;
            preConsistent = tree.preConsistent;
        }
        private static int counter;

        public int getIndex() {
            if (preConsistent != tree.preConsistent) {
                //The Pre is not consistent
                DurableAVLNode currentParent = parent;
                int index = relativePosition;
                counter = 0;
                while (currentParent != null) {
                    index += currentParent.relativePosition;
                    currentParent = currentParent.parent;
                    counter++;

                    //TODO Remove This assert
                    assert counter < 10000;
                }
                value.pre = index;
                value.getPost();
                preConsistent = tree.preConsistent;
            }
            return value.pre;
        }

        public void setIndex(int index) {
            value.pre = index;
            value.getPost();
            preConsistent = tree.preConsistent;
        }

        public boolean isConsistent() {
            return preConsistent == tree.preConsistent;
        }

        /**
         * Gets the value.
         * 
         * @return the value of this node
         */
        public PreNode getValue() {
            return value;
        }

        /**
         * Sets the value.
         * 
         * @param obj  the value to store
         */
        void setValue(PreNode obj) {
            this.value = obj;
            obj.avlNode = this;
        }

        /**
         * Locate the element with the given index relative to the
         * offset of the parent of this node.
         */
        DurableAVLNode get(int index) {
            int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                //value.setPre(index);
                return this;
            }

            DurableAVLNode nextNode = ((indexRelativeToMe < 0) ? getLeftSubTree() : getRightSubTree());
            if (nextNode == null) {
                return null;
            }
            return nextNode.get(indexRelativeToMe);
        }

        /**
         * Locate the index that contains the specified object.
         */
        int indexOf(PreNode object, int index) {
            //value.setPre(index);
            if (getLeftSubTree() != null) {
                int result = left.indexOf(object, index + left.relativePosition);
                if (result != -1) {
                    return result;
                }
            }
            if (value == null ? value == object : value.equals(object)) {
                return index;
            }
            if (getRightSubTree() != null) {
                return right.indexOf(object, index + right.relativePosition);
            }
            return -1;
        }

        /**
         * Stores the node and its children into the array specified.
         * 
         * @param array the array to be filled
         * @param index the index of this node
         */
        void toArray(PreNode[] array, int index) {
            array[index] = value;
            if (getLeftSubTree() != null) {
                left.toArray(array, index + left.relativePosition);
            }
            if (getRightSubTree() != null) {
                right.toArray(array, index + right.relativePosition);
            }
        }

        /**
         * Gets the next node in the list after this one.
         * 
         * @return the next node
         */
        public DurableAVLNode next() {
            if (rightIsNext || right == null) {
                return right;
            }
            return right.min();
        }

        /**
         * Gets the node in the list before this one.
         * 
         * @return the previous node
         */
        DurableAVLNode previous() {
            if (leftIsPrevious || left == null) {
                return left;
            }
            return left.max();
        }

        /**
         * Inserts a node at the position index.  
         * 
         * @param index is the index of the position relative to the position of 
         * the parent node.
         * @param obj is the object to be stored in the position.
         */
        DurableAVLNode insert(int index, PreNode obj) {
            int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe <= 0) {
                return insertOnLeft(indexRelativeToMe, obj);
            } else {
                return insertOnRight(indexRelativeToMe, obj);
            }
        }

        private DurableAVLNode insertOnLeft(int indexRelativeToMe, PreNode obj) {
            DurableAVLNode ret = this;

            if (getLeftSubTree() == null) {
                setLeft(new DurableAVLNode(tree, -1, obj, this, left, this), null);
            } else {
                setLeft(left.insert(indexRelativeToMe, obj), null);
            }

            if (relativePosition >= 0) {
                relativePosition++;
            }
            ret = balance();
            recalcHeight();
            return ret;
        }

        private DurableAVLNode insertOnRight(int indexRelativeToMe, PreNode obj) {
            DurableAVLNode ret = this;

            if (getRightSubTree() == null) {
                setRight(new DurableAVLNode(tree, +1, obj, right, this, this), null);
            } else {
                setRight(right.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition < 0) {
                relativePosition--;
            }
            ret = balance();
            recalcHeight();
            return ret;
        }

        //-----------------------------------------------------------------------
        /**
         * Gets the left node, returning null if its a faedelung.
         */
        private DurableAVLNode getLeftSubTree() {
            return (leftIsPrevious ? null : left);
        }

        /**
         * Gets the right node, returning null if its a faedelung.
         */
        private DurableAVLNode getRightSubTree() {
            return (rightIsNext ? null : right);
        }

        /**
         * Gets the rightmost child of this node.
         * 
         * @return the rightmost child (greatest index)
         */
        private DurableAVLNode max() {
            return (getRightSubTree() == null) ? this : right.max();
        }

        /**
         * Gets the leftmost child of this node.
         * 
         * @return the leftmost child (smallest index)
         */
        private DurableAVLNode min() {
            return (getLeftSubTree() == null) ? this : left.min();
        }

        /**
         * Removes the node at a given position.
         * 
         * @param index is the index of the element to be removed relative to the position of 
         * the parent node of the current node.
         */
        DurableAVLNode remove(int index) {
            int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                return removeSelf();
            }
            if (indexRelativeToMe > 0) {
                setRight(right.remove(indexRelativeToMe), right.right);
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                setLeft(left.remove(indexRelativeToMe), left.left);
                if (relativePosition > 0) {
                    relativePosition--;
                }
            }

            recalcHeight();
            return balance();
        }

        private DurableAVLNode removeMax() {
            if (getRightSubTree() == null) {
                return removeSelf();
            }
            setRight(right.removeMax(), right.right);
            if (relativePosition < 0) {
                relativePosition++;
            }
            recalcHeight();
            return balance();
        }

        private DurableAVLNode removeMin() {
            if (getLeftSubTree() == null) {
                return removeSelf();
            }
            setLeft(left.removeMin(), left.left);
            if (relativePosition > 0) {
                relativePosition--;
            }
            recalcHeight();
            return balance();
        }

        /**
         * Removes this node from the tree.
         *
         * @return the node that replaces this one in the parent
         */
        private DurableAVLNode removeSelf() {
            if (getRightSubTree() == null && getLeftSubTree() == null) {
                return null;
            }
            if (getRightSubTree() == null) {
                if (relativePosition > 0) {
                    left.relativePosition += relativePosition + (relativePosition > 0 ? 0 : 1);
                }
                left.max().setRight(null, right);
                return left;
            }
            if (getLeftSubTree() == null) {
                right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
                right.min().setLeft(null, left);
                return right;
            }

            if (heightRightMinusLeft() > 0) {
                // more on the right, so delete from the right
                DurableAVLNode rightMin = right.min();
                value = rightMin.value;
                value.avlNode = this;
                if (leftIsPrevious) {
                    left = rightMin.left;
                }
                right = right.removeMin();
                right.parent = this;
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                // more on the left or equal, so delete from the left
                DurableAVLNode leftMax = left.max();
                value = leftMax.value;
                value.avlNode = this;
                if (rightIsNext) {
                    right = leftMax.right;
                }
                DurableAVLNode leftPrevious = left.left;
                left = left.removeMax();

                if (left == null) {
                    // special case where left that was deleted was a double link
                    // only occurs when height difference is equal
                    left = leftPrevious;
                    leftIsPrevious = true;
                } else {
                    left.parent = this;
                }

                if (relativePosition > 0) {
                    relativePosition--;
                }
            }

            recalcHeight();
            return this;
        }

        //-----------------------------------------------------------------------
        /**
         * Balances according to the AVL algorithm.
         */
        private DurableAVLNode balance() {
            switch (heightRightMinusLeft()) {
                case 1:
                case 0:
                case -1:
                    return this;
                case -2:
                    if (left.heightRightMinusLeft() > 0) {
                        setLeft(left.rotateLeft(), null);
                    }
                    return rotateRight();
                case 2:
                    if (right.heightRightMinusLeft() < 0) {
                        setRight(right.rotateRight(), null);
                    }
                    return rotateLeft();
                default:
                    throw new RuntimeException("tree inconsistent!");
            }
        }

        /**
         * Gets the relative position.
         */
        private int getOffset(DurableAVLNode node) {
            if (node == null) {
                return 0;
            }
            return node.relativePosition;
        }

        /**
         * Sets the relative position.
         */
        private int setOffset(DurableAVLNode node, int newOffest) {
            if (node == null) {
                return 0;
            }
            int oldOffset = getOffset(node);
            node.relativePosition = newOffest;
            return oldOffset;
        }

        /**
         * Sets the height by calculation.
         */
        private void recalcHeight() {
            height = Math.max(
                    getLeftSubTree() == null ? -1 : getLeftSubTree().height,
                    getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
        }

        /**
         * Returns the height of the node or -1 if the node is null.
         */
        private int getHeight(DurableAVLNode node) {
            return (node == null ? -1 : node.height);
        }

        /**
         * Returns the height difference right - left
         */
        private int heightRightMinusLeft() {
            return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
        }

        private DurableAVLNode rotateLeft() {
            DurableAVLNode newTop = right; // can't be faedelung!
            DurableAVLNode movedNode = getRightSubTree().getLeftSubTree();

            int newTopPosition = relativePosition + getOffset(newTop);
            int myNewPosition = -newTop.relativePosition;
            int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setRight(movedNode, newTop);
            newTop.parent = parent;
            newTop.setLeft(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);
            return newTop;
        }

        private DurableAVLNode rotateRight() {
            DurableAVLNode newTop = left; // can't be faedelung
            DurableAVLNode movedNode = getLeftSubTree().getRightSubTree();

            int newTopPosition = relativePosition + getOffset(newTop);
            int myNewPosition = -newTop.relativePosition;
            int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setLeft(movedNode, newTop);
            newTop.parent = parent;
            newTop.setRight(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);
            return newTop;
        }

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
        private void setLeft(DurableAVLNode node, DurableAVLNode previous) {
            leftIsPrevious = (node == null);
            if (leftIsPrevious) {
                left = previous;
            } else {
                left = node;
                left.parent = this;
            }
            //left = (leftIsPrevious ? previous : node);

            recalcHeight();
        }

        /**
         * Sets the right field to the node, or the next node if that is null
         *
         * @param node  the new left subtree node
         * @param next  the next node in the linked list
         */
        private void setRight(DurableAVLNode node, DurableAVLNode next) {
            rightIsNext = (node == null);
            if (rightIsNext) {
                right = next;
            } else {
                right = node;
                right.parent = this;
            }
            //right = (rightIsNext ? next : node);
            recalcHeight();
        }

//      private void checkFaedelung() {
//          DurableAVLNode maxNode = left.max();
//          if (!maxNode.rightIsFaedelung || maxNode.right != this) {
//              throw new RuntimeException(maxNode + " should right-faedel to " + this);
//          }
//          DurableAVLNode minNode = right.min();
//          if (!minNode.leftIsFaedelung || minNode.left != this) {
//              throw new RuntimeException(maxNode + " should left-faedel to " + this);
//          }
//      }
//
//        private int checkTreeDepth() {
//            int hright = (getRightSubTree() == null ? -1 : getRightSubTree().checkTreeDepth());
//            //          System.out.print("checkTreeDepth");
//            //          System.out.print(this);
//            //          System.out.print(" left: ");
//            //          System.out.print(_left);
//            //          System.out.print(" right: ");
//            //          System.out.println(_right);
//
//            int hleft = (left == null ? -1 : left.checkTreeDepth());
//            if (height != Math.max(hright, hleft) + 1) {
//                throw new RuntimeException(
//                    "height should be max" + hleft + "," + hright + " but is " + height);
//            }
//            return height;
//        }
//
//        private int checkLeftSubNode() {
//            if (getLeftSubTree() == null) {
//                return 0;
//            }
//            int count = 1 + left.checkRightSubNode();
//            if (left.relativePosition != -count) {
//                throw new RuntimeException();
//            }
//            return count + left.checkLeftSubNode();
//        }
//        
//        private int checkRightSubNode() {
//            DurableAVLNode right = getRightSubTree();
//            if (right == null) {
//                return 0;
//            }
//            int count = 1;
//            count += right.checkLeftSubNode();
//            if (right.relativePosition != count) {
//                throw new RuntimeException();
//            }
//            return count + right.checkRightSubNode();
//        }
        /**
         * Used for debugging.
         */
        public String toString() {
            return "AVLNode(" + relativePosition + "," + (left != null) + "," + value +
                    "," + (getRightSubTree() != null) + ", faedelung " + rightIsNext + " )";
        }
    }

    public DurableAVLNode getRoot() {
        return root;
    }
}