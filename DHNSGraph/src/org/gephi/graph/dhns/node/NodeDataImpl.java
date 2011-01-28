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
package org.gephi.graph.dhns.node;

import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Node;
import org.gephi.graph.spi.LayoutData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.TextData;
import org.gephi.graph.dhns.utils.avl.ViewNodeTree;

/**
 * Implementation of the node data interface.
 *
 * @author Mathieu Bastian
 */
public class NodeDataImpl implements NodeData, GroupData {

    //Dhns
    protected final int ID;
    protected final ViewNodeTree nodes;
    //NodeData
    protected LayoutData layoutData;
    protected float x;
    protected float y;
    protected float z;
    protected float r = 0f;
    protected float g = 0f;
    protected float b = 0f;
    protected float alpha = 1f;
    protected float size = 1f;
    protected Model model;
    protected boolean fixed;
    protected String label;
    protected Attributes attributes;
    protected TextData textData;
    protected Model hullModel;

    public NodeDataImpl(int ID, AbstractNode rootNode) {
        this.nodes = new ViewNodeTree();
        if (rootNode != null) {
            this.nodes.add(rootNode);
        }
        this.ID = ID;
        this.x = (float) ((0.01 + Math.random()) * 1000) - 500;
        this.y = (float) ((0.01 + Math.random()) * 1000) - 500;
    }

    public int getID() {
        return ID;
    }

    public ViewNodeTree getNodes() {
        return nodes;
    }

    public AbstractNode getRootNode() {
        return nodes.get(0);
    }

    public Node getNode(int viewId) {
        return nodes.get(viewId);
    }

    public LayoutData getLayoutData() {
        return layoutData;
    }

    public void setLayoutData(LayoutData layoutData) {
        this.layoutData = layoutData;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttributes() {
        return attributes != null;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
        updatePositionFlag();
    }

    public void setY(float y) {
        this.y = y;
        updatePositionFlag();
    }

    public void setZ(float z) {
        this.z = z;
        updatePositionFlag();
    }

    private void updatePositionFlag() {
        if (model != null) {
            model.updatePositionFlag();
        }
    }

    public float getRadius() {
        return size;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setB(float b) {
        this.b = b;
    }

    public void setColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void setLabel(String label) {
        if (attributes != null) {
            attributes.setValue(PropertiesColumn.NODE_LABEL.getIndex(), label);
        } else {
            this.label = label;
        }
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model obj) {
        this.model = obj;
    }

    public String getLabel() {
        if (attributes != null) {
            return (String) attributes.getValue(PropertiesColumn.NODE_LABEL.getIndex());
        } else {
            return label;
        }
    }

    public String getId() {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.getValue(PropertiesColumn.NODE_ID.getIndex());
    }

    public String setId(String id) {
        if (attributes == null) {
            return null;
        }
        String oldId = (String) attributes.getValue(PropertiesColumn.NODE_ID.getIndex());
        attributes.setValue(PropertiesColumn.NODE_ID.getIndex(), id);
        return oldId;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }

    public Model getHullModel() {
        return hullModel;
    }

    public void setHullModel(Model hullModel) {
        this.hullModel = hullModel;
    }
}
