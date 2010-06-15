/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.plugin.file;

import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ImporterDL implements FileImporter, LongTask {

    //enum
    private enum Format {

        FULLMATRIX, EDGELIST1
    };
    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Data
    private Format format = Format.FULLMATRIX;
    private Map<String, String> headerMap;
    private int numNodes;
    private int numMatricies;
    private int dataLineStartDelta = -1;

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

        if (lines.isEmpty() || (!lines.get(0).startsWith("DL") && !lines.get(0).startsWith("dl"))) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_firstline"), Issue.Level.CRITICAL));
        }

        headerMap = new HashMap<String, String>();
        readHeaderLine(lines.get(0).substring(2));

        int i = 1;
        for (; i < lines.size(); i++) {
            String line = lines.get(i).toLowerCase();
            if (line.trim().endsWith("data:") || line.trim().endsWith("labels:")) {
                break;
            } else {
                readHeaderLine(line);
            }
        }

        computeHeaders();

        if (lines.get(i).toLowerCase().trim().endsWith("labels:") && lines.size() > i + 1) {
            readLabels(lines.get(++i));
        }

        int dataLineStart = -1;
        for (; i < lines.size(); i++) {
            String line = lines.get(i).toLowerCase();
            if (line.trim().endsWith("data:")) {
                dataLineStart = i + 1;
                break;
            }
        }
        if (dataLineStart == -1) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_nodata"), Issue.Level.SEVERE));
        } else if (lines.size() > dataLineStart) {
            dataLineStartDelta = dataLineStart + 1;
            lines = lines.subList(dataLineStart, lines.size());

            if (format.equals(Format.FULLMATRIX)) {
                readeMatrixBlock(lines);
            } else if (format.equals(Format.EDGELIST1)) {
                readEdgelistBlock(lines);
            }
        }
    }

    private void readHeaderLine(String line) {
        StringTokenizer firstLineTokenizer = new StringTokenizer(line, " ,;");
        while (firstLineTokenizer.hasMoreTokens()) {
            String tag = firstLineTokenizer.nextToken().toLowerCase();
            if (tag.indexOf("=") != -1) {
                headerMap.put(tag.substring(0, tag.indexOf("=")).trim(), tag.substring(tag.indexOf("=") + 1).trim());
            } else {
                //report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_unknowntag", tag), Issue.Level.WARNING));
            }
        }
    }

    private void computeHeaders() {
        //read format
        String form = (String) headerMap.get("format");
        if (form == null) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_formatmissing"), Issue.Level.INFO));
        } else if (!form.equals("edgelist1") && !form.equals("fullmatrix")) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_badformat", form), Issue.Level.SEVERE));
        } else if (form.equals("edgelist1")) {
            format = Format.EDGELIST1;
        } else if (form.equals("fullmatrix")) {
            format = Format.FULLMATRIX;
        }

        // read number of nodes
        try {
            String nArg = (String) headerMap.get("n");
            numNodes = Integer.parseInt(nArg);
        } catch (Exception e) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_nmissing"), Issue.Level.SEVERE));
        }

        // read number matricies
        String mats = (String) headerMap.get("nm");
        if (mats != null) {
            try {
                numMatricies = Integer.parseInt(mats);
            } catch (Exception e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_mmissing"), Issue.Level.SEVERE));
            }
        } else {
            numMatricies = 1;
        }
    }

    private void readLabels(String labels) {
        StringTokenizer labelkonizer = new StringTokenizer(labels, ",");
        // check that there are the right number of labels
        if (labelkonizer.countTokens() != numNodes) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_labelscount", labelkonizer.countTokens(), numNodes), Issue.Level.SEVERE));
        }
        int nodeCount = 0;
        while (labelkonizer.hasMoreTokens()) {
            String label = labelkonizer.nextToken();
            nodeCount++;
            NodeDraft nodeDraft = container.factory().newNodeDraft();
            nodeDraft.setId("" + nodeCount);
            nodeDraft.setLabel(label);
            container.addNode(nodeDraft);
        }
    }

    private void readeMatrixBlock(List<String> data) {
        int startTime = 0;
        for (int i = 0; i < data.size(); i++) {
            int rowNum = 0;
            for (; i < data.size() && !data.get(i).trim().equals("!"); i++) {
                if (rowNum <= numNodes) {
                    readMatrixRow(data.get(i), i, rowNum, startTime, startTime + 1);
                    rowNum++;
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_matrixrowscount", rowNum, numNodes), Issue.Level.SEVERE));
                    break;
                }
            }
            if (rowNum < numNodes) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_matrixrowscount2", rowNum, numNodes), Issue.Level.SEVERE));
            }
            startTime++;
        }
        if (startTime != numMatricies) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_matriciescount", startTime, numMatricies), Issue.Level.SEVERE));
        }
    }

    private void readMatrixRow(String line, int pointer, int row, int startTime, int endTime) {
        StringTokenizer rowkonizer = new StringTokenizer(line, " ");
        int from = row + 1;
        int to = 1;
        double weight = 0;
        while (rowkonizer.hasMoreTokens()) {
            String toParse = (String) rowkonizer.nextToken();
            if (to > numNodes) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_matrixentriescount", row, startTime, getLineNumber(pointer)), Issue.Level.SEVERE));
            }
            try {
                weight = Double.parseDouble(toParse);
            } catch (Exception e) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_weightparseerror", toParse, startTime, getLineNumber(pointer)), Issue.Level.SEVERE));
            }

            if (weight != 0) {
                NodeDraft sourceNode = container.getNode("" + from);
                NodeDraft targetNode = container.getNode("" + to);
                EdgeDraft edgeDraft = null;
                if (container.edgeExists(sourceNode, targetNode)) {
                    edgeDraft = container.getEdge(sourceNode, targetNode);
                    //dynamic
                } else {
                    edgeDraft = container.factory().newEdgeDraft();
                    edgeDraft.setSource(sourceNode);
                    edgeDraft.setTarget(targetNode);
                    edgeDraft.setWeight((float) weight);
                    container.addEdge(edgeDraft);
                }
            }
            to++;
        }
    }

    private void readEdgelistBlock(List<String> data) {
        int startTime = 0;
        for (int i = 0; i < data.size(); i++) {
            for (; i < data.size() && !data.get(i).trim().equals("!"); i++) {
                readEdgelistRow(data.get(i), i, startTime, startTime + 1);
            }
            // increment the time step before starting next matrix
            startTime++;
        }
        if (startTime != numMatricies) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_edgelistssetscount", startTime, numMatricies), Issue.Level.SEVERE));
        }
    }

    private void readEdgelistRow(String row, int pointer, double startTime, double endTime) {
        StringTokenizer rowkonizer = new StringTokenizer(row, " ");
        // should have three entries, int from, int to, weight
        String from = rowkonizer.nextToken();
        String to = rowkonizer.nextToken();
        double weight = -1;

        String weightParse = rowkonizer.nextToken();
        weight = 1.0;
        try {
            weight = Double.parseDouble(weightParse);
        } catch (Exception e) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterDL.class, "importerDL_error_edgeparseweight", weightParse, getLineNumber(pointer)), Issue.Level.WARNING));
        }

        NodeDraft sourceNode = container.getNode(from);
        NodeDraft targetNode = container.getNode(to);
        EdgeDraft edgeDraft = null;
        if (container.edgeExists(sourceNode, targetNode)) {
            edgeDraft = container.getEdge(sourceNode, targetNode);
            //dynamic
        } else {
            edgeDraft = container.factory().newEdgeDraft();
            edgeDraft.setSource(sourceNode);
            edgeDraft.setTarget(targetNode);
            edgeDraft.setWeight((float) weight);
            container.addEdge(edgeDraft);
        }
    }

    private int getLineNumber(int pointer) {
        return pointer + dataLineStartDelta;
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
