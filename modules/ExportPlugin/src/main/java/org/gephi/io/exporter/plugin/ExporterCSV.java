/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
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

    @Override
    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();
        try {
            exportData(graph);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private void exportData(Graph graph) throws Exception {
        int max = graph.getNodeCount();

        Progress.start(progressTicket, max);

        if (!list) {
            if (header) {
                writer.append(SEPARATOR);
                int i = 0;
                NodeIterable itr = graph.getNodes();
                for (Node node : itr) {
                    writeMatrixNode(node, i++ < max - 1);
                    if (cancel) {
                        itr.doBreak();
                        return;
                    }
                }
                writer.append(EOL);
            }
        }

        if (list) {
            NodeIterable itr = graph.getNodes();
            for (Node n : itr) {
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

                if (cancel) {
                    itr.doBreak();
                    return;
                }
            }
        } else {
            Node[] nodes = graph.getNodes().toArray();
            for (Node n : nodes) {
                if (cancel) {
                    return;
                }
                writeMatrixNode(n, true);
                for (int j = 0; j < nodes.length; j++) {
                    Node m = nodes[j];
                    Edge e = graph.getEdge(n, m);
                    writeEdge(e, j < nodes.length - 1);
                }
                Progress.progress(progressTicket);
                writer.append(EOL);
            }
        }

        Progress.finish(progressTicket);
    }

    private void writeEdge(Edge edge, boolean writeSeparator) throws IOException {
        if (edge != null) {
            if (edgeWeight) {
                writer.append(Double.toString(edge.getWeight()));
            } else {
                writer.append(Double.toString(1.0));
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
            Object label = node.getId();
            writer.append(label.toString());
            if (writeSeparator) {
                writer.append(SEPARATOR);
            }
        }
    }

    private void writeListNode(Node node, boolean writeSeparator) throws IOException {
        Object label = node.getId();
        writer.append(label.toString());
        if (writeSeparator) {
            writer.append(SEPARATOR);
        }
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
}
