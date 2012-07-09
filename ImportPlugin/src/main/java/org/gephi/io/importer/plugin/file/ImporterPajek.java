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
import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.gephi.io.importer.api.ContainerLoader;
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
 */
public class ImporterPajek implements FileImporter, LongTask {

    // Crayola colors used by Pajek
    private static final HashMap<String, Integer> PAJEK_COLORS = new HashMap<String, Integer>();

    static {
        PAJEK_COLORS.put("Apricot", 0xFDD9B5);
        PAJEK_COLORS.put("Aquamarine", 0x78DBE2);
        PAJEK_COLORS.put("Bittersweet", 0xFD7C6E);
        PAJEK_COLORS.put("Black", 0x232323);
        PAJEK_COLORS.put("Blue", 0x1F75FE);
        PAJEK_COLORS.put("BlueGreen", 0x199EBD);
        PAJEK_COLORS.put("BlueViolet", 0x7366BD);
        PAJEK_COLORS.put("BrickRed", 0xCB4154);
        PAJEK_COLORS.put("Brown", 0xB4674D);
        PAJEK_COLORS.put("BurntOrange", 0xFF7F49);
        PAJEK_COLORS.put("CadetBlue", 0xB0B7C6);
        PAJEK_COLORS.put("Canary", 0xFFFF99);
        PAJEK_COLORS.put("CarnationPink", 0xFFAACC);
        PAJEK_COLORS.put("Cerulean", 0x1DACD6);
        PAJEK_COLORS.put("CornflowerBlue", 0x9ACEEB);
        PAJEK_COLORS.put("Cyan", 0x00FFFF);
        PAJEK_COLORS.put("Dandelion", 0xFDDB6D);
        PAJEK_COLORS.put("DarkOrchid", 0xFDDB7D);
        PAJEK_COLORS.put("Emerald", 0x50C878);
        PAJEK_COLORS.put("ForestGreen", 0x6DAE81);
        PAJEK_COLORS.put("Fuchsia", 0xC364C5);
        PAJEK_COLORS.put("Goldenrod", 0xFCD975);
        PAJEK_COLORS.put("Gray", 0x95918C);
        PAJEK_COLORS.put("Gray05", 0x0D0D0D);
        PAJEK_COLORS.put("Gray10", 0x1A1A1A);
        PAJEK_COLORS.put("Gray15", 0x262626);
        PAJEK_COLORS.put("Gray20", 0x333333);
        PAJEK_COLORS.put("Gray25", 0x404040);
        PAJEK_COLORS.put("Gray30", 0x4D4D4D);
        PAJEK_COLORS.put("Gray35", 0x595959);
        PAJEK_COLORS.put("Gray40", 0x666666);
        PAJEK_COLORS.put("Gray45", 0x737373);
        PAJEK_COLORS.put("Gray55", 0x8C8C8C);
        PAJEK_COLORS.put("Gray60", 0x999999);
        PAJEK_COLORS.put("Gray65", 0xA6A6A6);
        PAJEK_COLORS.put("Gray70", 0xB3B3B3);
        PAJEK_COLORS.put("Gray75", 0xBFBFBF);
        PAJEK_COLORS.put("Gray80", 0xCCCCCC);
        PAJEK_COLORS.put("Gray85", 0xD9D9D9);
        PAJEK_COLORS.put("Gray90", 0xE5E5E5);
        PAJEK_COLORS.put("Gray95", 0xF2F2F2);
        PAJEK_COLORS.put("Green", 0x1CAC78);
        PAJEK_COLORS.put("GreenYellow", 0xF0E891);
        PAJEK_COLORS.put("JungleGreen", 0x3BB08F);
        PAJEK_COLORS.put("Lavender", 0xFCB4D5);
        PAJEK_COLORS.put("LFadedGreen", 0x548B54);
        PAJEK_COLORS.put("LightCyan", 0xE0FFFF);
        PAJEK_COLORS.put("LightGreen", 0x90EE90);
        PAJEK_COLORS.put("LightMagenta", 0xFF00FF);
        PAJEK_COLORS.put("LightOrange", 0xFF6F1A);
        PAJEK_COLORS.put("LightPurple", 0xE066FF);
        PAJEK_COLORS.put("LightYellow", 0xFFFFE0);
        PAJEK_COLORS.put("LimeGreen", 0x32CD32);
        PAJEK_COLORS.put("LSkyBlue", 0x87CEFA);
        PAJEK_COLORS.put("Magenta", 0xF664AF);
        PAJEK_COLORS.put("Mahogany", 0xCD4A4A);
        PAJEK_COLORS.put("Maroon", 0xC8385A);
        PAJEK_COLORS.put("Melon", 0xFDBCB4);
        PAJEK_COLORS.put("MidnightBlue", 0x1A4876);
        PAJEK_COLORS.put("Mulberry", 0xAA709F);
        PAJEK_COLORS.put("NavyBlue", 0x1974D2);
        PAJEK_COLORS.put("OliveGreen", 0xBAB86C);
        PAJEK_COLORS.put("Orange", 0xFF7538);
        PAJEK_COLORS.put("OrangeRed", 0xFF5349);
        PAJEK_COLORS.put("Orchid", 0xE6A8D7);
        PAJEK_COLORS.put("Peach", 0xFFCFAB);
        PAJEK_COLORS.put("Periwinkle", 0xC5D0E6);
        PAJEK_COLORS.put("PineGreen", 0x158078);
        PAJEK_COLORS.put("Pink", 0xFFC0CB);
        PAJEK_COLORS.put("Plum", 0x8E4585);
        PAJEK_COLORS.put("ProcessBlue", 0x4169E1);
        PAJEK_COLORS.put("Purple", 0x926EAE);
        PAJEK_COLORS.put("RawSienna", 0xD68A59);
        PAJEK_COLORS.put("Red", 0xEE204D);
        PAJEK_COLORS.put("RedOrange", 0xFF5349);
        PAJEK_COLORS.put("RedViolet", 0xC0448F);
        PAJEK_COLORS.put("Rhodamine", 0xE0119D);
        PAJEK_COLORS.put("RoyalBlue", 0x4169E1);
        PAJEK_COLORS.put("RoyalPurple", 0x7851A9);
        PAJEK_COLORS.put("RubineRed", 0xCA005D);
        PAJEK_COLORS.put("Salmon", 0xFF9BAA);
        PAJEK_COLORS.put("SeaGreen", 0x9FE2BF);
        PAJEK_COLORS.put("Sepia", 0xA5694F);
        PAJEK_COLORS.put("SkyBlue", 0x80DAEB);
        PAJEK_COLORS.put("SpringGreen", 0xECEABE);
        PAJEK_COLORS.put("Tan", 0xFAA76C);
        PAJEK_COLORS.put("TealBlue", 0x008080);
        PAJEK_COLORS.put("Thistle", 0xD8BFD8);
        PAJEK_COLORS.put("Turquoise", 0x77DDE7);
        PAJEK_COLORS.put("Violet", 0x926EAE);
        PAJEK_COLORS.put("VioletRed", 0xF75394);
        PAJEK_COLORS.put("White", 0xEDEDED);
        PAJEK_COLORS.put("WildStrawberry", 0xFF43A4);
        PAJEK_COLORS.put("Yellow", 0xFCE883);
        PAJEK_COLORS.put("YellowGreen", 0xC5E384);
        PAJEK_COLORS.put("YellowOrange", 0xFFB653);
    }
    //Architecture
    private Reader reader;
    private LineNumberReader lineReader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Node data
    private NodeDraft[] verticesArray;

    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        lineReader = ImportUtils.getTextReader(reader);
        try {
            importData(lineReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return !cancel;
    }

    private void importData(LineNumberReader reader) throws Exception {
        Progress.start(progressTicket);        //Progress

        try {
            // ignore everything until we see '*Vertices'
            String curLine = skip(reader, "*vertices");

            if (curLine == null) // no vertices in the graph; return empty graph
            {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat1"), Issue.Level.CRITICAL));
            }

            // create appropriate number of vertices
            StringTokenizer stringTokenizer = new StringTokenizer(curLine);
            stringTokenizer.nextToken(); // skip past "*vertices";
            int num_vertices = Integer.parseInt(stringTokenizer.nextToken());

            Progress.switchToDeterminate(progressTicket, num_vertices);        //Progress

            verticesArray = new NodeDraft[num_vertices];
            for (int i = 0; i < num_vertices; i++) {
                NodeDraft node = container.factory().newNodeDraft();
                String label = "" + (i + 1);
                node.setId(label);
                node.setLabel(label);
                verticesArray[i] = node;
            }

            curLine = null;
            while (reader.ready()) {
                if (cancel) {
                    reader.close();
                    return;
                }
                curLine = reader.readLine();
                if (curLine == null || curLine.startsWith("*")) {
                    break;
                }
                if (curLine.isEmpty()) { // skip blank lines
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat2", reader.getLineNumber()), Issue.Level.WARNING));
                    continue;
                }

                try {
                    readVertex(curLine, num_vertices);
                } catch (IllegalArgumentException iae) {
                    reader.close();
                    throw iae;
                }

                Progress.progress(progressTicket);
            }

            //Append nodes
            for (NodeDraft node : verticesArray) {
                container.addNode(node);
            }

            //Get arcs
            curLine = readArcsOrEdges(curLine, reader);

            //Get edges
            readArcsOrEdges(curLine, reader);

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Progress.finish(progressTicket);
    }

    private void readVertex(String curLine, int num_vertices) throws Exception {
        String[] parts = null;
        int firstParts = -1;     // index of first coordinate in parts; -1 indicates no coordinates found
        String index;
        String label = null;
        // if there are quote marks on this line, split on them; label is surrounded by them
        if (curLine.indexOf('"') != -1) {
            String[] initial_split = curLine.trim().split("\"");
            // if there are any quote marks, there should be exactly 2
            if (initial_split.length < 1 || initial_split.length > 3) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat3", lineReader.getLineNumber()), Issue.Level.SEVERE));
            }
            index = initial_split[0].trim();
            if (initial_split.length > 1) {
                label = initial_split[1].trim();
            }

            if (initial_split.length == 3) {
                parts = initial_split[2].trim().split("\\s+", -1);
            }
            firstParts = 0;
        } else // no quote marks, but are there coordinates?
        {
            parts = curLine.trim().split("\\s+", -1);
            index = parts[0];
            switch (parts.length) {
                case 1:         // just the ID; nothing to do, continue
                    break;
                case 2:         // just the ID and a label
                    label = parts[1];
                    break;
                case 3:         // ID, no label, coordinates
                    firstParts = 1;
                    break;
                case 4:         // ID, label, (x,y) coordinates
                    firstParts = 2;
                    break;
            }
        }
        int v_id = Integer.parseInt(index) - 1; // go from 1-based to 0-based index
        if (v_id >= num_vertices || v_id < 0) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat4", v_id, num_vertices), Issue.Level.SEVERE));
        }

        NodeDraft node = verticesArray[v_id];

        // only attach the label if there's one to attach
        if (label != null && label.length() > 0) {
            node.setLabel(label);
        }

        // parse the rest of the line
        if (firstParts != -1 && parts != null && parts.length >= firstParts + 2) {
            int i = firstParts;
            //Coordinates
            if (i < parts.length - 1) {
                try {
                    float x = Float.parseFloat(parts[i]);
                    float y = Float.parseFloat(parts[i + 1]);

                    node.setX(x);
                    node.setY(y);

                    i++;
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat5", lineReader.getLineNumber()), Issue.Level.WARNING));
                }
            }

            //Size
            if (i < parts.length - 1) {
                try {
                    float size = Float.parseFloat(parts[i]);

                    node.setSize(size);

                    i++;
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat6", lineReader.getLineNumber()), Issue.Level.WARNING));
                }
            }

            // parse colors
            for (; i < parts.length - 1; i++) {
                // node's internal color
                if ("ic".equals(parts[i])) {
                    String colorName = parts[i + 1].replaceAll(" ", ""); // remove spaces from color's name so we can look it up
                    Color color = getPajekColorFromName(colorName);
                    node.setColor(color);
                    break;
                }
            }
        }
    }

