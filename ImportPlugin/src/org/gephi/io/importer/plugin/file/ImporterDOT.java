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
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;
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

public class ImporterDOT implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Data
    private Map<String, Color> colorTable = new HashMap<String, Color>();
    private String graphName = "";

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
        initColorTable();
        StreamTokenizer streamTokenizer = new StreamTokenizer(reader);
        setSyntax(streamTokenizer);

        graph(streamTokenizer);
    }

    protected void setSyntax(StreamTokenizer tk) {
        tk.resetSyntax();
        tk.eolIsSignificant(false);
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

    protected void graph(StreamTokenizer streamTokenizer) throws Exception {
        boolean found = false;
        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                if (streamTokenizer.sval.equalsIgnoreCase("digraph") || streamTokenizer.sval.equalsIgnoreCase("graph")) {
                    found = true;
                    container.setEdgeDefault(streamTokenizer.sval.equalsIgnoreCase("digraph") ? EdgeDefault.DIRECTED : EdgeDefault.UNDIRECTED);
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

    protected void stmtList(StreamTokenizer streamTokenizer) throws Exception {
        do {
            streamTokenizer.nextToken();
            stmt(streamTokenizer);
        } while (streamTokenizer.ttype != StreamTokenizer.TT_EOF);
    }

    protected void stmt(StreamTokenizer streamTokenizer) throws Exception {
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
            }
        }
    }

    protected String nodeID(StreamTokenizer streamTokenizer) {
        if (streamTokenizer.ttype == '"' || streamTokenizer.ttype == StreamTokenizer.TT_WORD || (streamTokenizer.ttype >= 'a' && streamTokenizer.ttype <= 'z')
                || (streamTokenizer.ttype >= 'A' && streamTokenizer.ttype <= 'Z')) {
            return streamTokenizer.sval;
        } else {
            return null;
        }
    }

    protected NodeDraft getOrCreateNode(String id) {
        if (!container.nodeExists(id)) {
            NodeDraft nodeDraft = container.factory().newNodeDraft();
            nodeDraft.setId(id);
            container.addNode(nodeDraft);
            return nodeDraft;
        }
        return container.getNode(id);
    }

    protected void nodeAttributes(StreamTokenizer streamTokenizer, final NodeDraft nodeDraft) throws Exception {
        streamTokenizer.nextToken();

        if (streamTokenizer.ttype == ']' || streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
            return;
        } else if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {

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
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        if (colorTable.containsKey(streamTokenizer.sval)) {
                            nodeDraft.setColor(colorTable.get(streamTokenizer.sval));
                        } else {
                            try {
                                String[] colors = streamTokenizer.sval.split(" ");
                                nodeDraft.setColor(Float.parseFloat(colors[0]), Float.parseFloat(colors[1]), Float.parseFloat(colors[2]));
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_colorunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
                        streamTokenizer.pushBack();
                    }
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_error_colorunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
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
            }
        }
        nodeAttributes(streamTokenizer, nodeDraft);
    }

    protected void edgeStructure(StreamTokenizer streamTokenizer, final NodeDraft nodeDraft) throws Exception {
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

    protected void edgeAttributes(StreamTokenizer streamTokenizer, final EdgeDraft edge) throws Exception {
        streamTokenizer.nextToken();

        if (streamTokenizer.ttype == ']' || streamTokenizer.ttype == StreamTokenizer.TT_EOF) {
            return;
        } else if (streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
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
                    if (streamTokenizer.ttype == StreamTokenizer.TT_WORD || streamTokenizer.ttype == '"') {
                        if (colorTable.containsKey(streamTokenizer.sval)) {
                            edge.setColor(colorTable.get(streamTokenizer.sval));
                        } else {
                            try {
                                String[] colors = streamTokenizer.sval.split(" ");
                                edge.setColor(Float.parseFloat(colors[0]), Float.parseFloat(colors[1]), Float.parseFloat(colors[2]));
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterDOT.class, "importerDOT_color_labelunreachable", streamTokenizer.lineno()), Issue.Level.WARNING));
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
            }
        }
        edgeAttributes(streamTokenizer, edge);
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

    private void initColorTable() {
        colorTable.put("aliceblue", new Color(240, 248, 255));
        colorTable.put("antiquewhite", new Color(250, 235, 215));
        colorTable.put("antiquewhite1", new Color(255, 239, 219));
        colorTable.put("antiquewhite2", new Color(238, 223, 204));
        colorTable.put("antiquewhite3", new Color(205, 192, 176));
        colorTable.put("antiquewhite4", new Color(139, 131, 120));
        colorTable.put("aquamarine", new Color(127, 255, 212));
        colorTable.put("aquamarine1", new Color(127, 255, 212));
        colorTable.put("aquamarine2", new Color(118, 238, 198));
        colorTable.put("aquamarine3", new Color(102, 205, 170));
        colorTable.put("aquamarine4", new Color(69, 139, 116));
        colorTable.put("azure", new Color(240, 255, 255));
        colorTable.put("azure1", new Color(240, 255, 255));
        colorTable.put("azure2", new Color(224, 238, 238));
        colorTable.put("azure3", new Color(193, 205, 205));
        colorTable.put("azure4", new Color(131, 139, 139));
        colorTable.put("beige", new Color(245, 245, 220));
        colorTable.put("bisque", new Color(255, 228, 196));
        colorTable.put("bisque1", new Color(255, 228, 196));
        colorTable.put("bisque2", new Color(238, 213, 183));
        colorTable.put("bisque3", new Color(205, 183, 158));
        colorTable.put("bisque4", new Color(139, 125, 107));
        colorTable.put("black", new Color(0, 0, 0));
        colorTable.put("blanchedalmond", new Color(255, 235, 205));
        colorTable.put("blue", new Color(0, 0, 255));
        colorTable.put("blue1", new Color(0, 0, 255));
        colorTable.put("blue2", new Color(0, 0, 238));
        colorTable.put("blue3", new Color(0, 0, 205));
        colorTable.put("blue4", new Color(0, 0, 139));
        colorTable.put("blueviolet", new Color(138, 43, 226));
        colorTable.put("brown", new Color(165, 42, 42));
        colorTable.put("brown1", new Color(255, 64, 64));
        colorTable.put("brown2", new Color(238, 59, 59));
        colorTable.put("brown3", new Color(205, 51, 51));
        colorTable.put("brown4", new Color(139, 35, 35));
        colorTable.put("burlywood", new Color(222, 184, 135));
        colorTable.put("burlywood1", new Color(255, 211, 155));
        colorTable.put("burlywood2", new Color(238, 197, 145));
        colorTable.put("burlywood3", new Color(205, 170, 125));
        colorTable.put("burlywood4", new Color(139, 115, 85));
        colorTable.put("cadetblue", new Color(95, 158, 160));
        colorTable.put("cadetblue1", new Color(152, 245, 255));
        colorTable.put("cadetblue2", new Color(142, 229, 238));
        colorTable.put("cadetblue3", new Color(122, 197, 205));
        colorTable.put("cadetblue4", new Color(83, 134, 139));
        colorTable.put("chartreuse", new Color(127, 255, 0));
        colorTable.put("chartreuse1", new Color(127, 255, 0));
        colorTable.put("chartreuse2", new Color(118, 238, 0));
        colorTable.put("chartreuse3", new Color(102, 205, 0));
        colorTable.put("chartreuse4", new Color(69, 139, 0));
        colorTable.put("chocolate", new Color(210, 105, 30));
        colorTable.put("chocolate1", new Color(255, 127, 36));
        colorTable.put("chocolate2", new Color(238, 118, 33));
        colorTable.put("chocolate3", new Color(205, 102, 29));
        colorTable.put("chocolate4", new Color(139, 69, 19));
        colorTable.put("coral", new Color(255, 127, 80));
        colorTable.put("coral1", new Color(255, 114, 86));
        colorTable.put("coral2", new Color(238, 106, 80));
        colorTable.put("coral3", new Color(205, 91, 69));
        colorTable.put("coral4", new Color(139, 62, 47));
        colorTable.put("cornflowerblue", new Color(100, 149, 237));
        colorTable.put("cornsilk", new Color(255, 248, 220));
        colorTable.put("cornsilk1", new Color(255, 248, 220));
        colorTable.put("cornsilk2", new Color(238, 232, 205));
        colorTable.put("cornsilk3", new Color(205, 200, 177));
        colorTable.put("cornsilk4", new Color(139, 136, 120));
        colorTable.put("crimson", new Color(220, 20, 60));
        colorTable.put("cyan", new Color(0, 255, 255));
        colorTable.put("cyan1", new Color(0, 255, 255));
        colorTable.put("cyan2", new Color(0, 238, 238));
        colorTable.put("cyan3", new Color(0, 205, 205));
        colorTable.put("cyan4", new Color(0, 139, 139));
        colorTable.put("darkblue", new Color(0, 0, 139));
        colorTable.put("darkcyan", new Color(0, 139, 139));
        colorTable.put("darkgoldenrod", new Color(184, 134, 11));
        colorTable.put("darkgoldenrod1", new Color(255, 185, 15));
        colorTable.put("darkgoldenrod2", new Color(238, 173, 14));
        colorTable.put("darkgoldenrod3", new Color(205, 149, 12));
        colorTable.put("darkgoldenrod4", new Color(139, 101, 8));
        colorTable.put("darkgray", new Color(169, 169, 169));
        colorTable.put("darkgreen", new Color(0, 100, 0));
        colorTable.put("darkgrey", new Color(169, 169, 169));
        colorTable.put("darkkhaki", new Color(189, 183, 107));
        colorTable.put("darkmagenta", new Color(139, 0, 139));
        colorTable.put("darkolivegreen", new Color(85, 107, 47));
        colorTable.put("darkolivegreen1", new Color(202, 255, 112));
        colorTable.put("darkolivegreen2", new Color(188, 238, 104));
        colorTable.put("darkolivegreen3", new Color(162, 205, 90));
        colorTable.put("darkolivegreen4", new Color(110, 139, 61));
        colorTable.put("darkorange", new Color(255, 140, 0));
        colorTable.put("darkorange1", new Color(255, 127, 0));
        colorTable.put("darkorange2", new Color(238, 118, 0));
        colorTable.put("darkorange3", new Color(205, 102, 0));
        colorTable.put("darkorange4", new Color(139, 69, 0));
        colorTable.put("darkorchid", new Color(153, 50, 204));
        colorTable.put("darkorchid1", new Color(191, 62, 255));
        colorTable.put("darkorchid2", new Color(178, 58, 238));
        colorTable.put("darkorchid3", new Color(154, 50, 205));
        colorTable.put("darkorchid4", new Color(104, 34, 139));
        colorTable.put("darkred", new Color(139, 0, 0));
        colorTable.put("darksalmon", new Color(233, 150, 122));
        colorTable.put("darkseagreen", new Color(143, 188, 143));
        colorTable.put("darkseagreen1", new Color(193, 255, 193));
        colorTable.put("darkseagreen2", new Color(180, 238, 180));
        colorTable.put("darkseagreen3", new Color(155, 205, 155));
        colorTable.put("darkseagreen4", new Color(105, 139, 105));
        colorTable.put("darkslateblue", new Color(72, 61, 139));
        colorTable.put("darkslategray", new Color(47, 79, 79));
        colorTable.put("darkslategray1", new Color(151, 255, 255));
        colorTable.put("darkslategray2", new Color(141, 238, 238));
        colorTable.put("darkslategray3", new Color(121, 205, 205));
        colorTable.put("darkslategray4", new Color(82, 139, 139));
        colorTable.put("darkslategrey", new Color(47, 79, 79));
        colorTable.put("darkturquoise", new Color(0, 206, 209));
        colorTable.put("darkviolet", new Color(148, 0, 211));
        colorTable.put("deeppink", new Color(255, 20, 147));
        colorTable.put("deeppink1", new Color(255, 20, 147));
        colorTable.put("deeppink2", new Color(238, 18, 137));
        colorTable.put("deeppink3", new Color(205, 16, 118));
        colorTable.put("deeppink4", new Color(139, 10, 80));
        colorTable.put("deepskyblue", new Color(0, 191, 255));
        colorTable.put("deepskyblue1", new Color(0, 191, 255));
        colorTable.put("deepskyblue2", new Color(0, 178, 238));
        colorTable.put("deepskyblue3", new Color(0, 154, 205));
        colorTable.put("deepskyblue4", new Color(0, 104, 139));
        colorTable.put("dimgray", new Color(105, 105, 105));
        colorTable.put("dimgrey", new Color(105, 105, 105));
        colorTable.put("dodgerblue", new Color(30, 144, 255));
        colorTable.put("dodgerblue1", new Color(30, 144, 255));
        colorTable.put("dodgerblue2", new Color(28, 134, 238));
        colorTable.put("dodgerblue3", new Color(24, 116, 205));
        colorTable.put("dodgerblue4", new Color(16, 78, 139));
        colorTable.put("firebrick", new Color(178, 34, 34));
        colorTable.put("firebrick1", new Color(255, 48, 48));
        colorTable.put("firebrick2", new Color(238, 44, 44));
        colorTable.put("firebrick3", new Color(205, 38, 38));
        colorTable.put("firebrick4", new Color(139, 26, 26));
        colorTable.put("floralwhite", new Color(255, 250, 240));
        colorTable.put("forestgreen", new Color(34, 139, 34));
        colorTable.put("gainsboro", new Color(220, 220, 220));
        colorTable.put("ghostwhite", new Color(248, 248, 255));
        colorTable.put("gold", new Color(255, 215, 0));
        colorTable.put("gold1", new Color(255, 215, 0));
        colorTable.put("gold2", new Color(238, 201, 0));
        colorTable.put("gold3", new Color(205, 173, 0));
        colorTable.put("gold4", new Color(139, 117, 0));
        colorTable.put("goldenrod", new Color(218, 165, 32));
        colorTable.put("goldenrod1", new Color(255, 193, 37));
        colorTable.put("goldenrod2", new Color(238, 180, 34));
        colorTable.put("goldenrod3", new Color(205, 155, 29));
        colorTable.put("goldenrod4", new Color(139, 105, 20));
        colorTable.put("green", new Color(0, 255, 0));
        colorTable.put("green1", new Color(0, 255, 0));
        colorTable.put("green2", new Color(0, 238, 0));
        colorTable.put("green3", new Color(0, 205, 0));
        colorTable.put("green4", new Color(0, 139, 0));
        colorTable.put("greenyellow", new Color(173, 255, 47));
        colorTable.put("gray", new Color(190, 190, 190));
        colorTable.put("grey", new Color(190, 190, 190));
        colorTable.put("gray0", new Color(0, 0, 0));
        colorTable.put("grey0", new Color(0, 0, 0));
        colorTable.put("gray1", new Color(3, 3, 3));
        colorTable.put("grey1", new Color(3, 3, 3));
        colorTable.put("gray2", new Color(5, 5, 5));
        colorTable.put("grey2", new Color(5, 5, 5));
        colorTable.put("gray3", new Color(8, 8, 8));
        colorTable.put("grey3", new Color(8, 8, 8));
        colorTable.put("gray4", new Color(10, 10, 10));
        colorTable.put("grey4", new Color(10, 10, 10));
        colorTable.put("gray5", new Color(13, 13, 13));
        colorTable.put("grey5", new Color(13, 13, 13));
        colorTable.put("gray6", new Color(15, 15, 15));
        colorTable.put("grey6", new Color(15, 15, 15));
        colorTable.put("gray7", new Color(18, 18, 18));
        colorTable.put("grey7", new Color(18, 18, 18));
        colorTable.put("gray8", new Color(20, 20, 20));
        colorTable.put("grey8", new Color(20, 20, 20));
        colorTable.put("gray9", new Color(23, 23, 23));
        colorTable.put("grey9", new Color(23, 23, 23));
        colorTable.put("gray10", new Color(26, 26, 26));
        colorTable.put("grey10", new Color(26, 26, 26));
        colorTable.put("gray11", new Color(28, 28, 28));
        colorTable.put("grey11", new Color(28, 28, 28));
        colorTable.put("gray12", new Color(31, 31, 31));
        colorTable.put("grey12", new Color(31, 31, 31));
        colorTable.put("gray13", new Color(33, 33, 33));
        colorTable.put("grey13", new Color(33, 33, 33));
        colorTable.put("gray14", new Color(36, 36, 36));
        colorTable.put("grey14", new Color(36, 36, 36));
        colorTable.put("gray15", new Color(38, 38, 38));
        colorTable.put("grey15", new Color(38, 38, 38));
        colorTable.put("gray16", new Color(41, 41, 41));
        colorTable.put("grey16", new Color(41, 41, 41));
        colorTable.put("gray17", new Color(43, 43, 43));
        colorTable.put("grey17", new Color(43, 43, 43));
        colorTable.put("gray18", new Color(46, 46, 46));
        colorTable.put("grey18", new Color(46, 46, 46));
        colorTable.put("gray19", new Color(48, 48, 48));
        colorTable.put("grey19", new Color(48, 48, 48));
        colorTable.put("gray20", new Color(51, 51, 51));
        colorTable.put("grey20", new Color(51, 51, 51));
        colorTable.put("gray21", new Color(54, 54, 54));
        colorTable.put("grey21", new Color(54, 54, 54));
        colorTable.put("gray22", new Color(56, 56, 56));
        colorTable.put("grey22", new Color(56, 56, 56));
        colorTable.put("gray23", new Color(59, 59, 59));
        colorTable.put("grey23", new Color(59, 59, 59));
        colorTable.put("gray24", new Color(61, 61, 61));
        colorTable.put("grey24", new Color(61, 61, 61));
        colorTable.put("gray25", new Color(64, 64, 64));
        colorTable.put("grey25", new Color(64, 64, 64));
        colorTable.put("gray26", new Color(66, 66, 66));
        colorTable.put("grey26", new Color(66, 66, 66));
        colorTable.put("gray27", new Color(69, 69, 69));
        colorTable.put("grey27", new Color(69, 69, 69));
        colorTable.put("gray28", new Color(71, 71, 71));
        colorTable.put("grey28", new Color(71, 71, 71));
        colorTable.put("gray29", new Color(74, 74, 74));
        colorTable.put("grey29", new Color(74, 74, 74));
        colorTable.put("gray30", new Color(77, 77, 77));
        colorTable.put("grey30", new Color(77, 77, 77));
        colorTable.put("gray31", new Color(79, 79, 79));
        colorTable.put("grey31", new Color(79, 79, 79));
        colorTable.put("gray32", new Color(82, 82, 82));
        colorTable.put("grey32", new Color(82, 82, 82));
        colorTable.put("gray33", new Color(84, 84, 84));
        colorTable.put("grey33", new Color(84, 84, 84));
        colorTable.put("gray34", new Color(87, 87, 87));
        colorTable.put("grey34", new Color(87, 87, 87));
        colorTable.put("gray35", new Color(89, 89, 89));
        colorTable.put("grey35", new Color(89, 89, 89));
        colorTable.put("gray36", new Color(92, 92, 92));
        colorTable.put("grey36", new Color(92, 92, 92));
        colorTable.put("gray37", new Color(94, 94, 94));
        colorTable.put("grey37", new Color(94, 94, 94));
        colorTable.put("gray38", new Color(97, 97, 97));
        colorTable.put("grey38", new Color(97, 97, 97));
        colorTable.put("gray39", new Color(99, 99, 99));
        colorTable.put("grey39", new Color(99, 99, 99));
        colorTable.put("gray40", new Color(102, 102, 102));
        colorTable.put("grey40", new Color(102, 102, 102));
        colorTable.put("gray41", new Color(105, 105, 105));
        colorTable.put("grey41", new Color(105, 105, 105));
        colorTable.put("gray42", new Color(107, 107, 107));
        colorTable.put("grey42", new Color(107, 107, 107));
        colorTable.put("gray43", new Color(110, 110, 110));
        colorTable.put("grey43", new Color(110, 110, 110));
        colorTable.put("gray44", new Color(112, 112, 112));
        colorTable.put("grey44", new Color(112, 112, 112));
        colorTable.put("gray45", new Color(115, 115, 115));
        colorTable.put("grey45", new Color(115, 115, 115));
        colorTable.put("gray46", new Color(117, 117, 117));
        colorTable.put("grey46", new Color(117, 117, 117));
        colorTable.put("gray47", new Color(120, 120, 120));
        colorTable.put("grey47", new Color(120, 120, 120));
        colorTable.put("gray48", new Color(122, 122, 122));
        colorTable.put("grey48", new Color(122, 122, 122));
        colorTable.put("gray49", new Color(125, 125, 125));
        colorTable.put("grey49", new Color(125, 125, 125));
        colorTable.put("gray50", new Color(127, 127, 127));
        colorTable.put("grey50", new Color(127, 127, 127));
        colorTable.put("gray51", new Color(130, 130, 130));
        colorTable.put("grey51", new Color(130, 130, 130));
        colorTable.put("gray52", new Color(133, 133, 133));
        colorTable.put("grey52", new Color(133, 133, 133));
        colorTable.put("gray53", new Color(135, 135, 135));
        colorTable.put("grey53", new Color(135, 135, 135));
        colorTable.put("gray54", new Color(138, 138, 138));
        colorTable.put("grey54", new Color(138, 138, 138));
        colorTable.put("gray55", new Color(140, 140, 140));
        colorTable.put("grey55", new Color(140, 140, 140));
        colorTable.put("gray56", new Color(143, 143, 143));
        colorTable.put("grey56", new Color(143, 143, 143));
        colorTable.put("gray57", new Color(145, 145, 145));
        colorTable.put("grey57", new Color(145, 145, 145));
        colorTable.put("gray58", new Color(148, 148, 148));
        colorTable.put("grey58", new Color(148, 148, 148));
        colorTable.put("gray59", new Color(150, 150, 150));
        colorTable.put("grey59", new Color(150, 150, 150));
        colorTable.put("gray60", new Color(153, 153, 153));
        colorTable.put("grey60", new Color(153, 153, 153));
        colorTable.put("gray61", new Color(156, 156, 156));
        colorTable.put("grey61", new Color(156, 156, 156));
        colorTable.put("gray62", new Color(158, 158, 158));
        colorTable.put("grey62", new Color(158, 158, 158));
        colorTable.put("gray63", new Color(161, 161, 161));
        colorTable.put("grey63", new Color(161, 161, 161));
        colorTable.put("gray64", new Color(163, 163, 163));
        colorTable.put("grey64", new Color(163, 163, 163));
        colorTable.put("gray65", new Color(166, 166, 166));
        colorTable.put("grey65", new Color(166, 166, 166));
        colorTable.put("gray66", new Color(168, 168, 168));
        colorTable.put("grey66", new Color(168, 168, 168));
        colorTable.put("gray67", new Color(171, 171, 171));
        colorTable.put("grey67", new Color(171, 171, 171));
        colorTable.put("gray68", new Color(173, 173, 173));
        colorTable.put("grey68", new Color(173, 173, 173));
        colorTable.put("gray69", new Color(176, 176, 176));
        colorTable.put("grey69", new Color(176, 176, 176));
        colorTable.put("gray70", new Color(179, 179, 179));
        colorTable.put("grey70", new Color(179, 179, 179));
        colorTable.put("gray71", new Color(181, 181, 181));
        colorTable.put("grey71", new Color(181, 181, 181));
        colorTable.put("gray72", new Color(184, 184, 184));
        colorTable.put("grey72", new Color(184, 184, 184));
        colorTable.put("gray73", new Color(186, 186, 186));
        colorTable.put("grey73", new Color(186, 186, 186));
        colorTable.put("gray74", new Color(189, 189, 189));
        colorTable.put("grey74", new Color(189, 189, 189));
        colorTable.put("gray75", new Color(191, 191, 191));
        colorTable.put("grey75", new Color(191, 191, 191));
        colorTable.put("gray76", new Color(194, 194, 194));
        colorTable.put("grey76", new Color(194, 194, 194));
        colorTable.put("gray77", new Color(196, 196, 196));
        colorTable.put("grey77", new Color(196, 196, 196));
        colorTable.put("gray78", new Color(199, 199, 199));
        colorTable.put("grey78", new Color(199, 199, 199));
        colorTable.put("gray79", new Color(201, 201, 201));
        colorTable.put("grey79", new Color(201, 201, 201));
        colorTable.put("gray80", new Color(204, 204, 204));
        colorTable.put("grey80", new Color(204, 204, 204));
        colorTable.put("gray81", new Color(207, 207, 207));
        colorTable.put("grey81", new Color(207, 207, 207));
        colorTable.put("gray82", new Color(209, 209, 209));
        colorTable.put("grey82", new Color(209, 209, 209));
        colorTable.put("gray83", new Color(212, 212, 212));
        colorTable.put("grey83", new Color(212, 212, 212));
        colorTable.put("gray84", new Color(214, 214, 214));
        colorTable.put("grey84", new Color(214, 214, 214));
        colorTable.put("gray85", new Color(217, 217, 217));
        colorTable.put("grey85", new Color(217, 217, 217));
        colorTable.put("gray86", new Color(219, 219, 219));
        colorTable.put("grey86", new Color(219, 219, 219));
        colorTable.put("gray87", new Color(222, 222, 222));
        colorTable.put("grey87", new Color(222, 222, 222));
        colorTable.put("gray88", new Color(224, 224, 224));
        colorTable.put("grey88", new Color(224, 224, 224));
        colorTable.put("gray89", new Color(227, 227, 227));
        colorTable.put("grey89", new Color(227, 227, 227));
        colorTable.put("gray90", new Color(229, 229, 229));
        colorTable.put("grey90", new Color(229, 229, 229));
        colorTable.put("gray91", new Color(232, 232, 232));
        colorTable.put("grey91", new Color(232, 232, 232));
        colorTable.put("gray92", new Color(235, 235, 235));
        colorTable.put("grey92", new Color(235, 235, 235));
        colorTable.put("gray93", new Color(237, 237, 237));
        colorTable.put("grey93", new Color(237, 237, 237));
        colorTable.put("gray94", new Color(240, 240, 240));
        colorTable.put("grey94", new Color(240, 240, 240));
        colorTable.put("gray95", new Color(242, 242, 242));
        colorTable.put("grey95", new Color(242, 242, 242));
        colorTable.put("gray96", new Color(245, 245, 245));
        colorTable.put("grey96", new Color(245, 245, 245));
        colorTable.put("gray97", new Color(247, 247, 247));
        colorTable.put("grey97", new Color(247, 247, 247));
        colorTable.put("gray98", new Color(250, 250, 250));
        colorTable.put("grey98", new Color(250, 250, 250));
        colorTable.put("gray99", new Color(252, 252, 252));
        colorTable.put("grey99", new Color(252, 252, 252));
        colorTable.put("gray100", new Color(255, 255, 255));
        colorTable.put("grey100", new Color(255, 255, 255));
        colorTable.put("honeydew", new Color(240, 255, 240));
        colorTable.put("honeydew1", new Color(240, 255, 240));
        colorTable.put("honeydew2", new Color(224, 238, 224));
        colorTable.put("honeydew3", new Color(193, 205, 193));
        colorTable.put("honeydew4", new Color(131, 139, 131));
        colorTable.put("hotpink", new Color(255, 105, 180));
        colorTable.put("hotpink1", new Color(255, 110, 180));
        colorTable.put("hotpink2", new Color(238, 106, 167));
        colorTable.put("hotpink3", new Color(205, 96, 144));
        colorTable.put("hotpink4", new Color(139, 58, 98));
        colorTable.put("indianred", new Color(205, 92, 92));
        colorTable.put("indianred1", new Color(255, 106, 106));
        colorTable.put("indianred2", new Color(238, 99, 99));
        colorTable.put("indianred3", new Color(205, 85, 85));
        colorTable.put("indianred4", new Color(139, 58, 58));
        colorTable.put("indigo", new Color(75, 0, 130));
        colorTable.put("ivory", new Color(255, 255, 240));
        colorTable.put("ivory1", new Color(255, 255, 240));
        colorTable.put("ivory2", new Color(238, 238, 224));
        colorTable.put("ivory3", new Color(205, 205, 193));
        colorTable.put("ivory4", new Color(139, 139, 131));
        colorTable.put("khaki", new Color(240, 230, 140));
        colorTable.put("khaki1", new Color(255, 246, 143));
        colorTable.put("khaki2", new Color(238, 230, 133));
        colorTable.put("khaki3", new Color(205, 198, 115));
        colorTable.put("khaki4", new Color(139, 134, 78));
        colorTable.put("lavender", new Color(230, 230, 250));
        colorTable.put("lavenderblush", new Color(255, 240, 245));
        colorTable.put("lavenderblush1", new Color(255, 240, 245));
        colorTable.put("lavenderblush2", new Color(238, 224, 229));
        colorTable.put("lavenderblush3", new Color(205, 193, 197));
        colorTable.put("lavenderblush4", new Color(139, 131, 134));
        colorTable.put("lawngreen", new Color(124, 252, 0));
        colorTable.put("lemonchiffon", new Color(255, 250, 205));
        colorTable.put("lemonchiffon1", new Color(255, 250, 205));
        colorTable.put("lemonchiffon2", new Color(238, 233, 191));
        colorTable.put("lemonchiffon3", new Color(205, 201, 165));
        colorTable.put("lemonchiffon4", new Color(139, 137, 112));
        colorTable.put("lightblue", new Color(173, 216, 230));
        colorTable.put("lightblue1", new Color(191, 239, 255));
        colorTable.put("lightblue2", new Color(178, 223, 238));
        colorTable.put("lightblue3", new Color(154, 192, 205));
        colorTable.put("lightblue4", new Color(104, 131, 139));
        colorTable.put("lightcoral", new Color(240, 128, 128));
        colorTable.put("lightcyan", new Color(224, 255, 255));
        colorTable.put("lightcyan1", new Color(224, 255, 255));
        colorTable.put("lightcyan2", new Color(209, 238, 238));
        colorTable.put("lightcyan3", new Color(180, 205, 205));
        colorTable.put("lightcyan4", new Color(122, 139, 139));
        colorTable.put("lightgoldenrod", new Color(238, 221, 130));
        colorTable.put("lightgoldenrod1", new Color(255, 236, 139));
        colorTable.put("lightgoldenrod2", new Color(238, 220, 130));
        colorTable.put("lightgoldenrod3", new Color(205, 190, 112));
        colorTable.put("lightgoldenrod4", new Color(139, 129, 76));
        colorTable.put("lightgoldenrodyellow", new Color(250, 250, 210));
        colorTable.put("lightgray", new Color(211, 211, 211));
        colorTable.put("lightgreen", new Color(144, 238, 144));
        colorTable.put("lightgrey", new Color(211, 211, 211));
        colorTable.put("lightpink", new Color(255, 182, 193));
        colorTable.put("lightpink1", new Color(255, 174, 185));
        colorTable.put("lightpink2", new Color(238, 162, 173));
        colorTable.put("lightpink3", new Color(205, 140, 149));
        colorTable.put("lightpink4", new Color(139, 95, 101));
        colorTable.put("lightsalmon", new Color(255, 160, 122));
        colorTable.put("lightsalmon1", new Color(255, 160, 122));
        colorTable.put("lightsalmon2", new Color(238, 149, 114));
        colorTable.put("lightsalmon3", new Color(205, 129, 98));
        colorTable.put("lightsalmon4", new Color(139, 87, 66));
        colorTable.put("lightseagreen", new Color(32, 178, 170));
        colorTable.put("lightskyblue", new Color(135, 206, 250));
        colorTable.put("lightskyblue1", new Color(176, 226, 255));
        colorTable.put("lightskyblue2", new Color(164, 211, 238));
        colorTable.put("lightskyblue3", new Color(141, 182, 205));
        colorTable.put("lightskyblue4", new Color(96, 123, 139));
        colorTable.put("lightslateblue", new Color(132, 112, 255));
        colorTable.put("lightslategray", new Color(119, 136, 153));
        colorTable.put("lightslategrey", new Color(119, 136, 153));
        colorTable.put("lightsteelblue", new Color(176, 196, 222));
        colorTable.put("lightsteelblue1", new Color(202, 225, 255));
        colorTable.put("lightsteelblue2", new Color(188, 210, 238));
        colorTable.put("lightsteelblue3", new Color(162, 181, 205));
        colorTable.put("lightsteelblue4", new Color(110, 123, 139));
        colorTable.put("lightyellow", new Color(255, 255, 224));
        colorTable.put("lightyellow1", new Color(255, 255, 224));
        colorTable.put("lightyellow2", new Color(238, 238, 209));
        colorTable.put("lightyellow3", new Color(205, 205, 180));
        colorTable.put("lightyellow4", new Color(139, 139, 122));
        colorTable.put("limegreen", new Color(50, 205, 50));
        colorTable.put("linen", new Color(250, 240, 230));
        colorTable.put("magenta", new Color(255, 0, 255));
        colorTable.put("magenta1", new Color(255, 0, 255));
        colorTable.put("magenta2", new Color(238, 0, 238));
        colorTable.put("magenta3", new Color(205, 0, 205));
        colorTable.put("magenta4", new Color(139, 0, 139));
        colorTable.put("maroon", new Color(176, 48, 96));
        colorTable.put("maroon1", new Color(255, 52, 179));
        colorTable.put("maroon2", new Color(238, 48, 167));
        colorTable.put("maroon3", new Color(205, 41, 144));
        colorTable.put("maroon4", new Color(139, 28, 98));
        colorTable.put("mediumaquamarine", new Color(102, 205, 170));
        colorTable.put("mediumblue", new Color(0, 0, 205));
        colorTable.put("mediumorchid", new Color(186, 85, 211));
        colorTable.put("mediumorchid1", new Color(224, 102, 255));
        colorTable.put("mediumorchid2", new Color(209, 95, 238));
        colorTable.put("mediumorchid3", new Color(180, 82, 205));
        colorTable.put("mediumorchid4", new Color(122, 55, 139));
        colorTable.put("mediumpurple", new Color(147, 112, 219));
        colorTable.put("mediumpurple1", new Color(171, 130, 255));
        colorTable.put("mediumpurple2", new Color(159, 121, 238));
        colorTable.put("mediumpurple3", new Color(137, 104, 205));
        colorTable.put("mediumpurple4", new Color(93, 71, 139));
        colorTable.put("mediumseagreen", new Color(60, 179, 113));
        colorTable.put("mediumslateblue", new Color(123, 104, 238));
        colorTable.put("mediumspringgreen", new Color(0, 250, 154));
        colorTable.put("mediumturquoise", new Color(72, 209, 204));
        colorTable.put("mediumvioletred", new Color(199, 21, 133));
        colorTable.put("midnightblue", new Color(25, 25, 112));
        colorTable.put("mintcream", new Color(245, 255, 250));
        colorTable.put("mistyrose", new Color(255, 228, 225));
        colorTable.put("mistyrose1", new Color(255, 228, 225));
        colorTable.put("mistyrose2", new Color(238, 213, 210));
        colorTable.put("mistyrose3", new Color(205, 183, 181));
        colorTable.put("mistyrose4", new Color(139, 125, 123));
        colorTable.put("moccasin", new Color(255, 228, 181));
        colorTable.put("navajowhite", new Color(255, 222, 173));
        colorTable.put("navajowhite1", new Color(255, 222, 173));
        colorTable.put("navajowhite2", new Color(238, 207, 161));
        colorTable.put("navajowhite3", new Color(205, 179, 139));
        colorTable.put("navajowhite4", new Color(139, 121, 94));
        colorTable.put("navy", new Color(0, 0, 128));
        colorTable.put("navyblue", new Color(0, 0, 128));
        colorTable.put("oldlace", new Color(253, 245, 230));
        colorTable.put("olivedrab", new Color(107, 142, 35));
        colorTable.put("olivedrab1", new Color(192, 255, 62));
        colorTable.put("olivedrab2", new Color(179, 238, 58));
        colorTable.put("olivedrab3", new Color(154, 205, 50));
        colorTable.put("olivedrab4", new Color(105, 139, 34));
        colorTable.put("orange", new Color(255, 165, 0));
        colorTable.put("orange1", new Color(255, 165, 0));
        colorTable.put("orange2", new Color(238, 154, 0));
        colorTable.put("orange3", new Color(205, 133, 0));
        colorTable.put("orange4", new Color(139, 90, 0));
        colorTable.put("orangered", new Color(255, 69, 0));
        colorTable.put("orangered1", new Color(255, 69, 0));
        colorTable.put("orangered2", new Color(238, 64, 0));
        colorTable.put("orangered3", new Color(205, 55, 0));
        colorTable.put("orangered4", new Color(139, 37, 0));
        colorTable.put("orchid", new Color(218, 112, 214));
        colorTable.put("orchid1", new Color(255, 131, 250));
        colorTable.put("orchid2", new Color(238, 122, 233));
        colorTable.put("orchid3", new Color(205, 105, 201));
        colorTable.put("orchid4", new Color(139, 71, 137));
        colorTable.put("palegoldenrod", new Color(238, 232, 170));
        colorTable.put("palegreen", new Color(152, 251, 152));
        colorTable.put("palegreen1", new Color(154, 255, 154));
        colorTable.put("palegreen2", new Color(144, 238, 144));
        colorTable.put("palegreen3", new Color(124, 205, 124));
        colorTable.put("palegreen4", new Color(84, 139, 84));
        colorTable.put("paleturquoise", new Color(175, 238, 238));
        colorTable.put("paleturquoise1", new Color(187, 255, 255));
        colorTable.put("paleturquoise2", new Color(174, 238, 238));
        colorTable.put("paleturquoise3", new Color(150, 205, 205));
        colorTable.put("paleturquoise4", new Color(102, 139, 139));
        colorTable.put("palevioletred", new Color(219, 112, 147));
        colorTable.put("palevioletred1", new Color(255, 130, 171));
        colorTable.put("palevioletred2", new Color(238, 121, 159));
        colorTable.put("palevioletred3", new Color(205, 104, 137));
        colorTable.put("palevioletred4", new Color(139, 71, 93));
        colorTable.put("papayawhip", new Color(255, 239, 213));
        colorTable.put("peachpuff", new Color(255, 218, 185));
        colorTable.put("peachpuff1", new Color(255, 218, 185));
        colorTable.put("peachpuff2", new Color(238, 203, 173));
        colorTable.put("peachpuff3", new Color(205, 175, 149));
        colorTable.put("peachpuff4", new Color(139, 119, 101));
        colorTable.put("peru", new Color(205, 133, 63));
        colorTable.put("pink", new Color(255, 192, 203));
        colorTable.put("pink1", new Color(255, 181, 197));
        colorTable.put("pink2", new Color(238, 169, 184));
        colorTable.put("pink3", new Color(205, 145, 158));
        colorTable.put("pink4", new Color(139, 99, 108));
        colorTable.put("plum", new Color(221, 160, 221));
        colorTable.put("plum1", new Color(255, 187, 255));
        colorTable.put("plum2", new Color(238, 174, 238));
        colorTable.put("plum3", new Color(205, 150, 205));
        colorTable.put("plum4", new Color(139, 102, 139));
        colorTable.put("powderblue", new Color(176, 224, 230));
        colorTable.put("purple", new Color(160, 32, 240));
        colorTable.put("purple1", new Color(155, 48, 255));
        colorTable.put("purple2", new Color(145, 44, 238));
        colorTable.put("purple3", new Color(125, 38, 205));
        colorTable.put("purple4", new Color(85, 26, 139));
        colorTable.put("red", new Color(255, 0, 0));
        colorTable.put("red1", new Color(255, 0, 0));
        colorTable.put("red2", new Color(238, 0, 0));
        colorTable.put("red3", new Color(205, 0, 0));
        colorTable.put("red4", new Color(139, 0, 0));
        colorTable.put("rosybrown", new Color(188, 143, 143));
        colorTable.put("rosybrown1", new Color(255, 193, 193));
        colorTable.put("rosybrown2", new Color(238, 180, 180));
        colorTable.put("rosybrown3", new Color(205, 155, 155));
        colorTable.put("rosybrown4", new Color(139, 105, 105));
        colorTable.put("royalblue", new Color(65, 105, 225));
        colorTable.put("royalblue1", new Color(72, 118, 255));
        colorTable.put("royalblue2", new Color(67, 110, 238));
        colorTable.put("royalblue3", new Color(58, 95, 205));
        colorTable.put("royalblue4", new Color(39, 64, 139));
        colorTable.put("saddlebrown", new Color(139, 69, 19));
        colorTable.put("salmon", new Color(250, 128, 114));
        colorTable.put("salmon1", new Color(255, 140, 105));
        colorTable.put("salmon2", new Color(238, 130, 98));
        colorTable.put("salmon3", new Color(205, 112, 84));
        colorTable.put("salmon4", new Color(139, 76, 57));
        colorTable.put("sandybrown", new Color(244, 164, 96));
        colorTable.put("seagreen", new Color(46, 139, 87));
        colorTable.put("seagreen1", new Color(84, 255, 159));
        colorTable.put("seagreen2", new Color(78, 238, 148));
        colorTable.put("seagreen3", new Color(67, 205, 128));
        colorTable.put("seagreen4", new Color(46, 139, 87));
        colorTable.put("seashell", new Color(255, 245, 238));
        colorTable.put("seashell1", new Color(255, 245, 238));
        colorTable.put("seashell2", new Color(238, 229, 222));
        colorTable.put("seashell3", new Color(205, 197, 191));
        colorTable.put("seashell4", new Color(139, 134, 130));
        colorTable.put("sgiindigo2", new Color(33, 136, 104));
        colorTable.put("sienna", new Color(160, 82, 45));
        colorTable.put("sienna1", new Color(255, 130, 71));
        colorTable.put("sienna2", new Color(238, 121, 66));
        colorTable.put("sienna3", new Color(205, 104, 57));
        colorTable.put("sienna4", new Color(139, 71, 38));
        colorTable.put("skyblue", new Color(135, 206, 235));
        colorTable.put("skyblue1", new Color(135, 206, 255));
        colorTable.put("skyblue2", new Color(126, 192, 238));
        colorTable.put("skyblue3", new Color(108, 166, 205));
        colorTable.put("skyblue4", new Color(74, 112, 139));
        colorTable.put("slateblue", new Color(106, 90, 205));
        colorTable.put("slateblue1", new Color(131, 111, 255));
        colorTable.put("slateblue2", new Color(122, 103, 238));
        colorTable.put("slateblue3", new Color(105, 89, 205));
        colorTable.put("slateblue4", new Color(71, 60, 139));
        colorTable.put("slategray", new Color(112, 128, 144));
        colorTable.put("slategray1", new Color(198, 226, 255));
        colorTable.put("slategray2", new Color(185, 211, 238));
        colorTable.put("slategray3", new Color(159, 182, 205));
        colorTable.put("slategray4", new Color(108, 123, 139));
        colorTable.put("slategrey", new Color(112, 128, 144));
        colorTable.put("snow", new Color(255, 250, 250));
        colorTable.put("snow1", new Color(255, 250, 250));
        colorTable.put("snow2", new Color(238, 233, 233));
        colorTable.put("snow3", new Color(205, 201, 201));
        colorTable.put("snow4", new Color(139, 137, 137));
        colorTable.put("springgreen", new Color(0, 255, 127));
        colorTable.put("springgreen1", new Color(0, 255, 127));
        colorTable.put("springgreen2", new Color(0, 238, 118));
        colorTable.put("springgreen3", new Color(0, 205, 102));
        colorTable.put("springgreen4", new Color(0, 139, 69));
        colorTable.put("steelblue", new Color(70, 130, 180));
        colorTable.put("steelblue1", new Color(99, 184, 255));
        colorTable.put("steelblue2", new Color(92, 172, 238));
        colorTable.put("steelblue3", new Color(79, 148, 205));
        colorTable.put("steelblue4", new Color(54, 100, 139));
        colorTable.put("tan", new Color(210, 180, 140));
        colorTable.put("tan1", new Color(255, 165, 79));
        colorTable.put("tan2", new Color(238, 154, 73));
        colorTable.put("tan3", new Color(205, 133, 63));
        colorTable.put("tan4", new Color(139, 90, 43));
        colorTable.put("thistle", new Color(216, 191, 216));
        colorTable.put("thistle1", new Color(255, 225, 255));
        colorTable.put("thistle2", new Color(238, 210, 238));
        colorTable.put("thistle3", new Color(205, 181, 205));
        colorTable.put("thistle4", new Color(139, 123, 139));
        colorTable.put("tomato", new Color(255, 99, 71));
        colorTable.put("tomato1", new Color(255, 99, 71));
        colorTable.put("tomato2", new Color(238, 92, 66));
        colorTable.put("tomato3", new Color(205, 79, 57));
        colorTable.put("tomato4", new Color(139, 54, 38));
        colorTable.put("turquoise", new Color(64, 224, 208));
        colorTable.put("turquoise1", new Color(0, 245, 255));
        colorTable.put("turquoise2", new Color(0, 229, 238));
        colorTable.put("turquoise3", new Color(0, 197, 205));
        colorTable.put("turquoise4", new Color(0, 134, 139));
        colorTable.put("violet", new Color(238, 130, 238));
        colorTable.put("violetred", new Color(208, 32, 144));
        colorTable.put("violetred1", new Color(255, 62, 150));
        colorTable.put("violetred2", new Color(238, 58, 140));
        colorTable.put("violetred3", new Color(205, 50, 120));
        colorTable.put("violetred4", new Color(139, 34, 82));
        colorTable.put("wheat", new Color(245, 222, 179));
        colorTable.put("wheat1", new Color(255, 231, 186));
        colorTable.put("wheat2", new Color(238, 216, 174));
        colorTable.put("wheat3", new Color(205, 186, 150));
        colorTable.put("wheat4", new Color(139, 126, 102));
        colorTable.put("white", new Color(255, 255, 255));
        colorTable.put("whitesmoke", new Color(245, 245, 245));
        colorTable.put("yellow", new Color(255, 255, 0));
        colorTable.put("yellow1", new Color(255, 255, 0));
        colorTable.put("yellow2", new Color(238, 238, 0));
        colorTable.put("yellow3", new Color(205, 205, 0));
        colorTable.put("yellow4", new Color(139, 139, 0));
        colorTable.put("yellowgreen", new Color(154, 205, 50));
    }
}
