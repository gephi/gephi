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
package org.gephi.io.importer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Issue.Level;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerImpl implements Container, ContainerLoader, ContainerUnloader {

    //MetaData
    private String source;
    //Factory
    private final FactoryImpl factory;
    //Parameters
    private final ImportContainerParameters parameters;
    //Maps
    private HashMap<String, NodeDraftImpl> nodeMap;
    private HashMap<String, NodeDraftImpl> nodeLabelMap;
    private final HashMap<String, EdgeDraftImpl> edgeMap;
    private final HashMap<String, EdgeDraftImpl> edgeSourceTargetMap;
    //Attributes
    private final AttributeModel attributeModel;
    //Management
    private boolean dynamicGraph = false;
    private boolean hierarchicalGraph = false;
    private Report report;
    //Counting
    private int directedEdgesCount = 0;
    private int undirectedEdgesCount = 0;
    //Dynamic
    private TimeFormat timeFormat = TimeFormat.DOUBLE;
    private Double timeIntervalMin;
    private Double timeIntervalMax;

    public ImportContainerImpl() {
        parameters = new ImportContainerParameters();
        nodeMap = new LinkedHashMap<String, NodeDraftImpl>();//to maintain the order
        nodeLabelMap = new HashMap<String, NodeDraftImpl>();
        edgeMap = new LinkedHashMap<String, EdgeDraftImpl>();
        edgeSourceTargetMap = new HashMap<String, EdgeDraftImpl>();
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).newModel();
        factory = new FactoryImpl();
    }

    public ContainerLoader getLoader() {
        return this;
    }

    public synchronized ContainerUnloader getUnloader() {
        return this;
    }

    public DraftFactory factory() {
        return factory;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void addNode(NodeDraft nodeDraft) {
        if (nodeDraft == null) {
            throw new NullPointerException();
        }
        NodeDraftImpl nodeDraftImpl = (NodeDraftImpl) nodeDraft;

        if (nodeMap.containsKey(nodeDraftImpl.getId())) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId());
            report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        if (parameters.isDuplicateWithLabels()
                && nodeDraftImpl.getLabel() != null
                && !nodeDraftImpl.getLabel().equals(nodeDraftImpl.getId())
                && nodeLabelMap.containsKey(nodeDraftImpl.getLabel())) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId());
            report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        nodeMap.put(nodeDraftImpl.getId(), nodeDraftImpl);
        if (nodeDraftImpl.getLabel() != null && !nodeDraftImpl.getLabel().equals(nodeDraftImpl.getId())) {
            nodeLabelMap.put(nodeDraftImpl.getLabel(), nodeDraftImpl);
        }
    }

    public NodeDraftImpl getNode(String id) {
        if (id == null || id.isEmpty()) {
            throw new NullPointerException();
        }
        NodeDraftImpl node = nodeMap.get(id);
        if (node == null) {
            if (parameters.isAutoNode()) {
                //Creates the missing node
                node = factory.newNodeDraft();
                node.setId(id);
                addNode(node);
                node.setCreatedAuto(true);
                report.logIssue(new Issue("Unknown node id, creates node from id='" + id + "'", Level.INFO));
            } else {
                String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_UnknowNodeId", id);
                report.logIssue(new Issue(message, Level.SEVERE));
            }
        }
        return node;
    }

    public boolean nodeExists(String id) {
        if (id == null || id.isEmpty()) {
            throw new NullPointerException();
        }
        return nodeMap.containsKey(id);
    }

    public void addEdge(EdgeDraft edgeDraft) {
        if (edgeDraft == null) {
            throw new NullPointerException();
        }
        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        if (edgeDraftImpl.getSource() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }
        if (edgeDraftImpl.getTarget() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }

        //Self loop
        if (edgeDraftImpl.getSource() == edgeDraftImpl.getTarget() && !parameters.isSelfLoops()) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_SelfLoop");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }

        if (edgeDraftImpl.getType() != null) {
            //Counting
            switch (edgeDraftImpl.getType()) {
                case DIRECTED:
                    directedEdgesCount++;
                    break;
                case UNDIRECTED:
                    undirectedEdgesCount++;
                    break;
                case MUTUAL:
                    directedEdgesCount += 2;
                    break;
            }

            //Test if the given type match with parameters
            switch (parameters.getEdgeDefault()) {
                case DIRECTED:
                    EdgeDraft.EdgeType type1 = edgeDraftImpl.getType();
                    if (type1.equals(EdgeDraft.EdgeType.UNDIRECTED)) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type"), Level.WARNING));
                    }
                    break;
                case UNDIRECTED:
                    EdgeDraft.EdgeType type2 = edgeDraftImpl.getType();
                    if (type2.equals(EdgeDraft.EdgeType.DIRECTED)) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type"), Level.WARNING));
                    }
                    break;
                case MIXED:
                    break;
            }
        }


        String id = edgeDraftImpl.getId();
        String sourceTargetId = edgeDraftImpl.getSource().getId() + "-" + edgeDraftImpl.getTarget().getId();
        if (edgeMap.containsKey(id) || edgeSourceTargetMap.containsKey(sourceTargetId)) {
            if (!parameters.isParallelEdges()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist"), Level.WARNING));
                return;
            } else {
                EdgeDraftImpl existingEdge = edgeMap.get(id);
                if (existingEdge == null) {
                    existingEdge = edgeSourceTargetMap.get(sourceTargetId);
                }

                //Manage parallel edges
                if (parameters.isMergeParallelEdgesWeight()) {
                    existingEdge.setWeight(existingEdge.getWeight() + edgeDraftImpl.getWeight());
                }
                if (parameters.isMergeParallelEdgesAttributes()) {
                    mergeAttributes(existingEdge.getAttributeRow(), edgeDraftImpl.getAttributeRow());
                }

                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Parallel_Edge", id), Level.INFO));
                return;
            }
        }

        edgeSourceTargetMap.put(sourceTargetId, edgeDraftImpl);
        edgeMap.put(id, edgeDraftImpl);

        //Mutual
        if (edgeDraftImpl.getType() != null && edgeDraftImpl.getType().equals(EdgeDraft.EdgeType.MUTUAL)) {
            id = edgeDraftImpl.getId() + "-mutual";
            sourceTargetId = edgeDraftImpl.getTarget().getId() + "-" + edgeDraftImpl.getSource().getId();
            if (edgeSourceTargetMap.containsKey(sourceTargetId)) {
                if (!parameters.isParallelEdges()) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist"), Level.WARNING));
                    return;
                } else {
                    EdgeDraftImpl existingEdge = edgeSourceTargetMap.get(sourceTargetId);
                    //Manage parallel edges
                    if (parameters.isMergeParallelEdgesWeight()) {
                        existingEdge.setWeight(existingEdge.getWeight() + edgeDraftImpl.getWeight());
                    }
                    if (parameters.isMergeParallelEdgesAttributes()) {
                        mergeAttributes(existingEdge.getAttributeRow(), edgeDraftImpl.getAttributeRow());
                    }
                    report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Parallel_Edge", id), Level.INFO));
                    return;
                }
            }

            edgeSourceTargetMap.put(sourceTargetId, edgeDraftImpl);
            edgeMap.put(id, edgeDraftImpl);
        }
    }

    private void mergeAttributes(AttributeRow srcRow, AttributeRow dstRow) {
        for (AttributeValue v : srcRow.getValues()) {
            AttributeColumn col = v.getColumn();
            if (!col.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                Object existingVal = dstRow.getValue(col.getIndex());
                if (v.getValue() != null && existingVal == null) {
                    dstRow.setValue(col.getIndex(), existingVal);
                }
            }
        }
    }

    public void removeEdge(EdgeDraft edgeDraft) {
        if (edgeDraft == null) {
            throw new NullPointerException();
        }
        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        String id = edgeDraftImpl.getId();
        String sourceTargetId = edgeDraftImpl.getSource().getId() + "-" + edgeDraftImpl.getTarget().getId();

        if (!edgeMap.containsKey(id) && !edgeSourceTargetMap.containsKey(sourceTargetId)) {
            return;
        }

        if (edgeDraftImpl.getType() != null) {
            //UnCounting
            switch (edgeDraftImpl.getType()) {
                case DIRECTED:
                    directedEdgesCount--;
                    break;
                case UNDIRECTED:
                    undirectedEdgesCount--;
                    break;
                case MUTUAL:
                    directedEdgesCount -= 2;
                    break;
            }
        }

        edgeSourceTargetMap.remove(sourceTargetId);
        edgeMap.remove(id);

        if (edgeDraftImpl.getType() != null && edgeDraftImpl.getType().equals(EdgeDraft.EdgeType.MUTUAL)) {
            id = edgeDraftImpl.getId() + "-mutual";
            sourceTargetId = edgeDraftImpl.getTarget().getId() + "-" + edgeDraftImpl.getSource().getId();
            edgeSourceTargetMap.remove(sourceTargetId);
            edgeMap.remove(id);
        }
    }

    public boolean edgeExists(String id) {
        if (id == null || id.isEmpty()) {
            throw new NullPointerException();
        }
        return edgeMap.containsKey(id);
    }

    public boolean edgeExists(NodeDraft source, NodeDraft target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        return edgeSourceTargetMap.containsKey(((NodeDraftImpl) source).getId() + "-" + ((NodeDraftImpl) target).getId());
    }

    public EdgeDraft getEdge(String id) {
        if (id == null || id.isEmpty()) {
            throw new NullPointerException();
        }
        return edgeMap.get(id);
    }

    public EdgeDraft getEdge(NodeDraft source, NodeDraft target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        return edgeSourceTargetMap.get(((NodeDraftImpl) source).getId() + "-" + ((NodeDraftImpl) target).getId());
    }

    public EdgeDraftGetter getEdge(NodeDraftGetter source, NodeDraftGetter target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        return edgeSourceTargetMap.get(((NodeDraftImpl) source).getId() + "-" + ((NodeDraftImpl) target).getId());
    }

    public Collection<? extends NodeDraftGetter> getNodes() {
        return nodeMap.values();
    }

    public Collection<? extends EdgeDraftGetter> getEdges() {
        return edgeMap.values();
    }

    public AttributeModel getAttributeModel() {
        return attributeModel;
    }

    public AttributeValueFactory getFactory() {
        return attributeModel.valueFactory();
    }

    public Double getTimeIntervalMin() {
        return timeIntervalMin;
    }

    public Double getTimeIntervalMax() {
        return timeIntervalMax;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeIntervalMax(String timeIntervalMax) {
        try {
            if (timeFormat.equals(TimeFormat.DATE)) {
                this.timeIntervalMax = DynamicUtilities.getDoubleFromXMLDateString(timeIntervalMax);
            } else if (timeFormat.equals(TimeFormat.DATETIME)) {
                this.timeIntervalMax = DynamicUtilities.getDoubleFromXMLDateTimeString(timeIntervalMax);
            } else if (timeFormat.equals(TimeFormat.TIMESTAMP)) {
                this.timeIntervalMax = Double.parseDouble(timeIntervalMax + "000");
            } else {
                this.timeIntervalMax = Double.parseDouble(timeIntervalMax);
            }
        } catch (Exception ex) {
            report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_TimeInterval_ParseError", timeIntervalMax), Level.SEVERE));
        }
    }

    public void setTimeIntervalMin(String timeIntervalMin) {
        try {
            if (timeFormat.equals(TimeFormat.DATE)) {
                this.timeIntervalMin = DynamicUtilities.getDoubleFromXMLDateString(timeIntervalMin);
            } else if (timeFormat.equals(TimeFormat.DATETIME)) {
                this.timeIntervalMin = DynamicUtilities.getDoubleFromXMLDateTimeString(timeIntervalMin);
            } else if (timeFormat.equals(TimeFormat.TIMESTAMP)) {
                this.timeIntervalMin = Double.parseDouble(timeIntervalMin + "000");
            } else {
                this.timeIntervalMin = Double.parseDouble(timeIntervalMin);
            }
        } catch (Exception ex) {
            report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_TimeInterval_ParseError", timeIntervalMin), Level.SEVERE));
        }
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public boolean verify() {
        //Edge weight 0
        for (EdgeDraftImpl edge : edgeMap.values().toArray(new EdgeDraftImpl[0])) {
            if (edge.getWeight() <= 0f) {
                if (parameters.isRemoveEdgeWithWeightZero()) {
                    String id = edge.getId();
                    String sourceTargetId = edge.getSource().getId() + "-" + edge.getTarget().getId();
                    edgeMap.remove(id);
                    edgeSourceTargetMap.remove(sourceTargetId);
                    report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Weight_Zero_Ignored", id), Level.SEVERE));
                }
            }
        }

        //Graph EdgeDefault
        if (directedEdgesCount > 0 && undirectedEdgesCount == 0) {
            parameters.setEdgeDefault(EdgeDefault.DIRECTED);
        } else if (directedEdgesCount == 0 && undirectedEdgesCount > 0) {
            parameters.setEdgeDefault(EdgeDefault.UNDIRECTED);
        } else if (directedEdgesCount > 0 && undirectedEdgesCount > 0) {
            parameters.setEdgeDefault(EdgeDefault.MIXED);
        }

        //Is dynamic graph
        for (NodeDraftImpl node : nodeMap.values()) {
            dynamicGraph = node.getTimeInterval() != null;
            if (dynamicGraph) {
                break;
            }
        }
        if (!dynamicGraph) {
            for (EdgeDraftImpl edge : edgeMap.values()) {
                dynamicGraph = edge.getTimeInterval() != null;
                if (dynamicGraph) {
                    break;
                }
            }
        }
        if (!dynamicGraph) {
            for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
                dynamicGraph = col.getType().isDynamicType();
                if (dynamicGraph) {
                    break;
                }
            }
            for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
                dynamicGraph = dynamicGraph || col.getType().isDynamicType();
                if (dynamicGraph) {
                    break;
                }
            }
        }

        //Print time interval values to report
        if (timeIntervalMin != null || timeIntervalMax != null) {
            if (timeFormat.equals(TimeFormat.DATE) || timeFormat.equals(TimeFormat.DATETIME)) {
                try {
                    String message = "[" + (timeIntervalMin != null ? DynamicUtilities.getXMLDateStringFromDouble(timeIntervalMin) : "-inf") + ",";
                    message += (timeIntervalMax != null ? DynamicUtilities.getXMLDateStringFromDouble(timeIntervalMax) : "+inf") + "]";
                    report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeInterval", message));
                } catch (Exception e) {
                }
            } else {
                String message = "[" + (timeIntervalMin != null ? timeIntervalMin.toString() : "-inf") + ",";
                message += (timeIntervalMax != null ? timeIntervalMax.toString() : "+inf") + "]";
                report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeInterval", message));
            }
        }

        //Print TimeFormat
        if (dynamicGraph) {
            report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeFormat", timeFormat.toString()), Level.INFO));
        }

        //Remove overlapping
        if (dynamicGraph && parameters.isRemoveIntervalsOverlapping()) {
            for (NodeDraftImpl node : nodeMap.values()) {
                AttributeValue[] values = node.getAttributeRow().getValues();
                for (int i = 0; i < values.length; i++) {
                    AttributeValue val = values[i];
                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
                        DynamicType type = (DynamicType) val.getValue();
                        type = DynamicUtilities.removeOverlapping(type);
                        node.getAttributeRow().setValue(val.getColumn(), type);
                    }
                }
            }
            for (EdgeDraftImpl edge : edgeMap.values()) {
                AttributeValue[] values = edge.getAttributeRow().getValues();
                for (int i = 0; i < values.length; i++) {
                    AttributeValue val = values[i];
                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
                        DynamicType type = (DynamicType) val.getValue();
                        type = DynamicUtilities.removeOverlapping(type);
                        edge.getAttributeRow().setValue(val.getColumn(), type);
                    }
                }
            }
        }

        //Dynamic attributes bounds
        if (dynamicGraph && (timeIntervalMin != null || timeIntervalMax != null)) {
            for (NodeDraftImpl node : nodeMap.values()) {
                boolean issue = false;
                if (timeIntervalMin != null || timeIntervalMax != null) {
                    if (timeIntervalMin != null && node.getTimeInterval() != null && node.getTimeInterval().getLow() < timeIntervalMin) {
                        node.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(node.getTimeInterval(), timeIntervalMin, node.getTimeInterval().getHigh()));
                        issue = true;
                    }
                    if (timeIntervalMax != null && node.getTimeInterval() != null && node.getTimeInterval().getHigh() > timeIntervalMax) {
                        node.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(node.getTimeInterval(), node.getTimeInterval().getLow(), timeIntervalMax));
                        issue = true;
                    }
                    if (node.getTimeInterval() == null) {
                        node.setTimeInterval(new TimeInterval(timeIntervalMin, timeIntervalMax));
                    }
                }

                AttributeValue[] values = node.getAttributeRow().getValues();
                for (int i = 0; i < values.length; i++) {
                    AttributeValue val = values[i];
                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
                        DynamicType type = (DynamicType) val.getValue();
                        if (timeIntervalMin != null && type.getLow() < timeIntervalMin) {
                            if (!Double.isInfinite(type.getLow())) {
                                issue = true;
                            }
                            node.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, timeIntervalMin, type.getHigh()));
                        }
                        if (timeIntervalMax != null && type.getHigh() > timeIntervalMax) {
                            if (!Double.isInfinite(type.getHigh())) {
                                issue = true;
                            }
                            node.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, type.getLow(), timeIntervalMax));
                        }
                    }
                }
            }
            for (EdgeDraftImpl edge : edgeMap.values()) {
                boolean issue = false;
                if (timeIntervalMin != null || timeIntervalMax != null) {
                    if (timeIntervalMin != null && edge.getTimeInterval() != null && edge.getTimeInterval().getLow() < timeIntervalMin) {
                        edge.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(edge.getTimeInterval(), timeIntervalMin, edge.getTimeInterval().getHigh()));
                        issue = true;
                    }
                    if (timeIntervalMax != null && edge.getTimeInterval() != null && edge.getTimeInterval().getHigh() > timeIntervalMax) {
                        edge.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(edge.getTimeInterval(), edge.getTimeInterval().getLow(), timeIntervalMax));
                        issue = true;
                    }
                    if (edge.getTimeInterval() == null) {
                        edge.setTimeInterval(new TimeInterval(timeIntervalMin, timeIntervalMax));
                    }
                }

                AttributeValue[] values = edge.getAttributeRow().getValues();
                for (int i = 0; i < values.length; i++) {
                    AttributeValue val = values[i];
                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
                        DynamicType type = (DynamicType) val.getValue();
                        if (timeIntervalMin != null && type.getLow() < timeIntervalMin) {
                            if (!Double.isInfinite(type.getLow())) {
                                issue = true;
                            }
                            edge.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, timeIntervalMin, type.getHigh()));
                        }
                        if (timeIntervalMax != null && type.getHigh() > timeIntervalMax) {
                            if (!Double.isInfinite(type.getHigh())) {
                                issue = true;
                            }
                            edge.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, type.getLow(), timeIntervalMax));
                        }
                    }
                }
            }
        }

        return true;
    }

    public void closeLoader() {
        //Clean undirected edges
        if (parameters.getEdgeDefault().equals(EdgeDefault.UNDIRECTED)) {
            for (Iterator<EdgeDraftImpl> itr = edgeMap.values().iterator(); itr.hasNext();) {
                EdgeDraftImpl edge = itr.next();
                String oppositekey = edge.getTarget().getId() + "-" + edge.getSource().getId();
                EdgeDraftImpl opposite = edgeSourceTargetMap.get(oppositekey);
                if (opposite != null) {
                    if (parameters.isUndirectedSumDirectedEdgesWeight()) {
                        opposite.setWeight(edge.getWeight() + opposite.getWeight());
                    } else {
                        opposite.setWeight(Math.max(edge.getWeight(), opposite.getWeight()));
                    }
                    itr.remove();
                    edgeSourceTargetMap.remove(edge.getSource().getId() + "-" + edge.getTarget().getId());
                }
            }
        } else if (parameters.getEdgeDefault().equals(EdgeDefault.MIXED)) {
            //Clean undirected edges when graph is mixed
            for (EdgeDraftImpl edge : edgeMap.values().toArray(new EdgeDraftImpl[0])) {
                if (edge.getType() == null) {
                    edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
                }
                if (edge.getType().equals(EdgeDraft.EdgeType.UNDIRECTED)) {
                    String myKey = edge.getSource().getId() + "-" + edge.getTarget().getId();
                    String oppositekey = edge.getTarget().getId() + "-" + edge.getSource().getId();
                    EdgeDraftImpl opposite = edgeSourceTargetMap.get(oppositekey);
                    if (opposite != null) {
                        if (parameters.isUndirectedSumDirectedEdgesWeight()) {
                            edge.setWeight(edge.getWeight() + opposite.getWeight());
                        } else {
                            edge.setWeight(Math.max(edge.getWeight(), opposite.getWeight()));
                        }
                        edgeMap.remove(edge.getId());
                        edgeSourceTargetMap.remove(myKey);
                    }
                }
            }
        }

        //Clean autoNode
        if (!allowAutoNode()) {
            for (NodeDraftImpl nodeDraftImpl : nodeMap.values().toArray(new NodeDraftImpl[0])) {
                if (nodeDraftImpl.isCreatedAuto()) {
                    nodeMap.remove(nodeDraftImpl.getId());
                    for (Iterator<EdgeDraftImpl> itr = edgeMap.values().iterator(); itr.hasNext();) {
                        EdgeDraftImpl edge = itr.next();
                        if (edge.getSource() == nodeDraftImpl || edge.getTarget() == nodeDraftImpl) {
                            itr.remove();
                        }
                    }
                }
            }
        }

        //Sort nodes by height
        LinkedHashMap<String, NodeDraftImpl> sortedNodeMap = new LinkedHashMap<String, NodeDraftImpl>();
        ArrayList<NodeDraftImpl> sortedMapValues = new ArrayList<NodeDraftImpl>(nodeMap.values());
        Collections.sort(sortedMapValues, new Comparator<NodeDraftImpl>() {
            public int compare(NodeDraftImpl o1, NodeDraftImpl o2) {
                return new Integer(o2.getHeight()).compareTo(o1.getHeight());
            }
        });
        for (NodeDraftImpl n : sortedMapValues) {
            sortedNodeMap.put(n.getId(), n);
        }
        nodeMap = sortedNodeMap;

        //Set id as label for nodes that miss label
        for (NodeDraftImpl node : nodeMap.values()) {
            if (node.getLabel() == null) {
                node.setLabel(node.getId());
            }
        }

        //Set random position
        boolean customPosition = false;
        for (NodeDraftImpl node : nodeMap.values()) {
            if (Float.isNaN(node.getX())) {
                node.setX(0);
            }
            if (Float.isNaN(node.getY())) {
                node.setY(0);
            }
            if (Float.isNaN(node.getZ())) {
                node.setZ(0);
            }
            if (node.getX() != 0f || node.getY() != 0) {
                customPosition = true;
            }
        }

        if (!customPosition) {
            // keep the original behavior for small graphs
            if (nodeMap.size() < 5000) {
                for (NodeDraftImpl node : nodeMap.values()) {
                    node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                    node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                }
            // layout large graphs in non-overlapping gird
            // because the overlaps kill gl rendering
            } else {
                double side = Math.sqrt(nodeMap.size());
                double nodeDist = 20;
                double x = 0, y = 0;
                int yNodes = 0;
                for (NodeDraftImpl node : nodeMap.values()) {
                    node.setX((float)x);
                    node.setY((float)y);
                    y += nodeDist;
                    yNodes++;
                    if (yNodes > side) {
                        y = 0;
                        x += nodeDist;
                        yNodes = 0;
                    }
                }
            }
        }
    }
    private static int nodeIDgen = 0;
    private static int edgeIDgen = 0;

    /**
     * Factory for draft objects
     */
    public class FactoryImpl implements DraftFactory {

        public NodeDraftImpl newNodeDraft() {
            NodeDraftImpl node = new NodeDraftImpl(ImportContainerImpl.this, "n" + nodeIDgen);
            nodeIDgen++;
            return node;
        }

        public EdgeDraftImpl newEdgeDraft() {
            EdgeDraftImpl edge = new EdgeDraftImpl(ImportContainerImpl.this, "e" + edgeIDgen);
            edgeIDgen++;
            return edge;
        }
    }

    //MANAGEMENT
    public boolean isDynamicGraph() {
        return dynamicGraph;
    }

    public boolean isHierarchicalGraph() {
        return hierarchicalGraph;
    }

    public void setDynamicGraph(boolean dynamicGraph) {
        this.dynamicGraph = dynamicGraph;
    }

    public void setHierarchicalGraph(boolean hierarchicalGraph) {
        this.hierarchicalGraph = hierarchicalGraph;
    }

    //REPORT
    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    //PARAMETERS
    public void setAllowAutoNode(boolean value) {
        parameters.setAutoNode(value);
    }

    public void setAllowParallelEdge(boolean value) {
        parameters.setParallelEdges(value);
    }

    public void setAllowSelfLoop(boolean value) {
        parameters.setSelfLoops(value);
    }

    public void setEdgeDefault(EdgeDefault edgeDefault) {
        parameters.setEdgeDefault(edgeDefault);
        report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Set_EdgeDefault", edgeDefault.toString()), Level.INFO));
    }

    public void setUndirectedSumDirectedEdgesWeight(boolean value) {
        parameters.setUndirectedSumDirectedEdgesWeight(value);
    }

    public boolean allowAutoNode() {
        return parameters.isAutoNode();
    }

    public boolean allowParallelEdges() {
        return parameters.isParallelEdges();
    }

    public boolean allowSelfLoop() {
        return parameters.isSelfLoops();
    }

    public EdgeDefault getEdgeDefault() {
        return parameters.getEdgeDefault();
    }

    public boolean isAutoScale() {
        return parameters.isAutoScale();
    }

    public void setAutoScale(boolean autoscale) {
        parameters.setAutoScale(autoscale);
    }
}
