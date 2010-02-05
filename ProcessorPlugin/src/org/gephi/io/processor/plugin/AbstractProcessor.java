/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.processor.plugin;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.project.api.Workspace;
import org.gephi.timeline.api.TimelineController;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractProcessor {

    protected TimelineController timelineController;
    protected Workspace workspace;

    protected void flushToNode(NodeDraftGetter nodeDraft, Node node) {

        if (nodeDraft.getColor() != null) {
            node.getNodeData().setR(nodeDraft.getColor().getRed() / 255f);
            node.getNodeData().setG(nodeDraft.getColor().getGreen() / 255f);
            node.getNodeData().setB(nodeDraft.getColor().getBlue() / 255f);
        }

        if (nodeDraft.getLabel() != null) {
            node.getNodeData().setLabel(nodeDraft.getLabel());
        }

        node.getNodeData().getTextData().setVisible(nodeDraft.isLabelVisible());

        if (nodeDraft.getLabelColor() != null) {
            Color labelColor = nodeDraft.getLabelColor();
            node.getNodeData().getTextData().setColor(labelColor.getRed() / 255f, labelColor.getGreen() / 255f, labelColor.getBlue() / 255f, labelColor.getAlpha() / 255f);
        }

        if (nodeDraft.getLabelSize() != -1f) {
            node.getNodeData().getTextData().setSize(nodeDraft.getLabelSize());
        }

        if (nodeDraft.getX() != 0 && !Float.isNaN(nodeDraft.getX())) {
            node.getNodeData().setX(nodeDraft.getX());
        } else {
            node.getNodeData().setX((float) ((0.01 + Math.random()) * 1000) - 500);
        }
        if (nodeDraft.getY() != 0 && !Float.isNaN(nodeDraft.getY())) {
            node.getNodeData().setY(nodeDraft.getY());
        } else {
            node.getNodeData().setY((float) ((0.01 + Math.random()) * 1000) - 500);
        }

        if (nodeDraft.getZ() != 0 && !Float.isNaN(nodeDraft.getZ())) {
            node.getNodeData().setZ(nodeDraft.getZ());
        }

        if (nodeDraft.getSize() != 0 && !Float.isNaN(nodeDraft.getSize())) {
            node.getNodeData().setSize(nodeDraft.getSize());
        } else {
            node.getNodeData().setSize(10f);
        }

        if (nodeDraft.getId() != null) {
            node.getNodeData().setId(nodeDraft.getId());
        }

        //Dynamic
        if (timelineController != null && nodeDraft.getSlices() != null) {
            for (String[] slice : nodeDraft.getSlices()) {
                String from = slice[0];
                String to = slice[1];
                timelineController.pushSlice(workspace, from, to, node);
            }
        }

        //Attributes
        if (node.getNodeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            for (AttributeValue val : nodeDraft.getAttributeValues()) {
                if (val.getValue() != null) {
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
        edge.getEdgeData().getTextData().setVisible(edgeDraft.isLabelVisible());

        if (edgeDraft.getLabelSize() != -1f) {
            edge.getEdgeData().getTextData().setSize(edgeDraft.getLabelSize());
        }

        if (edgeDraft.getLabelColor() != null) {
            Color labelColor = edgeDraft.getLabelColor();
            edge.getEdgeData().getTextData().setColor(labelColor.getRed() / 255f, labelColor.getGreen() / 255f, labelColor.getBlue() / 255f, labelColor.getAlpha() / 255f);
        }

        //Attributes
        if (edge.getEdgeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            for (AttributeValue val : edgeDraft.getAttributeValues()) {
                if (val.getValue() != null) {
                    row.setValue(val.getColumn(), val.getValue());
                }
            }
        }
    }
}
