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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Mathieu Bastian
 */
public class ExporterCSV implements GraphExporter, CharacterExporter, LongTask {

    private static final String SEPARATOR = ";";
    private static final String EOL = "\n";
    //Settings
    private boolean edgeWeight = true;
    private boolean writeZero = true;
    private boolean header = true;
    private boolean list = false;
    //Architecture
    private Workspace workspace;
    private Writer writer;
    private boolean exportVisible;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = null;
        if (exportVisible) {
            graph = graphModel.getGraphVisible();
        } else {
            graph = graphModel.getGraph();
        }
        try {
            exportData(graph);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return !cancel;
    }

    private void exportData(Graph graph) throws Exception {
        int max = graph.getNodeCount();

        Progress.start(progressTicket, max);

        if (!list) {
            if (header) {
                writer.append(SEPARATOR);
                Node[] nodes = graph.getNodes().toArray();
                for (int i = 0; i < nodes.length; i++) {
                    writeMatrixNode(nodes[i], i < nodes.length - 1);
                }
                writer.append(EOL);
            }
        }

        if (list) {
            Node[] nodes = graph.getNodes().toArray();
            for (int i = 0; i < nodes.length; i++) {
                Node n = nodes[i];
                List<Node> neighbours = new ArrayList<Node>();
                for (Edge e : graph.getEdges(n)) {
                    if (!e.isDirected() || (e.isDirected() && n == e.getSource())) {
                        Node m = graph.getOpposite(n, e);
                        neighbours.add(m);
                    }
                }

                writeListNode(n, !neighbours.isEmpty());
                for (int j = 0; j < neighbours.size(); j++) {
                    writeListNode(neighbours.get(j), j < neighbours.size() - 1);

                }
                writer.append(EOL);
            }
        } else {
            if (graph instanceof DirectedGraph) {
                DirectedGraph directedGraph = (DirectedGraph) graph;
                Node[] nodes = graph.getNodes().toArray();
                for (Node n : nodes) {
                    if (cancel) {
                        break;
                    }
                    writeMatrixNode(n, true);
                    for (int j = 0; j < nodes.length; j++) {
                        Node m = nodes[j];
                        Edge e = directedGraph.getEdge(n, m);
                        writeEdge(e, j < nodes.length - 1);
                    }
                    Progress.progress(progressTicket);
                    writer.append(EOL);
                }
            } else if (graph instanceof UndirectedGraph) {
                UndirectedGraph undirectedGraph = (UndirectedGraph) graph;
                Node[] nodes = graph.getNodes().toArray();
                for (Node n : nodes) {
                    if (cancel) {
                        break;
                    }
                    writeMatrixNode(n, true);
                    for (int j = 0; j < nodes.length; j++) {
                        Node m = nodes[j];
                        Edge e = undirectedGraph.getEdge(n, m);
                        writeEdge(e, j < nodes.length - 1);
                    }
                    Progress.progress(progressTicket);
                    writer.append(EOL);
                }
            } else {
                MixedGraph mixedGraph = (MixedGraph) graph;
                Node[] nodes = graph.getNodes().toArray();
                for (Node n : graph.getNodes()) {
                    if (cancel) {
                        break;
                    }
                    writeMatrixNode(n, true);
                    for (int j = 0; j < nodes.length; j++) {
                        Node m = nodes[j];
                        Edge e = mixedGraph.getEdge(n, m);
                        writeEdge(e, j < nodes.length - 1);
                    }
                    Progress.progress(progressTicket);
                    writer.append(EOL);
                }
            }
        }

        graph.readUnlockAll();

        Progress.finish(progressTicket);
    }

    private void writeEdge(Edge edge, boolean writeSeparator) throws IOException {
        if (edge != null) {
            if (edgeWeight) {
                writer.append(Float.toString(edge.getWeight()));
            } else {
                writer.append(Float.toString(1f));
            }
            if (writeSeparator) {
                writer.append(SEPARATOR);
            }

        } else {
            if (writeZero) {
                writer.append("0");
            }
            if (writeSeparator) {
                writer.append(SEPARATOR);
            }
        }
    }

    private void writeMatrixNode(Node node, boolean writeSeparator) throws IOException {
        if (header) {
            String label = node.getNodeData().getLabel();
            if (label == null) {
                label = node.getNodeData().getId();
            }
            writer.append(label);
            if (writeSeparator) {
                writer.append(SEPARATOR);
            }
        }
    }

    private void writeListNode(Node node, boolean writeSeparator) throws IOException {
        String label = node.getNodeData().getLabel();
        if (label == null) {
            label = node.getNodeData().getId();
        }
        writer.append(label);
        if (writeSeparator) {
            writer.append(SEPARATOR);
        }
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public boolean isEdgeWeight() {
        return edgeWeight;
    }

    public void setEdgeWeight(boolean edgeWeight) {
        this.edgeWeight = edgeWeight;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isWriteZero() {
        return writeZero;
    }

    public void setWriteZero(boolean writeZero) {
        this.writeZero = writeZero;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
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
