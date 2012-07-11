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
import java.util.Arrays;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
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
    private TimeInterval visibleInterval;
    private AttributeModel attributeModel;
    private DynamicModel dynamicModel;
    //settings
    private boolean exportEdgeWeight = true;
    private boolean exportCoords = true;
    private boolean exportSize = true;
    private boolean exportShortLabel = true;
    private boolean exportColor = true;
    private boolean exportAttributes = true;
    private boolean normalize = false;
    private Writer writer;
    private StringBuilder stringBuilder;
    //normalization
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minSize;
    private double maxSize;
    private double getLow;//borders for dynamic edge weight
    private double getHigh;

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public boolean isExportVisible() {
        return exportVisible;
    }

    public boolean execute() {
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph;
        if (exportVisible) {
            graph = graphModel.getGraphVisible();
        } else {
            graph = graphModel.getGraph();
        }
        dynamicModel = workspace.getLookup().lookup(DynamicModel.class);
        visibleInterval = dynamicModel != null && exportVisible ? dynamicModel.getVisibleInterval() : new TimeInterval();

        getLow = Double.NEGATIVE_INFINITY;//whole interval, if graph is not dynamic
        getHigh = Double.POSITIVE_INFINITY;
        if (visibleInterval != null) {
            getLow = visibleInterval.getLow();
            getHigh = visibleInterval.getHigh();
        }

        graph.readLock();


        //nodes are counted twice, because they are printed in exportNodeData and exportNodeProperties
        progressTicket.start(graph.getNodeCount() * 2 + graph.getEdgeCount());

        stringBuilder = new StringBuilder();
        try {
            exportData(graph);
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
        if (exportAttributes && atLeastOneNonStandartAttribute()) {
            exportNodeData(graph);
        }
        exportNodeProperties(graph);
        exportEdgeData(graph);
        writer.write(stringBuilder.toString());
        writer.flush();
        progressTicket.finish();
    }

    /*
     * For non-standart and string attributes
     */
    private String printParameter(Object s) {
        Object val = DynamicUtilities.getDynamicValue(s, visibleInterval.getLow(), visibleInterval.getHigh());
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
    //sorted arrays
    static final private String[] standartNodeAttributes = {"Id", "Label", "Time Interval"};
    static final private String[] standartEdgeAttributes = {"Id", "Label", "Time Interval", "Weight"};

    private boolean atLeastOneNonStandartAttribute() {
        for (int i = 0; i < attributeModel.getNodeTable().getColumns().length; i++) {
            if (Arrays.binarySearch(standartNodeAttributes, attributeModel.getNodeTable().getColumn(i).getTitle()) < 0) {
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
        stringBuilder.append("*Node data\n");
        stringBuilder.append("ID");
        for (int i = 0; i < attributeModel.getNodeTable().getColumns().length; i++) {
            if (Arrays.binarySearch(standartNodeAttributes, attributeModel.getNodeTable().getColumn(i).getTitle()) < 0) //ignore standart
            {
                stringBuilder.append(" ").append(attributeModel.getNodeTable().getColumn(i).getTitle().replace(' ', '_').toString());
                //replace spaces because importer can't read attributes titles in quotes
            }
        }
        stringBuilder.append("\n");

        //body
        for (Node node : graph.getNodes()) {
            progressTicket.progress();
            if (cancel) {
                break;
            }
            stringBuilder.append(printParameter(node.getNodeData().getId()));

            for (int i = 0; i < attributeModel.getNodeTable().getColumns().length; i++) {
                if (Arrays.binarySearch(standartNodeAttributes, attributeModel.getNodeTable().getColumn(i).getTitle()) < 0) //ignore standart
                {
                    if (node.getNodeData().getAttributes().getValue(i) != null) {
                        stringBuilder.append(" ").append(printParameter(node.getNodeData().getAttributes().getValue(i)));
                    } else {
                        stringBuilder.append(" ").append(valueForEmptyAttributes);
                    }
                }
            }
            stringBuilder.append("\n");
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

        for (Node node : graph.getNodes()) {
            if (cancel) {
                break;
            }
            minX = Math.min(minX, node.getNodeData().x());
            maxX = Math.max(maxX, node.getNodeData().x());

            minY = Math.min(minY, node.getNodeData().y());
            maxY = Math.max(maxY, node.getNodeData().y());

            minSize = Math.min(minSize, node.getNodeData().r());
            maxSize = Math.max(maxSize, node.getNodeData().r());
        }
    }

    /*
     * prints node properties as "id (x)? (y)? (size)? (color)? (shortlabel)?"
     */
    private void exportNodeProperties(Graph graph) throws IOException {
        //header
        stringBuilder.append("*Node properties\n");
        stringBuilder.append("ID");
        if (exportCoords) {
            stringBuilder.append(" x y");
        }
        if (exportSize) {
            stringBuilder.append(" size");
        }
        if (exportColor) {
            stringBuilder.append(" color");
        }
        if (exportShortLabel) {
            stringBuilder.append(" shortlabel");
        }
        stringBuilder.append("\n");

        //body
        for (Node node : graph.getNodes()) {
            progressTicket.progress();
            if (cancel) {
                break;
            }
            stringBuilder.append(node.getNodeData().getId());
            if (exportCoords) {
                if (!normalize) {
                    stringBuilder.append(" ").append(node.getNodeData().x()).append(" ").append(node.getNodeData().y());
                } else {
                    stringBuilder.append(" ").append((node.getNodeData().x() - minX) / (maxX - minX)).append(" ").append((node.getNodeData().y() - minY) / (maxY - minY));
                }
            }
            if (exportSize) {
                if (!normalize) {
                    stringBuilder.append(" ").append(node.getNodeData().getRadius());
                } else {
                    stringBuilder.append(" ").append((node.getNodeData().getRadius() - minSize) / (maxSize - minSize));
                }
            }
            if (exportColor) {
                stringBuilder.append(" ").append((int) (node.getNodeData().r() * 255f));//[0..1] to [0..255]
            }
            if (exportShortLabel) {
                if (node.getNodeData().getLabel() != null) {
                    stringBuilder.append(" ").append(printParameter(node.getNodeData().getLabel()));
                } else {
                    stringBuilder.append(" ").append(printParameter(node.getNodeData().getId()));
                }
            }
            stringBuilder.append("\n");
        }
    }

    void printEdgeData(Edge edge, Node source, Node target) {
        stringBuilder.append(printParameter(source.getNodeData().getId()));//from
        stringBuilder.append(" ").append(printParameter(target.getNodeData().getId()));//to
        if (exportEdgeWeight) {
            stringBuilder.append(" ").append(edge.getWeight(getLow, getHigh));//strength
        }

        if (exportAttributes) {
            for (int i = 0; i < attributeModel.getEdgeTable().getColumns().length; i++) {
                if (Arrays.binarySearch(standartEdgeAttributes, attributeModel.getEdgeTable().getColumn(i).getTitle()) < 0) //ignore standart
                {
                    if (edge.getEdgeData().getAttributes().getValue(i) != null) {
                        stringBuilder.append(" ").append(printParameter(edge.getEdgeData().getAttributes().getValue(i)));
                    } else {
                        stringBuilder.append(" " + valueForEmptyAttributes);
                    }
                }
            }
        }
        stringBuilder.append("\n");
    }
    /*
     * prints edge data as "from to (strength)? (attributes)*"
     */

    private void exportEdgeData(Graph graph) throws IOException {
        stringBuilder.append("*Tie data\n");
        stringBuilder.append("from to");
        if (exportEdgeWeight) {
            stringBuilder.append(" strength");
        }
        if (exportAttributes) {
            for (int i = 0; i < attributeModel.getEdgeTable().getColumns().length; i++) {
                if (Arrays.binarySearch(standartEdgeAttributes, attributeModel.getEdgeTable().getColumn(i).getTitle()) < 0) //ignore standart
                {
                    stringBuilder.append(" ").append(printParameter(attributeModel.getEdgeTable().getColumn(i).getTitle()).replace(' ', '_'));
                    //replace spaces because importer can't read attributes titles in quotes
                }
            }
        }
        stringBuilder.append("\n");

        for (Edge edge : graph.getEdges()) {
            if (cancel) {
                break;
            }
            progressTicket.progress();
            printEdgeData(edge, edge.getSource(), edge.getTarget());//all edges in vna are directed, so make clone
            if (!edge.isDirected() && !edge.isSelfLoop()) {
                printEdgeData(edge, edge.getTarget(), edge.getSource());
            }
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
