/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.io.importer.plugin.file;

import java.awt.Color;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
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

//Inspired from infovis.graph.io;
//Original author Jean-Daniel Fekete
public class ImporterGML implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        LineNumberReader lineReader = ImportUtils.getTextReader(reader);
        try {
            importData(lineReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return !cancel;
    }

    private void importData(LineNumberReader reader) throws Exception {
        Progress.start(progressTicket);

        ArrayList list;
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.ordinaryChar('[');
        tokenizer.ordinaryChar(']');
        tokenizer.wordChars('_', '_');
        list = parseList(tokenizer);

        boolean ret = false;
        for (int i = 0; i < list.size(); i++) {
            if ("graph".equals(list.get(i)) && list.size() >= i + 2 && list.get(i + 1) instanceof ArrayList) {
                ret = parseGraph((ArrayList) list.get(i + 1));
            }
        }
        if (!ret) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGML.class, "importerGML_error_badparsing"), Issue.Level.SEVERE));
        }

        Progress.finish(progressTicket);
    }

    private ArrayList parseList(StreamTokenizer tokenizer) throws IOException {
        ArrayList list = new ArrayList();
        loop:
        while (true) {
            int t = tokenizer.nextToken();
            if (t == ']' || t == StreamTokenizer.TT_EOF) {
                return list;
            } else if (t != StreamTokenizer.TT_WORD) {
                break;
            }
            String key = tokenizer.sval;
            list.add(key);
            t = tokenizer.nextToken();
            switch (t) {
                case '[':
                    list.add(parseList(tokenizer));
                    break;
                case StreamTokenizer.TT_NUMBER:
                    list.add(new Double(tokenizer.nval));
                    break;
                case StreamTokenizer.TT_WORD:
                case '"':
                    list.add(tokenizer.sval);
                    break;
                default:
                    break loop;
            }
        }
        report.logIssue(new Issue(NbBundle.getMessage(ImporterGML.class, "importerGML_error_listtoken", tokenizer.lineno()), Issue.Level.SEVERE));
        return list;
    }

    private boolean parseGraph(ArrayList list) {
        if ((list.size() & 1) != 0) {
            return false;
        }
        Progress.switchToDeterminate(progressTicket, list.size());

        boolean ret = true;
        for (int i = 0; i < list.size(); i += 2) {
            Object key = list.get(i);
            Object value = list.get(i + 1);
            if ("node".equals(key)) {
                ret = parseNode((ArrayList) value);
            } else if ("edge".equals(key)) {
                ret = parseEdge((ArrayList) value);
            } else if ("directed".equals(key)) {
                if (value instanceof Double) {
                    EdgeDefault edgeDefault = ((Double) value) == 1 ? EdgeDefault.DIRECTED : EdgeDefault.UNDIRECTED;
                    container.setEdgeDefault(edgeDefault);
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGML.class, "importerGML_error_directedgraphparse"), Issue.Level.WARNING));
                }
            } else {
            }
            if (!ret) {
                break;
            }
            if (cancel) {
                break;
            }
            Progress.progress(progressTicket);
        }
        return ret;
    }

    private boolean parseNode(ArrayList list) {
        NodeDraft node = container.factory().newNodeDraft();
        String id = null;
        for (int i = 0; i < list.size(); i += 2) {
            String key = (String) list.get(i);
            Object value = list.get(i + 1);
            if ("id".equals(key)) {
                id = value.toString();
                node.setId(id);
            } else if ("label".equals(key)) {
                String label = value.toString();
                node.setLabel(label);
            }
        }
        if (id == null) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGML.class, "importerGML_error_nodeidmissing"), Issue.Level.WARNING));
        }
        boolean ret = addNodeAttributes(node, "", list);
        container.addNode(node);
        return ret;
    }

    private boolean addNodeAttributes(NodeDraft node, String prefix, ArrayList list) {
        boolean ret = true;
        for (int i = 0; i < list.size(); i += 2) {
            String key = (String) list.get(i);
            Object value = list.get(i + 1);
            if ("id".equals(key) || "label".equals(key)) {
                continue; // already parsed
            }
            if (value instanceof ArrayList) {
                // keep the  hierarchy
                ret = addNodeAttributes(node, prefix + "." + key, (ArrayList) value);
                if (!ret) {
                    break;
                }
            } else if ("x".equals(key) && value instanceof Double) {
                node.setX(((Double) value).floatValue());
            } else if ("y".equals(key) && value instanceof Double) {
                node.setY(((Double) value).floatValue());
            } else if ("z".equals(key) && value instanceof Double) {
                node.setZ(((Double) value).floatValue());
            } else if ("w".equals(key) && value instanceof Double) {
                node.setSize(((Double) value).floatValue());
            } else if ("h".equals(key)) {
            } else if ("fill".equals(key)) {
                int colorHex = -1;
                if (value instanceof String) {
                    String str = ((String) value).trim().replace("#", "");
                    try {
                        colorHex = Integer.valueOf(str, 16).intValue();
                    } catch (Exception e) {
                    }
                }
                if (colorHex != -1) {
                    node.setColor(new Color(colorHex));
                }
            } else {
                AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
                AttributeColumn column = null;
                if ((column = nodeClass.getColumn(key)) == null) {
                    column = nodeClass.addColumn(key, AttributeType.STRING);
                    report.log("Node attribute " + column.getTitle() + " (" + column.getType() + ")");
                }
                node.addAttributeValue(column, value.toString());
            }
        }
        return ret;
    }

    private boolean parseEdge(ArrayList list) {
        EdgeDraft edgeDraft = container.factory().newEdgeDraft();
        for (int i = 0; i < list.size(); i += 2) {
            String key = (String) list.get(i);
            Object value = list.get(i + 1);
            if ("source".equals(key)) {
                NodeDraft source = container.getNode(value.toString());
                edgeDraft.setSource(source);
            } else if ("target".equals(key)) {
                NodeDraft target = container.getNode(value.toString());
                edgeDraft.setTarget(target);
            } else if ("value".equals(key)) {
                if (value instanceof Double) {
                    edgeDraft.setWeight(((Double) value).floatValue());
                }
            } else if ("label".equals(key)) {
                edgeDraft.setLabel(value.toString());
            }
        }
        boolean ret = addEdgeAttributes(edgeDraft, "", list);
        container.addEdge(edgeDraft);
        return ret;
    }

    private boolean addEdgeAttributes(EdgeDraft edge, String prefix, ArrayList list) {
        boolean ret = true;
        for (int i = 0; i < list.size(); i += 2) {
            String key = (String) list.get(i);
            Object value = list.get(i + 1);
            if ("source".equals(key) || "target".equals(key) || "value".equals(key) || "label".equals(key)) {
                continue; // already parsed
            }
            if (value instanceof ArrayList) {
                // keep the hierarchy
                ret = addEdgeAttributes(edge, prefix + "." + key, (ArrayList) value);
                if (!ret) {
                    break;
                }
            } else if ("directed".equals(key)) {
                if (value instanceof Double) {
                    EdgeDraft.EdgeType type = ((Double) value) == 1 ? EdgeDraft.EdgeType.DIRECTED : EdgeDraft.EdgeType.UNDIRECTED;
                    edge.setType(type);
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGML.class, "importerGML_error_directedparse", edge.toString()), Issue.Level.WARNING));
                }
            } else {
                AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
                AttributeColumn column = null;
                if ((column = edgeClass.getColumn(key)) == null) {
                    column = edgeClass.addColumn(key, AttributeType.STRING);
                    report.log("Edge attribute " + column.getTitle() + " (" + column.getType() + ")");
                }
                edge.addAttributeValue(column, value.toString());
            }
        }
        return ret;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public ContainerLoader getContainer() {
        return container;
    }

    public Report getReport() {
        return report;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
