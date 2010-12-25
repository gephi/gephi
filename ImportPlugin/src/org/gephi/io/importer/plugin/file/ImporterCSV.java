/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Sebastien Heymann <sebastien.heymann@gephi.org>
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

import java.io.LineNumberReader;
import java.io.Reader;
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
        Progress.start(progressTicket);        //Progress

        List<String> lines = new ArrayList<String>();
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
            List<String> labels = new ArrayList<String>();
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
                throw new Exception("Inconsistent number of matrix lines compared to the number of labels.");
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
            node = container.factory().newNodeDraft();
            node.setId(id);
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
            sourceNode = container.factory().newNodeDraft();
            sourceNode.setId(source);
            container.addNode(sourceNode);
        } else {
            sourceNode = container.getNode(source);
        }
        NodeDraft targetNode;
        if (!container.nodeExists(target)) {
            targetNode = container.factory().newNodeDraft();
            targetNode.setId(target);
            container.addNode(targetNode);
        } else {
            targetNode = container.getNode(target);
        }
        EdgeDraft edge = container.getEdge(sourceNode, targetNode);
        if (edge == null) {
            edge = container.factory().newEdgeDraft();
            edge.setSource(sourceNode);
            edge.setTarget(targetNode);
            container.addEdge(edge);
        } else {
            edge.setWeight(edge.getWeight() + weight);
        }
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
