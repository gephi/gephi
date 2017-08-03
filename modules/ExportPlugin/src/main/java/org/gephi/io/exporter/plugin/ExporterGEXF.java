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
package org.gephi.io.exporter.plugin;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.*;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.joda.time.DateTimeZone;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian, Sébastien Heymann
 */
public class ExporterGEXF implements GraphExporter, CharacterExporter, LongTask {

    //GEXF
    private static final String GEXF = "gexf";
    private static final String GEXF_NAMESPACE = "http://www.gexf.net/1.3";
    private static final String GEXF_NAMESPACE_LOCATION = "http://www.gexf.net/1.3 http://www.gexf.net/1.3/gexf.xsd";
    private static final String VIZ = "viz";
    private static final String VIZ_NAMESPACE = "http://www.gexf.net/1.3/viz";
    private static final String GEXF_VERSION = "version";
    private static final String GRAPH = "graph";
    private static final String GRAPH_MODE = "mode";
    private static final String GRAPH_DEFAULT_EDGETYPE = "defaultedgetype";
    private static final String GRAPH_START = "start";
    private static final String GRAPH_END = "end";
    private static final String GRAPH_TIMEFORMAT = "timeformat";
    private static final String GRAPH_TIMEREPRESENTATION = "timerepresentation";
    private static final String GRAPH_IDTYPE = "idtype";
    private static final String TIMESTAMP = "timestamp";
    private static final String TIMESTAMPS = "timestamps";
    private static final String INTERVALS = "intervals";
    private static final String META = "meta";
    private static final String META_LASTMODIFIEDDATE = "lastmodifieddate";
    private static final String META_CREATOR = "creator";
    private static final String META_DESCRIPTION = "description";
    private static final String NODES = "nodes";
    private static final String NODE = "node";
    private static final String NODE_ID = "id";
    private static final String NODE_LABEL = "label";
    private static final String NODE_POSITION = "position";
    private static final String NODE_COLOR = "color";
    private static final String NODE_SIZE = "size";
    private static final String EDGES = "edges";
    private static final String EDGE = "edge";
    private static final String EDGE_ID = "id";
    private static final String EDGE_SOURCE = "source";
    private static final String EDGE_TARGET = "target";
    private static final String EDGE_LABEL = "label";
    private static final String EDGE_TYPE = "type";
    private static final String EDGE_WEIGHT = "weight";
    private static final String EDGE_COLOR = "color";
    private static final String EDGE_KIND = "kind";
    private static final String START = "start";
    private static final String END = "end";
    private static final String SPELLS = "spells";
    private static final String SPELL = "spell";
    private static final String ATTRIBUTE = "attribute";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TITLE = "title";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_DEFAULT = "default";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTES_CLASS = "class";
    private static final String ATTRIBUTES_MODE = "mode";
    private static final String ATTVALUES = "attvalues";
    private static final String ATTVALUE = "attvalue";
    private static final String ATTVALUE_FOR = "for";
    private static final String ATTVALUE_VALUE = "value";
    //Architecture
    private boolean cancel = false;
    private ProgressTicket progress;
    private Workspace workspace;
    private boolean exportVisible;
    private Writer writer;
    //Settings
    private boolean normalize = false;
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportSize = true;
    private boolean exportAttributes = true;
    private boolean exportDynamic = true;
    //Settings Helper
    private float minSize;
    private float maxSize;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private float minZ;
    private float maxZ;

    @Override
    public boolean execute() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        Progress.start(progress);
        graph.readLock();

        //Is it a dynamic graph?
        exportDynamic = exportDynamic && graphModel.isDynamic();

        //Calculate min & max
        calculateMinMax(graph);

