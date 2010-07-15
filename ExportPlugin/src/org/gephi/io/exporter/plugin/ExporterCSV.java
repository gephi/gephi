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
    //Buffer
    private StringBuilder stringBuilder;

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
        stringBuilder = new StringBuilder();

        int max = graph.getNodeCount();

        Progress.start(progressTicket, max);

        if (!list) {
            if (header) {
                stringBuilder.append(SEPARATOR);

                for (Node n : graph.getNodes()) {
                    writeMatrixNode(n);
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append(EOL);
            }
        }

        if (list) {
            for (Node n : graph.getNodes()) {
                writeListNode(n);
                for (Edge e : graph.getEdges(n)) {
                    if (!e.isDirected() || (e.isDirected() && n == e.getSource())) {
                        Node m = graph.getOpposite(n, e);
                        writeListNode(m);
                    }
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append(EOL);
            }
        } else {
            if (graph instanceof DirectedGraph) {
                DirectedGraph directedGraph = (DirectedGraph) graph;
                for (Node n : graph.getNodes()) {
                    if (cancel) {
                        break;
                    }
                    writeMatrixNode(n);
                    for (Node m : graph.getNodes()) {
                        Edge e = directedGraph.getEdge(n, m);
                        writeEdge(e);
                    }
                    Progress.progress(progressTicket);
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    stringBuilder.append(EOL);
                }
            } else if (graph instanceof UndirectedGraph) {
                UndirectedGraph undirectedGraph = (UndirectedGraph) graph;
                for (Node n : graph.getNodes()) {
                    if (cancel) {
                        break;
                    }
                    writeMatrixNode(n);
                    for (Node m : graph.getNodes()) {
                        Edge e = undirectedGraph.getEdge(n, m);
                        writeEdge(e);
                    }
                    Progress.progress(progressTicket);
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    stringBuilder.append(EOL);
                }
            } else {
                MixedGraph mixedGraph = (MixedGraph) graph;
                for (Node n : graph.getNodes()) {
                    if (cancel) {
                        break;
                    }
                    writeMatrixNode(n);
                    for (Node m : graph.getNodes()) {
                        Edge e = mixedGraph.getEdge(n, m);
                        writeEdge(e);
                    }
                    Progress.progress(progressTicket);
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    stringBuilder.append(EOL);
                }
            }
        }

        if (!cancel) {
            writer.append(stringBuilder);
        }
        graph.readUnlockAll();

        Progress.finish(progressTicket);
    }

    private void writeEdge(Edge edge) {
        if (edge != null) {
            if (edgeWeight) {
                stringBuilder.append(Float.toString(edge.getWeight()));
            } else {
                stringBuilder.append(Float.toString(1f));
            }
            stringBuilder.append(SEPARATOR);
        } else {
            if (writeZero) {
                stringBuilder.append("0");
            }
            stringBuilder.append(SEPARATOR);
        }
    }

    private void writeMatrixNode(Node node) {
        if (header) {
            String label = node.getNodeData().getLabel();
            if (label == null) {
                label = node.getNodeData().getId();
            }
            stringBuilder.append(label);
            stringBuilder.append(SEPARATOR);
        }
    }

    private void writeListNode(Node node) {
        String label = node.getNodeData().getLabel();
        if (label == null) {
            label = node.getNodeData().getId();
        }
        stringBuilder.append(label);
        stringBuilder.append(SEPARATOR);
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
