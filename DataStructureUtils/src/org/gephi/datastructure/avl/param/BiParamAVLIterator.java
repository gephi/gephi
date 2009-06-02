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
package org.gephi.datastructure.avl.param;

import java.util.Iterator;

/**
 * Identical at <code>ParamAVLIterator</code>, but with two trees. When the first is fully browsed, 
 * the iterator moves to the second tree items. Used for <b>IN</b> and <b>OUT</b> edges tree.
 * <p>
 * Support <code>null</code> values for trees.
 *
 * @author Mathieu Bastian
 * @param <Item> The type of Object in the tree
 */
public class BiParamAVLIterator<Item> implements Iterator<Item> {

    private ParamAVLTree tree2;
    private ParamAVLTree currentTree;
    private ParamAVLNode<Item> next;
    private Item current;

    public BiParamAVLIterator(ParamAVLTree tree1, ParamAVLTree tree2) {
        if (tree1 == null) {
            this.currentTree = tree2;
        } else {
            this.currentTree = tree1;
            this.tree2 = tree2;
        }
        if (currentTree != null) {
            next = currentTree.root;
        }
        goToDownLeft();
    }

    private void goToDownLeft() {
        if (next != null) {
            while (next.left != null) {
                next = next.left;
            }
        }
    }

    public boolean hasNext() {
        if (next == null) {
            if (tree2 != null && currentTree != tree2) {
                currentTree = tree2;
                next = currentTree.root;
                if (next == null) {
                    return false;
                }
                goToDownLeft();
            } else {
                return false;
            }
        }

        current = this.next.item;

        if (next.right == null) {
            while ((next.parent != null) && (next == next.parent.right)) {
                this.next = this.next.parent;
            }

            this.next = this.next.parent;
        } else {
            this.next = this.next.right;

            while (this.next.left != null) {
                this.next = this.next.left;
            }
        }

        return true;
    }

    public Item next() {
        return current;
    }

    public void remove() {
        currentTree.remove(current);   //TODO Optimize, remove in O(1) instead of O(ln(n))
    }
}
