/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.io.Writer;
import org.gephi.data.attributes.api.AttributeModel;
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
    private AttributeModel attributeModel;
    private boolean exportEdgeWeight = true;
    private boolean exportCoords = true;
    private boolean exportSize = true;
    private boolean exportShortLabel = true;
    private boolean exportColor = true;
    private boolean exportAttributes = true;
    private boolean normalize = false;
    private Writer writer;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minSize;
    private double maxSize;

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public boolean isExportVisible() {
        return exportVisible;
    }

    public boolean execute() {
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
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
        if (normalize) {
            calculateMinMaxForNormalization(graph);
        }
        if (exportAttributes) {
            exportNodeData(graph);
        }
        exportNodeProperties(graph);
        exportEdgeData(graph);
    }

    private void exportNodeData(Graph graph) throws IOException {
        //header
        writer.write("*Node data\n");
        writer.write("ID");
        for (int i = 0; i < attributeModel.getNodeTable().getColumns().length; i++) {
            if (!attributeModel.getNodeTable().getColumns()[i].getTitle().equalsIgnoreCase("id")
                    && !attributeModel.getNodeTable().getColumns()[i].getTitle().equalsIgnoreCase("label")) //ignore standart
            {
                writer.write(" " + attributeModel.getNodeTable().getColumn(i).getTitle().replace(' ','_'));
            }
        }
        writer.write("\n");

        //body
        for (NodeIterator nodeIterator = graph.getNodes().iterator(); nodeIterator.hasNext();) {
            Node node = nodeIterator.next();
            writer.write(node.getNodeData().getId());

            for (int i = 0; i < attributeModel.getNodeTable().getColumns().length; i++) {
                if (!attributeModel.getNodeTable().getColumns()[i].getTitle().equalsIgnoreCase("id")
                        && !attributeModel.getNodeTable().getColumns()[i].getTitle().equalsIgnoreCase("label")) //ignore standart
                {
                    if (node.getNodeData().getAttributes().getValue(i) != null) {
                        writer.write(" " + node.getNodeData().getAttributes().getValue(i));
                    } else {
                        writer.write(" " + valueForEmptyAttributes);
                    }
                }
            }
            writer.write("\n");
        }
    }
    static final String valueForEmptyAttributes = "zero";

    private void calculateMinMaxForNormalization(Graph graph) {
        minX = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;

        minY = Double.POSITIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;

        minSize = Double.POSITIVE_INFINITY;
        maxSize = Double.NEGATIVE_INFINITY;

        for (NodeIterator nodeIterator = graph.getNodes().iterator(); nodeIterator.hasNext();) {
            Node node = nodeIterator.next();
            minX = Math.min(minX, node.getNodeData().x());
            maxX = Math.max(maxX, node.getNodeData().x());

            minY = Math.min(minY, node.getNodeData().y());
            maxY = Math.max(maxY, node.getNodeData().y());

            minSize = Math.min(minSize, node.getNodeData().r());
            maxSize = Math.max(maxSize, node.getNodeData().r());
        }
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
                if (!normalize) {
                    writer.write(" " + node.getNodeData().x() + " " + node.getNodeData().y());
                } else {
                    writer.write(" " + (node.getNodeData().x() - minX) / (maxX - minX) + " "
                            + (node.getNodeData().y() - minY) / (maxY - minY));
                }
            }
            if (exportSize) {
                if (!normalize) {
                    writer.write(" " + node.getNodeData().getRadius());
                } else {
                    writer.write(" " + (node.getNodeData().getRadius() - minSize) / (maxSize - minSize));
                }
            }
            if (exportColor) {
                writer.write(" " + ((int) (node.getNodeData().r() * 255)));//[0..1] to [0..255]
            }
            if (exportShortLabel) {
                if (node.getNodeData().getLabel() != null)
                    writer.write(" " + node.getNodeData().getLabel());
                else
                    writer.write(" " + node.getNodeData().getId());
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
        if (exportAttributes) {
            for (int i = 0; i < attributeModel.getEdgeTable().getColumns().length; i++) {
                if (!attributeModel.getEdgeTable().getColumns()[i].getTitle().equalsIgnoreCase("Weight")
                        && !attributeModel.getEdgeTable().getColumns()[i].getTitle().equalsIgnoreCase("id")
                        && !attributeModel.getEdgeTable().getColumns()[i].getTitle().equalsIgnoreCase("label")) //ignore standart
                {
                    writer.write(" " + attributeModel.getEdgeTable().getColumn(i).getTitle());
                }
            }
        }
        writer.write("\n");

        for (EdgeIterator edgeIterator = graph.getEdges().iterator(); edgeIterator.hasNext();) {
            Edge edge = edgeIterator.next();

            writer.write(edge.getSource().getNodeData().getId());//from
            writer.write(" " + edge.getTarget().getNodeData().getId());//to
            if (exportEdgeWeight) {
                writer.write(" " + edge.getWeight());//strength
            }

            if (exportAttributes) {
                for (int i = 0; i < attributeModel.getEdgeTable().getColumns().length; i++) {
                    if (!attributeModel.getEdgeTable().getColumns()[i].getTitle().equalsIgnoreCase("Weight")
                            && !attributeModel.getEdgeTable().getColumns()[i].getTitle().equalsIgnoreCase("Label")
                            && !attributeModel.getEdgeTable().getColumns()[i].getTitle().equalsIgnoreCase("id")) //ignore standart
                    {
                        if (edge.getEdgeData().getAttributes().getValue(i) != null) {
                            writer.write(" " + edge.getEdgeData().getAttributes().getValue(i));
                        } else {
                            writer.write(" " + valueForEmptyAttributes);
                        }
                    }
                }
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

    public boolean isExportColor() {
        return exportColor;
    }

    public void setExportColor(boolean exportColor) {
        this.exportColor = exportColor;
    }

    public boolean isExportCoords() {
        return exportCoords;
    }

    public void setExportCoords(boolean exportCoords) {
        this.exportCoords = exportCoords;
    }

    public boolean isExportEdgeWeight() {
        return exportEdgeWeight;
    }

    public void setExportEdgeWeight(boolean exportEdgeWeight) {
        this.exportEdgeWeight = exportEdgeWeight;
    }

    public boolean isExportShortLabel() {
        return exportShortLabel;
    }

    public void setExportShortLabel(boolean exportShortLabel) {
        this.exportShortLabel = exportShortLabel;
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

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }
}
