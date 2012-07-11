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
package org.gephi.graph.dhns.edge;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicFloat;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.spi.LayoutData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.TextData;

/**
 * Implementation of the edge data interface.
 *
 * @author Mathieu Bastian
 */
public class EdgeDataImpl implements EdgeData {

    protected AbstractEdge edge;
    protected LayoutData layoutData;
    protected float r = -1f;
    protected float g = 0f;
    protected float b = 0f;
    protected float alpha = 1f;
    private String label;
    private Model model;
    protected Attributes attributes;
    protected TextData textData;

    public EdgeDataImpl(AbstractEdge edge) {
        this.edge = edge;
    }

    public AbstractEdge getEdge() {
        return edge;
    }

    public NodeData getSource() {
        return edge.getSource().getNodeData();
    }

    public NodeData getTarget() {
        return edge.getTarget().getNodeData();
    }

    public String getLabel() {
        if (attributes != null) {
            return (String) attributes.getValue(PropertiesColumn.EDGE_LABEL.getIndex());
        } else {
            return label;
        }
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

    public float x() {
        return (getSource().x() + 2 * getTarget().x()) / 3f;
    }

    public float y() {
        return (getSource().y() + 2 * getTarget().y()) / 3f;
    }

    public float z() {
        return (getSource().z() + 2 * getTarget().z()) / 3f;
    }

    public void setX(float x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setY(float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setZ(float z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getRadius() {
        return 0;
    }

    public float getSize() {
        return edge.getWeight();
    }

    public void setSize(float size) {
        edge.setWeight(size);
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

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }

    public void setLabel(String label) {
        if (attributes != null) {
            attributes.setValue(PropertiesColumn.EDGE_LABEL.getIndex(), label);
        } else {
            this.label = label;
        }
    }

    public String setId(String id) {
        if (attributes == null) {
            return null;
        }
        String oldId = (String) attributes.getValue(PropertiesColumn.EDGE_ID.getIndex());
        attributes.setValue(PropertiesColumn.EDGE_ID.getIndex(), id);
        return oldId;
    }

    public String getId() {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.getValue(PropertiesColumn.EDGE_ID.getIndex());
    }

    public float getWeight() {
        if (attributes == null) {
            return 1f;
        }
        Object weight = attributes.getValue(PropertiesColumn.EDGE_WEIGHT.getIndex());
        if (weight instanceof DynamicFloat) {
            weight = ((DynamicFloat) weight).getValue(Estimator.AVERAGE);
        }
        if (weight == null) {
            return 1f;
        }
        return (Float) weight;
    }

    public float getWeight(double low, double high) {
        if (attributes == null) {
            return 1f;
        }
        Object weight = attributes.getValue(PropertiesColumn.EDGE_WEIGHT.getIndex());
        if (weight instanceof DynamicFloat) {
            weight = ((DynamicFloat) weight).getValue(low, high, Estimator.AVERAGE);
        }
        if (weight == null) {
            return 1f;
        }
        return (Float) weight;
    }

    public void setWeight(float weight) {
        if (attributes != null) {
            if (!((AttributeRow) attributes).getColumnAt(PropertiesColumn.EDGE_WEIGHT.getIndex()).getType().equals(AttributeType.DYNAMIC_FLOAT)) {
                attributes.setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), weight);
            }
        }
    }

    public void moveFrom(EdgeData edgeData) {
        this.r = edgeData.r();
        this.g = edgeData.g();
        this.b = edgeData.b();
        this.alpha = edgeData.alpha();
        this.label = edgeData.getLabel();
        this.textData = edgeData.getTextData();
        if (attributes != null) {
            ((AttributeRow) attributes).setValues((AttributeRow) edgeData.getAttributes());
        }
    }
}
