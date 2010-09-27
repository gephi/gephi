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
package org.gephi.io.processor.plugin;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicFloat;
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
            row.setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), weight);
        }
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setContainer(ContainerUnloader container) {
        this.container = container;
    }
}
