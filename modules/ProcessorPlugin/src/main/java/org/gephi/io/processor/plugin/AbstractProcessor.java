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
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;

public abstract class AbstractProcessor {

    protected ProgressTicket progressTicket;
    protected Workspace workspace;
    protected ContainerUnloader[] containers;
    protected GraphModel graphModel;

    protected void flushColumns(ContainerUnloader container) {
        TimeRepresentation timeRepresentation = container.getTimeRepresentation();
        Table nodeTable = graphModel.getNodeTable();
        for (ColumnDraft col : container.getNodeColumns()) {
            if (!nodeTable.hasColumn(col.getId())) {
                Class typeClass = col.getTypeClass();
                if (col.isDynamic()) {
                    if (timeRepresentation.equals(TimeRepresentation.TIMESTAMP)) {
                        typeClass = AttributeUtils.getTimestampMapType(typeClass);
                    } else {
                        typeClass = AttributeUtils.getIntervalMapType(typeClass);
                    }
                }
                nodeTable.addColumn(col.getId(), col.getTitle(), typeClass, Origin.DATA, col.getDefaultValue(), !col.isDynamic());
            }
        }
        Table edgeTable = graphModel.getEdgeTable();
        for (ColumnDraft col : container.getEdgeColumns()) {
            if (!edgeTable.hasColumn(col.getId())) {
                Class typeClass = col.getTypeClass();
                if (col.isDynamic()) {
                    if (timeRepresentation.equals(TimeRepresentation.TIMESTAMP)) {
                        typeClass = AttributeUtils.getTimestampMapType(typeClass);
                    } else {
                        typeClass = AttributeUtils.getIntervalMapType(typeClass);
                    }
                }
                edgeTable.addColumn(col.getId(), col.getTitle(), typeClass, Origin.DATA, col.getDefaultValue(), !col.isDynamic());
            }
        }
    }

    protected void flushToNode(NodeDraft nodeDraft, Node node) {
        if (nodeDraft.getColor() != null) {
            node.setColor(nodeDraft.getColor());
        }

        if (nodeDraft.getLabel() != null) {
            node.setLabel(nodeDraft.getLabel());
        }

        if (node.getTextProperties() != null) {
            node.getTextProperties().setVisible(nodeDraft.isLabelVisible());
        }

        if (nodeDraft.getLabelColor() != null && node.getTextProperties() != null) {
            Color labelColor = nodeDraft.getLabelColor();
            node.getTextProperties().setColor(labelColor);
        }

        if (nodeDraft.getLabelSize() != -1f && node.getTextProperties() != null) {
            node.getTextProperties().setSize(nodeDraft.getLabelSize());
        }

        node.setX(nodeDraft.getX());
        node.setY(nodeDraft.getY());
        node.setZ(nodeDraft.getZ());

        if (nodeDraft.getSize() != 0 && !Float.isNaN(nodeDraft.getSize())) {
            node.setSize(nodeDraft.getSize());
        } else {
            node.setSize(10f);
        }

        //Timeset
        if (nodeDraft.getTimeSet() != null) {
            flushTimeSet(nodeDraft.getTimeSet(), node);
        }

        //Attributes
        flushToElementAttributes(nodeDraft, node);
    }

    protected void flushToElementAttributes(ElementDraft elementDraft, Element element) {
        for (ColumnDraft col : elementDraft.getColumns()) {
            Object val = elementDraft.getValue(col.getId());
            if (val != null) {
                TimeMap existingMap;
                if (col.isDynamic() && (existingMap = (TimeMap) element.getAttribute(col.getId())) != null && !existingMap.isEmpty()) {
                    Object[] keys = ((TimeMap) val).toKeysArray();
                    Object[] vals = ((TimeMap) val).toValuesArray();
                    for (int i = 0; i < keys.length; i++) {
                        existingMap.put(keys[i], vals[i]);
                    }
                } else {
                    element.setAttribute(col.getId(), val);
                }
            }
        }
    }

    protected void flushToEdge(EdgeDraft edgeDraft, Edge edge) {
        if (edgeDraft.getColor() != null) {
            edge.setColor(edgeDraft.getColor());
        } else {
            edge.setR(0f);
            edge.setG(0f);
            edge.setB(0f);
            edge.setAlpha(0f);
        }

        if (edgeDraft.getLabel() != null) {
            edge.setLabel(edgeDraft.getLabel());
        }

        if (edge.getTextProperties() != null) {
            edge.getTextProperties().setVisible(edgeDraft.isLabelVisible());
        }

        if (edgeDraft.getLabelSize() != -1f && edge.getTextProperties() != null) {
            edge.getTextProperties().setSize(edgeDraft.getLabelSize());
        }

        if (edgeDraft.getLabelColor() != null && edge.getTextProperties() != null) {
            Color labelColor = edgeDraft.getLabelColor();
            edge.getTextProperties().setColor(labelColor);
        }

        //Timeset
        if (edgeDraft.getTimeSet() != null) {
            flushTimeSet(edgeDraft.getTimeSet(), edge);
        }

        //Attributes
        flushToElementAttributes(edgeDraft, edge);
    }

    protected void flushTimeSet(TimeSet timeSet, Element element) {
        TimeSet existingTimeSet = (TimeSet) element.getAttribute("timeset");
        if (existingTimeSet != null && !existingTimeSet.isEmpty()) {
            for (Object o : timeSet.toArray()) {
                existingTimeSet.add(o);
            }
        } else {
            element.setAttribute("timeset", timeSet);
        }
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setContainers(ContainerUnloader[] containers) {
        this.containers = containers;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
