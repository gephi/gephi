/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.plugin.file;

import java.io.LineNumberReader;
import java.util.Set;
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

        String SEPARATOR = ",;|";
        String line = "";
        for (; reader.ready() && !cancel;) {
            line = reader.readLine();
            if (line != null && !line.isEmpty()) {
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
            }
        }
    }

    private void addEdge(String source, String target) {
        NodeDraft sourceNode = container.getNode(source);
        if (sourceNode == null) {
            sourceNode = container.factory().newNodeDraft();
            sourceNode.setId(source);
            container.addNode(sourceNode);
        }
        NodeDraft targetNode = container.getNode(target);
        if (targetNode == null) {
            targetNode = container.factory().newNodeDraft();
            targetNode.setId(source);
            container.addNode(targetNode);
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
