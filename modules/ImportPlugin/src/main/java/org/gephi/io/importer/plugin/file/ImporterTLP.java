/*
 Copyright 2008-2010 Gephi
 Authors : Sebastien Heymann <sebastien.heymann@gephi.org>
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
            String id = tokens[i];
            NodeDraft node = container.factory().newNodeDraft(id);
            container.addNode(node);
        }
    }

    private void parseEdge(String[] tokens, int cptLine) {
        if (tokens.length != 4) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGDF.class, "importerTPL_error_dataformat1", cptLine), Issue.Level.WARNING));
        }
        String id = tokens[1];
        EdgeDraft edge = container.factory().newEdgeDraft(id);
        NodeDraft source = container.getNode(tokens[2]);
        NodeDraft target = container.getNode(tokens[3]);
        edge.setSource(source);
        edge.setTarget(target);
        container.addEdge(edge);
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
