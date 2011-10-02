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
package org.gephi.io.processor.plugin;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicFloat;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.project.api.Workspace;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractProcessor {

    protected Workspace workspace;
    protected ContainerUnloader container;
    protected AttributeModel attributeModel;

    protected void flushToNode(NodeDraftGetter nodeDraft, Node node) {

        if (nodeDraft.getColor() != null) {
            node.getNodeData().setR(nodeDraft.getColor().getRed() / 255f);
            node.getNodeData().setG(nodeDraft.getColor().getGreen() / 255f);
            node.getNodeData().setB(nodeDraft.getColor().getBlue() / 255f);
        }

        if (nodeDraft.getLabel() != null) {
            node.getNodeData().setLabel(nodeDraft.getLabel());
        }

        if (node.getNodeData().getTextData() != null) {
            node.getNodeData().getTextData().setVisible(nodeDraft.isLabelVisible());
        }

        if (nodeDraft.getLabelColor() != null && node.getNodeData().getTextData() != null) {
            Color labelColor = nodeDraft.getLabelColor();
            node.getNodeData().getTextData().setColor(labelColor.getRed() / 255f, labelColor.getGreen() / 255f, labelColor.getBlue() / 255f, labelColor.getAlpha() / 255f);
        }

        if (nodeDraft.getLabelSize() != -1f && node.getNodeData().getTextData() != null) {
            node.getNodeData().getTextData().setSize(nodeDraft.getLabelSize());
        }

        node.getNodeData().setX(nodeDraft.getX());
        node.getNodeData().setY(nodeDraft.getY());
        node.getNodeData().setZ(nodeDraft.getZ());

        if (nodeDraft.getSize() != 0 && !Float.isNaN(nodeDraft.getSize())) {
            node.getNodeData().setSize(nodeDraft.getSize());
        } else {
            node.getNodeData().setSize(10f);
        }

        if (nodeDraft.getTimeInterval() != null) {
            AttributeColumn col = attributeModel.getNodeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
            if (col == null) {
                col = attributeModel.getNodeTable().addColumn(DynamicModel.TIMEINTERVAL_COLUMN, "Time Interval", AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY, null);
            }
            node.getNodeData().getAttributes().setValue(col.getIndex(), nodeDraft.getTimeInterval());
        }

        //Attributes
        flushToNodeAttributes(nodeDraft, node);
    }

    protected void flushToNodeAttributes(NodeDraftGetter nodeDraft, Node node) {
        if (node.getNodeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            for (AttributeValue val : nodeDraft.getAttributeRow().getValues()) {
                if (!val.getColumn().getOrigin().equals(AttributeOrigin.PROPERTY) && val.getValue() != null) {
                    row.setValue(val.getColumn(), val.getValue());
                }
            }
        }
    }

    protected void flushToEdge(EdgeDraftGetter edgeDraft, Edge edge) {
        if (edgeDraft.getColor() != null) {
            edge.getEdgeData().setR(edgeDraft.getColor().getRed() / 255f);
            edge.getEdgeData().setG(edgeDraft.getColor().getGreen() / 255f);
            edge.getEdgeData().setB(edgeDraft.getColor().getBlue() / 255f);
        } else {
            edge.getEdgeData().setR(-1f);
            edge.getEdgeData().setG(-1f);
            edge.getEdgeData().setB(-1f);
        }

        if (edgeDraft.getLabel() != null) {
            edge.getEdgeData().setLabel(edgeDraft.getLabel());
        }

        if (edge.getEdgeData().getTextData() != null) {
            edge.getEdgeData().getTextData().setVisible(edgeDraft.isLabelVisible());
        }

        if (edgeDraft.getLabelSize() != -1f && edge.getEdgeData().getTextData() != null) {
            edge.getEdgeData().getTextData().setSize(edgeDraft.getLabelSize());
        }

        if (edgeDraft.getLabelColor() != null && edge.getEdgeData().getTextData() != null) {
            Color labelColor = edgeDraft.getLabelColor();
            edge.getEdgeData().getTextData().setColor(labelColor.getRed() / 255f, labelColor.getGreen() / 255f, labelColor.getBlue() / 255f, labelColor.getAlpha() / 255f);
        }

        if (edgeDraft.getTimeInterval() != null) {
            AttributeColumn col = attributeModel.getEdgeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
            if (col == null) {
                col = attributeModel.getEdgeTable().addColumn(DynamicModel.TIMEINTERVAL_COLUMN, "Time Interval", AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY, null);
            }
            edge.getEdgeData().getAttributes().setValue(col.getIndex(), edgeDraft.getTimeInterval());
        }

        //Attributes
        flushToEdgeAttributes(edgeDraft, edge);
    }

    protected void flushToEdgeAttributes(EdgeDraftGetter edgeDraft, Edge edge) {
        if (edge.getEdgeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            for (AttributeValue val : edgeDraft.getAttributeRow().getValues()) {
                if (!val.getColumn().getOrigin().equals(AttributeOrigin.PROPERTY) && val.getValue() != null) {
                    row.setValue(val.getColumn(), val.getValue());
                }
            }
        }

        //Dynamic Weight
        AttributeColumn dynamicWeightCol = container.getAttributeModel().getEdgeTable().getColumn(PropertiesColumn.EDGE_WEIGHT.getTitle(), AttributeType.DYNAMIC_FLOAT);
        if (dynamicWeightCol != null) {
            DynamicFloat weight = (DynamicFloat) edgeDraft.getAttributeRow().getValue(dynamicWeightCol.getIndex());
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            if (weight == null) {
                row.setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), new DynamicFloat(new Interval<Float>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, edgeDraft.getWeight())));
            } else {
                row.setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), weight);
            }
        }
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setContainer(ContainerUnloader container) {
        this.container = container;
    }
}
