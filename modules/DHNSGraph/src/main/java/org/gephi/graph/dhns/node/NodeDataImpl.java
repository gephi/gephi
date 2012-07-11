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
    protected float r = 0.6f;
    protected float g = 0.6f;
    protected float b = 0.6f;
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
