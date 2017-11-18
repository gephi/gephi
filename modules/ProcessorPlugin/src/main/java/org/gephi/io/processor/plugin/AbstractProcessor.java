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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.Workspace;
import org.gephi.utils.Attributes;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

public abstract class AbstractProcessor implements Processor {

    protected ProgressTicket progressTicket;
    protected Workspace workspace;
    protected ContainerUnloader[] containers;
    protected GraphModel graphModel;

    private final Set<Column> columnsTypeMismatchAlreadyWarned = new HashSet<>();
    protected Report report = new Report();
    protected Report reportAfterDone = new Report();
    private final Object2IntOpenHashMap<Edge> edgeCountForAverage = new Object2IntOpenHashMap<>();

    protected void clean() {
        workspace = null;
        graphModel = null;
        containers = null;
        progressTicket = null;
        columnsTypeMismatchAlreadyWarned.clear();
        //Flush report and create a new one for next process:
        reportAfterDone = report;
        report = new Report();
        edgeCountForAverage.clear();
    }

    protected void flushColumns(ContainerUnloader container) {
        addColumnsToTable(container, graphModel.getNodeTable(), container.getNodeColumns());
        addColumnsToTable(container, graphModel.getEdgeTable(), container.getEdgeColumns());
    }

    private void addColumnsToTable(ContainerUnloader container, Table table, Iterable<ColumnDraft> columns) {
        TimeRepresentation timeRepresentation = container.getTimeRepresentation();
        for (ColumnDraft col : columns) {
            if (!table.hasColumn(col.getId())) {
                Class typeClass = col.getResolvedTypeClass(container);
                
                if (Attributes.isTypeAvailable(typeClass, timeRepresentation)) {
                    Object defaultValue = col.getResolvedDefaultValue(container);
                    if (defaultValue != null && !typeClass.isAssignableFrom(defaultValue.getClass())) {
                        String error = NbBundle.getMessage(
                                AbstractProcessor.class, "AbstractProcessor.error.columnDefaultValueTypeMismatch",
                                col.getId(),
                                defaultValue.toString(),
                                defaultValue.getClass().getSimpleName(),
                                typeClass.getSimpleName()
                        );

                        report.logIssue(new Issue(error, Issue.Level.SEVERE));
                        defaultValue = null;
                    }

                    table.addColumn(col.getId(), col.getTitle(), typeClass, Origin.DATA, defaultValue, !col.isDynamic());
                } else {
                    String error = NbBundle.getMessage(
                            AbstractProcessor.class, "AbstractProcessor.error.unavailableColumnType",
                            typeClass.getSimpleName(),
                            timeRepresentation.name(),
                            col.getId()
                    );
                    report.logIssue(new Issue(error, Issue.Level.SEVERE));
                }
            }
        }
    }

    protected void flushToNode(ContainerUnloader container, NodeDraft nodeDraft, Node node) {
        if (nodeDraft.getColor() != null) {
            node.setColor(nodeDraft.getColor());
        }

        if (nodeDraft.getLabel() != null) {
            if (node.getLabel() == null || !nodeDraft.isCreatedAuto()) {
                node.setLabel(nodeDraft.getLabel());
            }
        }

        if (node.getTextProperties() != null) {
            node.getTextProperties().setVisible(nodeDraft.isLabelVisible());
        }

        if (nodeDraft.getLabelColor() != null && node.getTextProperties() != null) {
            Color labelColor = nodeDraft.getLabelColor();
            node.getTextProperties().setColor(labelColor);
        } else {
            node.getTextProperties().setColor(new Color(0, 0, 0, 0));
        }

        if (nodeDraft.getLabelSize() != -1f && node.getTextProperties() != null) {
            node.getTextProperties().setSize(nodeDraft.getLabelSize());
        }

        if ((nodeDraft.getX() != 0 || nodeDraft.getY() != 0 || nodeDraft.getZ() != 0)
                && (node.x() == 0 && node.y() == 0 && node.z() == 0)) {
            node.setX(nodeDraft.getX());
            node.setY(nodeDraft.getY());
            node.setZ(nodeDraft.getZ());
        }

        if (nodeDraft.getSize() != 0 && !Float.isNaN(nodeDraft.getSize())) {
            node.setSize(nodeDraft.getSize());
        } else if (node.size() == 0) {
            node.setSize(10f);
        }

        //Timeset
        if (nodeDraft.getTimeSet() != null) {
            flushTimeSet(nodeDraft.getTimeSet(), node);
        }

        //Graph timeset
        if (nodeDraft.getGraphTimestamp() != null) {
            node.addTimestamp(nodeDraft.getGraphTimestamp());
        } else if (nodeDraft.getGraphInterval() != null) {
            node.addInterval(nodeDraft.getGraphInterval());
        }

        //Attributes
        flushToElementAttributes(container, nodeDraft, node);
    }

