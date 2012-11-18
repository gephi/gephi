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
package org.gephi.io.importer.plugin.file;

import java.awt.Color;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.importer.api.*;
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

        ArrayList<Object> list;
        list = parseList(reader);

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

    private ArrayList<Object> parseList(LineNumberReader reader) throws IOException {
        
        ArrayList<Object> list = new ArrayList<Object>();
        char t;
        boolean readString = false;
        String stringBuffer = new String();
        
        while ( reader.ready() ) {
            t = (char) reader.read();
            if ( readString ) {
                if ( t == '"' ) {
                    list.add(stringBuffer);
                    stringBuffer = new String();
                    readString = false;
                } else {
                    stringBuffer += t;
                }
            } else {
                switch(t) {
                    case '[':
                        list.add(parseList(reader));
                        break;
                    case ']':
                        return list;
                    case '"':
                        readString = true;
                        break;
                    case ' ':
                    case '\t':
                    case '\n':
                        if ( !stringBuffer.isEmpty() ) {
                            try {
                                Double doubleValue = Double.valueOf(stringBuffer);
                                list.add(doubleValue);
                            } catch(NumberFormatException e) {
                                list.add(stringBuffer);
                            }
                            stringBuffer = new String();
                        }
                        break;
                    default:
                        stringBuffer += t;
                        break;
                }
            }      
        }
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
            } else if ("d".equals(key)) { 
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
                AttributeColumn column = nodeClass.getColumn(key);
                if (column == null) {
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
            } else if ("value".equals(key) || "weight".equals(key)) {
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
            if ("source".equals(key) || "target".equals(key) || "value".equals(key) || "weight".equals(key) || "label".equals(key)) {
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
                AttributeColumn column = edgeClass.getColumn(key);
                if (column == null) {
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