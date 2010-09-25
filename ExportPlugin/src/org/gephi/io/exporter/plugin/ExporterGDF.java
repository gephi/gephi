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
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ExporterGDF implements GraphExporter, CharacterExporter, LongTask {

    private Workspace workspace;
    private boolean exportVisible;
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    //Settings
    private boolean normalize = false;
    private boolean simpleQuotes = false;
    private boolean useQuotes = true;
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportAttributes = true;
    private boolean exportVisibility = false;
    //Settings Helper
    private float minSize;
    private float maxSize;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    //Columns
    private NodeColumnsGDF[] defaultNodeColumnsGDFs;
    private EdgeColumnsGDF[] defaultEdgeColumnsGDFs;
    private AttributeColumn[] nodeColumns;
    private AttributeColumn[] edgeColumns;
    //Buffer
    private Writer writer;
    //Dynamic
    private TimeInterval visibleInterval;

    public boolean execute() {
        AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = null;
        if (exportVisible) {
            graph = graphModel.getGraphVisible();
        } else {
            graph = graphModel.getGraph();
        }
        DynamicModel dynamicModel = workspace.getLookup().lookup(DynamicModel.class);
        visibleInterval = dynamicModel !=null && exportVisible ? dynamicModel.getVisibleInterval() : new TimeInterval();
        try {
            exportData(graph, attributeModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return !cancel;
    }

    private void exportData(Graph graph, AttributeModel attributeModel) throws Exception {

        Progress.start(progressTicket);

        defaultNodeColumns(graph);
        defaultEdgeColumns(graph);
        attributesNodeColumns(attributeModel);
        attributesEdgeColumns(attributeModel);

        StringBuilder stringBuilder = new StringBuilder();

        //Node intro
        stringBuilder.append("nodedef> name VARCHAR,");

        //Default Node columns title
        for (NodeColumnsGDF c : defaultNodeColumnsGDFs) {
            if (c.isEnable()) {
                stringBuilder.append(c.getTitle());
                stringBuilder.append(" ");
                stringBuilder.append(c.getType().toString().toUpperCase());
                if (c.getDefaultValue() != null) {
                    stringBuilder.append(" default ");
                    stringBuilder.append(c.getDefaultValue().toString());
                }
                stringBuilder.append(",");
            }
        }

        //Attributes Node columns
        for (AttributeColumn c : nodeColumns) {
            if (!c.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                stringBuilder.append(c.getTitle());
                stringBuilder.append(" ");
                DataTypeGDF dataTypeGDF = getDataTypeGDF(c.getType());
                stringBuilder.append(dataTypeGDF.toString().toUpperCase());
                if (c.getDefaultValue() != null) {
                    stringBuilder.append(" default ");
                    stringBuilder.append(c.getDefaultValue().toString());
                }
                stringBuilder.append(",");
            }
        }

        //Remove last coma
        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append("\n");

        //Lock
        graph.readLock();

        //Options
        if (normalize) {
            calculateMinMax(graph);
        }

        //Calculate progress units count
        int max = graph.getNodeCount() + graph.getEdgeCount();
        Progress.switchToDeterminate(progressTicket, max);

        //Node lines
        for (Node node : graph.getNodes()) {
            if (cancel) {
                break;
            }
            NodeData nodeData = node.getNodeData();

            //Id
            stringBuilder.append(nodeData.getId());
            stringBuilder.append(",");

            //Default columns
            for (NodeColumnsGDF c : defaultNodeColumnsGDFs) {
                if (c.isEnable()) {
                    c.writeData(stringBuilder, node);
                    stringBuilder.append(",");
                }
            }

            //Attributes columns
            for (AttributeColumn c : nodeColumns) {
                if (!c.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    Object val = node.getNodeData().getAttributes().getValue(c.getIndex());
                    val = DynamicUtilities.getDynamicValue(val, visibleInterval.getLow(), visibleInterval.getHigh());
                    if (val != null) {
                        if (c.getType().equals(AttributeType.STRING) || c.getType().equals(AttributeType.LIST_STRING)) {
                            String quote = !useQuotes ? "" : simpleQuotes ? "'" : "\"";
                            stringBuilder.append(quote);
                            stringBuilder.append(val.toString());
                            stringBuilder.append(quote);
                        } else {
                            stringBuilder.append(val.toString());
                        }
                    }
                    stringBuilder.append(",");
                }
            }

            //Remove last coma
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append("\n");

            Progress.progress(progressTicket);
        }

        //Edge intro
        stringBuilder.append("edgedef> node1,node2,");

        //Edge columns title
        for (EdgeColumnsGDF c : defaultEdgeColumnsGDFs) {
            if (c.isEnable()) {
                stringBuilder.append(c.getTitle());
                stringBuilder.append(" ");
                stringBuilder.append(c.getType().toString().toUpperCase());
                if (c.getDefaultValue() != null) {
                    stringBuilder.append(" default ");
                    stringBuilder.append(c.getDefaultValue().toString());
                }
                stringBuilder.append(",");
            }
        }

        //Attributes Edge columns
        for (AttributeColumn c : edgeColumns) {
            if (c.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                stringBuilder.append(c.getTitle());
                stringBuilder.append(" ");
                DataTypeGDF dataTypeGDF = getDataTypeGDF(c.getType());
                stringBuilder.append(dataTypeGDF.toString().toUpperCase());
                if (c.getDefaultValue() != null) {
                    stringBuilder.append(" default ");
                    stringBuilder.append(c.getDefaultValue().toString());
                }
                stringBuilder.append(",");
            }
        }

        //Remove last coma
        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append("\n");

        //MetaEdges
        EdgeIterable edgeIterable;
        if (graph.getGraphModel().isHierarchical()) {
            HierarchicalGraph hierarchicalGraph = (HierarchicalGraph) graph;
            edgeIterable = hierarchicalGraph.getEdgesAndMetaEdges();
        } else {
            edgeIterable = graph.getEdges();
        }

        //Edge lines
        for (Edge edge : edgeIterable) {
            if (cancel) {
                break;
            }
            //Source & Target
            stringBuilder.append(edge.getSource().getNodeData().getId());
            stringBuilder.append(",");
            stringBuilder.append(edge.getTarget().getNodeData().getId());
            stringBuilder.append(",");

            //Default columns
            for (EdgeColumnsGDF c : defaultEdgeColumnsGDFs) {
                if (c.isEnable()) {
                    c.writeData(stringBuilder, edge);
                    stringBuilder.append(",");
                }
            }

            //Attributes columns
            for (AttributeColumn c : nodeColumns) {
                Object val = edge.getEdgeData().getAttributes().getValue(c.getIndex());
                val = DynamicUtilities.getDynamicValue(val, visibleInterval.getLow(), visibleInterval.getHigh());
                if (val != null) {
                    if (c.getType().equals(AttributeType.STRING) || c.getType().equals(AttributeType.LIST_STRING)) {
                        String quote = !useQuotes ? "" : simpleQuotes ? "'" : "\"";
                        stringBuilder.append(quote);
                        stringBuilder.append(val.toString());
                        stringBuilder.append(quote);
                    } else {
                        stringBuilder.append(val.toString());
                    }
                }
                stringBuilder.append(",");
            }

            //Remove last coma
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append("\n");

            Progress.progress(progressTicket);
        }

        //Unlock
        graph.readUnlockAll();

        //Write StringBuilder
        if (!cancel) {
            writer.append(stringBuilder);
        }
        Progress.finish(progressTicket);
    }

    private void attributesNodeColumns(AttributeModel attributeModel) {
        List<AttributeColumn> cols = new ArrayList<AttributeColumn>();
        if (attributeModel != null) {
            for (AttributeColumn column : attributeModel.getNodeTable().getColumns()) {
                if (!isNodeDefaultColumn(column.getId())) {
                    cols.add(column);
                }
            }
        }
        nodeColumns = cols.toArray(new AttributeColumn[0]);
    }

    private void attributesEdgeColumns(AttributeModel attributeModel) {
        List<AttributeColumn> cols = new ArrayList<AttributeColumn>();
        if (attributeModel != null) {
            for (AttributeColumn column : attributeModel.getEdgeTable().getColumns()) {
                if (!isEdgeDefaultColumn(column.getId())) {
                    cols.add(column);
                }
            }
        }
        edgeColumns = cols.toArray(new AttributeColumn[0]);
    }

    private boolean isNodeDefaultColumn(String id) {
        for (NodeColumnsGDF c : defaultNodeColumnsGDFs) {
            if (c.title.equals(id.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isEdgeDefaultColumn(String id) {
        for (EdgeColumnsGDF c : defaultEdgeColumnsGDFs) {
            if (c.title.equals(id.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void defaultNodeColumns(Graph graph) {
        NodeColumnsGDF labelColumn = new NodeColumnsGDF("label") {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                String label = node.getNodeData().getLabel();
                if (label != null) {
                    String quote = !useQuotes ? "" : simpleQuotes ? "'" : "\"";
                    builder.append(quote);
                    builder.append(label);
                    builder.append(quote);
                }
            }
        };

        NodeColumnsGDF visibleColumn = new NodeColumnsGDF("visible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return exportVisibility;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                builder.append(true);
            }
        };

        NodeColumnsGDF labelVisibleColumn = new NodeColumnsGDF("labelvisible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return exportVisibility;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                builder.append(node.getNodeData().getTextData().isVisible());
            }
        };

        NodeColumnsGDF widthColumn = new NodeColumnsGDF("width", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return exportPosition;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                float size = node.getNodeData().getSize();
                if (normalize) {
                    size = (size - minSize) / (maxSize - minSize);
                }
                builder.append(size);
            }
        };

        NodeColumnsGDF heightColumn = new NodeColumnsGDF("height", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return exportPosition;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                float size = node.getNodeData().getSize();
                if (normalize) {
                    size = (size - minSize) / (maxSize - minSize);
                }
                builder.append(size);
            }
        };

        NodeColumnsGDF xColumn = new NodeColumnsGDF("x", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return exportPosition;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                float x = node.getNodeData().x();
                if (normalize && x != 0.0) {
                    x = (x - minX) / (maxX - minX);
                }
                builder.append(x);
            }
        };

        NodeColumnsGDF yColumn = new NodeColumnsGDF("y", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return exportPosition;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                float y = node.getNodeData().y();
                if (normalize && y != 0.0) {
                    y = (y - minY) / (maxY - minY);
                }
                builder.append(y);
            }
        };

        NodeColumnsGDF colorColumn = new NodeColumnsGDF("color") {

            @Override
            public boolean isEnable() {
                return exportColors;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                String quote = "'";
                builder.append(quote);
                builder.append((int) (node.getNodeData().r() * 255f));
                builder.append(",");
                builder.append((int) (node.getNodeData().g() * 255f));
                builder.append(",");
                builder.append((int) (node.getNodeData().b() * 255f));
                builder.append(quote);
            }
        };

        NodeColumnsGDF fixedColumn = new NodeColumnsGDF("fixed", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return exportVisibility;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                builder.append(node.getNodeData().isFixed());
            }
        };

        NodeColumnsGDF styleColumn = new NodeColumnsGDF("style", DataTypeGDF.INT) {

            @Override
            public boolean isEnable() {
                return false;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        defaultNodeColumnsGDFs = new NodeColumnsGDF[10];
        defaultNodeColumnsGDFs[0] = labelColumn;
        defaultNodeColumnsGDFs[1] = visibleColumn;
        defaultNodeColumnsGDFs[2] = labelVisibleColumn;
        defaultNodeColumnsGDFs[3] = widthColumn;
        defaultNodeColumnsGDFs[4] = heightColumn;
        defaultNodeColumnsGDFs[5] = xColumn;
        defaultNodeColumnsGDFs[6] = yColumn;
        defaultNodeColumnsGDFs[7] = colorColumn;
        defaultNodeColumnsGDFs[8] = fixedColumn;
        defaultNodeColumnsGDFs[9] = styleColumn;
    }

    private void defaultEdgeColumns(final Graph graph) {
        EdgeColumnsGDF labelColumn = new EdgeColumnsGDF("label") {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                String label = edge.getEdgeData().getLabel();
                if (label != null) {
                    String quote = !useQuotes ? "" : simpleQuotes ? "'" : "\"";
                    builder.append(quote);
                    builder.append(label);
                    builder.append(quote);
                }
            }
        };

        EdgeColumnsGDF weightColumn = new EdgeColumnsGDF("weight", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                builder.append(edge.getWeight(visibleInterval.getLow(), visibleInterval.getHigh()));
            }
        };

        EdgeColumnsGDF directedColumn = new EdgeColumnsGDF("directed", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                builder.append(graph.isDirected(edge));
            }
        };

        EdgeColumnsGDF colorColumn = new EdgeColumnsGDF("color") {

            @Override
            public boolean isEnable() {
                return exportColors;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                String quote = "'";
                builder.append(quote);
                builder.append((int) (edge.getEdgeData().r() * 255f));
                builder.append(",");
                builder.append((int) (edge.getEdgeData().g() * 255f));
                builder.append(",");
                builder.append((int) (edge.getEdgeData().b() * 255f));
                builder.append(quote);
            }
        };

        EdgeColumnsGDF visibleColumn = new EdgeColumnsGDF("visible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                builder.append(true);
            }
        };

        EdgeColumnsGDF labelVisibleColumn = new EdgeColumnsGDF("labelvisible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                builder.append(edge.getEdgeData().getTextData().isVisible());
            }
        };

        defaultEdgeColumnsGDFs = new EdgeColumnsGDF[6];
        defaultEdgeColumnsGDFs[0] = labelColumn;
        defaultEdgeColumnsGDFs[1] = weightColumn;
        defaultEdgeColumnsGDFs[2] = directedColumn;
        defaultEdgeColumnsGDFs[3] = colorColumn;
        defaultEdgeColumnsGDFs[4] = visibleColumn;
        defaultEdgeColumnsGDFs[5] = labelVisibleColumn;
    }

    private void calculateMinMax(Graph graph) {
        minX = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        minSize = Float.POSITIVE_INFINITY;
        maxSize = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getNodes()) {
            NodeData nodeData = node.getNodeData();
            minX = Math.min(minX, nodeData.x());
            maxX = Math.max(maxX, nodeData.x());
            minY = Math.min(minY, nodeData.y());
            maxY = Math.max(maxY, nodeData.y());
            minSize = Math.min(minSize, nodeData.getSize());
            maxSize = Math.max(maxSize, nodeData.getSize());
        }
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "ExporterGDF_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gdf", NbBundle.getMessage(getClass(), "fileType_GDF_Name"));
        return new FileType[]{ft};
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

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setSimpleQuotes(boolean simpleQuotes) {
        this.simpleQuotes = simpleQuotes;
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

    public boolean isNormalize() {
        return normalize;
    }

    public boolean isSimpleQuotes() {
        return simpleQuotes;
    }

    public boolean isUseQuotes() {
        return useQuotes;
    }

    public boolean isExportVisibility() {
        return exportVisibility;
    }

    public void setExportVisibility(boolean exportVisibility) {
        this.exportVisibility = exportVisibility;
    }

    public void setUseQuotes(boolean useQuotes) {
        this.useQuotes = useQuotes;
    }

    private DataTypeGDF getDataTypeGDF(AttributeType type) {
        switch (type) {
            case BOOLEAN:
                return DataTypeGDF.BOOLEAN;
            case DOUBLE:
                return DataTypeGDF.DOUBLE;
            case FLOAT:
                return DataTypeGDF.FLOAT;
            case INT:
                return DataTypeGDF.INTEGER;
            case LONG:
                return DataTypeGDF.INTEGER;
            case STRING:
                return DataTypeGDF.VARCHAR;
            case DYNAMIC_BOOLEAN:
                return DataTypeGDF.BOOLEAN;
            case DYNAMIC_DOUBLE:
                return DataTypeGDF.DOUBLE;
            case DYNAMIC_FLOAT:
                return DataTypeGDF.FLOAT;
            case DYNAMIC_INT:
                return DataTypeGDF.INTEGER;
            case DYNAMIC_LONG:
                return DataTypeGDF.INTEGER;
            case DYNAMIC_STRING:
                return DataTypeGDF.VARCHAR;
            default:
                return DataTypeGDF.VARCHAR;
        }
    }

    private enum DataTypeGDF {

        VARCHAR, BOOL, BOOLEAN, INTEGER, TINYINT, INT, DOUBLE, FLOAT
    };

    private abstract class NodeColumnsGDF {

        protected final String title;
        protected final DataTypeGDF type;
        protected final Object defaultValue;

        public NodeColumnsGDF(String title) {
            this(title, DataTypeGDF.VARCHAR);
        }

        public NodeColumnsGDF(String title, DataTypeGDF type) {
            this(title, type, null);
        }

        public NodeColumnsGDF(String title, DataTypeGDF type, Object defaultValue) {
            this.title = title;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public abstract boolean isEnable();

        public abstract void writeData(StringBuilder builder, Node node);

        public String getTitle() {
            return title;
        }

        public DataTypeGDF getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    private abstract class EdgeColumnsGDF {

        protected final String title;
        protected final DataTypeGDF type;
        protected final Object defaultValue;

        public EdgeColumnsGDF(String title) {
            this(title, DataTypeGDF.VARCHAR);
        }

        public EdgeColumnsGDF(String title, DataTypeGDF type) {
            this(title, type, null);
        }

        public EdgeColumnsGDF(String title, DataTypeGDF type, Object defaultValue) {
            this.title = title;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public abstract boolean isEnable();

        public abstract void writeData(StringBuilder builder, Edge edge);

        public String getTitle() {
            return title;
        }

        public DataTypeGDF getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    public boolean isExportVisible() {
        return exportVisible;
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
