/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.importer.standard;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.importer.FileType;
import org.gephi.io.importer.TextImporter;
import org.gephi.io.logging.Issue;
import org.gephi.io.logging.Report;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
public class ImporterGDF implements TextImporter, LongTask {

    //Architecture
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    //Extract
    private List<String> nodeLines = new ArrayList<String>();
    private List<String> edgeLines = new ArrayList<String>();

    //Matcher
    private String[] nodeLineStart;
    private String[] edgeLineStart;

    //Columns
    private GDFColumn[] nodeColumns;
    private GDFColumn[] edgeColumns;

    public ImporterGDF() {
        nodeLineStart = new String[]{"nodedef>name", "nodedef> name", "Nodedef>name", "Nodedef> name", "nodedef>\"name", "nodedef> \"name", "Nodedef>\"name", "Nodedef> \"name"};
        edgeLineStart = new String[]{"edgedef>", "Edgedef>"};
    }

    public void importData(BufferedReader reader, ContainerLoader container, Report report) throws Exception {
        this.container = container;
        this.report = report;

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
            NodeDraft node = container.factory().newNodeDraft();

            Matcher m = pattern.matcher(nodeLine);
            int count = 0;
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                if (start != end) {
                    String data = nodeLine.substring(start, end);
                    data = data.trim();
                    if (!data.isEmpty() && !data.toLowerCase().equals("null")) {
                        if (count == 0) {
                            //Id
                            node.setId(data);
                        } else if (count - 1 < nodeColumns.length) {
                            setNodeData(node, nodeColumns[count - 1], data);
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

            Matcher m = pattern.matcher(edgeLine);
            int count = 0;
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
                        } else if (count == 1) {
                            NodeDraft nodeTarget = container.getNode(data);
                            edge.setTarget(nodeTarget);
                        } else if (count - 2 < edgeColumns.length) {
                            setEdgeData(edge, edgeColumns[count - 2], data);
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
                    } else {
                        if (!edgesWalking) {
                            //Nodes
                            nodeLines.add(line);
                        } else {
                            //Edges
                            edgeLines.add(line);
                        }
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
            AttributeType type = AttributeType.STRING;
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
                type = AttributeType.STRING;
            } else if (typeString.equals("bool")) {
                type = AttributeType.BOOLEAN;
            } else if (typeString.equals("boolean")) {
                type = AttributeType.BOOLEAN;
            } else if (typeString.equals("integer")) {
                type = AttributeType.INT;
            } else if (typeString.equals("tinyint")) {
                type = AttributeType.INT;
            } else if (typeString.equals("int")) {
                type = AttributeType.INT;
            } else if (typeString.equals("double")) {
                type = AttributeType.DOUBLE;
            } else if (typeString.equals("float")) {
                type = AttributeType.FLOAT;
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
                AttributeClass nodeClass = container.getAttributeManager().getNodeClass();
                AttributeColumn newColumn = nodeClass.addAttributeColumn(columnName, type);
                nodeColumns[i - 1] = new GDFColumn(newColumn);
                report.log("Node attribute " + columnName + " (" + type.getTypeString() + ")");
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
            AttributeType type = AttributeType.STRING;
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
                type = AttributeType.STRING;
            } else if (typeString.equals("bool")) {
                type = AttributeType.BOOLEAN;
            } else if (typeString.equals("boolean")) {
                type = AttributeType.BOOLEAN;
            } else if (typeString.equals("integer")) {
                type = AttributeType.INT;
            } else if (typeString.equals("tinyint")) {
                type = AttributeType.INT;
            } else if (typeString.equals("int")) {
                type = AttributeType.INT;
            } else if (typeString.equals("double")) {
                type = AttributeType.DOUBLE;
            } else if (typeString.equals("float")) {
                type = AttributeType.FLOAT;
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
                AttributeClass edgeClass = container.getAttributeManager().getEdgeClass();
                AttributeColumn newColumn = edgeClass.addAttributeColumn(columnName, type);
                edgeColumns[i - 2] = new GDFColumn(newColumn);
                report.log("Edge attribute " + columnName + " (" + type.getTypeString() + ")");
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
                        String[] rgb = data.split(",");
                        if (rgb.length == 3) {
                            node.setColor(rgb[0], rgb[1], rgb[2]);
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
                    case VISIBLE:
                        node.setVisible(Boolean.parseBoolean(data));
                        break;
                }
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat3", column.getNodeColumn(), node, data);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        } else if (column.getAttributeColumn() != null) {
            try {
                node.addAttributeValue(column.getAttributeColumn(), data);
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat4", column.getAttributeColumn().getAttributeType(), column.getAttributeColumn().getTitle(), node);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        }
    }

    private void setEdgeData(EdgeDraft edge, GDFColumn column, String data) throws Exception {
        if (column.getEdgeColumn() != null) {
            try {
                switch (column.getEdgeColumn()) {
                    case COLOR:
                        String[] rgb = data.split(",");
                        if (rgb.length == 3) {
                            edge.setColor(rgb[0], rgb[1], rgb[2]);
                        }
                        break;
                    case VISIBLE:
                        edge.setVisible(Boolean.parseBoolean(data));
                        break;
                    case WEIGHT:
                        edge.setWeight(Float.parseFloat(data));
                        break;
                    case DIRECTED:
                        if (Boolean.parseBoolean(data)) {
                            edge.setType(EdgeDraft.EdgeType.DIRECTED);
                        } else {
                            edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
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
                edge.addAttributeValue(column.getAttributeColumn(), data);
            } catch (Exception e) {
                String message = NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat4", column.getAttributeColumn().getAttributeType(), column.getAttributeColumn().getTitle(), edge);
                report.logIssue(new Issue(message, Issue.Level.WARNING, e));
            }
        }
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

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
        private AttributeColumn column;
        private NodeGuessColumn nodeColumn;
        private EdgeGuessColumn edgeColumn;

        public GDFColumn(NodeGuessColumn column) {
            this.nodeColumn = column;
        }

        public GDFColumn(EdgeGuessColumn column) {
            this.edgeColumn = column;
        }

        public GDFColumn(AttributeColumn column) {
            this.column = column;
        }

        public NodeGuessColumn getNodeColumn() {
            return nodeColumn;
        }

        public EdgeGuessColumn getEdgeColumn() {
            return edgeColumn;
        }

        public AttributeColumn getAttributeColumn() {
            return column;
        }
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gdf", NbBundle.getMessage(getClass(), "fileType_GDF_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.hasExt("gdf");
    }
}
