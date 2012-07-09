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
package org.gephi.utils.collection.avl;

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
