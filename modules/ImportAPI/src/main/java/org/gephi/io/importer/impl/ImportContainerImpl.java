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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDiretionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Issue.Level;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.api.TimeFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerImpl implements Container, ContainerLoader, ContainerUnloader {

    protected static final int NULL_INDEX = -1;
    //MetaData
    private String source;
    //Factory
    private final ElementFactoryImpl factory;
    //Parameters
    private final ImportContainerParameters parameters;
    //Maps and Data
    private final ObjectList<NodeDraftImpl> nodeList;
    private final ObjectList<EdgeDraftImpl> edgeList;
    private final Object2IntMap<String> nodeMap;
    private final Object2IntMap<String> edgeMap;
    private final Object2IntMap edgeTypeMap;
    private Long2ObjectMap<int[]>[] edgeTypeSets;
    private EdgeDiretionDefault edgeDefault = EdgeDiretionDefault.MIXED;
    private final Object2ObjectMap<String, ColumnDraft> nodeColumns;
    private final Object2ObjectMap<String, ColumnDraft> edgeColumns;
    //Management
    private boolean dynamicGraph = false;
    private Report report;
    //Counting
    private int directedEdgesCount = 0;
    private int undirectedEdgesCount = 0;
    private int selfLoops = 0;
    //Dynamic
    private TimeFormat timeFormat = TimeFormat.DOUBLE;
    private Double timeIntervalMin;
    private Double timeIntervalMax;

    public ImportContainerImpl() {
        parameters = new ImportContainerParameters();
        nodeMap = new Object2IntOpenHashMap<String>();
        edgeMap = new Object2IntOpenHashMap<String>();
        nodeMap.defaultReturnValue(NULL_INDEX);
        edgeMap.defaultReturnValue(NULL_INDEX);
        nodeList = new ObjectArrayList<NodeDraftImpl>();
        edgeList = new ObjectArrayList<EdgeDraftImpl>();
        edgeTypeMap = new Object2IntOpenHashMap();
        edgeTypeSets = new Long2ObjectMap[0];
        factory = new ElementFactoryImpl(this);
        nodeColumns = new Object2ObjectOpenHashMap<String, ColumnDraft>();
        edgeColumns = new Object2ObjectOpenHashMap<String, ColumnDraft>();
    }

    @Override
    public ContainerLoader getLoader() {
        return this;
    }

    @Override
    public synchronized ContainerUnloader getUnloader() {
        return this;
    }

    @Override
    public ElementFactoryImpl factory() {
        return factory;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void addNode(NodeDraft nodeDraft) {
        checkElementDraftImpl(nodeDraft);
        NodeDraftImpl nodeDraftImpl = (NodeDraftImpl) nodeDraft;

        if (nodeMap.containsKey(nodeDraftImpl.getId())) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId());
            report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        int index = nodeList.size();
        nodeList.add(nodeDraftImpl);
        nodeMap.put(nodeDraftImpl.getId(), index);
    }

    @Override
    public NodeDraftImpl getNode(String id) {
        checkId(id);

        int index = nodeMap.get(id);
        NodeDraftImpl node = null;
        if (index == NULL_INDEX) {
            if (parameters.isAutoNode()) {
                //Creates the missing node
                node = factory.newNodeDraft(id);
                addNode(node);
                node.setCreatedAuto(true);
                report.logIssue(new Issue("Unknown node id, creates node from id='" + id + "'", Level.INFO));
            } else {
                String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_UnknowNodeId", id);
                report.logIssue(new Issue(message, Level.SEVERE));
            }
        } else {
            node = nodeList.get(index);
        }
        return node;
    }

    @Override
    public boolean nodeExists(String id) {
        checkId(id);
        return nodeMap.containsKey(id);
    }

    @Override
    public void addEdge(EdgeDraft edgeDraft) {
        checkElementDraftImpl(edgeDraft);

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

        //Check if already exists
        if (edgeMap.containsKey(edgeDraftImpl.getId())) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist", edgeDraftImpl.getId());
            report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        //Self loop
        if (edgeDraftImpl.isSelfLoop() && !parameters.isSelfLoops()) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_SelfLoop");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }

        //Check direction and defaut type
        if (edgeDraftImpl.getDirection() != null) {
            //Test if the given type match with parameters
            switch (edgeDefault) {
                case DIRECTED:
                    if (edgeDraftImpl.getDirection().equals(EdgeDirection.UNDIRECTED)) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type", edgeDefault, edgeDraftImpl.getId()), Level.SEVERE));
                        return;
                    }
                    break;
                case UNDIRECTED:
                    if (edgeDraftImpl.getDirection().equals(EdgeDirection.DIRECTED)) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type", edgeDefault, edgeDraftImpl.getId()), Level.SEVERE));
                        return;
                    }
                    break;
            }
        }

        //Get index
        int index = edgeList.size();

        //Type
        int edgeType = getEdgeType(edgeDraftImpl.getType());
        long sourceTargetLong = getLongId(edgeDraftImpl);
        ensureLongSetArraySize(edgeType);
        Long2ObjectMap<int[]> edgeTypeSet = edgeTypeSets[edgeType];

        if (edgeTypeSet.containsKey(sourceTargetLong)) {
            if (!parameters.isParallelEdges()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Parallel_Edge_Forbidden", edgeDraftImpl.getId()), Level.SEVERE));
                return;
            } else {
                int[] edges = edgeTypeSet.get(sourceTargetLong);
                int[] newEdges = new int[edges.length + 1];
                System.arraycopy(edges, 0, newEdges, 0, edges.length);
                edgeTypeSet.put(sourceTargetLong, newEdges);

                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Parallel_Edge", edgeDraftImpl.getId()), Level.INFO));
            }
        } else {
            edgeTypeSet.put(sourceTargetLong, new int[]{index});
        }

        //Self loop
        if (edgeDraftImpl.isSelfLoop()) {
            selfLoops++;
        }

        //Direction
        EdgeDirection direction = edgeDraftImpl.getDirection();
        if (direction != null) {
            //Counting
            switch (direction) {
                case DIRECTED:
                    directedEdgesCount++;
                    break;
                case UNDIRECTED:
                    undirectedEdgesCount++;
                    break;
            }
        }

        //Adding
        edgeList.add(edgeDraftImpl);
        edgeMap.put(edgeDraft.getId(), index);
    }

    @Override
    public void removeEdge(EdgeDraft edgeDraft) {
        checkElementDraftImpl(edgeDraft);

        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        String id = edgeDraftImpl.getId();

        if (!edgeMap.containsKey(id)) {
            return;
        }

        if (edgeDraftImpl.getDirection() != null) {
            //UnCounting
            switch (edgeDraftImpl.getDirection()) {
                case DIRECTED:
                    directedEdgesCount--;
                    break;
                case UNDIRECTED:
                    undirectedEdgesCount--;
                    break;
            }
        }

        if (edgeDraftImpl.isSelfLoop()) {
            selfLoops--;
        }

        int edgeType = getEdgeType(edgeDraftImpl.getType());
        long sourceTargetLong = getLongId(edgeDraftImpl);
        ensureLongSetArraySize(edgeType);
        Long2ObjectMap<int[]> edgeTypeSet = edgeTypeSets[edgeType];

        //Get index
        int index = edgeMap.remove(id);

        //Update edgeType set
        int[] edges = edgeTypeSet.remove(sourceTargetLong);
        if (edges.length > 1) {
            int[] newEdges = new int[edges.length - 1];
            int i = 0;
            for (int e : edges) {
                if (e != index) {
                    newEdges[i++] = e;
                }
            }
            edgeTypeSet.put(sourceTargetLong, newEdges);
        }

        //Remove edge
        edgeList.set(index, null);
    }

    @Override
    public boolean edgeExists(String id) {
        checkId(id);

        return edgeMap.containsKey(id);
    }

    @Override
    public EdgeDraft getEdge(String id) {
        checkId(id);

        int index = edgeMap.get(id);
        if (index == NULL_INDEX) {
            return null;
        }
        return edgeList.get(index);
    }

    @Override
    public Iterable<NodeDraft> getNodes() {
        return new NullFilterIterable<NodeDraft>(nodeList);
    }

    @Override
    public int getNodeCount() {
        return nodeMap.size();
    }

    @Override
    public Iterable<EdgeDraft> getEdges() {
        return new NullFilterIterable<EdgeDraft>(edgeList);
    }

    @Override
    public int getEdgeCount() {
        return edgeMap.size();
    }

    @Override
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

