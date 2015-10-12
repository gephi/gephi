/*
 Copyright 2008-2011 Gephi
 Authors : Taras Klaskovsky <megaterik@gmail.com>
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

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author megaterik
 */
public class ExporterVNA implements GraphExporter, CharacterExporter, LongTask {

    private boolean exportVisible;
    private Workspace workspace;
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    //settings
    private boolean exportEdgeWeight = true;
    private boolean exportCoords = true;
    private boolean exportSize = true;
    private boolean exportShortLabel = true;
    private boolean exportColor = true;
    private boolean exportDynamicWeight = true;
    private boolean exportAttributes = true;
    private boolean normalize = false;
    private Writer writer;
    //normalization
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minSize;
    private double maxSize;
    private double getLow;//borders for dynamic edge weight
    private double getHigh;

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
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();

        //nodes are counted twice, because they are printed in exportNodeData and exportNodeProperties
        Progress.start(progressTicket, graph.getNodeCount() * 2 + graph.getEdgeCount());

        try {
            exportData(graph);
        } catch (Exception e) {
            Logger.getLogger(ExporterVNA.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private void exportData(Graph graph) throws IOException {
        if (normalize) {
            calculateMinMaxForNormalization(graph);
        }
        if (exportAttributes && atLeastOneNonStandartAttribute(graph.getModel())) {
            exportNodeData(graph);
        }
        exportNodeProperties(graph);
        exportEdgeData(graph);

        Progress.finish(progressTicket);
    }

    /*
     * For non-standart and string attributes
     */
    private String printParameter(Object val) {
        if (val == null) {
            return valueForEmptyAttributes;
        }
        String res = val.toString().replaceAll("\\r\\n|\\r|\\n", " ").replace('\"', ' ');
        if (res.contains(" ")) {
            return "\"" + res + "\"";
        } else {
            return res;
        }
    }

    private boolean atLeastOneNonStandartAttribute(GraphModel graphModel) {
        for (Column col : graphModel.getNodeTable()) {
            if (!col.isProperty()) {
                return true;
            }
        }
        return false;
    }
    /*
     * prints node data in format "id (attributes)*
     */

    private void exportNodeData(Graph graph) throws IOException {
        //header
        writer.append("*Node data\n");
        writer.append("ID");
        for (Column column : graph.getModel().getNodeTable()) {
            if (!column.isProperty()) {
                writer.append(" ").append(column.getTitle().replace(' ', '_'));
                //replace spaces because importer can't read attributes titles in quotes
            }
        }
        writer.append("\n");

        //body
        NodeIterable nodeIterable = graph.getNodes();
        for (Node node : nodeIterable) {
            writer.append(printParameter(node.getId()));
            for (Column column : node.getAttributeColumns()) {
                if (!column.isProperty()) {
                    Object value = node.getAttribute(column, graph.getView());
                    if (value != null) {
                        writer.append(" ").append(printParameter(value));
                    } else {
                        writer.append(" ").append(valueForEmptyAttributes);
                    }
                }
            }
            writer.append("\n");

            Progress.progress(progressTicket);
            if (cancel) {
                nodeIterable.doBreak();
                return;
            }
        }
    }
    static final String valueForEmptyAttributes = "\"\"";

    private void calculateMinMaxForNormalization(Graph graph) {
        minX = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;

        minY = Double.POSITIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;

        minSize = Double.POSITIVE_INFINITY;
        maxSize = Double.NEGATIVE_INFINITY;

        NodeIterable nodeIterable = graph.getNodes();
        for (Node node : nodeIterable) {
            if (cancel) {
                nodeIterable.doBreak();
                break;
            }
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());

            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());

            minSize = Math.min(minSize, node.r());
            maxSize = Math.max(maxSize, node.r());
        }
    }

    /*
     * prints node properties as "id (x)? (y)? (size)? (color)? (shortlabel)?"
     */
    private void exportNodeProperties(Graph graph) throws IOException {
        //header
        writer.append("*Node properties\n");
        writer.append("ID");
        if (exportCoords) {
            writer.append(" x y");
        }
        if (exportSize) {
            writer.append(" size");
        }
        if (exportColor) {
            writer.append(" color");
        }
        if (exportShortLabel) {
            writer.append(" shortlabel");
        }
        writer.append("\n");

        //body
        NodeIterable nodeIterable = graph.getNodes();
        for (Node node : nodeIterable) {
            Progress.progress(progressTicket);
            if (cancel) {
                nodeIterable.doBreak();
                return;
            }
            writer.append(node.getId().toString());
            if (exportCoords) {
                if (!normalize) {
                    writer.append(" ").append(Float.toString(node.x())).append(" ").append(Float.toString(node.y()));
                } else {
                    writer.append(" ").append(Double.toString((node.x() - minX) / (maxX - minX))).append(" ").append(Double.toString((node.y() - minY) / (maxY - minY)));
                }
            }
            if (exportSize) {
                if (!normalize) {
                    writer.append(" ").append(Float.toString(node.size()));
                } else {
                    writer.append(" ").append(Double.toString((node.size() - minSize) / (maxSize - minSize)));
                }
            }
            if (exportColor) {
                writer.append(" ").append(Integer.toString((int) (node.r() * 255f)));//[0..1] to [0..255]
            }
            if (exportShortLabel) {
                if (node.getLabel() != null) {
                    writer.append(" ").append(printParameter(node.getLabel()));
                } else {
                    writer.append(" ").append(printParameter(node.getId()));
                }
            }
            writer.append("\n");
        }
    }

    void printEdgeData(Edge edge, Node source, Node target, Graph graph) throws IOException {
        writer.append(printParameter(source.getId()));//from
        writer.append(" ").append(printParameter(target.getId()));//to
        if (exportEdgeWeight) {
            Double weight;
            if (exportDynamicWeight) {
                weight = edge.getWeight(graph.getView());
            } else {
                weight = edge.getWeight();
            }
            writer.append(" ").append(weight.toString());
        }

        if (exportAttributes) {
            for (Column column : edge.getAttributeColumns()) {
                if (!column.isProperty()) {
                    Object value = edge.getAttribute(column, graph.getView());
                    if (value != null) {
                        writer.append(" ").append(printParameter(value));
                    } else {
                        writer.append(" " + valueForEmptyAttributes);
                    }
                }
            }
        }
        writer.append("\n");
    }
    /*
     * prints edge data as "from to (strength)? (attributes)*"
     */

    private void exportEdgeData(Graph graph) throws IOException {
        writer.append("*Tie data\n");
        writer.append("from to");
        if (exportEdgeWeight) {
            writer.append(" strength");
        }
        if (exportAttributes) {
            for (Column col : graph.getModel().getEdgeTable()) {
                if (!col.isProperty()) {
                    writer.append(" ").append(printParameter(col.getTitle()).replace(' ', '_'));
                    //replace spaces because importer can't read attributes titles in quotes
                }
            }
        }
        writer.append("\n");

        EdgeIterable edgeIterable = graph.getEdges();
        for (Edge edge : edgeIterable) {
            printEdgeData(edge, edge.getSource(), edge.getTarget(), graph);//all edges in vna are directed, so make clone
            if (!edge.isDirected() && !edge.isSelfLoop()) {
                printEdgeData(edge, edge.getTarget(), edge.getSource(), graph);
            }
            Progress.progress(progressTicket);
            if (cancel) {
                edgeIterable.doBreak();
                return;
            }
        }
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
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
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
