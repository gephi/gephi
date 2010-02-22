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

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
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

    public AttributeRow newNodeAttributes() {
        if (attributesFactory == null) {
            return null;
        }
        return attributesFactory.newNodeRow();
    }

    public AttributeRow newEdgeAttributes() {
        if (attributesFactory == null) {
            return null;
        }
        return attributesFactory.newEdgeRow();
    }

    public TextData newTextData() {
        if (textDataFactory == null) {
            return null;
        }
        return textDataFactory.newTextData();
    }

    public AbstractNode newNode() {
        AbstractNode node = new AbstractNode(idGen.newNodeId(), 0, 0, 0, 0, null);  //with wiew = 0
        node.getNodeData().setAttributes(newNodeAttributes());
        node.getNodeData().setTextData(newTextData());
        return node;
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
        edge.setAttributes(newEdgeAttributes());
        edge.getEdgeData().setTextData(newTextData());
        return edge;
    }

    public AbstractEdge newEdge(Node source, Node target, float weight, boolean directed) {
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
        edge.setWeight(weight);

        edge.setAttributes(newEdgeAttributes());
        edge.getEdgeData().setTextData(newTextData());
        return edge;
    }

    public MetaEdgeImpl newMetaEdge(Node source, Node target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        AbstractNode nodeSource = (AbstractNode) source;
        AbstractNode nodeTarget = (AbstractNode) target;
        MetaEdgeImpl edge = new MetaEdgeImpl(idGen.newEdgeId(), nodeSource, nodeTarget);
        edge.setAttributes(newEdgeAttributes());
        edge.getEdgeData().setTextData(newTextData());
        return edge;
    }
}
