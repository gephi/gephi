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
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.*;
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
    private double getLow;//time interval, used for getting edge weight in dynamic graphs
    private double getHigh;

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
        progressTicket.start();
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = null;
        if (exportVisible) {
            graph = graphModel.getGraphVisible();
        } else {
            graph = graphModel.getGraph();
        }
        graph.readLock();

        NodeIterable nodeIterable = graph.getNodes();

        //use labels only if every node has label and no two nodes have the same label
        boolean useLabels = true;
        while (nodeIterable.iterator().hasNext()) {
            if (cancel) {
                break;
            }
            useLabels &= (nodeIterable.iterator().next().getNodeData().getLabel() != null);
        }
        System.err.println("use labels " + useLabels);

        //find borders of the interval for edge.getWeight(low, high). If it's a static graph, then (-inf, inf)
        DynamicModel dynamicModel = workspace.getLookup().lookup(DynamicModel.class);
        TimeInterval visibleInterval = dynamicModel != null && exportVisible ? dynamicModel.getVisibleInterval() : new TimeInterval();
        getLow = Double.NEGATIVE_INFINITY;
        getHigh = Double.POSITIVE_INFINITY;
        if (visibleInterval != null) {
            getLow = visibleInterval.getLow();
            getHigh = visibleInterval.getHigh();
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

    private void saveAsEdgeList1(boolean useLabels, Graph graph) throws IOException {

        HashMap<Integer, String> idToLabel = new HashMap<Integer, String>();//systemId to changed label
        HashSet<String> labelUsed = new HashSet<String>();
        //edgelist format forbids equal nodes
        if (useLabels) {
            for (Node node : graph.getNodes()) {
                if (labelUsed.contains(node.getNodeData().getLabel())) {
                    for (int i = 0;; i++) {
                        if (!labelUsed.contains(node.getNodeData().getLabel() + "_" + i)) {
                            idToLabel.put(node.getId(), node.getNodeData().getLabel() + "_" + i);
                            labelUsed.add(node.getNodeData().getLabel() + "_" + i);
                            break;
                        }
                    }
                } else {
                    idToLabel.put(node.getId(), node.getNodeData().getLabel());
                    labelUsed.add(node.getNodeData().getLabel());
                }
            }
        }

        writer.write("dl\n");
        writer.write("format = edgelist1\n");
        writer.write("n = " + graph.getNodeCount() + "\n");
        EdgeIterable edgeIterator = graph.getEdges();
        writer.write("labels embedded:\n");
        writer.write("data:\n");
        while (edgeIterator.iterator().hasNext()) {
            if (cancel) {
                break;
            }
            Edge edge = edgeIterator.iterator().next();
            if (useLabels) {
                writer.write(formatLabel(idToLabel.get(edge.getSource().getId()), false) + " "
                        + formatLabel(idToLabel.get(edge.getTarget().getId()), false) + " " + edge.getWeight(getLow, getHigh) + "\n");
            } else {
                writer.write(formatLabel(edge.getSource().getNodeData().getId(), false) + " "
                        + formatLabel(edge.getTarget().getNodeData().getId(), false) + " " + edge.getWeight(getLow, getHigh) + "\n");
            }

            if (!edge.isDirected()) {
                if (useLabels) {
                    writer.write(formatLabel(idToLabel.get(edge.getTarget().getId()), false) + " "
                            + formatLabel(idToLabel.get(edge.getSource().getId()), false) + " " + edge.getWeight(getLow, getHigh) + "\n");
                } else {
                    writer.write(formatLabel(edge.getTarget().getNodeData().getId(), false) + " "
                            + formatLabel(edge.getSource().getNodeData().getId(), false) + " " + edge.getWeight(getLow, getHigh) + "\n");
                }
            }
        }
    }

    private String formatLabel(String input, boolean strictFormatting) {
        String res = input.replace(' ', '_');
        if (strictFormatting) {//for matrix view
            res = res.replace("\r\n", "-").replace('\n', '-').replace(',', '_');
        }
        return res;
    }

    private void saveAsFullMatrix(boolean useLabels, Graph graph) throws IOException {
        writer.write("dl\n");
        writer.write("format = fullmatrix\n");
        writer.write("n = " + graph.getNodeCount() + "\n");

        HashMap<Integer, Node> idToNode = new HashMap<Integer, Node>();
        int idForNode = 0;
        for (Node node : graph.getNodes()) {
            if (useLabels) {
                idToNode.put(idForNode++, node);
            } else {
                idToNode.put(idForNode++, node);
            }
        }
        int maxLengthOfEdgeWeight = 0;
        if (makeSymmetricMatrix) {
            for (Edge edge : graph.getEdges()) {
                maxLengthOfEdgeWeight = Math.max(maxLengthOfEdgeWeight, Double.toString(edge.getWeight(getLow, getHigh)).length());
            }
        }

        writer.write("labels:\n");
        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (i > 0) {
                writer.write(',');
            }

            if (useLabels) {
                writer.write(formatLabel(idToNode.get(i).getNodeData().getLabel(), true));
            } else {
                writer.write(formatLabel(idToNode.get(i).getNodeData().getId(), true));
            }
        }
        writer.write("\n");
        writer.write("data:\n");

        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (cancel) {
                break;
            }
            Node source = idToNode.get(i);
            for (int j = 0; j < graph.getNodeCount(); j++) {
                if (cancel) {
                    break;
                }
                Node target = idToNode.get(j);
                double weight = 0;
                if (graph.getEdge(source, target) != null) {
                    weight = graph.getEdge(source, target).getWeight(getLow, getHigh);
                } else if (graph.getEdge(target, source) != null && !graph.getEdge(target, source).isDirected()) {
                    weight = graph.getEdge(target, source).getWeight(getLow, getHigh);
                }
                writer.write(Double.toString(weight) + " ");
                if (makeSymmetricMatrix) {
                    for (int repeatSpace = Double.toString(weight).length(); repeatSpace < maxLengthOfEdgeWeight; repeatSpace++) {
                        writer.write(" ");
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