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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
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

    //Architecture
    private Reader reader;
    private LineNumberReader lineReader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Node data
    private NodeDraft[] verticesArray;

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        lineReader = ImportUtils.getTextReader(reader);
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
                String label = "" + (i + 1);
                NodeDraft node = container.factory().newNodeDraft(label);
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
                    node.setColor(colorName);
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
                    double edgeWeight = 1.0;
                    try {
                        edgeWeight = new Double(st.nextToken());
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

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
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
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