    protected void flushToElementAttributes(ContainerUnloader container, ElementDraft elementDraft, Element element) {
        for (ColumnDraft columnDraft : elementDraft.getColumns()) {
            if (elementDraft instanceof EdgeDraft && columnDraft.getId().equalsIgnoreCase("weight")) {
                continue;//Special weight column
            }

            Object val = elementDraft.getValue(columnDraft.getId());

            Column column = element.getTable().getColumn(columnDraft.getId());
            if (column == null) {
                continue;//The column might be not present, for cases when it cannot be added due to time representation mismatch, etc
            }

            if (column.isReadOnly()) {
                continue;
            }

            Class columnDraftTypeClass = columnDraft.getResolvedTypeClass(container);

            if (!column.getTypeClass().equals(columnDraftTypeClass)) {
                if (!columnsTypeMismatchAlreadyWarned.contains(column)) {
                    columnsTypeMismatchAlreadyWarned.add(column);

                    String error = NbBundle.getMessage(
                            AbstractProcessor.class, "AbstractProcessor.error.columnTypeMismatch",
                            column.getId(),
                            column.getTypeClass().getSimpleName(),
                            columnDraftTypeClass.getSimpleName()
                    );

                    report.logIssue(new Issue(error, Issue.Level.SEVERE));
                }

                continue;//Incompatible types!
            }

            if (val != null) {
                Object processedNewValue = val;

                Object existingValue = element.getAttribute(columnDraft.getId());

                if (columnDraft.isDynamic() && existingValue != null) {
                    if (TimeMap.class.isAssignableFrom(columnDraft.getTypeClass())) {
                        TimeMap existingMap = (TimeMap) existingValue;
                        if (!existingMap.isEmpty()) {
                            TimeMap valMap = (TimeMap) val;
                            TimeMap newMap = (TimeMap) existingMap;

                            Object[] keys = valMap.toKeysArray();
                            Object[] vals = valMap.toValuesArray();
                            for (int i = 0; i < keys.length; i++) {
                                try {
                                    newMap.put(keys[i], vals[i]);
                                } catch (IllegalArgumentException e) {
                                    //Overlapping intervals, ignore
                                }
                            }

                            processedNewValue = newMap;
                        }
                    } else if (TimeSet.class.isAssignableFrom(columnDraft.getTypeClass())) {
                        TimeSet existingTimeSet = (TimeSet) existingValue;

                        processedNewValue = mergeTimeSets(existingTimeSet, (TimeSet) val);
                    }
                }

                element.setAttribute(columnDraft.getId(), processedNewValue);
            }
        }
    }

    protected void flushToEdge(ContainerUnloader container, EdgeDraft edgeDraft, Edge edge, boolean newEdge) {
        //Edge weight
        flushEdgeWeight(container, edgeDraft, edge, newEdge);

        //Replace data when a new edge is created or the merge strategy is not to keep the first edge data:
        EdgeMergeStrategy edgesMergeStrategy = containers[0].getEdgesMergeStrategy();
        if (newEdge || edgesMergeStrategy != EdgeMergeStrategy.FIRST) {
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
            } else {
                edge.getTextProperties().setColor(new Color(0, 0, 0, 0));
            }

            //Attributes
            flushToElementAttributes(container, edgeDraft, edge);
        }

        //Timeset
        if (edgeDraft.getTimeSet() != null) {
            flushTimeSet(edgeDraft.getTimeSet(), edge);
        }