        Progress.switchToDeterminate(progress, graph.getNodeCount() + graph.getEdgeCount());

        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);

            XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
            xmlWriter = new IndentingXMLStreamWriter(xmlWriter);

            xmlWriter.writeStartDocument("UTF-8", "1.0");
            xmlWriter.setPrefix("", GEXF_NAMESPACE);
            xmlWriter.writeStartElement(GEXF_NAMESPACE, GEXF);
            xmlWriter.writeNamespace("", GEXF_NAMESPACE);
            xmlWriter.writeAttribute(GEXF_VERSION, "1.3");

            if (exportColors || exportPosition || exportSize) {
                xmlWriter.writeNamespace(VIZ, VIZ_NAMESPACE);
            }
            xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlWriter.writeAttribute("xsi:schemaLocation", GEXF_NAMESPACE_LOCATION);

            writeMeta(xmlWriter);
            writeGraph(xmlWriter, graph);

            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.close();

        } catch (Exception e) {
            Logger.getLogger(ExporterGEXF.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progress);
        }

        return !cancel;
    }

    private void writeGraph(XMLStreamWriter xmlWriter, Graph graph) throws Exception {
        xmlWriter.writeStartElement(GRAPH);
        if (!(graph.isMixed())) {
            xmlWriter.writeAttribute(GRAPH_DEFAULT_EDGETYPE, graph.isDirected() ? "directed" : "undirected");
        }

        Configuration graphConfig = graph.getModel().getConfiguration();
        if (graphConfig.getEdgeIdType().equals(Integer.class) && graphConfig.getNodeIdType().equals(Integer.class)) {
            xmlWriter.writeAttribute(GRAPH_IDTYPE, "integer");
        } else if (graphConfig.getEdgeIdType().equals(Long.class) && graphConfig.getNodeIdType().equals(Long.class)) {
            xmlWriter.writeAttribute(GRAPH_IDTYPE, "long");
        }

        if (exportDynamic) {
            TimeFormat timeFormat = graph.getModel().getTimeFormat();
            xmlWriter.writeAttribute(GRAPH_TIMEFORMAT, timeFormat.toString().toLowerCase());

            TimeRepresentation timeRepresentation = graphConfig.getTimeRepresentation();
            xmlWriter.writeAttribute(GRAPH_TIMEREPRESENTATION, timeRepresentation.toString().toLowerCase());
        }
        xmlWriter.writeAttribute(GRAPH_MODE, exportDynamic ? "dynamic" : "static");

        writeAttributes(xmlWriter, graph.getModel().getNodeTable());
        writeAttributes(xmlWriter, graph.getModel().getEdgeTable());
        writeNodes(xmlWriter, graph);
        writeEdges(xmlWriter, graph);

        xmlWriter.writeEndElement();
    }

    private void writeMeta(XMLStreamWriter xmlWriter) throws Exception {
        xmlWriter.writeStartElement(META);
        xmlWriter.writeAttribute(META_LASTMODIFIEDDATE, getDateTime());

        xmlWriter.writeStartElement(META_CREATOR);
        xmlWriter.writeCharacters("Gephi 0.9");
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(META_DESCRIPTION);
        xmlWriter.writeCharacters("");
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();
    }

    private void writeAttributes(XMLStreamWriter xmlWriter, Table table) throws Exception {
        List<Column> staticCols = new ArrayList<>();
        List<Column> dynamicCols = new ArrayList<>();
        String attClass = table.getElementClass().equals(Node.class) ? "node" : "edge";

        for (Column col : table) {
            if (exportAttributes && !col.isProperty()) {
                if (exportDynamic && col.isDynamic()) {
                    dynamicCols.add(col);
                } else {
                    staticCols.add(col);
                }
            } else if (exportDynamic && (AttributeUtils.isEdgeColumn(col) && col.isDynamic() && col.getId().equals("weight"))) {
                dynamicCols.add(col);
            }
        }

        if (!staticCols.isEmpty()) {
            writeAttributes(xmlWriter, staticCols.toArray(new Column[0]), "static", attClass);
        }
        if (!dynamicCols.isEmpty()) {
            writeAttributes(xmlWriter, dynamicCols.toArray(new Column[0]), "dynamic", attClass);
        }
    }

    private void writeAttributes(XMLStreamWriter xmlWriter, Column[] cols, String mode, String attClass) throws Exception {
        xmlWriter.writeStartElement(ATTRIBUTES);
        xmlWriter.writeAttribute(ATTRIBUTES_CLASS, attClass);
        xmlWriter.writeAttribute(ATTRIBUTES_MODE, mode);

        for (Column col : cols) {

            xmlWriter.writeStartElement(ATTRIBUTE);
            xmlWriter.writeAttribute(ATTRIBUTE_ID, col.getId());
            xmlWriter.writeAttribute(ATTRIBUTE_TITLE, col.getTitle());

            if (col.isArray()) {
                xmlWriter.writeAttribute(ATTRIBUTE_TYPE, "list" + col.getTypeClass().getComponentType().getSimpleName().toLowerCase());
            } else if (col.isDynamic()) {
                xmlWriter.writeAttribute(ATTRIBUTE_TYPE, AttributeUtils.getStaticType((Class<? extends TimeMap>) col.getTypeClass()).getSimpleName().toLowerCase());
            } else {
                xmlWriter.writeAttribute(ATTRIBUTE_TYPE, col.getTypeClass().getSimpleName().toLowerCase());
            }
            if (col.getDefaultValue() != null) {
                xmlWriter.writeStartElement(ATTRIBUTE_DEFAULT);
                String valString;
                if (col.isArray()) {
                    valString = AttributeUtils.printArray(col.getDefaultValue());
                } else {
                    valString = col.getDefaultValue().toString();
                }
                xmlWriter.writeCharacters(valString);
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndElement();
    }

    private void writeNodes(XMLStreamWriter xmlWriter, Graph graph) throws Exception {
        if (cancel) {
            return;
        }
        xmlWriter.writeStartElement(NODES);

        NodeIterable nodeIterable = graph.getNodes();
        for (Node node : nodeIterable) {
            xmlWriter.writeStartElement(NODE);

            String id = node.getId().toString();
            xmlWriter.writeAttribute(NODE_ID, id);
            if (node.getLabel() != null && !node.getLabel().isEmpty()) {
                xmlWriter.writeAttribute(NODE_LABEL, node.getLabel());
            }

            if (exportDynamic) {
                writeTimeSet(xmlWriter, graph, node);
            }

            writeAttValues(xmlWriter, graph, node);

            if (exportSize) {
                writeNodeSize(xmlWriter, node);
            }

            if (exportPosition) {
                writeNodePosition(xmlWriter, node);
            }

            if (exportColors) {
                writeNodeColor(xmlWriter, node);
            }

            xmlWriter.writeEndElement();
            Progress.progress(progress);
            if (cancel) {
                nodeIterable.doBreak();
                break;
            }
        }

        xmlWriter.writeEndElement();
    }

    private void writeAttValue(XMLStreamWriter xmlWriter, Graph graph, Column column, Element element) throws Exception {
        if (!column.isDynamic()) {
            Object val = element.getAttribute(column);
            if (val != null) {
                xmlWriter.writeStartElement(ATTVALUE);
                xmlWriter.writeAttribute(ATTVALUE_FOR, column.getId());
                String valString;
                if (column.isArray()) {
                    valString = AttributeUtils.printArray(val);
                } else {
                    valString = val.toString();
                }
                xmlWriter.writeAttribute(ATTVALUE_VALUE, valString);
                xmlWriter.writeEndElement();
            }
        } else if (exportDynamic) {
            Interval visibleInterval = graph.getView().getTimeInterval();
            TimeRepresentation timeRepresentation = graph.getModel().getConfiguration().getTimeRepresentation();
            TimeFormat timeFormat = graph.getModel().getTimeFormat();
            DateTimeZone timeZone = graph.getModel().getTimeZone();
            if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
                IntervalMap timeMap = (IntervalMap) element.getAttribute(column);
                if (timeMap != null) {
                    for (Interval interval : timeMap.toKeysArray()) {
                        if (!exportVisible || interval.compareTo(visibleInterval) == 0) {
                            final Object defaultValue = null;
                            final Object value = timeMap.get(interval, defaultValue);
                            if (value != null) {
                                xmlWriter.writeStartElement(ATTVALUE);
                                xmlWriter.writeAttribute(ATTVALUE_FOR, column.getId());
                                xmlWriter.writeAttribute(ATTVALUE_VALUE, value.toString());
                                if (!Double.isInfinite(interval.getLow())) {
                                    String intervalLow = AttributeUtils.printTimestampInFormat(interval.getLow(), timeFormat, timeZone);
                                    xmlWriter.writeAttribute(START, intervalLow);
                                }
                                if (!Double.isInfinite(interval.getHigh())) {
                                    String intervalHigh = AttributeUtils.printTimestampInFormat(interval.getHigh(), timeFormat, timeZone);
                                    xmlWriter.writeAttribute(END, intervalHigh);
                                }
                                xmlWriter.writeEndElement();
                            }
                        }
                    }
                }
            } else {
                TimestampMap timeMap = (TimestampMap) element.getAttribute(column);
                if (timeMap != null) {
                    for (Double timestamp : timeMap.toKeysArray()) {
                        if (!exportVisible || visibleInterval.compareTo(timestamp) == 0) {
                            final Object defaultValue = null;
                            final Object value = timeMap.get(timestamp, defaultValue);
                            if (value != null) {
                                xmlWriter.writeStartElement(ATTVALUE);
                                xmlWriter.writeAttribute(ATTVALUE_FOR, column.getId());
                                xmlWriter.writeAttribute(ATTVALUE_VALUE, value.toString());
                                xmlWriter.writeAttribute(TIMESTAMP, AttributeUtils.printTimestampInFormat(timestamp, timeFormat, timeZone));
                                xmlWriter.writeEndElement();
                            }
                        }
                    }
                }
            }
        } else {
            Object value = element.getAttribute(column, graph.getView());
            if (value != null) {
                xmlWriter.writeStartElement(ATTVALUE);
                xmlWriter.writeAttribute(ATTVALUE_FOR, column.getId());
                xmlWriter.writeAttribute(ATTVALUE_VALUE, value.toString());
                xmlWriter.writeEndElement();
            }
        }
    }

    private void writeAttValues(XMLStreamWriter xmlWriter, Graph graph, Element element) throws Exception {
        List<Column> columns = new ArrayList<>();
        for (Column column : element.getAttributeColumns()) {
            if ((exportAttributes && !column.isProperty()) || (element instanceof Edge && ((Edge) element).hasDynamicWeight() && column.getId().equals("weight"))) {
                columns.add(column);

            }
        }
        if (!columns.isEmpty()) {
            xmlWriter.writeStartElement(ATTVALUES);
            for (Column column : columns) {
                writeAttValue(xmlWriter, graph, column, element);
            }
            xmlWriter.writeEndElement();
        }
    }

    private void writeNodePosition(XMLStreamWriter xmlWriter, Node node) throws Exception {
        float x = node.x();
        if (normalize && x != 0.0) {
            x = (x - minX) / (maxX - minX);
        }
        float y = node.y();
        if (normalize && y != 0.0) {
            y = (y - minY) / (maxY - minY);
        }
        float z = node.z();
        if (normalize && z != 0.0) {
            z = (z - minZ) / (maxZ - minZ);
        }
        if (!(x == 0 && y == 0 && z == 0)) {
            xmlWriter.writeStartElement(VIZ, NODE_POSITION, VIZ_NAMESPACE);
            xmlWriter.writeAttribute("x", "" + x);
            xmlWriter.writeAttribute("y", "" + y);
            if (minZ != 0 || maxZ != 0) {
                xmlWriter.writeAttribute("z", "" + z);
            }
            xmlWriter.writeEndElement();
        }
    }

    private void writeNodeSize(XMLStreamWriter xmlWriter, Node node) throws Exception {
        xmlWriter.writeStartElement(VIZ, NODE_SIZE, VIZ_NAMESPACE);
        float size = node.size();
        if (normalize) {
            size = (size - minSize) / (maxSize - minSize);
        }
        xmlWriter.writeAttribute("value", "" + size);
        xmlWriter.writeEndElement();
    }

    private void writeNodeColor(XMLStreamWriter xmlWriter, Node node) throws Exception {
        int r = Math.round(node.r() * 255f);
        int g = Math.round(node.g() * 255f);
        int b = Math.round(node.b() * 255f);
        if (r != 0 || g != 0 || b != 0) {
            xmlWriter.writeStartElement(VIZ, NODE_COLOR, VIZ_NAMESPACE);
            xmlWriter.writeAttribute("r", "" + r);
            xmlWriter.writeAttribute("g", "" + g);
            xmlWriter.writeAttribute("b", "" + b);
            if (node.alpha() != 1f) {
                xmlWriter.writeAttribute("a", "" + node.alpha());
            }
            xmlWriter.writeEndElement();
        }
    }

    private void writeTimeSet(XMLStreamWriter xmlWriter, Graph graph, Element element) throws Exception {
        Interval visibleInterval = graph.getView().getTimeInterval();
        TimeRepresentation timeRepresentation = graph.getModel().getConfiguration().getTimeRepresentation();
        TimeSet timeSet = (TimeSet) element.getAttribute("timeset");
        TimeFormat timeFormat = graph.getModel().getTimeFormat();
        DateTimeZone timeZone = graph.getModel().getTimeZone();
        if (timeSet != null && !timeSet.isEmpty()) {
            if (timeSet.size() > 1) {
                xmlWriter.writeStartElement(SPELLS);
                if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
                    for (Interval interval : ((IntervalSet) timeSet).toArray()) {
                        if (!exportVisible || interval.compareTo(visibleInterval) == 0) {
                            xmlWriter.writeStartElement(SPELL);
                            if (!Double.isInfinite(interval.getLow())) {
                                String intervalLow = AttributeUtils.printTimestampInFormat(interval.getLow(), timeFormat, timeZone);
                                xmlWriter.writeAttribute(START, intervalLow);
                            }
                            if (!Double.isInfinite(interval.getHigh())) {
                                String intervalHigh = AttributeUtils.printTimestampInFormat(interval.getHigh(), timeFormat, timeZone);
                                xmlWriter.writeAttribute(END, intervalHigh);
                            }
                            xmlWriter.writeEndElement();
                        }
                    }
                } else if (timeRepresentation.equals(TimeRepresentation.TIMESTAMP)) {
                    for (Double timestamp : ((TimestampSet) timeSet).toArray()) {
                        if (!exportVisible || visibleInterval.compareTo(timestamp) == 0) {
                            xmlWriter.writeStartElement(SPELL);
                            xmlWriter.writeAttribute(TIMESTAMP, AttributeUtils.printTimestampInFormat(timestamp, timeFormat, timeZone));
                            xmlWriter.writeEndElement();
                        }
                    }
                }
                xmlWriter.writeEndElement();
            } else if (timeRepresentation.equals(TimeRepresentation.INTERVAL)) {
                Interval interval = ((IntervalSet) timeSet).toArray()[0];
                if (!Double.isInfinite(interval.getLow())) {
                    String intervalLow = AttributeUtils.printTimestampInFormat(interval.getLow(), timeFormat, timeZone);
                    xmlWriter.writeAttribute(START, intervalLow);
                }
                if (!Double.isInfinite(interval.getHigh())) {
                    String intervalHigh = AttributeUtils.printTimestampInFormat(interval.getHigh(), timeFormat, timeZone);
                    xmlWriter.writeAttribute(END, intervalHigh);
                }
            } else if (timeRepresentation.equals(TimeRepresentation.TIMESTAMP)) {
                Double timestamp = ((TimestampSet) timeSet).toArray()[0];
                xmlWriter.writeAttribute(TIMESTAMP, AttributeUtils.printTimestampInFormat(timestamp, timeFormat, timeZone));
            }
        }
    }

    private void writeEdges(XMLStreamWriter xmlWriter, Graph graph) throws Exception {
        if (cancel) {
            return;
        }

        xmlWriter.writeStartElement(EDGES);

        EdgeIterable edgeIterable = graph.getEdges();
        for (Edge edge : edgeIterable) {
            xmlWriter.writeStartElement(EDGE);

            xmlWriter.writeAttribute(EDGE_ID, edge.getId().toString());

            xmlWriter.writeAttribute(EDGE_SOURCE, edge.getSource().getId().toString());
            xmlWriter.writeAttribute(EDGE_TARGET, edge.getTarget().getId().toString());

            if (graph.isMixed()) {
                if (edge.isDirected()) {
                    xmlWriter.writeAttribute(EDGE_TYPE, "directed");
                } else {
                    xmlWriter.writeAttribute(EDGE_TYPE, "undirected");
                }
            }

            String label = edge.getLabel();
            if (label != null && !label.isEmpty()) {
                xmlWriter.writeAttribute(EDGE_LABEL, label);
            }

            if (edge.getType() != 0) {
                xmlWriter.writeAttribute(EDGE_KIND, edge.getTypeLabel().toString());
            }

            if (!edge.hasDynamicWeight()) {
                double weight = edge.getWeight();
                if (weight != 1f) {
                    xmlWriter.writeAttribute(EDGE_WEIGHT, String.valueOf(weight));
                }
            }

            if (exportDynamic) {
                writeTimeSet(xmlWriter, graph, edge);
            }

            if (exportColors) {
                writeEdgeColor(xmlWriter, edge);
            }

            writeAttValues(xmlWriter, graph, edge);

            xmlWriter.writeEndElement();
            Progress.progress(progress);
            if (cancel) {
                edgeIterable.doBreak();
                break;
            }
        }

        xmlWriter.writeEndElement();
    }

    private void writeEdgeColor(XMLStreamWriter xmlWriter, Edge edge) throws Exception {
        if (edge.alpha() != 0) { //Edge has custom color
            int r = Math.round(edge.r() * 255f);
            int g = Math.round(edge.g() * 255f);
            int b = Math.round(edge.b() * 255f);
            if (r != 0 || g != 0 || b != 0) {
                xmlWriter.writeStartElement(VIZ, EDGE_COLOR, VIZ_NAMESPACE);
                xmlWriter.writeAttribute("r", "" + r);
                xmlWriter.writeAttribute("g", "" + g);
                xmlWriter.writeAttribute("b", "" + b);
                if (edge.alpha() != 1f) {
                    xmlWriter.writeAttribute("a", "" + edge.alpha());
                }
                xmlWriter.writeEndElement();
            }
        }
    }

    private void calculateMinMax(Graph graph) {
        minX = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        minZ = Float.POSITIVE_INFINITY;
        maxZ = Float.NEGATIVE_INFINITY;
        minSize = Float.POSITIVE_INFINITY;
        maxSize = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getNodes()) {
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
            minZ = Math.min(minZ, node.z());
            maxZ = Math.max(maxZ, node.z());
            minSize = Math.min(minSize, node.size());
            maxSize = Math.max(maxSize, node.size());
        }
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "ExporterGEXF_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gexf", NbBundle.getMessage(getClass(), "fileType_GEXF_Name"));
        return new FileType[]{ft};
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void setExportAttributes(boolean exportAttributes) {
        this.exportAttributes = exportAttributes;
    }

    public void setExportColors(boolean exportColors) {
        this.exportColors = exportColors;
    }

    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    public void setExportSize(boolean exportSize) {
        this.exportSize = exportSize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setExportDynamic(boolean exportDynamic) {
        this.exportDynamic = exportDynamic;
    }

    public boolean isExportAttributes() {
        return exportAttributes;
    }

    public boolean isExportColors() {
        return exportColors;
    }

    public boolean isExportPosition() {
        return exportPosition;
    }

    public boolean isExportSize() {
        return exportSize;
    }

    public boolean isNormalize() {
        return normalize;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    public boolean isExportDynamic() {
        return exportDynamic;
    }

    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
