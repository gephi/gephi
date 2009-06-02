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
package org.gephi.importer.standard;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.importer.api.FileType;
import org.gephi.importer.api.ImportContainer;
import org.gephi.importer.api.ImportException;
import org.gephi.importer.api.EdgeDraft;
import org.gephi.importer.api.NodeDraft;
import org.gephi.importer.api.TextImporter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
public class ImporterGDF implements TextImporter {

    //Container
    ImportContainer container;

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
        nodeLineStart = new String[]{"nodedef>name", "nodedef> name", "Nodedef>name", "Nodedef> name"};
        edgeLineStart = new String[]{"edgedef>", "Edgedef>"};
    }

    public void importData(BufferedReader reader, ImportContainer container) throws ImportException {
        this.container = container;
        try {

            //Verify a node line exists and puts nodes and edges lines in arrays
            walkFile(reader);

            //Magix regex
            Pattern pattern = Pattern.compile("(?<=(?:,|^)\")(.*?)(?=(?<=(?:[^\\\\]))\",|\"$)|(?<=(?:,|^)')(.*?)(?=(?<=(?:[^\\\\]))',|'$)|(?<=(?:,|^))(?=[^'\"])(.*?)(?=(?:,|$))|(?<=,)($)");

            //Nodes
            for (String nodeLine : nodeLines) {

                //Create Node
                NodeDraft node = container.newNodeDraft();

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
                            } else if (count < nodeColumns.length) {
                                setNodeData(node, nodeColumns[count-1], data);
                            }
                        }
                    }
                    count++;
                }

                container.addNode(node);
            }

            //Edges
            for (String edgeLine : edgeLines) {

                //Create Edge
                EdgeDraft edge = container.newEdgeDraft();

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
                                edge.setNodeSource(nodeSource);
                            } else if (count == 1) {
                                NodeDraft nodeTarget = container.getNode(data);
                                edge.setNodeTarget(nodeTarget);
                            } else if (count < edgeColumns.length) {
                                setEdgeData(edge, edgeColumns[count-2], data);
                            }
                        }
                    }
                    count++;
                }

                container.addEdge(edge);
            }

        } catch (Exception ex) {
            if (ex instanceof ImportException) {
                throw (ImportException) ex;
            } else {
                throw new ImportException(this, ex);
            }
        }

    }

    private void walkFile(BufferedReader reader) throws Exception {
        if (reader.ready()) {
            String firstLine = reader.readLine();
            if (isNodeFirstLine(firstLine)) {
                findNodeColumns(firstLine);
                boolean edgesWalking = false;
                while (reader.ready()) {
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
                throw new ImportException(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat1"));
            }
        } else {
            throw new ImportException(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat1"));
        }
    }

    private void findNodeColumns(String line) throws ImportException {
        String[] columns = line.split(",");
        nodeColumns = new GDFColumn[columns.length - 1];

        for (int i = 1; i < columns.length; i++) {
            String columnString = columns[i];
            String typeString;
            String columnName;
            AttributeType type = AttributeType.STRING;
            try {
                typeString = columnString.substring(columnString.lastIndexOf(" ")).toLowerCase();
                columnName = columnString.substring(0, columnString.lastIndexOf(" "));
            } catch (IndexOutOfBoundsException e) {
                throw new ImportException(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat2"));
            }

            if (typeString.isEmpty() || columnName.isEmpty()) {
                throw new ImportException(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat2"));
            }

            if (typeString.equals("varchar")) {
                type = AttributeType.STRING;
            } else if (typeString.equals("bool")) {
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
            }

            if (columnName.equals("x")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.X);
            } else if (columnName.equals("y")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.Y);
            } else if (columnName.equals("visible")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.VISIBLE);
            } else if (columnName.equals("color")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.COLOR);
            } else if (columnName.equals("fixed")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.FIXED);
            } else if (columnName.equals("style")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.STYLE);
            } else if (columnName.equals("width")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.WIDTH);
            } else if (columnName.equals("height")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.HEIGHT);
            } else if (columnName.equals("label")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.LABEL);
            } else if (columnName.equals("labelvisible")) {
                nodeColumns[i - 1] = new GDFColumn(GDFColumn.NodeGuessColumn.LABELVISIBLE);
            } else {
                AttributeClass nodeClass = container.getAttributeManager().getNodeClass();
                AttributeColumn newColumn = nodeClass.addAttributeColumn(columnName, type);
                nodeColumns[i - 1] = new GDFColumn(newColumn);
            }

        }
    }

    private void findEdgeColumns(String line) throws ImportException {
        String[] columns = line.split(",");
        edgeColumns = new GDFColumn[columns.length - 2];

        for (int i = 2; i < columns.length; i++) {
            String columnString = columns[i];
            String typeString;
            String columnName;
            AttributeType type = AttributeType.STRING;
            try {
                typeString = columnString.substring(columnString.lastIndexOf(" ")).toLowerCase();
                columnName = columnString.substring(0, columnString.lastIndexOf(" "));
            } catch (IndexOutOfBoundsException e) {
                throw new ImportException(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat2"));
            }

            if (typeString.isEmpty() || columnName.isEmpty()) {
                throw new ImportException(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat2"));
            }

            if (typeString.equals("varchar")) {
                type = AttributeType.STRING;
            } else if (typeString.equals("bool")) {
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
            }

            if (columnName.equals("color")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.COLOR);
            } else if (columnName.equals("fixed")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.DIRECTED);
            } else if (columnName.equals("style")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.WEIGHT);
            } else if (columnName.equals("width")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.WIDTH);
            } else if (columnName.equals("height")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.VISIBLE);
            } else if (columnName.equals("label")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.LABEL);
            } else if (columnName.equals("labelvisible")) {
                edgeColumns[i - 2] = new GDFColumn(GDFColumn.EdgeGuessColumn.LABELVISIBLE);
            } else {
                AttributeClass edgeClass = container.getAttributeManager().getEdgeClass();
                AttributeColumn newColumn = edgeClass.addAttributeColumn(columnName, type);
                edgeColumns[i - 2] = new GDFColumn(newColumn);
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

    private void setNodeData(NodeDraft node, GDFColumn column, String data) throws ImportException {
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
                String message = String.format(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat3"), column.getNodeColumn(), data);
                throw new ImportException(message);
            }
        } else if (column.getAttributeColumn() != null) {
            node.addAttributeValue(column.getAttributeColumn(), data);
        }
    }

    private void setEdgeData(EdgeDraft edge, GDFColumn column, String data) throws ImportException {
        if (column.getNodeColumn() != null) {
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
                        break;
                    case DIRECTED:
                        edge.setDirected(Boolean.parseBoolean(data));
                        break;
                    case LABEL:
                        edge.setLabel(data);
                        break;
                    case LABELVISIBLE:
                        edge.setLabelVisible(Boolean.parseBoolean(data));
                        break;
                }
            } catch (Exception e) {
                String message = String.format(NbBundle.getMessage(ImporterGDF.class, "importerGDF_error_dataformat3"), column.getNodeColumn(), data);
                throw new ImportException(message);
            }
        } else if (column.getAttributeColumn() != null) {
            edge.addAttributeValue(column.getAttributeColumn(), data);
        }
    }

    private static class GDFColumn {

        public enum NodeGuessColumn {

            X, Y, VISIBLE, FIXED, STYLE, COLOR, WIDTH, HEIGHT, LABEL, LABELVISIBLE
        };

        public enum EdgeGuessColumn {

            VISIBLE, COLOR, WEIGHT, WIDTH, DIRECTED, LABEL, LABELVISIBLE
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
