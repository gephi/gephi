package org.gephi.io.exporter.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.time.ZoneId;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.VersionUtils;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExporterJson implements GraphExporter, CharacterExporter, LongTask {

    // Architecture
    private boolean cancel = false;
    private ProgressTicket progress;
    private Workspace workspace;
    private boolean exportVisible;
    private Writer writer;
    private Graph graph;
    // Settings
    private boolean normalize = false;
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportSize = true;
    private boolean exportAttributes = true;
    private boolean exportDynamic = true;
    private boolean exportMeta = true;
    private boolean prettyPrint = true;

    // Helper
    private NormalizationHelper normalization;

    // Formats
    public enum Format {Graphology}

    private Format format = Format.Graphology;

    @Override
    public boolean execute() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        Progress.start(progress);
        graph.readLock();

        //Is it a dynamic graph?
        exportDynamic = exportDynamic && graphModel.isDynamic();

        //Calculate min & max
        normalization = NormalizationHelper.build(normalize, graph);

        Progress.switchToDeterminate(progress, graph.getNodeCount() + graph.getEdgeCount());

        try {
            exportData();
        } catch (Exception e) {
            Logger.getLogger(ExporterJson.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progress);
        }

        return !cancel;
    }

    private void exportData() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Color.class, new ColorAdapter())
                .registerTypeAdapterFactory(new GraphTypeAdapterFactory());

        if (prettyPrint) {
            gsonBuilder = gsonBuilder.setPrettyPrinting();
        }

        Gson gson = gsonBuilder
                .create();
        gson.toJson(graph, writer);
    }

    private class ColorAdapter extends WriteTypeAdapter<Color> {

        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            if (exportColors) {
                out.name("color");
                if (value.getAlpha() < 255) {
                    out.value(String.format("#%08x", (value.getRGB() << 8) | value.getAlpha()));
                } else {
                    out.value(String.format("#%06x", value.getRGB() & 0x00FFFFFF));
                }
            }
        }
    }

    private class GraphTypeAdapter extends WriteTypeAdapter<Graph> {

        private final TypeAdapter<Node> nodeTypeAdapter;
        private final TypeAdapter<Edge> edgeTypeAdapter;

        public GraphTypeAdapter(Gson gson) {
            nodeTypeAdapter = gson.getAdapter(Node.class);
            edgeTypeAdapter = gson.getAdapter(Edge.class);
        }

        @Override
        public void write(JsonWriter out, Graph graph) throws IOException {
            out.beginObject();

            // Attributes
            writeAttributes(out);

            // Options
            writeOptions(out, graph);

            // Nodes
            out.name("nodes");
            out.beginArray();
            for (Node node : graph.getNodes()) {
                if (!cancel) {
                    nodeTypeAdapter.write(out, node);
                }
            }
            out.endArray();

            // Edges
            out.name("edges");
            out.beginArray();
            for (Edge edge : graph.getEdges()) {
                if (!cancel) {
                    edgeTypeAdapter.write(out, edge);
                }
            }
            out.endArray();
            out.endObject();
        }

        protected void writeOptions(JsonWriter out, Graph graph) throws IOException {
            out.name("options");
            out.beginObject();
            out.name("multi");
            out.value(graph.getModel().getEdgeTypeLabels(false).length > 1);
            out.name("allowSelfLoops");
            out.value(true);
            out.name("type");
            out.value(
                    graph.getModel().isUndirected() ? "undirected" : graph.getModel().isMixed() ? "mixed" : "directed");
            out.endObject();
        }

        protected void writeAttributes(JsonWriter out) throws IOException {
            if (exportMeta) {
                out.name("attributes");
                out.beginObject();
                out.name("creator");
                out.value(VersionUtils.getGephiVersion());
                if (exportDynamic) {
                    Configuration graphConfig = graph.getModel().getConfiguration();
                    TimeFormat timeFormat = graph.getModel().getTimeFormat();
                    out.name("timeformat");
                    out.value(timeFormat.toString().toLowerCase());
                    out.name("timerepresentation");
                    out.value(graphConfig.getTimeRepresentation().toString().toLowerCase());
                    out.name("timezone");
                    out.value(graph.getModel().getTimeZone().getId());
                }
                out.endObject();
            }
        }
    }

    private abstract class ElementTypeAdapter<T extends Element> extends WriteTypeAdapter<T> {

        private final TypeAdapter<Color> colorAdapter;

        public ElementTypeAdapter(Gson gson) {
            this.colorAdapter = gson.getAdapter(Color.class);
        }

        protected abstract Set<String> getReservedKeys();

        @Override
        public void write(JsonWriter out, T element) throws IOException {
            throw new UnsupportedOperationException("Not to be called directly");
        }

        protected void writeAttValues(JsonWriter out, T element) throws IOException {
            if (exportAttributes) {
                TimeFormat timeFormat = graph.getModel().getTimeFormat();
                ZoneId timeZone = graph.getModel().getTimeZone();

                Set<String> reservedKeys = getReservedKeys();
                for (Column column : element.getAttributeColumns()) {
                    if (!column.isProperty() ||
                            (element instanceof Edge &&
                                    column.getId().equals("weight"))) {
                        if (!reservedKeys.contains(column.getId().toLowerCase())) {
                            // Col header, similar to spreadsheet
                            String columnId = column.getId();
                            String columnTitle = column.getTitle();
                            String columnHeader =
                                    columnId.equalsIgnoreCase(columnTitle) && !column.isProperty() ? columnTitle : columnId;

                            Object value = exportDynamic ? element.getAttribute(column) :
                                    element.getAttribute(column, graph.getView());
                            out.name(columnHeader);
                            if (value instanceof Number) {
                                out.value((Number) value);
                            } else if (value instanceof Boolean) {
                                out.value((Boolean) value);
                            } else {
                                out.value(AttributeUtils.print(value, timeFormat, timeZone));
                            }
                        } else {
                            Logger.getLogger(ExporterJson.class.getName()).log(Level.WARNING,
                                    "Attribute value for column '"+column.getId()+"' is ignored as its key overlap with a default key");
                        }
                    }
                }
            }
        }

        protected void writeColor(JsonWriter out, Color color) throws IOException {
            if (exportColors) {
                colorAdapter.write(out, color);
            }
        }

        protected void writeLabel(JsonWriter out, T element) throws IOException {
            if (element.getLabel() != null && !element.getLabel().isEmpty()) {
                out.name("label");
                out.value(element.getLabel());
            }
        }
    }

    private class NodeTypeAdapter extends ElementTypeAdapter<Node> {

        private final Set<String> reservedColumns = new HashSet<>();

        public NodeTypeAdapter(Gson gson) {
            super(gson);

            if (exportPosition) {
                reservedColumns.addAll(Arrays.asList("x", "y", "z"));
            }
            if (exportSize) {
                reservedColumns.add("size");
            }
            if (exportColors) {
                reservedColumns.add("color");
            }
        }

        @Override
        protected Set<String> getReservedKeys() {
            return reservedColumns;
        }

        @Override
        public void write(JsonWriter out, Node node) throws IOException {
            out.beginObject();
            out.name("key");
            out.value(node.getId().toString());
            writeAttributes(out, node);
            out.endObject();

            Progress.progress(progress);
        }

        private void writeAttributes(JsonWriter out, Node node) throws IOException {
            out.name("attributes");
            out.beginObject();

            // Label
            writeLabel(out, node);

            // Positions
            writePositions(out, node);

            // Size
            writeSize(out, node);

            // Colors
            writeColor(out, node.getColor());

            // Att values
            writeAttValues(out, node);

            out.endObject();
        }

        protected void writeSize(JsonWriter out, Node node) throws IOException {
            if (exportSize) {
                float size = normalization.normalizeSize(node.size());
                if (normalize || size != 0) {
                    out.name("size");
                    out.value(size);
                }
            }
        }

        private void writePositions(JsonWriter out, Node node) throws IOException {
            if (exportPosition) {
                float x = normalization.normalizeX(node.x());
                float y = normalization.normalizeY(node.y());
                float z = normalization.normalizeZ(node.z());
                if (normalize || !(x == 0 && y == 0 && z == 0)) {
                    out.name("x");
                    out.value(x);
                    out.name("y");
                    out.value(y);
                    if (normalization.minZ != 0 || normalization.maxZ != 0) {
                        out.name("z");
                        out.value(z);
                    }
                }
            }
        }
    }

    private class EdgeTypeAdapter extends ElementTypeAdapter<Edge> {

        private final Set<String> reservedColumns = new HashSet<>();

        public EdgeTypeAdapter(Gson gson) {
            super(gson);

            reservedColumns.add("type");
            if (exportColors) {
                reservedColumns.add("color");
            }
        }

        @Override
        protected Set<String> getReservedKeys() {
            return reservedColumns;
        }

        @Override
        public void write(JsonWriter out, Edge edge) throws IOException {
            out.beginObject();
            out.name("key");
            out.value(edge.getId().toString());
            out.name("source");
            out.value(edge.getSource().getId().toString());
            out.name("target");
            out.value(edge.getTarget().getId().toString());
            if (!edge.isDirected() && graph.isMixed()) {
                out.name("undirected");
                out.value(Boolean.TRUE);
            }
            writeAttributes(out, edge);
            out.endObject();

            Progress.progress(progress);
        }

        private void writeAttributes(JsonWriter out, Edge edge) throws IOException {
            out.name("attributes");
            out.beginObject();

            // Type/Kind
            if (edge.getType() != 0) {
                out.name("type");
                out.value(edge.getTypeLabel().toString());
            }

            // Label
            writeLabel(out, edge);

            // Colors
            if (edge.alpha() != 0) { //Edge has custom color
                writeColor(out, edge.getColor());
            }

            // Att values
            writeAttValues(out, edge);

            out.endObject();
        }
    }

    private class GraphTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (Node.class.isAssignableFrom(type.getRawType())) {
                return (TypeAdapter<T>) new NodeTypeAdapter(gson);
            } else if (Edge.class.isAssignableFrom(type.getRawType())) {
                return (TypeAdapter<T>) new EdgeTypeAdapter(gson);
            } else if (Graph.class.isAssignableFrom(type.getRawType())) {
                return (TypeAdapter<T>) new GraphTypeAdapter(gson);
            } else {
                return null;
            }
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

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public boolean isExportColors() {
        return exportColors;
    }

    public void setExportColors(boolean exportColors) {
        this.exportColors = exportColors;
    }

    public boolean isExportPosition() {
        return exportPosition;
    }

    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    public boolean isExportSize() {
        return exportSize;
    }

    public void setExportSize(boolean exportSize) {
        this.exportSize = exportSize;
    }

    public boolean isExportAttributes() {
        return exportAttributes;
    }

    public void setExportAttributes(boolean exportAttributes) {
        this.exportAttributes = exportAttributes;
    }

    public boolean isExportDynamic() {
        return exportDynamic;
    }

    public void setExportDynamic(boolean exportDynamic) {
        this.exportDynamic = exportDynamic;
    }

    public boolean isExportMeta() {
        return exportMeta;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public void setExportMeta(boolean exportMeta) {
        this.exportMeta = exportMeta;
    }

    /**
     * For convenience, the same as {@link TypeAdapter} but without the read support.
     *
     * @param <T> type
     */
    private abstract static class WriteTypeAdapter<T> extends TypeAdapter<T> {

        @Override
        public T read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