        //Graph timeset
        if (edgeDraft.getGraphTimestamp() != null) {
            edge.addTimestamp(edgeDraft.getGraphTimestamp());
        } else if (edgeDraft.getGraphInterval() != null) {
            edge.addInterval(edgeDraft.getGraphInterval());
        }
    }

    protected void flushEdgeWeight(ContainerUnloader container, EdgeDraft edgeDraft, Edge edge, boolean newEdge) {
        Column weightColumn = graphModel.getEdgeTable().getColumn("weight");
        ColumnDraft weightColumnDraft = container.getEdgeColumn("weight");

        boolean weightColumnDraftIsDynamic = weightColumnDraft != null && weightColumnDraft.isDynamic();

        if (weightColumn.isDynamic() != weightColumnDraftIsDynamic) {
            Class weightColumnDraftTypeClass = weightColumnDraft != null ? weightColumnDraft.getResolvedTypeClass(container): Double.class;
            if (!columnsTypeMismatchAlreadyWarned.contains(weightColumn)) {
                columnsTypeMismatchAlreadyWarned.add(weightColumn);

                String error = NbBundle.getMessage(
                        AbstractProcessor.class, "AbstractProcessor.error.columnTypeMismatch",
                        weightColumn.getId(),
                        weightColumn.getTypeClass().getSimpleName(),
                        weightColumnDraftTypeClass.getSimpleName()
                );

                report.logIssue(new Issue(error, Issue.Level.SEVERE));
            }

            return;
        }

        if (weightColumn.isDynamic()) {
            Object val = edgeDraft.getValue("weight");
            if (val != null && val instanceof TimeMap) {
                TimeMap valMap = (TimeMap) val;
                if (Number.class.isAssignableFrom(valMap.getTypeClass())) {
                    final TimeMap newMap;
                    if (val instanceof IntervalMap) {
                        newMap = new IntervalDoubleMap();
                    } else {
                        newMap = new TimestampDoubleMap();
                    }

                    TimeMap existingMap = (TimeMap) edge.getAttribute("weight");
                    if (existingMap != null) {
                        Object[] keys2 = existingMap.toKeysArray();
                        Object[] vals2 = existingMap.toValuesArray();

                        for (int i = 0; i < keys2.length; i++) {
                            newMap.put(keys2[i], ((Number) vals2[i]).doubleValue());
                        }
                    }

                    Object[] keys1 = valMap.toKeysArray();
                    Object[] vals1 = valMap.toValuesArray();

                    for (int i = 0; i < keys1.length; i++) {
                        try {
                            newMap.put(keys1[i], ((Number) vals1[i]).doubleValue());
                        } catch (IllegalArgumentException e) {
                            //Overlapping intervals, ignore
                        }
                    }

                    edge.setAttribute("weight", newMap);
                }
            }
        } else if (!newEdge) {
            if (edgeDraft.getTimeSet() != null || edgeDraft.getValue("timeset") != null || edge.getAttribute("timeset") != null) {
                //Don't merge double (non dynamic) weights when the edges have dynamic time intervals/timestamps, they are the same edge in different periods of time
                return;
            }

            //Merge the existing edge and the draft edge weights:
            double result = edge.getWeight();

            edgeCountForAverage.addTo(edge, 1);
            int edgeCount = edgeCountForAverage.getInt(edge);

            switch (containers[0].getEdgesMergeStrategy()) {
                case AVG:
                    result = (edge.getWeight() * edgeCount + edgeDraft.getWeight()) / (edgeCount + 1);
                    break;
                case MAX:
                    result = Math.max(edgeDraft.getWeight(), edge.getWeight());
                    break;
                case MIN:
                    result = Math.min(edgeDraft.getWeight(), edge.getWeight());
                    break;
                case SUM:
                    result = edgeDraft.getWeight() + edge.getWeight();
                    break;
                case FIRST:
                    result = edge.getWeight();
                    break;
                case LAST:
                    result = edgeDraft.getWeight();
                    break;
                default:
                    break;
            }

            edge.setWeight(result);
        }
    }

    protected void flushTimeSet(TimeSet timeSet, Element element) {
        TimeSet existingTimeSet = (TimeSet) element.getAttribute("timeset");
        element.setAttribute("timeset", mergeTimeSets(existingTimeSet, timeSet));
    }

    protected TimeSet mergeTimeSets(TimeSet set1, TimeSet set2) {
        if (set1 instanceof IntervalSet) {
            return mergeIntervalSets((IntervalSet) set1, (IntervalSet) set2);
        } else if (set1 instanceof TimestampSet) {
            return mergeTimestampSets((TimestampSet) set1, (TimestampSet) set2);
        } else {
            return set2;//Set 1 must be null
        }
    }

    protected IntervalSet mergeIntervalSets(IntervalSet set1, IntervalSet set2) {
        IntervalSet merged = new IntervalSet();
        for (Interval i : set1.toArray()) {
            merged.add(i);
        }

        boolean overlappingIntervals = false;
        for (Interval i : set2.toArray()) {
            try {
                merged.add(i);
            } catch (IllegalArgumentException e) {
                //Catch overlapping intervals not allowed
                overlappingIntervals = true;
            }
        }

        if (overlappingIntervals) {
            String warning = NbBundle.getMessage(
                    AbstractProcessor.class, "AbstractProcessor.warning.overlappingIntervals",
                    set1.toString(graphModel.getTimeFormat(), graphModel.getTimeZone()),
                    set2.toString(graphModel.getTimeFormat(), graphModel.getTimeZone()),
                    merged.toString(graphModel.getTimeFormat(), graphModel.getTimeZone())
            );
            report.logIssue(new Issue(warning, Issue.Level.WARNING));
        }

        return merged;
    }

    protected TimestampSet mergeTimestampSets(TimestampSet set1, TimestampSet set2) {
        TimestampSet merged = new TimestampSet();
        for (Double t : set1.toArray()) {
            merged.add(t);
        }
        for (Double t : set2.toArray()) {
            merged.add(t);
        }

        return merged;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void setContainers(ContainerUnloader[] containers) {
        this.containers = containers;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public Report getReport() {
        return reportAfterDone;
    }
}
