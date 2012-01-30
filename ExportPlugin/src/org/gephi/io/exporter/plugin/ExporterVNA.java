/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.io.Writer;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 *
 * @author megaterik
 */
public class ExporterVNA implements GraphExporter, CharacterExporter, LongTask {

    private boolean exportVisible;
    private Workspace workspace;
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private TimeInterval visibleInterval;
    private boolean exportEdgeWeight = true;
    private boolean exportCoords = true;
    private boolean exportSize = true;
    private boolean exportShortLabel = true;
    private boolean exportColor = true;
    private Writer writer;

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public boolean isExportVisible() {
        return exportVisible;
    }

    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = null;
        if (exportVisible) {
            graph = graphModel.getGraphVisible();
        } else {
            graph = graphModel.getGraph();
        }
        graph.readLock();
        try {
            exportData(graph);
            writer.flush();
        } catch (Exception e) {
            graph.readUnlockAll();
            throw new RuntimeException(e);
        }
        graph.readUnlockAll();
        return !cancel;
    }

    private void exportData(Graph graph) throws IOException {
        exportNodeData(graph);
        if (exportCoords || exportColor || exportShortLabel || exportSize) {
            exportNodeProperties(graph);
        }
        exportEdgeData(graph);
    }

    private void exportNodeData(Graph graph) {
    }

    private void exportNodeProperties(Graph graph) throws IOException {
        //header
        writer.write("*Node properties\n");
        writer.write("ID");
        if (exportCoords) {
            writer.write(" X Y");
        }
        if (exportSize) {
            writer.write(" SIZE");
        }
        if (exportColor) {
            writer.write(" COLOR");
        }
        if (exportShortLabel) {
            writer.write(" SHORTLABEL");
        }
        writer.write("\n");

        //body
        for (NodeIterator nodeIterator = graph.getNodes().iterator(); nodeIterator.hasNext();) {
            Node node = nodeIterator.next();
            writer.write(node.getNodeData().getId());
            if (exportCoords) {
                writer.write(" " + node.getNodeData().x() + " " + node.getNodeData().y());
            }
            if (exportSize) {
                writer.write(" " + node.getNodeData().getRadius());
            }
            if (exportColor) {
                writer.write(" " + ((int) (node.getNodeData().r() * 255)));
            }
            if (exportShortLabel) {
                if (node.getNodeData().getLabel() == null) {
                    writer.write(" " + node.getNodeData().getId());
                } else {
                    writer.write(" " + node.getNodeData().getLabel());
                }
            }
            writer.write("\n");
        }
    }

    private void exportEdgeData(Graph graph) throws IOException {
        writer.write("*Tie data\n");
        writer.write("FROM TO");
        if (exportEdgeWeight) {
            writer.write(" STRENGTH");
        }
        writer.write("\n");

        for (EdgeIterator edgeIterator = graph.getEdges().iterator(); edgeIterator.hasNext();) {
            Edge edge = edgeIterator.next();

            writer.write(edge.getSource().getNodeData().getId());//from
            writer.write(" " + edge.getTarget().getNodeData().getId());//to
            if (exportEdgeWeight) {
                writer.write(" " + edge.getWeight());//strength
            }
            writer.write("\n");
        }
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }
}
