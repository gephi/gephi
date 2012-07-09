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
package org.gephi.graph.dhns.node.iterators;

import java.util.Iterator;
import org.gephi.graph.dhns.core.DurableTreeList;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.utils.collection.avl.ResetableIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.predicate.Predicate;

/**
 * {@link AbstractNode} iterator for children of a node, or the root.
 *
 * @author Mathieu Bastian
 * @see DescendantIterator
 */
public class ChildrenIterator extends AbstractNodeIterator implements Iterator<Node>, ResetableIterator {

    protected int treeSize;
    protected DurableTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    protected DurableAVLNode currentNode;
    protected boolean loopStart = true;
    //Predicate
    protected Predicate<AbstractNode> predicate;

    public ChildrenIterator(TreeStructure treeStructure, Predicate<AbstractNode> predicate) {
        this.treeList = treeStructure.getTree();
        nextIndex = 1;
        diffIndex = 2;
        treeSize = treeList.size();
        this.predicate = predicate;
    }

    public ChildrenIterator(TreeStructure treeStructure, AbstractNode node, Predicate<AbstractNode> predicate) {
        this(treeStructure, predicate);
        setNode(node);
    }

    public void setNode(AbstractNode node) {
        nextIndex = node.getPre() + 1;
        treeSize = node.getPre() + node.size + 1;
        diffIndex = 2;
    }

    public boolean hasNext() {
        while (loopStart || !predicate.evaluate(currentNode.getValue())) {

            if (!loopStart) {
                nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
                diffIndex = nextIndex - currentNode.getValue().pre;
            }
            loopStart = false;

            if (nextIndex < treeSize) {
                if (diffIndex > 1) {
                    currentNode = treeList.getNode(nextIndex);
                } else {
                    currentNode = currentNode.next();
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public AbstractNode next() {
        nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
        diffIndex = nextIndex - currentNode.getValue().pre;
        loopStart = true;
        return currentNode.getValue();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
