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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.types.TimestampMap;
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
    private boolean exportDynamicWeight = true;
    private boolean exportVisibility = false;
    //Settings Helper
    private float minSize;
    private float maxSize;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private boolean edgeLabels;
    private boolean edgeColors;
    //Columns
    private NodeColumnsGDF[] defaultNodeColumnsGDFs;
    private EdgeColumnsGDF[] defaultEdgeColumnsGDFs;
    //Buffer
    private Writer writer;

    @Override
    public boolean execute() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();

        try {
            exportData(graph, graphModel);
        } catch (Exception e) {
            Logger.getLogger(ExporterGDF.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private void exportData(Graph graph, GraphModel graphModel) throws Exception {

        Progress.start(progressTicket);

        defaultNodeColumns(graph);
        defaultEdgeColumns(graph);
        Column[] nodeColumns = attributesNodeColumns(graphModel);
        Column[] edgeColumns = attributesEdgeColumns(graphModel);

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
        for (Column c : nodeColumns) {
            if (!c.isProperty()) {
                stringBuilder.append(c.getTitle());
                stringBuilder.append(" ");
                DataTypeGDF dataTypeGDF = getDataTypeGDF(c.getTypeClass());
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

        //Options
        if (normalize) {
            calculateMinMax(graph);
        }

        //Calculate progress units count
        int max = graph.getNodeCount() + graph.getEdgeCount();
        Progress.switchToDeterminate(progressTicket, max);

        //Node lines
        NodeIterable itr = graph.getNodes();
        for (Node node : itr) {
            if (cancel) {
                itr.doBreak();
                return;
            }

            //Id
            stringBuilder.append(node.getId());
            stringBuilder.append(",");

            //Default columns
            for (NodeColumnsGDF c : defaultNodeColumnsGDFs) {
                if (c.isEnable()) {
                    c.writeData(stringBuilder, node);
                    stringBuilder.append(",");
                }
            }

            //Attributes columns
            for (Column c : nodeColumns) {
                if (!c.isProperty()) {
                    Object val = node.getAttribute(c, graph.getView());
                    if (val != null) {
                        if (c.getTypeClass().equals(String.class) || c.getTypeClass().equals(String[].class)) {
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

        //Edge settings helper
        for (Edge e : graph.getEdges()) {
            edgeColors = edgeColors || e.alpha() != 0;
            edgeLabels = edgeLabels || (e.getLabel() != null && !e.getLabel().isEmpty());
        }

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
        for (Column c : edgeColumns) {
            if (!c.isProperty()) {
                stringBuilder.append(c.getTitle());
                stringBuilder.append(" ");
                DataTypeGDF dataTypeGDF = getDataTypeGDF(c.getTypeClass());
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

        //Edge lines
        EdgeIterable itrEdges = graph.getEdges();
        for (Edge edge : itrEdges) {
            if (cancel) {
                itrEdges.doBreak();
                return;
            }
            //Source & Target
            stringBuilder.append(edge.getSource().getId());
            stringBuilder.append(",");
            stringBuilder.append(edge.getTarget().getId());
            stringBuilder.append(",");

            //Default columns
            for (EdgeColumnsGDF c : defaultEdgeColumnsGDFs) {
                if (c.isEnable()) {
                    c.writeData(stringBuilder, edge);
                    stringBuilder.append(",");
                }
            }

            //Attributes columns
            for (Column c : edgeColumns) {
                if (!c.isProperty()) {
                    Object val = edge.getAttribute(c, graph.getView());
                    if (val != null) {
                        if (c.getTypeClass().equals(String.class) || c.getTypeClass().equals(String[].class)) {
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

        //Write StringBuilder
        if (!cancel) {
            writer.append(stringBuilder);
        }
        Progress.finish(progressTicket);
    }

    private Column[] attributesNodeColumns(GraphModel graphModel) {
        List<Column> cols = new ArrayList<Column>();
        if (exportAttributes && graphModel != null) {
            for (Column column : graphModel.getNodeTable()) {
                if (!isNodeDefaultColumn(column.getId())) {
                    cols.add(column);
                }
            }
        }
        return cols.toArray(new Column[0]);
    }

    private Column[] attributesEdgeColumns(GraphModel graphModel) {
        List<Column> cols = new ArrayList<Column>();
        if (exportAttributes && graphModel != null) {
            for (Column column : graphModel.getEdgeTable()) {
                if (!isEdgeDefaultColumn(column.getId())) {
                    cols.add(column);
                }
            }
        }
        return cols.toArray(new Column[0]);
    }

    private boolean isNodeDefaultColumn(String id) {
        for (NodeColumnsGDF c : defaultNodeColumnsGDFs) {
            if (c.title.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEdgeDefaultColumn(String id) {
        for (EdgeColumnsGDF c : defaultEdgeColumnsGDFs) {
            if (c.title.equalsIgnoreCase(id)) {
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
                String label = node.getLabel();
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
                builder.append(node.getTextProperties().isVisible());
            }
        };

        NodeColumnsGDF widthColumn = new NodeColumnsGDF("width", DataTypeGDF.DOUBLE) {
            @Override
            public boolean isEnable() {
                return exportPosition;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
                float size = node.size();
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
                float size = node.size();
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
                float x = node.x();
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
                float y = node.y();
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
                builder.append((int) (node.r() * 255f));
                builder.append(",");
                builder.append((int) (node.g() * 255f));
                builder.append(",");
                builder.append((int) (node.b() * 255f));
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
                builder.append(node.isFixed());
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
                return edgeLabels;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                String label = edge.getLabel();
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
                double weight;
                if (exportDynamicWeight) {
                    weight = edge.getWeight(graph.getView());
                } else {
                    weight = edge.getWeight();
                }
                builder.append(weight);
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
                return exportColors && edgeColors;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                if (edge.alpha() != 0) {
                    String quote = "'";
                    builder.append(quote);
                    builder.append((int) (edge.r() * 255f));
                    builder.append(",");
                    builder.append((int) (edge.g() * 255f));
                    builder.append(",");
                    builder.append((int) (edge.b() * 255f));
                    builder.append(quote);
                }
            }
        };

        EdgeColumnsGDF visibleColumn = new EdgeColumnsGDF("visible", DataTypeGDF.BOOLEAN) {
            @Override
            public boolean isEnable() {
                return exportVisibility;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                builder.append(true);
            }
        };

        EdgeColumnsGDF labelVisibleColumn = new EdgeColumnsGDF("labelvisible", DataTypeGDF.BOOLEAN) {
            @Override
            public boolean isEnable() {
                return exportVisibility;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
                builder.append(edge.getTextProperties().isVisible());
            }
        };

        EdgeColumnsGDF edgeIdColumn = new EdgeColumnsGDF("id", DataTypeGDF.VARCHAR) {
            @Override
            public boolean isEnable() {
                return false;
            }

            @Override
            public void writeData(StringBuilder builder, Edge edge) {
            }
        };

        defaultEdgeColumnsGDFs = new EdgeColumnsGDF[7];
        defaultEdgeColumnsGDFs[0] = edgeIdColumn;
        defaultEdgeColumnsGDFs[1] = labelColumn;
        defaultEdgeColumnsGDFs[2] = weightColumn;
        defaultEdgeColumnsGDFs[3] = directedColumn;
        defaultEdgeColumnsGDFs[4] = colorColumn;
        defaultEdgeColumnsGDFs[5] = visibleColumn;
        defaultEdgeColumnsGDFs[6] = labelVisibleColumn;
    }

    private void calculateMinMax(Graph graph) {
        minX = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        minSize = Float.POSITIVE_INFINITY;
        maxSize = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getNodes()) {
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
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

    private DataTypeGDF getDataTypeGDF(Class type) {
        if (AttributeUtils.isDynamicType(type)) {
            type = AttributeUtils.getStaticType((Class<? extends TimestampMap>) type);
        }
        if (type.equals(Boolean.class)) {
            return DataTypeGDF.BOOLEAN;
        } else if (type.equals(Double.class)) {
            return DataTypeGDF.DOUBLE;
        } else if (type.equals(Float.class)) {
            return DataTypeGDF.FLOAT;
        } else if (type.equals(Integer.class)) {
            return DataTypeGDF.INTEGER;
        } else if (type.equals(Long.class)) {
            return DataTypeGDF.INTEGER;
        } else if (type.equals(Short.class)) {
            return DataTypeGDF.INTEGER;
        } else if (type.equals(Byte.class)) {
            return DataTypeGDF.TINYINT;
        } else if (type.equals(String.class)) {
            return DataTypeGDF.VARCHAR;
        } else {
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

    @Override
    public boolean isExportVisible() {
        return exportVisible;
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
