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
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.DescendantAndSelfIterator;
import org.gephi.graph.dhns.predicate.Predicate;

/**
 * See {@link RangeEdgeIterator}
 *
 * @author Mathieu Bastian
 */
public class RangeEdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    protected AbstractNodeIterator nodeIterator;
    protected ParamAVLIterator<AbstractEdge> edgeIterator;
    protected AbstractNode currentNode;
    protected AbstractEdge pointer;
    protected boolean IN = false;
    protected boolean inner;
    protected int rangeStart;
    protected int rangeLimit;
    protected AbstractNode nodeGroup;
    protected boolean undirected;
    protected Predicate<AbstractEdge> edgePredicate;
    protected Predicate<AbstractNode> nodePredicate;
    protected int viewId;

    public RangeEdgeIterator(TreeStructure treeStructure, int viewId, AbstractNode nodeGroup, AbstractNode target, boolean inner, boolean undirected, Predicate<AbstractNode> nodePredicate, Predicate<AbstractEdge> edgePredicate) {
        nodeIterator = new DescendantAndSelfIterator(treeStructure, nodeGroup, nodePredicate);
        this.inner = inner;
        this.nodeGroup = nodeGroup;
        this.rangeStart = target.getPre();
        this.rangeLimit = rangeStart + target.size;
        this.undirected = undirected;
        this.edgeIterator = new ParamAVLIterator<AbstractEdge>();
        this.nodePredicate = nodePredicate;
        this.edgePredicate = edgePredicate;
        this.viewId = viewId;
    }

    @Override
    public boolean hasNext() {
        while (true) {
            while (!edgeIterator.hasNext()) {
                if (currentNode == null) {
                    if (nodeIterator.hasNext()) {
                        currentNode = nodeIterator.next();
                        edgeIterator.setNode(currentNode.getEdgesOutTree());
                        IN = false;
                    } else {
                        return false;
                    }
                } else {
                    edgeIterator.setNode(currentNode.getEdgesInTree());
                    currentNode = null;
                    IN = true;
                }
            }

            pointer = edgeIterator.next();
            if (testTarget(pointer)) {
                return true;
            }
        }
    }

    protected boolean testTarget(AbstractEdge edgeImpl) {
        if (!undirected || edgeImpl.getUndirected(viewId) == edgeImpl) {
            if (edgePredicate.evaluate(edgeImpl)) {
                if (IN) {
                    AbstractNode source = edgeImpl.getSource(viewId);
                    if (!nodePredicate.evaluate(source)) {
                        return false;
                    }
                    int pre = source.getPre();
                    if (!inner) {
                        return pre < rangeStart || pre > rangeLimit;
                    }
                } else {
                    AbstractNode target = edgeImpl.getTarget(viewId);
                    if (!nodePredicate.evaluate(target)) {
                        return false;
                    }
                    int pre = target.getPre();
                    boolean isInner = pre >= rangeStart && pre <= rangeLimit;
                    return (inner && isInner) || (!inner && !isInner);
                }
            }
        }
        return false;
    }

    @Override
    public AbstractEdge next() {
        return pointer;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
