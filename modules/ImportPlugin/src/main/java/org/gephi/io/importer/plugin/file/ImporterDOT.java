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
import java.io.StreamTokenizer;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.StreamTokenizerWithMultilineLiterals;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

public class ImporterDOT implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Data
    private String graphName = "";

    private static class ParseException extends RuntimeException {

        public ParseException() {
            super("Parse error while parsing DOT file");
        }
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

        Progress.start(progressTicket);
        StreamTokenizerWithMultilineLiterals streamTokenizer = new StreamTokenizerWithMultilineLiterals(reader);
        setSyntax(streamTokenizer);

        graph(streamTokenizer);
    }

    protected void setSyntax(StreamTokenizerWithMultilineLiterals tk) {
        tk.resetSyntax();
        tk.eolIsSignificant(false);
        tk.commentChar('#');
        tk.slashStarComments(true);
        tk.slashSlashComments(true);
        tk.whitespaceChars(0, ' ');
        tk.wordChars(' ' + 1, '\u00ff');
        tk.ordinaryChar('[');
        tk.ordinaryChar(']');
        tk.ordinaryChar('{');
        tk.ordinaryChar('}');
        tk.ordinaryChar('-');
        tk.ordinaryChar('>');
        tk.ordinaryChar('/');
        tk.ordinaryChar('*');
        tk.ordinaryChar(',');
        tk.quoteChar('"');
        tk.whitespaceChars(';', ';');
        tk.ordinaryChar('=');
    }

    protected void graph(StreamTokenizerWithMultilineLiterals streamTokenizer) throws Exception {
        boolean found = false;
        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                if (streamTokenizer.sval.equalsIgnoreCase("digraph") || streamTokenizer.sval.equalsIgnoreCase("graph")) {
                    found = true;
                    container.setEdgeDefault(streamTokenizer.sval.equalsIgnoreCase("digraph") ? EdgeDirectionDefault.DIRECTED : EdgeDirectionDefault.UNDIRECTED);
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                        graphName = streamTokenizer.sval;
                        streamTokenizer.nextToken();
                    }

                    while (streamTokenizer.ttype != '{') {
                        streamTokenizer.nextToken();
                        if (streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
                            return;
                        }
                    }
                    stmtList(streamTokenizer);
                }
            }
        }
        if (!found) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_nothingfound"), Issue.Level.SEVERE));
        }
    }

    protected void stmtList(StreamTokenizerWithMultilineLiterals streamTokenizer) throws Exception {
        do {
            streamTokenizer.nextToken();
            stmt(streamTokenizer);
        } while (streamTokenizer.ttype != StreamTokenizer.TT_EOF);
    }

    protected void stmt(StreamTokenizerWithMultilineLiterals streamTokenizer) throws Exception {
        //tk.nextToken();

        if (streamTokenizer.sval == null || streamTokenizer.sval.equalsIgnoreCase("graph") || streamTokenizer.sval.equalsIgnoreCase("node")
                || streamTokenizer.sval.equalsIgnoreCase("edge")) {
        } else {
            String nodeId = nodeID(streamTokenizer);
            streamTokenizer.nextToken();

            if (streamTokenizer.ttype == '-') {
                NodeDraft nodeDraft = getOrCreateNode(nodeId);
                edgeStructure(streamTokenizer, nodeDraft);
            } else if (streamTokenizer.ttype == '[') {
                NodeDraft nodeDraft = getOrCreateNode(nodeId);
                nodeAttributes(streamTokenizer, nodeDraft);
            } else {
                getOrCreateNode(nodeId);
                streamTokenizer.pushBack();
            }
        }
    }

    protected String nodeID(StreamTokenizerWithMultilineLiterals streamTokenizer) {
        if (streamTokenizer.ttype == '"' || streamTokenizer.ttype == StreamTokenizer.TT_WORD || (streamTokenizer.ttype >= 'a' && streamTokenizer.ttype <= 'z')
                || (streamTokenizer.ttype >= 'A' && streamTokenizer.ttype <= 'Z')) {
            return streamTokenizer.sval;
        } else {
            return null;
        }
    }

    protected NodeDraft getOrCreateNode(String id) {
        if (!container.nodeExists(id)) {
            NodeDraft nodeDraft = container.factory().newNodeDraft(id);
            container.addNode(nodeDraft);
            return nodeDraft;
        }
        return container.getNode(id);
    }

    protected Color parseColor(StreamTokenizerWithMultilineLiterals streamTokenizer) throws Exception {
        if (streamTokenizer.ttype == '#') {
            streamTokenizer.nextToken();
            return new Color(Integer.parseInt(streamTokenizer.sval, 16), true);
        } else if (streamTokenizer.ttype == '"' && streamTokenizer.sval.startsWith("#")) {
            return new Color(Integer.parseInt(streamTokenizer.sval.substring(1), 16), true);
        } else if (streamTokenizer.ttype != StreamTokenizer.TT_WORD && streamTokenizer.ttype != '"') {
            throw new ParseException();
        } else if (ImportUtils.parseColor(streamTokenizer.sval) != null) {
            return ImportUtils.parseColor(streamTokenizer.sval);
        } else {
            String toParse = streamTokenizer.sval.replace(", ", ",");
            String[] colors = toParse.split(" ");
            if (colors.length != 3) {
                colors = toParse.split(",");
            }
            if (colors.length != 3) {
                throw new ParseException();
            }

            return Color.getHSBColor(Float.parseFloat(colors[0]), Float.parseFloat(colors[1]), Float.parseFloat(colors[2]));
        }
    }

    protected void nodeAttributes(StreamTokenizerWithMultilineLiterals streamTokenizer, final NodeDraft nodeDraft) throws Exception {
        streamTokenizer.nextToken();

        if (streamTokenizer.ttype == ']' || streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
            return;
        } else if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {

            if (streamTokenizer.sval.equalsIgnoreCase("label")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        nodeDraft.setLabel(streamTokenizer.sval);
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_labelunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_labelunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                    streamTokenizer.pushBack();
                }
            } else if (streamTokenizer.sval.equalsIgnoreCase("color")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    try {
                        nodeDraft.setColor(parseColor(streamTokenizer));
                    } catch (ParseException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_colorunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_colorunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                    streamTokenizer.pushBack();
                }
            } else if (streamTokenizer.sval.equalsIgnoreCase("pos")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        try {
                            String[] positions = streamTokenizer.sval.split(",");
                            if (positions.length == 2) {
                                nodeDraft.setX(Float.parseFloat(positions[0]));
                                nodeDraft.setY(Float.parseFloat(positions[1]));
                            } else if (positions.length == 3) {
                                nodeDraft.setX(Float.parseFloat(positions[0]));
                                nodeDraft.setY(Float.parseFloat(positions[1]));
                                nodeDraft.setZ(Float.parseFloat(positions[2]));
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_posunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_posunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                    streamTokenizer.pushBack();
                }
            } else if (streamTokenizer.sval.equalsIgnoreCase("style")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                    } else {
                        //System.err.println("couldn't find style at line " + streamTokenizer.lineno());
                        streamTokenizer.pushBack();
                    }
                } else {
                    //System.err.println("couldn't find style at line " + streamTokenizer.lineno());
                    streamTokenizer.pushBack();
                }
            } else {
                // other attributes
                String attributeName = streamTokenizer.sval;
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        String value = streamTokenizer.sval;
                        if (value != null && !value.isEmpty()) {
                            nodeDraft.setValue(attributeName, value);
                        }
                    } else {
                        streamTokenizer.pushBack();
                    }
                } else {
                    streamTokenizer.pushBack();
                }
            }
        }
        nodeAttributes(streamTokenizer, nodeDraft);
    }

    protected void edgeStructure(StreamTokenizerWithMultilineLiterals streamTokenizer, final NodeDraft nodeDraft) throws Exception {
        streamTokenizer.nextToken();

        EdgeDraft edge = null;
        if (streamTokenizer.ttype == '>' || streamTokenizer.ttype == '-') {
            streamTokenizer.nextToken();
            if (streamTokenizer.ttype == '{') {
                while (true) {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == '}') {
                        break;
                    } else {
                        nodeID(streamTokenizer);
                        edge = container.factory().newEdgeDraft();
                        edge.setSource(nodeDraft);
                        edge.setTarget(getOrCreateNode("" + streamTokenizer.sval));
                        container.addEdge(edge);
                    }
                }
            } else {
                nodeID(streamTokenizer);
                edge = container.factory().newEdgeDraft();
                edge.setSource(nodeDraft);
                edge.setTarget(getOrCreateNode("" + streamTokenizer.sval));
                container.addEdge(edge);
            }
        } else {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_edgeparsing", streamTokenizer.lineno()), Issue.Level.SEVERE));
            if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                streamTokenizer.pushBack();
            }
            return;
        }

        streamTokenizer.nextToken();

        if (streamTokenizer.ttype == '[') {
            edgeAttributes(streamTokenizer, edge);
        } else {
            streamTokenizer.pushBack();
        }
    }

    protected void edgeAttributes(StreamTokenizerWithMultilineLiterals streamTokenizer, final EdgeDraft edge) throws Exception {
        streamTokenizer.nextToken();

        if (streamTokenizer.ttype == ']' || streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
            return;
        } else if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
            if (streamTokenizer.sval.equalsIgnoreCase("label")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        edge.setLabel(streamTokenizer.sval);
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_labelunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_labelunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                    streamTokenizer.pushBack();
                }
            } else if (streamTokenizer.sval.equalsIgnoreCase("color")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    try {
                        edge.setColor(parseColor(streamTokenizer));
                    } catch (ParseException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_colorunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_color_labelunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                    streamTokenizer.pushBack();
                }
            } else if (streamTokenizer.sval.equalsIgnoreCase("style")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"'); else {
                        //System.err.println("couldn't find style at line " + streamTokenizer.lineno());
                        streamTokenizer.pushBack();
                    }
                } else {
                    //System.err.println("couldn't find style at line " + streamTokenizer.lineno());
                    streamTokenizer.pushBack();
                }
            } else if (streamTokenizer.sval.equalsIgnoreCase("weight")) {
                streamTokenizer.nextToken();
                if (streamTokenizer.ttype == '=') {
                    streamTokenizer.nextToken();
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        try {
                            Float weight = Float.parseFloat(streamTokenizer.sval);
                            edge.setWeight(weight);
                        } catch (Exception e) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_weightunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        }
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_weightunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_weightunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                    streamTokenizer.pushBack();
                }
            }
        }
        edgeAttributes(streamTokenizer, edge);
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
}
