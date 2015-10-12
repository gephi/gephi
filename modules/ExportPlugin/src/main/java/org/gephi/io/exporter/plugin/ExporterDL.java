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
import org.gephi.graph.api.*;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

public class ExporterDL implements GraphExporter, CharacterExporter, LongTask {

    private boolean exportVisible = false;
    private Workspace workspace;
    private Writer writer;
    private boolean cancel = false;
    ProgressTicket progressTicket;
    private boolean useMatrixFormat = false;
    private boolean useListFormat = true;
    private boolean exportDynamicWeight = true;
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
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();

        Progress.start(progressTicket, graph.getNodeCount());

        try {
            //use labels only if every node has label and no two nodes have the same label
            boolean useLabels = true;
            NodeIterable nodeIterable = graph.getNodes();
            for (Node node : nodeIterable) {
                if (cancel) {
                    nodeIterable.doBreak();
                    break;
                }
                useLabels &= (node.getLabel() != null);
            }

            if (!cancel) {
                if (useListFormat) {
                    saveAsEdgeList1(useLabels, graph);
                } else {
                    saveAsFullMatrix(useLabels, graph);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ExporterDL.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
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

        HashMap<Object, String> idToLabel = new HashMap<Object, String>();//systemId to changed label
        HashSet<String> labelUsed = new HashSet<String>();
        //edgelist format forbids equal nodes
        if (useLabels) {
            for (Node node : graph.getNodes()) {
                if (labelUsed.contains(node.getLabel())) {
                    for (int i = 0;; i++) {
                        if (!labelUsed.contains(node.getLabel() + "_" + i)) {
                            idToLabel.put(node.getId(), node.getLabel() + "_" + i);
                            labelUsed.add(node.getLabel() + "_" + i);
                            break;
                        }
                    }
                } else {
                    idToLabel.put(node.getId(), node.getLabel());
                    labelUsed.add(node.getLabel());
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
            double weight;
            if (exportDynamicWeight) {
                weight = edge.getWeight(graph.getView());
            } else {
                weight = edge.getWeight();
            }

            if (useLabels) {
                writer.write(formatLabel(idToLabel.get(edge.getSource().getId()), false) + " "
                        + formatLabel(idToLabel.get(edge.getTarget().getId()), false) + " " + weight + "\n");
            } else {
                writer.write(formatLabel(edge.getSource().getId().toString(), false) + " "
                        + formatLabel(edge.getTarget().getId().toString(), false) + " " + weight + "\n");
            }

            if (!edge.isDirected()) {

                if (useLabels) {
                    writer.write(formatLabel(idToLabel.get(edge.getTarget().getId()), false) + " "
                            + formatLabel(idToLabel.get(edge.getSource().getId()), false) + " " + weight + "\n");
                } else {
                    writer.write(formatLabel(edge.getTarget().getId().toString(), false) + " "
                            + formatLabel(edge.getSource().getId().toString(), false) + " " + weight + "\n");
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
                double weight;
                if (exportDynamicWeight) {
                    weight = edge.getWeight(graph.getView());
                } else {
                    weight = edge.getWeight();
                }
                maxLengthOfEdgeWeight = Math.max(maxLengthOfEdgeWeight, Double.toString(weight).length());
            }
        }

        writer.write("labels:\n");
        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (i > 0) {
                writer.write(',');
            }

            if (useLabels) {
                writer.write(formatLabel(idToNode.get(i).getLabel(), true));
            } else {
                writer.write(formatLabel(idToNode.get(i).getId().toString(), true));
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
                Edge edge = graph.getEdge(source, target);
                if (edge != null) {
                    if (exportDynamicWeight) {
                        weight = edge.getWeight(graph.getView());
                    } else {
                        weight = edge.getWeight();
                    }
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
