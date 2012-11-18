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
package org.gephi.graph.dhns.edge.iterators;

import java.util.Iterator;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.predicate.Predicate;

/**
 * Iterator for main edges for the visible graph.
 *
 * @author Mathieu Bastian
 * @see EdgeIterator
 */
public class EdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    protected AbstractNodeIterator nodeIterator;
    protected ParamAVLIterator<ProperEdgeImpl> edgeIterator;
    protected AbstractNode currentNode;
    protected AbstractEdge pointer;
    protected boolean undirected;
    protected Predicate<AbstractNode> nodePredicate;
    protected Predicate<AbstractEdge> edgePredicate;

    public EdgeIterator(TreeStructure treeStructure, AbstractNodeIterator nodeIterator, boolean undirected, Predicate<AbstractNode> nodePredicate, Predicate<AbstractEdge> edgePredicate) {
        this.nodeIterator = nodeIterator;
        edgeIterator = new ParamAVLIterator<ProperEdgeImpl>();
        this.undirected = undirected;
        this.nodePredicate = nodePredicate;
        this.edgePredicate = edgePredicate;
    }

    @Override
    public boolean hasNext() {
        while (pointer == null || (undirected && pointer.getUndirected(currentNode.getViewId()) != pointer) || !edgePredicate.evaluate(pointer)) {
            while (!edgeIterator.hasNext()) {
                if (nodeIterator.hasNext()) {
                    currentNode = nodeIterator.next();
                    if (!currentNode.getEdgesOutTree().isEmpty()) {
                        edgeIterator.setNode(currentNode.getEdgesOutTree());
                    }
                } else {
                    return false;
                }
            }

            pointer = edgeIterator.next();
            if (!nodePredicate.evaluate(pointer.getTarget(currentNode.getViewId()))) {
                pointer = null;
            }
        }
        return true;
    }

    @Override
    public AbstractEdge next() {
        AbstractEdge e = pointer;
        pointer = null;
        return e;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
