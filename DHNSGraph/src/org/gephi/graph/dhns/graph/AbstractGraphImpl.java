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
package org.gephi.graph.dhns.graph;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Utilities methods for managing graphs implementation like locking of non-null checking
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractGraphImpl {

    protected final Dhns dhns;
    protected final GraphViewImpl view;
    protected final TreeStructure structure;

    public AbstractGraphImpl(Dhns dhns, GraphViewImpl view) {
        this.dhns = dhns;
        this.view = view;
        this.structure = view.getStructure();
        view.addGraphReference(this);
    }

    public GraphModel getGraphModel() {
        return dhns;
    }

    public GraphView getView() {
        return view;
    }

    public void readLock() {
        dhns.readLock();
    }

    public void readUnlock() {
        dhns.readUnlock();
    }

    public void readUnlockAll() {
        dhns.readUnlockAll();
    }

    public void writeLock() {
        dhns.writeLock();
    }

    public void writeUnlock() {
        dhns.writeUnlock();
    }

    public int getNodeVersion() {
        return dhns.getGraphVersion().getNodeVersion();
    }

    public int getEdgeVersion() {
        return dhns.getGraphVersion().getEdgeVersion();
    }

    protected AbstractNode checkNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node can't be null");
        }
        AbstractNode absNode = (AbstractNode) node;
        if (!absNode.isValid(view.getViewId())) {
            //Try to find the node in the proper view
            absNode = absNode.getInView(view.getViewId());
            if (absNode == null) {
                throw new IllegalArgumentException("Node must be in the graph");
            }
        }
        return absNode;
    }

    protected AbstractEdge checkEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge abstractEdge = (AbstractEdge) edge;
        if (!abstractEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");

        }
        if (abstractEdge.isMetaEdge()) {
            throw new IllegalArgumentException("Edge can't be a meta edge");
        }
        return abstractEdge;
    }

    protected MetaEdgeImpl checkMetaEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        if (!absEdge.isMetaEdge()) {
            throw new IllegalArgumentException("edge must be a meta edge");
        }
        if (!absEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");
        }
        return (MetaEdgeImpl) absEdge;
    }

    protected AbstractEdge checkEdgeOrMetaEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("edge can't be null");
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        if (!absEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");
        }
        return absEdge;
    }

    protected boolean checkEdgeExist(AbstractNode source, AbstractNode target) {
        if (source == null) {
            throw new IllegalArgumentException("Source node must be in the graph");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target node must be in the graph");
        }
        return source.getEdgesOutTree().hasNeighbour(target);
    }

    protected AbstractEdge getSymmetricEdge(AbstractEdge edge) {
        return edge.getTarget(view.getViewId()).getEdgesOutTree().getItem(edge.getSource().getNumber());
    }

    protected AbstractEdge getSymmetricMetaEdge(AbstractEdge edge) {
        return edge.getTarget(view.getViewId()).getMetaEdgesOutTree().getItem(edge.getSource().getNumber());
    }
}
