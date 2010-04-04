/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.plugin.file;

import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileFormatImporter;
import org.gephi.io.importer.spi.TextImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FileFormatImporter.class)
public class ImporterCSV implements TextImporter, LongTask {

    //Architecture
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    public boolean importData(LineNumberReader reader, ContainerLoader container, Report report) throws Exception {
        this.container = container;
        this.report = report;

        try {
            importData(reader);
        } catch (Exception e) {
            clean();
            throw e;
        }
        boolean result = !cancel;
        clean();
        return result;
    }

    private void clean() {
        this.container = null;
        this.report = null;
        this.cancel = false;
        this.progressTicket = null;
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

        String SEPARATOR = ",;|";
        for (String line : lines) {
            if (cancel) {
                break;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, SEPARATOR);
            String source = null;
            String target;
            for (int i = 0; tokenizer.hasMoreElements(); i++) {
                if (i == 0) {
                    source = tokenizer.nextToken();
                } else {
                    target = tokenizer.nextToken();
                    addEdge(source, target);
                }
            }

            Progress.progress(progressTicket);
        }
    }

    private void addEdge(String source, String target) {
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
            edge.setWeight(edge.getWeight() + 1f);
        }
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".csv", NbBundle.getMessage(getClass(), "fileType_CSV_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.hasExt("csv");
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