    private String readArcsOrEdges(String curLine, BufferedReader br) throws Exception {
        String nextLine = curLine;

        boolean reading_arcs = false;
        boolean reading_edges = false;

        if (nextLine.toLowerCase().startsWith("*arcs")) {
            reading_arcs = true;
        } else if (nextLine.toLowerCase().startsWith("*edges")) {
            reading_edges = true;
        }

        if (!(reading_arcs || reading_edges)) {
            return nextLine;
        }

        boolean is_list = false;
        if (nextLine.toLowerCase().endsWith("list")) {
            is_list = true;
        }

        while (br.ready()) {
            if (cancel) {
                return nextLine;
            }
            nextLine = br.readLine();
            if (nextLine == null || nextLine.startsWith("*")) {
                break;
            }
            if (nextLine.equals("")) { // skip blank lines
                report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat2", lineReader.getLineNumber()), Issue.Level.WARNING));
                continue;
            }

            StringTokenizer st = new StringTokenizer(nextLine.trim());

            int vid1 = Integer.parseInt(st.nextToken()) - 1;
            NodeDraft nodeFrom = verticesArray[vid1];

            if (is_list) // one source, multiple destinations
            {
                do {
                    int vid2 = Integer.parseInt(st.nextToken()) - 1;
                    NodeDraft nodeTo = verticesArray[vid2];
                    EdgeDraft edge = container.factory().newEdgeDraft();
                    edge.setSource(nodeFrom);
                    edge.setTarget(nodeTo);
                    container.addEdge(edge);
                } while (st.hasMoreTokens());
            } else // one source, one destination, at most one weight
            {
                int vid2 = Integer.parseInt(st.nextToken()) - 1;
                NodeDraft nodeTo = verticesArray[vid2];
                EdgeDraft edge = container.factory().newEdgeDraft();
                edge.setSource(nodeFrom);
                edge.setTarget(nodeTo);

                // get the edge weight if we care
                if (st.hasMoreTokens()) {
                    float edgeWeight = 1f;
                    try {
                        edgeWeight = new Float(st.nextToken());
                    } catch (Exception e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterPajek.class, "importerNET_error_dataformat7", lineReader.getLineNumber()), Issue.Level.WARNING));
                    }

                    edge.setWeight(edgeWeight);
                }

                container.addEdge(edge);
            }
        }
        return nextLine;
    }

    private Color getPajekColorFromName(String colorName) {
        Integer colorHex = PAJEK_COLORS.get(colorName);

        Color color = null;
        if (colorHex != null) {
            color = new Color(colorHex);
        }

        return color;
    }

    private String skip(BufferedReader br, String str) throws Exception {
        while (br.ready()) {
            String curLine = br.readLine();
            if (curLine == null) {
                break;
            }
            curLine = curLine.trim();
            if (curLine.toLowerCase().startsWith(str)) {
                return curLine;
            }
        }
        return null;
    }

    public boolean cancel() {
        cancel = true;
        return true;
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

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
