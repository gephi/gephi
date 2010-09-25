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
package org.gephi.io.exporter.plugin;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.attributes.type.TypeConvertor;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ExporterGEXF implements GraphExporter, CharacterExporter, LongTask {

    //GEXF
    private static final String GEXF = "gexf";
    private static final String GEXF_NAMESPACE = "http://www.gexf.net/1.1draft";
    private static final String GEXF_NAMESPACE_LOCATION = "http://www.gexf.net/1.1draft http://www.gexf.net/1.1draft/gexf.xsd";
    private static final String VIZ = "viz";
    private static final String VIZ_NAMESPACE = "http://www.gexf.net/1.1draft/viz";
    private static final String GEXF_VERSION = "version";
    private static final String GRAPH = "graph";
    private static final String GRAPH_MODE = "mode";
    private static final String GRAPH_DEFAULT_EDGETYPE = "defaultedgetype";
    private static final String GRAPH_START = "start";
    private static final String GRAPH_END = "end";
    private static final String GRAPH_TIMEFORMAT = "timeformat";
    private static final String META = "meta";
    private static final String META_LASTMODIFIEDDATE = "lastmodifieddate";
    private static final String META_CREATOR = "creator";
    private static final String META_DESCRIPTION = "description";
    private static final String NODES = "nodes";
    private static final String NODE = "node";
    private static final String NODE_ID = "id";
    private static final String NODE_LABEL = "label";
    private static final String NODE_PID = "pid";
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
    private static final String START = "start";
    private static final String END = "end";
    private static final String START_OPEN = "startopen";
    private static final String END_OPEN = "endopen";
    private static final String SLICES = "slices";
    private static final String SLICE = "slice";
    private static final String ATTRIBUTE = "attribute";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TITLE = "title";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_DEFAULT = "default";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTES_CLASS = "class";
    private static final String ATTRIBUTES_MODE = "mode";
    private static final String ATTVALUE = "attvalue";
    private static final String ATTVALUE_FOR = "for";
    private static final String ATTVALUE_VALUE = "value";
    //Architecture
    private boolean cancel = false;
    private ProgressTicket progress;
    private Workspace workspace;
    private boolean exportVisible;
    private Writer writer;
    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private TimeInterval visibleInterval;
    private DynamicModel dynamicModel;
    //Settings
    private boolean normalize = false;
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportSize = true;
    private boolean exportAttributes = true;
    private boolean exportHierarchy = false;
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

    public boolean execute() {
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        graphModel = workspace.getLookup().lookup(GraphModel.class);
        HierarchicalGraph graph = null;
        if (exportVisible) {
            graph = graphModel.getHierarchicalGraphVisible();
        } else {
            graph = graphModel.getHierarchicalGraph();
        }
        Progress.start(progress);
        graph.readLock();

        //Options
        if (normalize) {
            calculateMinMax(graph);
        }

        //Calculate progress units count
        int max = 0;
        if (exportHierarchy) {
            for (Node n : graph.getNodesTree()) {
                max++;
            }
            for (Edge e : graph.getEdgesTree()) {
                max++;
            }
        } else {
            max = graph.getNodeCount();
            for (Edge e : graph.getEdgesAndMetaEdges()) {
                max++;
            }
        }
        Progress.switchToDeterminate(progress, max);

        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);

            XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
            xmlWriter = new IndentingXMLStreamWriter(xmlWriter);

            xmlWriter.writeStartDocument("UTF-8", "1.0");
            xmlWriter.setPrefix("", GEXF_NAMESPACE);
            xmlWriter.writeStartElement(GEXF_NAMESPACE, GEXF);
            xmlWriter.writeNamespace("", GEXF_NAMESPACE);
            xmlWriter.writeAttribute(GEXF_VERSION, "1.1");

            if (exportColors || exportPosition || exportSize) {
                xmlWriter.writeNamespace(VIZ, VIZ_NAMESPACE);
            }
            xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlWriter.writeAttribute("xsi:schemaLocation", GEXF_NAMESPACE_LOCATION);

            if (exportDynamic) {
                DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
                dynamicModel = dynamicController != null ? dynamicController.getModel(workspace) : null;
                visibleInterval = dynamicModel == null ? null : exportVisible ? dynamicModel.getVisibleInterval() : new TimeInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            }

            writeMeta(xmlWriter);
            writeGraph(xmlWriter, graph);

            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.close();

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

        Progress.finish(progress);
        return !cancel;
    }

    private void writeGraph(XMLStreamWriter xmlWriter, HierarchicalGraph graph) throws Exception {
        xmlWriter.writeStartElement(GRAPH);
        xmlWriter.writeAttribute(GRAPH_DEFAULT_EDGETYPE, graph instanceof DirectedGraph ? "directed" : graph instanceof UndirectedGraph ? "undirected" : "mixed");

        if (exportDynamic) {
            if (!Double.isInfinite(visibleInterval.getLow())) {
                String intervalLow = formatTime(visibleInterval.getLow());
                xmlWriter.writeAttribute(GRAPH_START, intervalLow);
            }
            if (!Double.isInfinite(visibleInterval.getHigh())) {
                String intervalHigh = formatTime(visibleInterval.getHigh());
                xmlWriter.writeAttribute(GRAPH_END, intervalHigh);
            }
            String timeFormat = dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATE) ? "date" : "double";
            xmlWriter.writeAttribute(GRAPH_TIMEFORMAT, timeFormat);
        }
        xmlWriter.writeAttribute(GRAPH_MODE, exportDynamic ? "dynamic" : "static");

        writeAttributes(xmlWriter, attributeModel.getNodeTable());
        writeAttributes(xmlWriter, attributeModel.getEdgeTable());
        writeNodes(xmlWriter, graph);
        writeEdges(xmlWriter, graph);

        xmlWriter.writeEndElement();
    }

    private void writeMeta(XMLStreamWriter xmlWriter) throws Exception {
        xmlWriter.writeStartElement(META);
        xmlWriter.writeAttribute(META_LASTMODIFIEDDATE, getDateTime());

        xmlWriter.writeStartElement(META_CREATOR);
        xmlWriter.writeCharacters("Gephi 0.7");
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(META_DESCRIPTION);
        xmlWriter.writeCharacters("");
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();
    }

    private void writeAttributes(XMLStreamWriter xmlWriter, AttributeTable table) throws Exception {
        List<AttributeColumn> staticCols = new ArrayList<AttributeColumn>();
        List<AttributeColumn> dynamicCols = new ArrayList<AttributeColumn>();
        String attClass = table == attributeModel.getNodeTable() ? "node" : "edge";

        for (AttributeColumn col : table.getColumns()) {

            if (!col.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                if (exportDynamic && col.getType().isDynamicType()) {
                    dynamicCols.add(col);
                } else {
                    staticCols.add(col);
                }
            } else if (exportDynamic && col.getType().isDynamicType() && col.getOrigin().equals(AttributeOrigin.PROPERTY) && col.getIndex() == PropertiesColumn.EDGE_WEIGHT.getIndex()) {
                dynamicCols.add(col);
            }
        }

        if (!staticCols.isEmpty()) {
            writeAttributes(xmlWriter, staticCols.toArray(new AttributeColumn[0]), "static", attClass);
        }
        if (!dynamicCols.isEmpty()) {
            writeAttributes(xmlWriter, dynamicCols.toArray(new AttributeColumn[0]), "dynamic", attClass);
        }
    }

    private void writeAttributes(XMLStreamWriter xmlWriter, AttributeColumn[] cols, String mode, String attClass) throws Exception {
        xmlWriter.writeStartElement(ATTRIBUTES);
        xmlWriter.writeAttribute(ATTRIBUTES_CLASS, attClass);
        xmlWriter.writeAttribute(ATTRIBUTES_MODE, mode);

        for (AttributeColumn col : cols) {
            if (!col.getOrigin().equals(AttributeOrigin.PROPERTY)
                    || (exportDynamic && col.getOrigin().equals(AttributeOrigin.PROPERTY) && col.getIndex() == PropertiesColumn.EDGE_WEIGHT.getIndex())) {
                xmlWriter.writeStartElement(ATTRIBUTE);
                xmlWriter.writeAttribute(ATTRIBUTE_ID, col.getId());
                xmlWriter.writeAttribute(ATTRIBUTE_TITLE, col.getTitle());
                if (col.getType().equals(AttributeType.INT)) {
                    xmlWriter.writeAttribute(ATTRIBUTE_TYPE, "integer");
                } else if (col.getType().isListType()) {
                    if (col.getType().equals(AttributeType.LIST_INTEGER)) {
                        xmlWriter.writeAttribute(ATTRIBUTE_TYPE, "listint");
                    } else if (col.getType().equals(AttributeType.LIST_CHARACTER)) {
                        xmlWriter.writeAttribute(ATTRIBUTE_TYPE, "listchar");
                    } else {
                        xmlWriter.writeAttribute(ATTRIBUTE_TYPE, col.getType().getTypeString().toLowerCase().replace("_", ""));
                    }
                } else if (col.getType().isDynamicType()) {
                    AttributeType staticType = TypeConvertor.getStaticType(col.getType());
                    if (staticType.equals(AttributeType.INT)) {
                        xmlWriter.writeAttribute(ATTRIBUTE_TYPE, "integer");
                    } else {
                        xmlWriter.writeAttribute(ATTRIBUTE_TYPE, staticType.getTypeString().toLowerCase());
                    }
                } else {
                    xmlWriter.writeAttribute(ATTRIBUTE_TYPE, col.getType().getTypeString().toLowerCase());
                }
                if (col.getDefaultValue() != null) {
                    xmlWriter.writeStartElement(ATTRIBUTE_DEFAULT);
                    xmlWriter.writeCharacters(col.getDefaultValue().toString());
                    xmlWriter.writeEndElement();
                }
                xmlWriter.writeEndElement();
            }
        }

        xmlWriter.writeEndElement();
    }

    private void writeNodes(XMLStreamWriter xmlWriter, HierarchicalGraph graph) throws Exception {
        if (cancel) {
            return;
        }
        xmlWriter.writeStartElement(NODES);

        AttributeColumn dynamicCol = dynamicCol = attributeModel.getNodeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);

        NodeIterable nodeIterable = exportHierarchy ? graph.getNodesTree() : graph.getNodes();
        for (Node node : nodeIterable) {
            xmlWriter.writeStartElement(NODE);

            String id = node.getNodeData().getId();
            xmlWriter.writeAttribute(NODE_ID, id);
            if (node.getNodeData().getLabel() != null && !node.getNodeData().getLabel().isEmpty() && !node.getNodeData().getLabel().equals(id)) {
                xmlWriter.writeAttribute(NODE_LABEL, node.getNodeData().getLabel());
            }

            if (exportHierarchy) {
                Node parent = graph.getParent(node);
                if (parent != null) {
                    xmlWriter.writeAttribute(NODE_PID, parent.getNodeData().getId());
                }
            }

            if (exportDynamic && dynamicCol != null && visibleInterval != null) {
                TimeInterval timeInterval = (TimeInterval) node.getNodeData().getAttributes().getValue(dynamicCol.getIndex());
                if (timeInterval != null) {
                    writeTimeInterval(xmlWriter, timeInterval);
                }
            }

            if (exportAttributes && node.getNodeData().getAttributes() != null) {
                AttributeRow attributeRow = (AttributeRow) node.getNodeData().getAttributes();
                writeAttValue(xmlWriter, attributeRow, visibleInterval);
            }

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
                break;
            }
        }

        xmlWriter.writeEndElement();
    }

    private void writeAttValue(XMLStreamWriter xmlWriter, AttributeRow row, TimeInterval visibleInterval) throws Exception {
        for (AttributeValue val : row.getValues()) {
            AttributeColumn col = val.getColumn();
            if (!col.getOrigin().equals(AttributeOrigin.PROPERTY)
                    || (exportDynamic && col.getOrigin().equals(AttributeOrigin.PROPERTY) && col.getIndex() == PropertiesColumn.EDGE_WEIGHT.getIndex())) {
                AttributeType type = col.getType();
                if (type.isDynamicType()) {
                    DynamicType dynamicValue = (DynamicType) val.getValue();
                    if (dynamicValue != null && visibleInterval != null && exportDynamic) {
                        List<Interval<?>> intervals = dynamicValue.getIntervals(visibleInterval.getLow(), visibleInterval.getHigh());
                        for (Interval<?> interval : intervals) {
                            Object value = interval.getValue();
                            if (value != null) {
                                xmlWriter.writeStartElement(ATTVALUE);
                                xmlWriter.writeAttribute(ATTVALUE_FOR, col.getId());
                                xmlWriter.writeAttribute(ATTVALUE_VALUE, value.toString());
                                if (!Double.isInfinite(interval.getLow())) {
                                    String intervalLow = formatTime(interval.getLow());
                                    xmlWriter.writeAttribute(interval.isLowExcluded() ? START_OPEN : START, intervalLow);
                                }
                                if (!Double.isInfinite(interval.getHigh())) {
                                    String intervalHigh = formatTime(interval.getHigh());
                                    xmlWriter.writeAttribute(interval.isHighExcluded() ? END_OPEN : END, intervalHigh);
                                }
                                xmlWriter.writeEndElement();
                            }
                        }
                    } else if (dynamicValue != null) {
                        TimeInterval interval = visibleInterval;
                        if (interval == null) {
                            interval = new TimeInterval();
                        }
                        Object value = DynamicUtilities.getDynamicValue(val, interval.getLow(), interval.getHigh());
                        if (value != null) {
                            xmlWriter.writeStartElement(ATTVALUE);
                            xmlWriter.writeAttribute(ATTVALUE_FOR, val.getColumn().getId());
                            xmlWriter.writeAttribute(ATTVALUE_VALUE, value.toString());
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    if (val.getValue() != null) {
                        xmlWriter.writeStartElement(ATTVALUE);
                        xmlWriter.writeAttribute(ATTVALUE_FOR, col.getId());
                        xmlWriter.writeAttribute(ATTVALUE_VALUE, val.getValue().toString());
                        xmlWriter.writeEndElement();
                    }
                }
            }
        }
    }

    private void writeNodePosition(XMLStreamWriter xmlWriter, Node node) throws Exception {
        float x = node.getNodeData().x();
        if (normalize && x != 0.0) {
            x = (x - minX) / (maxX - minX);
        }
        float y = node.getNodeData().y();
        if (normalize && y != 0.0) {
            y = (y - minY) / (maxY - minY);
        }
        float z = node.getNodeData().z();
        if (normalize && z != 0.0) {
            z = (z - minZ) / (maxZ - minZ);
        }
        if (!(x == 0 && y == 0 && z == 0)) {
            xmlWriter.writeStartElement(VIZ, NODE_POSITION, VIZ_NAMESPACE);
            xmlWriter.writeAttribute("x", "" + x);
            xmlWriter.writeAttribute("y", "" + y);
            if (z != 0) {
                xmlWriter.writeAttribute("z", "" + z);
            }
            xmlWriter.writeEndElement();
        }
    }

    private void writeNodeSize(XMLStreamWriter xmlWriter, Node node) throws Exception {
        xmlWriter.writeStartElement(VIZ, NODE_SIZE, VIZ_NAMESPACE);
        float size = node.getNodeData().getSize();
        if (normalize) {
            size = (size - minSize) / (maxSize - minSize);
        }
        xmlWriter.writeAttribute("value", "" + size);
        xmlWriter.writeEndElement();
    }

    private void writeNodeColor(XMLStreamWriter xmlWriter, Node node) throws Exception {
        int r = Math.round(node.getNodeData().r() * 255f);
        int g = Math.round(node.getNodeData().g() * 255f);
        int b = Math.round(node.getNodeData().b() * 255f);
        if (r != 0 || g != 0 || b != 0) {
            xmlWriter.writeStartElement(VIZ, NODE_COLOR, VIZ_NAMESPACE);
            xmlWriter.writeAttribute("r", "" + r);
            xmlWriter.writeAttribute("g", "" + g);
            xmlWriter.writeAttribute("b", "" + b);
            xmlWriter.writeEndElement();
        }
    }

    private void writeTimeInterval(XMLStreamWriter xmlWriter, TimeInterval timeInterval) throws Exception {
        List<Interval<Double[]>> intervals = timeInterval.getIntervals(visibleInterval.getLow(), visibleInterval.getHigh());
        if (intervals.size() > 1) {
            xmlWriter.writeStartElement(SLICES);
            for (Interval<Double[]> interval : intervals) {
                xmlWriter.writeStartElement(SLICE);
                if (!Double.isInfinite(interval.getLow())) {
                    String intervalLow = formatTime(interval.getLow());
                    xmlWriter.writeAttribute(interval.isLowExcluded() ? START_OPEN : START, intervalLow);
                }
                if (!Double.isInfinite(interval.getHigh())) {
                    String intervalHigh = formatTime(interval.getHigh());
                    xmlWriter.writeAttribute(interval.isHighExcluded() ? END_OPEN : END, intervalHigh);
                }
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        } else if (intervals.size() == 1) {
            Interval<Double[]> interval = intervals.get(0);
            if (!Double.isInfinite(interval.getLow())) {
                String intervalLow = formatTime(interval.getLow());
                xmlWriter.writeAttribute(interval.isLowExcluded() ? START_OPEN : START, intervalLow);
            }
            if (!Double.isInfinite(interval.getHigh())) {
                String intervalHigh = formatTime(interval.getHigh());
                xmlWriter.writeAttribute(interval.isHighExcluded() ? END_OPEN : END, intervalHigh);
            }
        }
    }

    private void writeEdges(XMLStreamWriter xmlWriter, HierarchicalGraph graph) throws Exception {
        if (cancel) {
            return;
        }
        xmlWriter.writeStartElement(EDGES);

        AttributeColumn dynamicCol = dynamicCol = attributeModel.getEdgeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);

        EdgeIterable edgeIterable = exportHierarchy ? graph.getEdgesTree() : graph.getEdgesAndMetaEdges();
        for (Edge edge : edgeIterable) {
            xmlWriter.writeStartElement(EDGE);

            if (edge.getEdgeData().getId() != null && !edge.getEdgeData().getId().equals(Integer.toString(edge.getId()))) {
                xmlWriter.writeAttribute(EDGE_ID, edge.getEdgeData().getId());
            }
            xmlWriter.writeAttribute(EDGE_SOURCE, edge.getSource().getNodeData().getId());
            xmlWriter.writeAttribute(EDGE_TARGET, edge.getTarget().getNodeData().getId());

            if (edge.isDirected() && graphModel.isMixed()) {
                xmlWriter.writeAttribute(EDGE_TYPE, "directed");
            } else if (!edge.isDirected() && graphModel.isMixed()) {
                xmlWriter.writeAttribute(EDGE_TYPE, "undirected");
            }

            String label = edge.getEdgeData().getLabel();
            if (label != null && !label.isEmpty() && !label.equals(edge.getEdgeData().getId())) {
                xmlWriter.writeAttribute(EDGE_LABEL, label);
            }

            float weight = edge.getWeight();
            if (weight != 1f) {
                xmlWriter.writeAttribute(EDGE_WEIGHT, "" + weight);
            }

            if (exportDynamic && dynamicCol != null && visibleInterval != null) {
                TimeInterval timeInterval = (TimeInterval) edge.getEdgeData().getAttributes().getValue(dynamicCol.getIndex());
                if (timeInterval != null) {
                    writeTimeInterval(xmlWriter, timeInterval);
                }
            }

            writeEdgeColor(xmlWriter, edge);

            if (exportAttributes && edge.getEdgeData().getAttributes() != null) {
                AttributeRow attributeRow = (AttributeRow) edge.getEdgeData().getAttributes();
                writeAttValue(xmlWriter, attributeRow, visibleInterval);
            }

            xmlWriter.writeEndElement();
            Progress.progress(progress);
            if (cancel) {
                break;
            }
        }

        xmlWriter.writeEndElement();
    }

    private void writeEdgeColor(XMLStreamWriter xmlWriter, Edge edge) throws Exception {
        if (edge.getEdgeData().r() != -1) { //Edge has custom color
            int r = Math.round(edge.getEdgeData().r() * 255f);
            int g = Math.round(edge.getEdgeData().g() * 255f);
            int b = Math.round(edge.getEdgeData().b() * 255f);
            if (r != 0 || g != 0 || b != 0) {
                xmlWriter.writeStartElement(VIZ, EDGE_COLOR, VIZ_NAMESPACE);
                xmlWriter.writeAttribute("r", "" + r);
                xmlWriter.writeAttribute("g", "" + g);
                xmlWriter.writeAttribute("b", "" + b);
                if (edge.getEdgeData().alpha() != 1f) {
                    xmlWriter.writeAttribute("a", "" + b);
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
            NodeData nodeData = node.getNodeData();
            minX = Math.min(minX, nodeData.x());
            maxX = Math.max(maxX, nodeData.x());
            minY = Math.min(minY, nodeData.y());
            maxY = Math.max(maxY, nodeData.y());
            minZ = Math.min(minZ, nodeData.z());
            maxZ = Math.max(maxZ, nodeData.z());
            minSize = Math.min(minSize, nodeData.getSize());
            maxSize = Math.max(maxSize, nodeData.getSize());
        }
    }

    private String formatTime(double time) {
        if (dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DATE)) {
            String t = DynamicUtilities.getXMLDateStringFromDouble(time);
            if (t.endsWith("T00:00:00.000")) {
                t = t.substring(0, t.length() - 13);
            }
            return t;
        } else {
            return Double.toString(time);
        }
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

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

    public void setExportHierarchy(boolean exportHierarchy) {
        this.exportHierarchy = exportHierarchy;
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

    public boolean isExportVisible() {
        return exportVisible;
    }

    public boolean isExportDynamic() {
        return exportDynamic;
    }

    public boolean isExportHierarchy() {
        return exportHierarchy;
    }

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
