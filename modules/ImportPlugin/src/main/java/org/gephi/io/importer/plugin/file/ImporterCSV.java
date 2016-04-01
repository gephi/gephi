/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Sebastien Heymann <sebastien.heymann@gephi.org>
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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Mathieu Bastian, Sebastien Heymann
 */
public class ImporterCSV implements FileImporter, LongTask {

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
        Progress.start(progressTicket);        //Progress

        List<String> lines = new ArrayList<>();
        for (; reader.ready();) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                lines.add(line);
            }
        }

        Progress.switchToDeterminate(progressTicket, lines.size());

        //Magix regex
        Pattern pattern = Pattern.compile("(?<=(?:,|;|\\s|^)\")(.*?)(?=(?<=(?:[^\\\\]))\",|;|\"\\s|\"$)|(?<=(?:,|;|\\s|^)')(.*?)(?=(?<=(?:[^\\\\]))',|;|'\\s|'$)|(?<=(?:,|;|\\s|^))(?=[^'\"])(.*?)(?=(?:,|;|\\s|$))|(?<=,|;)($)");

        if (lines.get(0).startsWith(";")) { //Matrix
            //Fill the Labels array
            String line0 = lines.get(0);
            line0 = line0.substring(1, line0.length());
            lines.remove(0);
            Matcher m = pattern.matcher(line0); //Remove the first ";"
            List<String> labels = new ArrayList<>();
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                if (start != end) {
                    String data = line0.substring(start, end);
                    data = data.trim();
                    if (!data.isEmpty() && !data.toLowerCase().equals("null")) {
                        labels.add(data);
                    }
                }
            }

            int size = lines.size();
            if (size != labels.size()) {
                throw new Exception(
                        MessageFormat.format("Inconsistent number of matrix lines compared to the number of labels. {0} lines, {1} labels", size, labels.size())
                );
            }

            for (int i = 0; i < size; i++) {
                if (cancel) {
                    return;
                }
                String line = lines.get(i);
                m = pattern.matcher(line);
                int count = -1;
                String sourceID = "";
                while (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    if (start != end) {
                        String data = line.substring(start, end);
                        data = data.trim();
                        if (!data.isEmpty() && !data.toLowerCase().equals("null")) {
                            if (count == -1) {
                                sourceID = data;
                                addNode(sourceID, labels.get(i));
                            } else if (!data.equals("0")) {
                                //Create Edge
                                addEdge(sourceID, labels.get(count), Float.parseFloat(data));
                            }
                        }
                    }
                    count++;
                }
                Progress.progress(progressTicket);      //Progress
            }
        } else { //Edge or Adjacency list
            Matcher m;
            for (String line : lines) {
                if (cancel) {
                    return;
                }
                m = pattern.matcher(line);
                int count = 0;
                String sourceID = "";
                while (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    if (start != end) {
                        String data = line.substring(start, end);
                        data = data.trim();
                        if (!data.isEmpty() && !data.toLowerCase().equals("null")) {
                            if (count == 0) {
                                sourceID = data;
                                addNode(sourceID, data);
                            } else {
                                //Create Edge
                                addEdge(sourceID, data);
                            }
                        }
                    }
                    count++;
                }
                Progress.progress(progressTicket);      //Progress
            }
        }

    }

    private void addNode(String id, String label) {
        NodeDraft node;
        if (!container.nodeExists(id)) {
            node = container.factory().newNodeDraft(id);
            node.setLabel(label);
            container.addNode(node);
        }
    }

    private void addEdge(String source, String target) {
        addEdge(source, target, 1);
    }

    private void addEdge(String source, String target, float weight) {
        NodeDraft sourceNode;
        if (!container.nodeExists(source)) {
            sourceNode = container.factory().newNodeDraft(source);
            container.addNode(sourceNode);
        } else {
            sourceNode = container.getNode(source);
        }
        NodeDraft targetNode;
        if (!container.nodeExists(target)) {
            targetNode = container.factory().newNodeDraft(target);
            container.addNode(targetNode);
        } else {
            targetNode = container.getNode(target);
        }
        EdgeDraft edge = container.factory().newEdgeDraft();
        edge.setSource(sourceNode);
        edge.setTarget(targetNode);
        edge.setWeight(weight);
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
