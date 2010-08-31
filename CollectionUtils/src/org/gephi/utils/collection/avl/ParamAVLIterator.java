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
package org.gephi.utils.collection.avl;

import java.util.Iterator;
import org.gephi.utils.collection.avl.ResetableIterator;

/**
 * Iterator for the {@link ParamAVLTree}. Return items in an ascending order.
 * 
 * @author Mathieu Bastian
 * @param <Item> The type of Object in the tree
 */
public class ParamAVLIterator<Item> implements Iterator<Item>, ResetableIterator {

    private ParamAVLTree tree;
    private ParamAVLNode<Item> next;
    private Item current;

    public ParamAVLIterator() {
    }

    public ParamAVLIterator(ParamAVLNode node) {
        this.next = node;
        goToDownLeft();
    }

    public ParamAVLIterator(ParamAVLTree tree) {
        this(tree.root);
        this.tree = tree;
    }

    public void setNode(ParamAVLTree tree) {
        this.next = tree.root;
        this.tree = tree;
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
            return false;
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
        tree.remove(current);   //TODO Optimize, remove in O(1) instead of O(ln(n))
    }
}
