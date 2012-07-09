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
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;

/**
 * Edge Iterator for edges linked to the given node. It gives IN, OUT or IN+OUT edges
 *
 * @author Mathieu Bastian
 * @see EdgeNodeIterator
 */
public class MetaEdgeNodeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    public enum EdgeNodeIteratorMode {

        OUT, IN, BOTH
    };
    protected ParamAVLIterator<MetaEdgeImpl> edgeIterator;
    protected EdgeNodeIteratorMode mode;
    protected MetaEdgeImpl pointer;
    protected boolean undirected;
    protected MetaEdgeTree outTree;
    protected MetaEdgeTree inTree;

    public MetaEdgeNodeIterator(MetaEdgeTree outTree, MetaEdgeTree inTree, EdgeNodeIteratorMode mode, boolean undirected) {
        this.outTree = outTree;
        this.inTree = inTree;
        this.mode = mode;
        this.edgeIterator = new ParamAVLIterator<MetaEdgeImpl>();
        if (mode.equals(EdgeNodeIteratorMode.OUT) || mode.equals(EdgeNodeIteratorMode.BOTH)) {
            this.edgeIterator.setNode(outTree);
        } else {
            this.edgeIterator.setNode(inTree);
        }
        this.undirected = undirected;
    }

    public boolean hasNext() {
        while (pointer == null || (undirected && pointer.getUndirected() != pointer)) {
            if (mode.equals(EdgeNodeIteratorMode.BOTH)) {
                boolean res = edgeIterator.hasNext();
                if (res) {
                    pointer = edgeIterator.next();
                    if (pointer.isSelfLoop()) {  //Ignore self loop here to avoid double iteration
                        pointer = null;
                    }
                } else {
                    this.edgeIterator.setNode(inTree);
                    this.mode = EdgeNodeIteratorMode.IN;
                }
            } else {
                if (edgeIterator.hasNext()) {
                    pointer = edgeIterator.next();
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public AbstractEdge next() {
        AbstractEdge e = pointer;
        pointer = null;
        return e;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
