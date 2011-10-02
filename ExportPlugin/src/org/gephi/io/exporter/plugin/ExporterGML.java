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
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
public class ExporterGML implements GraphExporter, CharacterExporter, LongTask {

    private boolean exportVisible = false;
    private Workspace workspace;
    private Writer writer;
    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //options
    private int spaces = 2;
    private int currentSpaces = 0;
    private boolean exportLabel = true;
    private boolean exportCoordinates = true;
    private boolean exportNodeSize = true;
    private boolean exportEdgeSize = true;
    private boolean exportColor = true;
    private boolean exportNotRecognizedElements = true;
    //data to normalize
    private boolean normalize = false;
    double minX, maxX;
    double minY, maxY;
    double minZ, maxZ;
    double minSize, maxSize;

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public boolean isExportVisible() {
        return exportVisible;
    }

    public boolean execute() {
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        graphModel = workspace.getLookup().lookup(GraphModel.class);
        DirectedGraph graph;
        if (exportVisible) {
            graph = graphModel.getDirectedGraph();
        } else {
            graph = graphModel.getDirectedGraph();
        }
        progressTicket.start(graph.getNodeCount() + graph.getEdgeCount());

        graph.readLock();

        if (normalize) {
            computeNormalizeValues(graph);
        }

        try {
            exportData(graph);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        progressTicket.finish();
        graph.readUnlock();
        return !cancel;
    }

    void printOpen(String s) throws IOException {
        for (int i = 0; i < currentSpaces; i++) {
            writer.write(' ');
        }
        writer.write(s + "\n");
        for (int i = 0; i < currentSpaces; i++) {
            writer.write(' ');
        }
        writer.write("[\n");

        currentSpaces += spaces;
    }

    void printClose() throws IOException {
        currentSpaces -= spaces;

        for (int i = 0; i < currentSpaces; i++) {
            writer.write(' ');
        }
        writer.write("]\n");

    }

    void printTag(String s) throws IOException {
        for (int i = 0; i < currentSpaces; i++) {
            writer.write(' ');
        }
        writer.write(s + "\n");
    }

    void exportData(DirectedGraph graph) throws IOException {
        printOpen("graph");
        printTag("Creator Gephi");
        for (Node node : graph.getNodes()) {
            if (cancel) {
                break;
            }
            printNode(node);
        }
        for (Edge edge : graph.getEdges()) {
            if (cancel) {
                break;
            }
            printEdge(edge);
        }
        printClose();
    }

    private void printEdge(Edge edge) throws IOException {
        printOpen("edge");
        printTag("id " + edge.getId());
        printTag("source " + edge.getSource().getId());
        printTag("target " + edge.getTarget().getId());
        if (exportLabel && edge.getEdgeData().getLabel() != null) {
            printTag("label \"" + edge.getEdgeData().getLabel() + "\"");
        }
        if (exportEdgeSize) {
            printTag("value " + edge.getWeight());
        }

        if (exportNotRecognizedElements) {
            for (int i = 0; i < edge.getAttributes().countValues(); i++) {
                String s = attributeModel.getEdgeTable().getColumn(i).getTitle();
                //don't print again standart attributes
                if (edge.getAttributes().getValue(i) != null && !s.equals("Weight") && !s.equals("Id")) {
                    printTag(formatString(s) + " \"" + edge.getAttributes().getValue(i) + "\"");
                }
            }
        }

        printClose();
        progressTicket.progress();
    }

    private void printNode(Node node) throws IOException {
        printOpen("node");
        printTag("id " + node.getId());
        if (exportLabel && node.getNodeData().getLabel() != null) {
            printTag("label \"" + node.getNodeData().getLabel() + "\"");
        }
        if (exportCoordinates || exportNodeSize || exportColor) {
            printOpen("graphics");
            if (exportCoordinates) {
                if (!normalize) {
                    printTag("x " + node.getNodeData().x());
                    printTag("y " + node.getNodeData().y());
                    printTag("z " + node.getNodeData().z());
                } else {
                    printTag("x " + (node.getNodeData().x() - minX) / (maxX - minX));
                    printTag("y " + (node.getNodeData().y() - minY) / (maxY - minY));
                    printTag("z " + (node.getNodeData().z() - minZ) / (maxZ - minZ));
                }
            }
            if (exportNodeSize) {
                if (!normalize) {
                    printTag("w " + node.getNodeData().getSize());
                    printTag("h " + node.getNodeData().getSize());
                    printTag("d " + node.getNodeData().getSize());
                } else {
                    printTag("w " + (node.getNodeData().getSize() - minSize) / (maxSize - minSize));
                    printTag("h " + (node.getNodeData().getSize() - minSize) / (maxSize - minSize));
                    printTag("d " + (node.getNodeData().getSize() - minSize) / (maxSize - minSize));
                }
            }
            if (exportColor) {
                printTag("fill \"#" + Integer.toString((int) (node.getNodeData().r() * 255), 16)
                        + Integer.toString((int) (node.getNodeData().g() * 255), 16) + Integer.toString((int) (node.getNodeData().b() * 255), 16) + "\"");
            }
            printClose();
        }
        if (exportNotRecognizedElements) {
            for (int i = 0; i < node.getAttributes().countValues(); i++) {
                String s = attributeModel.getNodeTable().getColumn(i).getTitle();
                //don't print again standart attributes
                if (node.getAttributes().getValue(i) != null && !s.equals("d") && !s.equals("Label") && !s.equals("Id")) {
                    printTag(formatString(s) + " \"" + node.getAttributes().getValue(i) + "\"");
                }
            }
        }
        printClose();
        progressTicket.progress();
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public boolean isExportColor() {
        return exportColor;
    }

    public void setExportColor(boolean exportColor) {
        this.exportColor = exportColor;
    }

    public boolean isExportCoordinates() {
        return exportCoordinates;
    }

    public void setExportCoordinates(boolean exportCoordinates) {
        this.exportCoordinates = exportCoordinates;
    }

    public boolean isExportEdgeSize() {
        return exportEdgeSize;
    }

    public void setExportEdgeSize(boolean exportEdgeSize) {
        this.exportEdgeSize = exportEdgeSize;
    }

    public boolean isExportLabel() {
        return exportLabel;
    }

    public void setExportLabel(boolean exportLabel) {
        this.exportLabel = exportLabel;
    }

    public boolean isExportNodeSize() {
        return exportNodeSize;
    }

    public void setExportNodeSize(boolean exportNodeSize) {
        this.exportNodeSize = exportNodeSize;
    }

    public boolean isExportNotRecognizedElements() {
        return exportNotRecognizedElements;
    }

    public void setExportNotRecognizedElements(boolean exportNotRecognizedElements) {
        this.exportNotRecognizedElements = exportNotRecognizedElements;
    }

    public int getSpaces() {
        return spaces;
    }

    public void setSpaces(int spaces) {
        this.spaces = spaces;
    }

    private void computeNormalizeValues(DirectedGraph graph) {
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        minZ = Double.MAX_VALUE;

        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        maxZ = Double.MIN_VALUE;

        minSize = Double.MAX_VALUE;
        maxSize = Double.MIN_VALUE;
        for (Node node : graph.getNodes()) {
            if (cancel) {
                break;
            }
            minX = Math.min(minX, node.getNodeData().x());
            minY = Math.min(minY, node.getNodeData().y());
            minZ = Math.min(minZ, node.getNodeData().z());

            maxX = Math.max(maxX, node.getNodeData().x());
            maxY = Math.max(maxY, node.getNodeData().y());
            maxZ = Math.max(maxZ, node.getNodeData().z());

            minSize = Math.min(minSize, node.getNodeData().getSize());
            maxSize = Math.max(maxSize, node.getNodeData().getSize());
        }
    }

    //returns string that can be normally readed by import gml tokenizer. removes '"[]# and space, adds "column" to beginning of string if starts with digit
    private String formatString(String s) {
        String res = s.replace("\"", "").replace("\'", "").replace("[", "").replace("]", "").replace(" ", "").replace("#", "");
        if (s.charAt(0) >= '0' && s.charAt(0) <= '9')
            return ("column" + res);
        else
            return (res);
    }
}
