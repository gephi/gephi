/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

public class ExporterDL implements GraphExporter, CharacterExporter, LongTask {

    private boolean exportVisible = false;
    private Workspace workspace;
    private Writer writer;
    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private boolean cancel = false;
    ProgressTicket progressTicket;
    private boolean useMatrixFormat = false;
    private boolean useListFormat = true;
    private boolean makeSymmetricMatrix = false;

    public boolean isMakeSymmetricMatrix() {
        return makeSymmetricMatrix;
    }

    public void setMakeSymmetricMatrix(boolean makeSymmetricMatrix) {
        this.makeSymmetricMatrix = makeSymmetricMatrix;
    }

    public boolean isUseListFormat() {
        return useListFormat;
    }

    public void setUseListFormat(boolean useListFormat) {
        this.useListFormat = useListFormat;
    }

    public boolean isUseMatrixFormat() {
        return useMatrixFormat;
    }

    public void setUseMatrixFormat(boolean useMatrixFormat) {
        this.useMatrixFormat = useMatrixFormat;
    }

    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public boolean execute() {
        int max = 0;
        progressTicket.start();
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        graphModel = workspace.getLookup().lookup(GraphModel.class);
        DirectedGraph graph = null;
        if (exportVisible) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getDirectedGraph();
        }
        graph.readLock();

        NodeIterable nodeIterable = graph.getNodes();

        //use labels only if every node has label
        boolean useLabels = true;
        while (nodeIterable.iterator().hasNext()) {
            if (cancel) {
                break;
            }
            useLabels &= (nodeIterable.iterator().next().getNodeData().getLabel() != null);
        }

        if (!cancel) {
            try {
                if (useListFormat) {
                    saveAsEdgeList1(useLabels, graph);
                } else {
                    saveAsFullMatrix(useLabels, graph);
                }
            } catch (IOException ex) {
                Logger.getLogger(ExporterDL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        graph.readUnlock();
        progressTicket.finish();
        return true;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    private void saveAsEdgeList1(boolean useLabels, DirectedGraph graph) throws IOException {
        writer.write("dl\n");
        writer.write("format = edgelist1\n");
        writer.write("n = " + graph.getNodeCount() + "\n");
        EdgeIterable edgeIterator = graph.getEdges();
        writer.write("labels embedded:\n");
        writer.write("data:\n");
        while (edgeIterator.iterator().hasNext()) {
            if (cancel)
                break;
            Edge edge = edgeIterator.iterator().next();
            if (useLabels) {
                writer.write(edge.getSource().getNodeData().getLabel() + " "
                        + edge.getTarget().getNodeData().getLabel() + " " + edge.getWeight() + "\n");
            } else {
                writer.write(edge.getSource().getNodeData().getId() + " "
                        + edge.getTarget().getNodeData().getId() + " " + edge.getWeight() + "\n");
            }
        }
    }

    private void saveAsFullMatrix(boolean useLabels, DirectedGraph graph) throws IOException {
        writer.write("dl\n");
        writer.write("format = fullmatrix\n");
        writer.write("n = " + graph.getNodeCount() + "\n");
        int maxNumber = 0;
        NodeIterable nodeIterator = graph.getNodes();
        while (nodeIterator.iterator().hasNext()) {
            if (cancel)
                break;
            maxNumber = Math.max(maxNumber, nodeIterator.iterator().next().getId());
        }

        boolean[] isNodeHere = new boolean[maxNumber + 1];
        nodeIterator = graph.getNodes();
        while (nodeIterator.iterator().hasNext()) {
            if (cancel)
                break;
            isNodeHere[nodeIterator.iterator().next().getId()] = true;
        }

        int[] fullNumberOfNode = new int[graph.getNodeCount()];
        int c = 0;
        for (int i = 0; i < isNodeHere.length; i++) {
            if (cancel)
                break;
            if (isNodeHere[i]) {
                fullNumberOfNode[c++] = i;
            }
        }
        int maxLengthOfEdgeWeight = 0;
        if (makeSymmetricMatrix) {
            for (int i = 0; i < graph.getNodeCount(); i++) {
                if (cancel)
                    break;
                for (int j = 0; j < graph.getNodeCount(); j++) {
                    if (cancel)
                        break;
                    if (graph.getEdge(graph.getNode(fullNumberOfNode[i]), graph.getNode(fullNumberOfNode[j])) != null) {
                        maxLengthOfEdgeWeight = Math.max(Float.toString(graph.getEdge(graph.getNode(fullNumberOfNode[i]), graph.getNode(fullNumberOfNode[j])).getWeight()).length(), maxLengthOfEdgeWeight);
                    }
                }
            }
        }

        writer.write("labels:\n");
        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (cancel)
                break;
            if (useLabels) {
                writer.write(graph.getNode(fullNumberOfNode[i]).getNodeData().getLabel());
            } else {
                writer.write(graph.getNode(fullNumberOfNode[i]).getNodeData().getId());
            }
            if (i != graph.getNodeCount() - 1) {
                writer.write(",");
            }
        }
        writer.write("\n");
        writer.write("data:\n");
        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (cancel)
                break;
            for (int j = 0; j < graph.getNodeCount(); j++) {
                if (cancel)
                    break;
                if (graph.getEdge(graph.getNode(fullNumberOfNode[i]), graph.getNode(fullNumberOfNode[j])) == null) {
                    writer.write("0 ");
                    if (makeSymmetricMatrix) {
                        for (int repeatSpace = 2; repeatSpace < maxLengthOfEdgeWeight + 1; repeatSpace++) {
                            writer.write(" ");
                        }
                    }
                } else {
                    writer.write(Float.toString(graph.getEdge(graph.getNode(fullNumberOfNode[i]), graph.getNode(fullNumberOfNode[j])).getWeight()) + " ");
                    if (makeSymmetricMatrix) {
                        for (int repeatSpace = Float.toString(graph.getEdge(graph.getNode(fullNumberOfNode[i]), graph.getNode(fullNumberOfNode[j])).getWeight()).length() + 1; repeatSpace < maxLengthOfEdgeWeight + 1; repeatSpace++) {
                            writer.write(" ");
                        }
                    }
                }
            }
            writer.write("\n");
        }
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