//    public void setTimeIntervalMax(String timeIntervalMax) {
//        try {
//            if (timeFormat.equals(TimeFormat.DATE)) {
//                this.timeIntervalMax = DynamicUtilities.getDoubleFromXMLDateString(timeIntervalMax);
//            } else if (timeFormat.equals(TimeFormat.DATETIME)) {
//                this.timeIntervalMax = DynamicUtilities.getDoubleFromXMLDateTimeString(timeIntervalMax);
//            } else if (timeFormat.equals(TimeFormat.TIMESTAMP)) {
//                this.timeIntervalMax = Double.parseDouble(timeIntervalMax + "000");
//            } else {
//                this.timeIntervalMax = Double.parseDouble(timeIntervalMax);
//            }
//        } catch (Exception ex) {
//            report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_TimeInterval_ParseError", timeIntervalMax), Level.SEVERE));
//        }
//    }
//
//    public void setTimeIntervalMin(String timeIntervalMin) {
//        try {
//            if (timeFormat.equals(TimeFormat.DATE)) {
//                this.timeIntervalMin = DynamicUtilities.getDoubleFromXMLDateString(timeIntervalMin);
//            } else if (timeFormat.equals(TimeFormat.DATETIME)) {
//                this.timeIntervalMin = DynamicUtilities.getDoubleFromXMLDateTimeString(timeIntervalMin);
//            } else if (timeFormat.equals(TimeFormat.TIMESTAMP)) {
//                this.timeIntervalMin = Double.parseDouble(timeIntervalMin + "000");
//            } else {
//                this.timeIntervalMin = Double.parseDouble(timeIntervalMin);
//            }
//        } catch (Exception ex) {
//            report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_TimeInterval_ParseError", timeIntervalMin), Level.SEVERE));
//        }
//    }
    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    public ColumnDraft addNodeColumn(String key, Class typeClass) {
        ColumnDraft column = nodeColumns.get(key);
        if (column == null) {
            int index = nodeColumns.size();
            column = new ColumnDraftImpl(key, index, typeClass);
            nodeColumns.put(key, column);
        } else {
            if (!column.getTypeClass().equals(typeClass)) {
                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Column_Type_Mismatch", key, column.getTypeClass()), Level.SEVERE));
            }
        }
        return column;
    }

    @Override
    public ColumnDraft addEdgeColumn(String key, Class typeClass) {
        ColumnDraft column = edgeColumns.get(key);
        if (column == null) {
            int index = edgeColumns.size();
            column = new ColumnDraftImpl(key, index, typeClass);
            edgeColumns.put(key, column);
        } else {
            if (!column.getTypeClass().equals(typeClass)) {
                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Column_Type_Mismatch", key, column.getTypeClass()), Level.SEVERE));
            }
        }
        return column;
    }

    @Override
    public ColumnDraft getNodeColumn(String key) {
        return nodeColumns.get(key);
    }

    @Override
    public boolean hasNodeColumn(String key) {
        return nodeColumns.containsKey(key);
    }

    @Override
    public ColumnDraft getEdgeColumn(String key) {
        return edgeColumns.get(key);
    }

    @Override
    public boolean hasEdgeColumn(String key) {
        return edgeColumns.containsKey(key);
    }

    @Override
    public Iterable<ColumnDraft> getNodeColumns() {
        return nodeColumns.values();
    }

    @Override
    public Iterable<ColumnDraft> getEdgeColumns() {
        return edgeColumns.values();
    }

    @Override
    public boolean verify() {
        //Edge weight zero or negative
        for (EdgeDraftImpl edge : new NullFilterIterable<EdgeDraftImpl>(edgeList)) {
            String id = edge.getId();
            if (edge.getWeight() < 0f) {
                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Negative_Weight", id), Level.WARNING));
            } else if (edge.getWeight() == 0) {
                removeEdge(edge);
                report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Weight_Zero_Ignored", id), Level.SEVERE));
            }
        }

        //Graph EdgeDefault
        if (directedEdgesCount > 0 && undirectedEdgesCount == 0) {
            setEdgeDefault(EdgeDiretionDefault.DIRECTED);
        } else if (directedEdgesCount == 0 && undirectedEdgesCount > 0) {
            setEdgeDefault(EdgeDiretionDefault.UNDIRECTED);
        } else if (directedEdgesCount > 0 && undirectedEdgesCount > 0) {
            setEdgeDefault(EdgeDiretionDefault.MIXED);
        }

        //Is dynamic graph
//        for (NodeDraftImpl node : nodeMap.values()) {
//            dynamicGraph = node.getTimeInterval() != null;
//            if (dynamicGraph) {
//                break;
//            }
//        }
//        if (!dynamicGraph) {
//            for (EdgeDraftImpl edge : edgeMap.values()) {
//                dynamicGraph = edge.getTimeInterval() != null;
//                if (dynamicGraph) {
//                    break;
//                }
//            }
//        }
//        if (!dynamicGraph) {
//            for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
//                dynamicGraph = col.getType().isDynamicType();
//                if (dynamicGraph) {
//                    break;
//                }
//            }
//            for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
//                dynamicGraph = dynamicGraph || col.getType().isDynamicType();
//                if (dynamicGraph) {
//                    break;
//                }
//            }
//        }

        //Print time interval values to report
//        if (timeIntervalMin != null || timeIntervalMax != null) {
//            if (timeFormat.equals(TimeFormat.DATE) || timeFormat.equals(TimeFormat.DATETIME)) {
//                try {
//                    String message = "[" + (timeIntervalMin != null ? DynamicUtilities.getXMLDateStringFromDouble(timeIntervalMin) : "-inf") + ",";
//                    message += (timeIntervalMax != null ? DynamicUtilities.getXMLDateStringFromDouble(timeIntervalMax) : "+inf") + "]";
//                    report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeInterval", message));
//                } catch (Exception e) {
//                }
//            } else {
//                String message = "[" + (timeIntervalMin != null ? timeIntervalMin.toString() : "-inf") + ",";
//                message += (timeIntervalMax != null ? timeIntervalMax.toString() : "+inf") + "]";
//                report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeInterval", message));
//            }
//        }
//
//        //Print TimeFormat
//        if (dynamicGraph) {
//            report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeFormat", timeFormat.toString()), Level.INFO));
//        }

        //Remove overlapping
//        if (dynamicGraph && parameters.isRemoveIntervalsOverlapping()) {
//            for (NodeDraftImpl node : nodeMap.values()) {
//                AttributeValue[] values = node.getAttributeRow().getValues();
//                for (int i = 0; i < values.length; i++) {
//                    AttributeValue val = values[i];
//                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
//                        DynamicType type = (DynamicType) val.getValue();
//                        type = DynamicUtilities.removeOverlapping(type);
//                        node.getAttributeRow().setValue(val.getColumn(), type);
//                    }
//                }
//            }
//            for (EdgeDraftImpl edge : edgeMap.values()) {
//                AttributeValue[] values = edge.getAttributeRow().getValues();
//                for (int i = 0; i < values.length; i++) {
//                    AttributeValue val = values[i];
//                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
//                        DynamicType type = (DynamicType) val.getValue();
//                        type = DynamicUtilities.removeOverlapping(type);
//                        edge.getAttributeRow().setValue(val.getColumn(), type);
//                    }
//                }
//            }
//        }

        //Dynamic attributes bounds
//        if (dynamicGraph && (timeIntervalMin != null || timeIntervalMax != null)) {
//            for (NodeDraftImpl node : nodeMap.values()) {
//                boolean issue = false;
//                if (timeIntervalMin != null || timeIntervalMax != null) {
//                    if (timeIntervalMin != null && node.getTimeInterval() != null && node.getTimeInterval().getLow() < timeIntervalMin) {
//                        node.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(node.getTimeInterval(), timeIntervalMin, node.getTimeInterval().getHigh()));
//                        issue = true;
//                    }
//                    if (timeIntervalMax != null && node.getTimeInterval() != null && node.getTimeInterval().getHigh() > timeIntervalMax) {
//                        node.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(node.getTimeInterval(), node.getTimeInterval().getLow(), timeIntervalMax));
//                        issue = true;
//                    }
//                    if (node.getTimeInterval() == null) {
//                        node.setTimeInterval(new TimeInterval(timeIntervalMin, timeIntervalMax));
//                    }
//                }
//
//                AttributeValue[] values = node.getAttributeRow().getValues();
//                for (int i = 0; i < values.length; i++) {
//                    AttributeValue val = values[i];
//                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
//                        DynamicType type = (DynamicType) val.getValue();
//                        if (timeIntervalMin != null && type.getLow() < timeIntervalMin) {
//                            if (!Double.isInfinite(type.getLow())) {
//                                issue = true;
//                            }
//                            node.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, timeIntervalMin, type.getHigh()));
//                        }
//                        if (timeIntervalMax != null && type.getHigh() > timeIntervalMax) {
//                            if (!Double.isInfinite(type.getHigh())) {
//                                issue = true;
//                            }
//                            node.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, type.getLow(), timeIntervalMax));
//                        }
//                    }
//                }
//            }
//            for (EdgeDraftImpl edge : edgeMap.values()) {
//                boolean issue = false;
//                if (timeIntervalMin != null || timeIntervalMax != null) {
//                    if (timeIntervalMin != null && edge.getTimeInterval() != null && edge.getTimeInterval().getLow() < timeIntervalMin) {
//                        edge.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(edge.getTimeInterval(), timeIntervalMin, edge.getTimeInterval().getHigh()));
//                        issue = true;
//                    }
//                    if (timeIntervalMax != null && edge.getTimeInterval() != null && edge.getTimeInterval().getHigh() > timeIntervalMax) {
//                        edge.setTimeInterval((TimeInterval) DynamicUtilities.fitToInterval(edge.getTimeInterval(), edge.getTimeInterval().getLow(), timeIntervalMax));
//                        issue = true;
//                    }
//                    if (edge.getTimeInterval() == null) {
//                        edge.setTimeInterval(new TimeInterval(timeIntervalMin, timeIntervalMax));
//                    }
//                }
//
//                AttributeValue[] values = edge.getAttributeRow().getValues();
//                for (int i = 0; i < values.length; i++) {
//                    AttributeValue val = values[i];
//                    if (val.getValue() != null && val.getValue() instanceof DynamicType) {   //is Dynamic type
//                        DynamicType type = (DynamicType) val.getValue();
//                        if (timeIntervalMin != null && type.getLow() < timeIntervalMin) {
//                            if (!Double.isInfinite(type.getLow())) {
//                                issue = true;
//                            }
//                            edge.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, timeIntervalMin, type.getHigh()));
//                        }
//                        if (timeIntervalMax != null && type.getHigh() > timeIntervalMax) {
//                            if (!Double.isInfinite(type.getHigh())) {
//                                issue = true;
//                            }
//                            edge.getAttributeRow().setValue(val.getColumn(), DynamicUtilities.fitToInterval(type, type.getLow(), timeIntervalMax));
//                        }
//                    }
//                }
//            }
//        }

        return true;
    }

    @Override
    public void closeLoader() {
        //Merge parallel edges
        if (parameters.isParallelEdges()) {
            for (Long2ObjectMap<int[]> edgesTypeMap : edgeTypeSets) {
                if (edgeTypeMap != null) {
                    for (Long2ObjectMap.Entry<int[]> entry : edgesTypeMap.long2ObjectEntrySet()) {
                        if (entry.getValue().length > 1) {
                            int[] edges = entry.getValue();
                            //Sort and get min
                            Arrays.sort(edges);
                            int minIndex = edges[0];
                            EdgeDraftImpl min = edgeList.get(minIndex);
                            EdgeDraftImpl[] sources = new EdgeDraftImpl[edges.length - 1];
                            for (int i = 1; i < edges.length; i++) {
                                int sourceIndex = edges[i];
                                sources[i - 1] = edgeList.get(sourceIndex);
                                edgeList.set(sourceIndex, null);
                                edgeMap.remove(sources[i - 1].getId());
                            }
                            mergeParallelEdges(sources, min);
                            entry.setValue(new int[]{minIndex});
                        }
                    }
                }
            }
        }

        if (directedEdgesCount > 0 && edgeDefault.equals(EdgeDiretionDefault.UNDIRECTED)) {
            //Force undirected
            for (EdgeDraftImpl edge : edgeList.toArray(new EdgeDraftImpl[0])) {
                if (edge != null && edge.getDirection().equals(EdgeDirection.DIRECTED)) {
                    EdgeDraftImpl opposite = getOpposite(edge);
                    if (opposite != null) {
                        int oppositeIndex = edgeMap.getInt(opposite.getId());
                        mergeDirectedEdges(opposite, edge);

                        edgeMap.removeInt(opposite.getId());
                        edgeList.set(oppositeIndex, null);
                    }
                }
            }
        }
        //TODO check when mixed is forced

        //Clean autoNode
        if (!allowAutoNode()) {
            for (Iterator<NodeDraftImpl> itr = nodeList.iterator(); itr.hasNext();) {
                NodeDraftImpl node = itr.next();
                if (node != null && node.isCreatedAuto()) {
                    int index = nodeMap.remove(node.getId());
                    nodeList.set(index, null);
                }
            }
            for (Iterator<EdgeDraftImpl> itr = edgeList.iterator(); itr.hasNext();) {
                EdgeDraftImpl edge = itr.next();
                if (edge != null && (edge.getSource().isCreatedAuto() || edge.getTarget().isCreatedAuto())) {
                    int index = edgeMap.remove(edge.getId());
                    edgeList.set(index, null);
                }
            }
        }

        //Sort nodes by height
        if (parameters.isSortNodesBySize()) {
            Collections.sort(nodeList, new Comparator<NodeDraftImpl>() {
                @Override
                public int compare(NodeDraftImpl o1, NodeDraftImpl o2) {
                    return new Float(o2 != null ? o2.getSize() : 0f).compareTo(o1 != null ? o1.getSize() : 0f);
                }
            });
        }

        //Set id as label for nodes that miss label
        if (parameters.isFillLabelWithId()) {
            for (NodeDraftImpl node : nodeList) {
                if (node != null && node.getLabel() == null) {
                    node.setLabel(node.getId());
                }
            }
        }


        //Set random position
        boolean customPosition = false;
        for (NodeDraftImpl node : nodeList) {
            if (node != null) {
                if (Float.isNaN(node.getX())) {
                    node.setX(0);
                }
                if (Float.isNaN(node.getY())) {
                    node.setY(0);
                }
                if (Float.isNaN(node.getZ())) {
                    node.setZ(0);
                }
                if (node.getX() != 0f || node.getY() != 0f) {
                    customPosition = true;
                }
            }
        }
        if (!customPosition) {
            for (NodeDraftImpl node : nodeList) {
                if (node != null) {
                    node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                    node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                }
            }
        }

        //MANAGEMENT




    }

    public boolean isDynamicGraph() {
        return dynamicGraph;
    }

    public void setDynamicGraph(boolean dynamicGraph) {
        this.dynamicGraph = dynamicGraph;
    }

    //REPORT
    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public void setReport(Report report) {
        this.report = report;
    }

    //PARAMS GETTER
    @Override
    public boolean allowAutoNode() {
        return parameters.isAutoNode();
    }

    @Override
    public boolean allowParallelEdges() {
        return parameters.isParallelEdges();
    }

    @Override
    public boolean allowSelfLoop() {
        return parameters.isSelfLoops();
    }

    @Override
    public EdgeDiretionDefault getEdgeDefault() {
        return edgeDefault;
    }

    @Override
    public boolean isMultiGraph() {
        return edgeTypeMap.size() > 1;
    }

    @Override
    public boolean hasSelfLoops() {
        return selfLoops > 0;
    }

    //PARAMS
    @Override
    public void setAllowAutoNode(boolean value) {
        parameters.setAutoNode(value);
    }

    @Override
    public void setAllowParallelEdge(boolean value) {
        parameters.setParallelEdges(value);
    }

    @Override
    public void setAllowSelfLoop(boolean value) {
        parameters.setSelfLoops(value);
    }

    @Override
    public void setEdgeDefault(EdgeDiretionDefault edgeDefault) {
        this.edgeDefault = edgeDefault;
        report.logIssue(new Issue(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Set_EdgeDefault", edgeDefault.toString()), Level.INFO));
    }

    @Override
    public boolean isAutoScale() {
        return parameters.isAutoScale();
    }

    @Override
    public void setAutoScale(boolean autoscale) {
        parameters.setAutoScale(autoscale);
    }

    //
    protected void mergeParallelEdges(EdgeDraftImpl[] sources, EdgeDraftImpl dest) {
        ImportContainerParameters.EdgeWeightMergeStrategy mergeStrategy = parameters.getParallelEdgesMergeStrategy();
        int count = 1 + sources.length;
        double sum = dest.getWeight();
        double min = dest.getWeight();
        double max = dest.getWeight();
        for (EdgeDraftImpl edge : sources) {
            sum += edge.getWeight();
            min = Math.min(min, edge.getWeight());
            max = Math.max(max, edge.getWeight());
        }
        double result = dest.getWeight();
        if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.AVG)) {
            result = sum / count;
        } else if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.MAX)) {
            result = max;
        } else if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.MIN)) {
            result = min;
        } else if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.SUM)) {
            result = sum;
        }
        dest.setWeight(result);
    }

    protected void mergeDirectedEdges(EdgeDraftImpl source, EdgeDraftImpl dest) {
        ImportContainerParameters.EdgeWeightMergeStrategy mergeStrategy = parameters.getUndirectedMergeStrategy();
        double result = dest.getWeight();
        if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.AVG)) {
            result = (source.getWeight() + dest.getWeight()) / 2.0;
        } else if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.MAX)) {
            result = Math.max(source.getWeight(), dest.getWeight());
        } else if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.MIN)) {
            result = Math.min(source.getWeight(), dest.getWeight());;
        } else if (mergeStrategy.equals(ImportContainerParameters.EdgeWeightMergeStrategy.SUM)) {
            result = source.getWeight() + dest.getWeight();
        }
        dest.setWeight(result);
    }

    //Utility
    private int getEdgeType(Object type) {
        if (edgeTypeMap.containsKey(type)) {
            return edgeTypeMap.getInt(type);
        }
        int id = edgeTypeMap.size();
        edgeTypeMap.put(type, id);
        return id;
    }

    private void ensureLongSetArraySize(int type) {
        if (edgeTypeSets.length <= type) {
            Long2ObjectMap[] l = new Long2ObjectMap[type + 1];
            System.arraycopy(edgeTypeSets, 0, l, 0, edgeTypeSets.length);
            edgeTypeSets = l;
            edgeTypeSets[type] = new Long2ObjectOpenHashMap<int[]>();
        }
    }

    private long getLongId(EdgeDraftImpl edge) {
        EdgeDirection direction = edge.getDirection();
        boolean directed = edgeDefault.equals(EdgeDiretionDefault.DIRECTED)
                || (!edgeDefault.equals(EdgeDiretionDefault.UNDIRECTED) && direction != null && direction == EdgeDirection.DIRECTED);
        return getLongId(edge.getSource(), edge.getTarget(), directed);
    }

    private long getLongId(NodeDraftImpl source, NodeDraftImpl target, boolean directed) {
        if (directed) {
            long edgeId = ((long) source.hashCode()) << 32;
            edgeId = edgeId | (long) (target.hashCode());
            return edgeId;
        } else {
            long edgeId = ((long) (source.hashCode() > target.hashCode() ? source.hashCode() : target.hashCode())) << 32;
            edgeId = edgeId | (long) (source.hashCode() > target.hashCode() ? target.hashCode() : source.hashCode());
            return edgeId;
        }
    }

    private EdgeDraftImpl getOpposite(EdgeDraftImpl edge) {
        Long2ObjectMap<int[]> typeSet = edgeTypeSets[getEdgeType(edge.getType())];
        long longId = getLongId(edge.getTarget(), edge.getSource(), true);
        int[] opposites = typeSet.get(longId);
        if (opposites != null && opposites.length > 0) {
            return edgeList.get(opposites[0]);
        }
        return null;
    }

    private void checkElementDraftImpl(ElementDraft elmt) {
        if (elmt == null) {
            throw new NullPointerException();
        }
        if (!(elmt instanceof ElementDraftImpl)) {
            throw new ClassCastException();
        }
    }

    private void checkId(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("The id can't be empty");
        }
    }

    //UTILITY ITERATOR
    private static class NullFilterIterable<T extends ElementDraft> implements Iterable<T> {

        private final Collection<T> collection;

        public NullFilterIterable(Collection elementCollection) {
            this.collection = elementCollection;
        }

        @Override
        public Iterator<T> iterator() {
            return new NullFilterIterator<T>(collection);
        }
    }

    private static class NullFilterIterator<T extends ElementDraft> implements Iterator<T> {

        private final Iterator<T> itr;

        public NullFilterIterator(Collection<T> elementCollection) {
            this.itr = elementCollection.iterator();
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public T next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
