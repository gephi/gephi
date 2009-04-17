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
import org.gephi.importer.api.EdgeDraft;
import org.gephi.importer.api.FileType;
import org.gephi.importer.api.ImportContainer;
import org.gephi.importer.api.ImportException;
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

    private List<String> nodeLines = new ArrayList<String>();
    private List<String> edgeLines = new ArrayList<String>();

    //Matcher
    private String[] nodeLineStart;
    private String[] edgeLineStart;

    public ImporterGDF() {
        nodeLineStart = new String[]{"nodedef>name", "nodedef> name", "Nodedef>name", "Nodedef> name"};
        edgeLineStart = new String[]{"edgedef>", "Edgedef>"};
    }

    public void importData(BufferedReader reader, ImportContainer containter) throws ImportException {
        try {

            //Verify a node line exists and puts nodes and edges lines in arrays
            walkFile(reader);

            //Magix regex
            Pattern pattern = Pattern.compile("(?<=(?:,|^)\")(.*?)(?=(?<=(?:[^\\\\]))\",|\"$)|(?<=(?:,|^)')(.*?)(?=(?<=(?:[^\\\\]))',|'$)|(?<=(?:,|^))(?=[^'\"])(.*?)(?=(?:,|$))|(?<=,)($)");

            //Nodes
            for (String nodeLine : nodeLines) {

                //Create Node
                NodeDraft node = new NodeDraft();

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
                            }
                        }
                    }
                    count++;
                }

                containter.addNode(node);
            }

            //Edges
            for (String edgeLine : edgeLines) {

                //Create Edge
                EdgeDraft edge = new EdgeDraft();

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
                                NodeDraft nodeSource = containter.getNode(data);
                                edge.setNodeSource(nodeSource);
                            } else if (count == 1) {
                                NodeDraft nodeTarget = containter.getNode(data);
                                edge.setNodeTarget(nodeTarget);
                            }
                        }
                    }
                    count++;
                }

                containter.addEdge(edge);
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

    private void findNodeColumns(String line) {
    }

    private void findEdgeColumns(String line) {
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

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gdf", NbBundle.getMessage(getClass(), "fileType_GDF_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.hasExt("gdf");
    }
}
