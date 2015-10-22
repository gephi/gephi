/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>,
 Sebastien Heymann <sebastien.heymann@gephi.org>
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
package org.gephi.io.importer.plugin.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
public class ImporterGDF implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Extract
    private List<String> nodeLines = new ArrayList<String>();
    private List<String> edgeLines = new ArrayList<String>();
    //Matcher
    private final String[] nodeLineStart;
    private final String[] edgeLineStart;
    //Columns
    private GDFColumn[] nodeColumns;
    private GDFColumn[] edgeColumns;

    public ImporterGDF() {
        nodeLineStart = new String[]{"nodedef>name", "nodedef> name", "Nodedef>name", "Nodedef> name", "nodedef>\"name", "nodedef> \"name", "Nodedef>\"name", "Nodedef> \"name"};
        edgeLineStart = new String[]{"edgedef>", "Edgedef>"};
    }

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        LineNumberReader lineReader = ImportUtils.getTextReader(reader);
        try {
            importData(lineReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                lineReader.close();
            } catch (IOException ex) {
            }
        }
        return !cancel;
    }

    private void importData(LineNumberReader reader) throws Exception {
        Progress.start(progressTicket);        //Progress

        //Verify a node line exists and puts nodes and edges lines in arrays
        walkFile(reader);

        Progress.switchToDeterminate(progressTicket, nodeLines.size() + edgeLines.size());         //Progress

        //Magix regex
        Pattern pattern = Pattern.compile("(?<=(?:,|^)\")(.*?)(?=(?<=(?:[^\\\\]))\",|\"$)|(?<=(?:,|^)')(.*?)(?=(?<=(?:[^\\\\]))',|'$)|(?<=(?:,|^))(?=[^'\"])(.*?)(?=(?:,|$))|(?<=,)($)");

        //Nodes
        for (String nodeLine : nodeLines) {
            if (cancel) {
                return;
            }

            //Create Node
            NodeDraft node = null;

            Matcher m = pattern.matcher(nodeLine);
            int count = 0;
            String id = "";
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                if (start != end) {
                    String data = nodeLine.substring(start, end);
                    data = data.trim();
                    if (!data.isEmpty() && !data.toLowerCase().equals("null")) {
                        if (count == 0) {
                            //Id
                            id = data;
                            if (node == null) {
                                node = container.factory().newNodeDraft(id);
                            }
                        } else if (count - 1 < nodeColumns.length) {
                            if (nodeColumns[count - 1] != null) {
                                setNodeData(node, nodeColumns[count - 1], data);
                            }
                        } else {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat7", id), Issue.Level.SEVERE));
                        }
                    }
                }
                count++;
            }

            container.addNode(node);

            Progress.progress(progressTicket);      //Progress
        }

        //Edges
        for (String edgeLine : edgeLines) {
            if (cancel) {
                return;
            }
            //Create Edge
            EdgeDraft edge = container.factory().newEdgeDraft();

            //Default to undirected unless stated
            edge.setDirection(EdgeDirection.UNDIRECTED);

            Matcher m = pattern.matcher(edgeLine);
            int count = 0;
            String id = "";
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                if (start != end) {
                    String data = edgeLine.substring(start, end);
                    data = data.trim();
                    if (!data.isEmpty() && !data.toLowerCase().equals("null")) {
                        if (count == 0) {
                            NodeDraft nodeSource = container.getNode(data);
                            edge.setSource(nodeSource);
                            id = data;
                        } else if (count == 1) {
                            NodeDraft nodeTarget = container.getNode(data);
                            edge.setTarget(nodeTarget);
                            id += "," + data;
                        } else if (count - 2 < edgeColumns.length) {
                            if (edgeColumns[count - 2] != null) {
                                setEdgeData(edge, edgeColumns[count - 2], data);
                            }
                        } else {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat7", id), Issue.Level.SEVERE));
                        }
                    }
                }
                count++;
            }

            container.addEdge(edge);
            Progress.progress(progressTicket);      //Progress
        }
    }

    private void walkFile(BufferedReader reader) throws Exception {
        if (reader.ready()) {
            String firstLine = reader.readLine();
            if (isNodeFirstLine(firstLine)) {
                findNodeColumns(firstLine);
                boolean edgesWalking = false;
                while (reader.ready() && !cancel) {
                    String line = reader.readLine();
                    if (isEdgeFirstLine(line)) {
                        edgesWalking = true;
                        findEdgeColumns(line);
                    } else if (!edgesWalking) {
                        //Nodes
                        nodeLines.add(line);
                    } else {
                        //Edges
                        edgeLines.add(line);
                    }
                }
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat1"), Issue.Level.CRITICAL));
            }
        } else {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat1"), Issue.Level.CRITICAL));
        }
    }

    private void findNodeColumns(String line) throws Exception {
        String[] columns = line.split(",");
        nodeColumns = new GDFColumn[columns.length - 1];

        for (int i = 1; i < columns.length; i++) {
            String columnString = columns[i];
            String typeString = "";
            String columnName = "";
            Class type = String.class;
            try {
                typeString = columnString.substring(columnString.lastIndexOf(" ")).trim().toLowerCase();
            } catch (IndexOutOfBoundsException e) {
            }
            try {
                int end = columnString.lastIndexOf(" ");
                if (end != -1) {
                    columnName = columnString.substring(0, end).trim().toLowerCase();
                } else {
                    columnName = columnString.trim().toLowerCase();
                }
            } catch (IndexOutOfBoundsException e) {
            }

            //Check error
            if (columnName.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat2"), Issue.Level.SEVERE));
                columnName = "default" + i;
            }
            if (typeString.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat6", columnName), Issue.Level.INFO));
                typeString = "varchar";
            }

            //Clean parenthesis
            typeString = typeString.replaceAll("\\([0-9]*\\)", "");

            if (typeString.equals("varchar")) {
                type = String.class;
            } else if (typeString.equals("bool")) {
                type = Boolean.class;
            } else if (typeString.equals("boolean")) {
                type = Boolean.class;
            } else if (typeString.equals("integer")) {
                type = Integer.class;
            } else if (typeString.equals("tinyint")) {
                type = Integer.class;
            } else if (typeString.equals("int")) {
                type = Integer.class;
            } else if (typeString.equals("double")) {
                type = Double.class;
            } else if (typeString.equals("float")) {
                type = Float.class;
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat5", typeString), Issue.Level.WARNING));
            }

            if (columnName.equals("x")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.X);
                report.log("Node property found: x");
            } else if (columnName.equals("y")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.Y);
                report.log("Node property found: y");
            } else if (columnName.equals("visible")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.VISIBLE);
                report.log("Node property found: visible");
            } else if (columnName.equals("color")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.COLOR);
                report.log("Node property found: color");
            } else if (columnName.equals("fixed")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.FIXED);
                report.log("Node property found: fixed");
            } else if (columnName.equals("style")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.STYLE);
                report.log("Node property found: style");
            } else if (columnName.equals("width")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.WIDTH);
                report.log("Node property found: width");
            } else if (columnName.equals("height")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.HEIGHT);
                report.log("Node property found: height");
            } else if (columnName.equals("label")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.LABEL);
                report.log("Node property found: label");
            } else if (columnName.equals("labelvisible")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.LABELVISIBLE);
                report.log("Node property found: labelvisible");
            } else {
                ColumnDraft column = container.getNodeColumn(columnName);
                if (column == null) {
                    column = container.addNodeColumn(columnName, type);
                    nodeColumns[i - 1] = new GDFColumn(column);
                    report.log("Node attribute " + columnName + " (" + type.getSimpleName() + ")");
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat8", columnName), Issue.Level.SEVERE));
                }
            }
        }
    }

    private void findEdgeColumns(String line) throws Exception {
        String[] columns = line.split(",");
        edgeColumns = new GDFColumn[columns.length - 2];

        for (int i = 2; i < columns.length; i++) {
            String columnString = columns[i];
            String typeString = "";
            String columnName = "";
            Class type = String.class;
            try {
                typeString = columnString.substring(columnString.lastIndexOf(" ")).trim().toLowerCase();
            } catch (IndexOutOfBoundsException e) {
            }
            try {
                int end = columnString.lastIndexOf(" ");
                if (end != -1) {
                    columnName = columnString.substring(0, end).trim().toLowerCase();
                } else {
                    columnName = columnString.trim().toLowerCase();
                }
            } catch (IndexOutOfBoundsException e) {
            }

            //Check error
            if (columnName.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat2"), Issue.Level.SEVERE));
                columnName = "default" + i;
            }
            if (typeString.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat6", columnName), Issue.Level.INFO));
                typeString = "varchar";
            }

            //Clean parenthesis
            typeString = typeString.replaceAll("\\([0-9]*\\)", "");

            if (typeString.equals("varchar")) {
                type = String.class;
            } else if (typeString.equals("bool")) {
                type = Boolean.class;
            } else if (typeString.equals("boolean")) {
                type = Boolean.class;
            } else if (typeString.equals("integer")) {
                type = Integer.class;
            } else if (typeString.equals("tinyint")) {
                type = Integer.class;
            } else if (typeString.equals("int")) {
                type = Integer.class;
            } else if (typeString.equals("double")) {
                type = Double.class;
            } else if (typeString.equals("float")) {
                type = Float.class;
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat5", typeString), Issue.Level.WARNING));
            }

            if (columnName.equals("color")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.COLOR);
                report.log("Edge property found: color");
            } else if (columnName.equals("visible")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.VISIBLE);
                report.log("Edge property found: visible");
            } else if (columnName.equals("weight")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.WEIGHT);
                report.log("Edge property found: weight");
            } else if (columnName.equals("directed")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.DIRECTED);
                report.log("Edge property found: directed");
            } else if (columnName.equals("label")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.LABEL);
                report.log("Edge property found: label");
            } else if (columnName.equals("labelvisible")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.LABELVISIBLE);
                report.log("Edge property found: labelvisible");
            } else {
                ColumnDraft column = container.getEdgeColumn(columnName);
                if (column == null) {
                    column = container.addEdgeColumn(columnName, type);
                    edgeColumns[i - 2] = new GDFColumn(column);
                    report.log("Edge attribute " + columnName + " (" + type.getSimpleName() + ")");
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat9", columnName), Issue.Level.SEVERE));
                }
            }
        }
    }

    private boolean isNodeFirstLine(String line) {
        for (String s : nodeLineStart) {
            if (line.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEdgeFirstLine(String line) {
        for (String s : edgeLineStart) {
            if (line.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private void setNodeData(NodeDraft node, GDFColumn column, String data) throws Exception {
        if (column.getNodeColumn() != null) {
            try {
                switch (column.getNodeColumn()) {
                    case X:
                        node.setX(Float.parseFloat(data));
                        break;
                    case Y:
                        node.setY(Float.parseFloat(data));
                        break;
                    case COLOR:
                        String[] rgb = data.replace(" ", "").split(",");
                        if (rgb.length == 3) {
                            node.setColor(rgb[0], rgb[1], rgb[2]);
                        } else {
                            node.setColor(data);
                        }
                        break;
                    case FIXED:
                        node.setFixed(Boolean.parseBoolean(data));
                        break;
                    case HEIGHT:
                        break;
                    case WIDTH:
                        node.setSize(Float.parseFloat(data));
                        break;
                    case LABEL:
                        node.setLabel(data);
                        break;
                    case LABELVISIBLE:
                        node.setLabelVisible(Boolean.parseBoolean(data));
                        break;
                }
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat3", column.getNodeColumn(), node, data);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        } else if (column.getAttributeColumn() != null) {
            try {
                node.parseAndSetValue(column.getAttributeColumn().getId(), data);
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat4", column.getAttributeColumn().getTypeClass().getSimpleName(), column.getAttributeColumn().getTitle(), node);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        }
    }

    private void setEdgeData(EdgeDraft edge, GDFColumn column, String data) throws Exception {
        if (column.getEdgeColumn() != null) {
            try {
                switch (column.getEdgeColumn()) {
                    case COLOR:
                        String[] rgb = data.replace(" ", "").split(",");
                        if (rgb.length == 3) {
                            edge.setColor(rgb[0], rgb[1], rgb[2]);
                        } else {
                            edge.setColor(data);
                        }
                        break;
                    case WEIGHT:
                        edge.setWeight(Float.parseFloat(data));
                        break;
                    case DIRECTED:
                        if (Boolean.parseBoolean(data)) {
                            edge.setDirection(EdgeDirection.DIRECTED);
                        } else {
                            edge.setDirection(EdgeDirection.UNDIRECTED);
                        }
                        break;
                    case LABEL:
                        edge.setLabel(data);
                        break;
                    case LABELVISIBLE:
                        edge.setLabelVisible(Boolean.parseBoolean(data));
                        break;
                }
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat3", column.getEdgeColumn(), data);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        } else if (column.getAttributeColumn() != null) {
            try {
                edge.parseAndSetValue(column.getAttributeColumn().getId(), data);
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat4", column.getAttributeColumn().getTypeClass().getSimpleName(), column.getAttributeColumn().getTitle(), edge);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        }
    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
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

    private static class GDFColumn {

        public enum NodeGuessColumn {

            X, Y, VISIBLE, FIXED, STYLE, COLOR, WIDTH, HEIGHT, LABEL, LABELVISIBLE
        };

        public enum EdgeGuessColumn {

            VISIBLE, COLOR, WEIGHT, DIRECTED, LABEL, LABELVISIBLE
        };
        private ColumnDraft column;
        private NodeGuessColumn nodeColumn;
        private EdgeGuessColumn edgeColumn;

        public GDFColumn(NodeGuessColumn column) {
            this.nodeColumn = column;
        }

        public GDFColumn(EdgeGuessColumn column) {
            this.edgeColumn = column;
        }

        public GDFColumn(ColumnDraft column) {
            this.column = column;
        }

        public NodeGuessColumn getNodeColumn() {
            return nodeColumn;
        }

        public EdgeGuessColumn getEdgeColumn() {
            return edgeColumn;
        }

        public ColumnDraft getAttributeColumn() {
            return column;
        }
    }
}
