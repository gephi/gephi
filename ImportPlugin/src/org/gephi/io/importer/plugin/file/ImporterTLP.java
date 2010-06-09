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
package org.gephi.io.importer.plugin.file;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
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
 * @author Sebastien Heymann
 */
public class ImporterTLP implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = report;
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

        walkFile(reader);

        Progress.finish(progressTicket);
    }

    private void walkFile(BufferedReader reader) throws Exception {
        int cptLine = 0;
        int state = 0; // 0=topology, 1=properties
        while (reader.ready() && !cancel) {
            String line = reader.readLine();
            if (!isComment(line)) {
                String[] tokens = line.split("\\s|\\)");
                if (tokens.length > 0) {
                    if (state == 0) {
                        // topology
                        if (tokens[0].equals("(nodes")) {
                            //Nodes
                            parseNodes(tokens);
                        } else if (tokens[0].equals("(edge")) {
                            //Edges
                            parseEdge(tokens, cptLine);
                        } else if (tokens[0].equals("(property")) {
                            //switch to properties grabbing
                            state = 1;
                        }
                    }
                    if (state == 1) {
                        // properties
                        // exit loop for the moment
                        return;
                    }
                }
            }
            cptLine++;
        }
    }

    private boolean isComment(String s) {
        return s.startsWith(";");
    }

    private void parseNodes(String[] tokens) {
        for (int i = 1; i < tokens.length; i++) {
            NodeDraft node = container.factory().newNodeDraft();
            String id = tokens[i];
            node.setId(id);
            container.addNode(node);
        }
    }

    private void parseEdge(String[] tokens, int cptLine) {
        if (tokens.length != 4) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerTPL_error_dataformat1", cptLine), Issue.Level.WARNING));
        }
        EdgeDraft edge = container.factory().newEdgeDraft();
        String id = tokens[1];
        NodeDraft source = container.getNode(tokens[2]);
        NodeDraft target = container.getNode(tokens[3]);
        edge.setId(id);
        edge.setSource(source);
        edge.setTarget(target);
        container.addEdge(edge);
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
