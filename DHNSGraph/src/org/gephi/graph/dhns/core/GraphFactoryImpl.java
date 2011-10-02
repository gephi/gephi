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
package org.gephi.graph.dhns.core;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.TextData;
import org.gephi.graph.spi.TextDataFactory;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.edge.SelfLoopImpl;
import org.gephi.graph.dhns.edge.MixedEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.openide.util.Lookup;

/**
 * Implementation of a basic node and edge factory. If possible set {@link Attributes} to objets.
 * <p>
 * Return {@link AbstractNode} or {@link AbstractEdge}.
 *
 * @author Mathieu Bastian
 */
public class GraphFactoryImpl implements GraphFactory {

    private IDGen idGen;
    private AttributeRowFactory attributesFactory;
    private TextDataFactory textDataFactory;

    public GraphFactoryImpl(IDGen idGen, AttributeRowFactory attributesFactory) {
        this.idGen = idGen;
        this.attributesFactory = attributesFactory;
        this.textDataFactory = Lookup.getDefault().lookup(TextDataFactory.class);
    }

    public AttributeRow newNodeAttributes(NodeData nodeData) {
        if (attributesFactory == null) {
            return null;
        }
        return attributesFactory.newNodeRow(nodeData);
    }

    public AttributeRow newEdgeAttributes(EdgeData edgeData) {
        if (attributesFactory == null) {
            return null;
        }
        return attributesFactory.newEdgeRow(edgeData);
    }

    public TextData newTextData() {
        if (textDataFactory == null) {
            return null;
        }
        return textDataFactory.newTextData();
    }

    public AbstractNode newNode() {
        return newNode(null, 0);
    }

    public AbstractNode newNode(int viewId) {
        return newNode(null, viewId);
    }

    public AbstractNode newNode(String id, int viewId) {
        AbstractNode node = new AbstractNode(idGen.newNodeId(), viewId, 0, 0, 0, null);  //with wiew = 0
        node.getNodeData().setAttributes(newNodeAttributes(node.getNodeData()));
        node.getNodeData().setTextData(newTextData());
        if (id != null) {
            node.getNodeData().setId(id);
        } else {
            node.getNodeData().setId("" + node.getId());
        }
        return node;
    }

    public AbstractNode newNode(String id) {
        return newNode(id, 0);
    }

    public AbstractEdge newEdge(Node source, Node target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        AbstractNode nodeSource = (AbstractNode) source;
        AbstractNode nodeTarget = (AbstractNode) target;
        AbstractEdge edge;
        if (source == target) {
            edge = new SelfLoopImpl(idGen.newEdgeId(), nodeSource);
        } else {
            edge = new ProperEdgeImpl(idGen.newEdgeId(), nodeSource, nodeTarget);
        }
        edge.setAttributes(newEdgeAttributes(edge.getEdgeData()));
        edge.getEdgeData().setTextData(newTextData());
        edge.getEdgeData().setId("" + edge.getId());
        return edge;
    }

    public AbstractEdge newEdge(Node source, Node target, float weight, boolean directed) {
        return newEdge(null, source, target, weight, directed);
    }

    public AbstractEdge newEdge(String id, Node source, Node target, float weight, boolean directed) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        AbstractNode nodeSource = (AbstractNode) source;
        AbstractNode nodeTarget = (AbstractNode) target;
        AbstractEdge edge;
        if (source == target) {
            edge = new SelfLoopImpl(idGen.newEdgeId(), nodeSource);
        } else {
            edge = new MixedEdgeImpl(idGen.newEdgeId(), nodeSource, nodeTarget, directed);
        }

        edge.setAttributes(newEdgeAttributes(edge.getEdgeData()));
        edge.setWeight(weight);
        edge.getEdgeData().setTextData(newTextData());
        if (id != null) {
            edge.getEdgeData().setId(id);
        } else {
            edge.getEdgeData().setId("" + edge.getId());
        }
        return edge;
    }

    public MetaEdgeImpl newMetaEdge(Node source, Node target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        AbstractNode nodeSource = (AbstractNode) source;
        AbstractNode nodeTarget = (AbstractNode) target;
        MetaEdgeImpl edge = new MetaEdgeImpl(idGen.newEdgeId(), nodeSource, nodeTarget);
        edge.setAttributes(newEdgeAttributes(edge.getEdgeData()));
        edge.getEdgeData().setTextData(newTextData());
        edge.getEdgeData().setId("" + edge.getId());
        return edge;
    }
}
