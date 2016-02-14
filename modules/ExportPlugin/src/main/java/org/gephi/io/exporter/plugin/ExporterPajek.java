/*
 Copyright 2008-2011 Gephi
 Authors : Daniel Bernardes <daniel.bernardes@polytechnique.edu>
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
package org.gephi.io.exporter.plugin;

import java.io.Writer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Daniel Bernardes
 */
public class ExporterPajek implements GraphExporter, CharacterExporter, LongTask {

    // Options
    private boolean exportPosition = true;
    private boolean exportEdgeWeight = true;
    // Architecture
    private Workspace workspace;
    private Writer writer;
    private boolean exportVisible;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public void setExportEdgeWeight(boolean exportEdgeWeight) {
        this.exportEdgeWeight = exportEdgeWeight;
    }

    public boolean isExportEdgeWeight() {
        return exportEdgeWeight;
    }

    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    public boolean isExportPosition() {
        return exportPosition;
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

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "ExporterPajek_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".net", NbBundle.getMessage(getClass(), "fileType_Pajek_Name"));
        return new FileType[]{ft};
    }

    @Override
    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();

        try {
            exportData(graph);
        } catch (Exception e) {
            Logger.getLogger(ExporterPajek.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private void exportData(Graph graph) throws Exception {
        int max = graph.getNodeCount(), i = 1;
        HashMap<String, Integer> idx = new HashMap<>(3 * max / 2 + 1);

        Progress.start(progressTicket, max);

        writer.append("*Vertices " + max + "\n");

        NodeIterable nodeIterable = graph.getNodes();
        for (Node node : nodeIterable) {
            writer.append(Integer.toString(i));
            writer.append(" \"" + node.getLabel() + "\"");
            if (exportPosition) {
                writer.append(" " + node.x() + " " + node.y() + " " + node.z());
            }
            writer.append("\n");
            idx.put(node.getId().toString(), i++); // assigns Ids from the interval [1..max]

            if (cancel) {
                nodeIterable.doBreak();
                return;
            }
        }

        if (graph.isUndirected()) {
            writer.append("*Edges\n");
        } else {
            writer.append("*Arcs\n");
        }

        EdgeIterable edgeIterable = graph.getEdges();
        for (Edge edge : edgeIterable) {
            if (cancel) {
                edgeIterable.doBreak();
                return;
            }
            writer.append(Integer.toString(idx.get(edge.getSource().getId().toString())) + " ");
            writer.append(Integer.toString(idx.get(edge.getTarget().getId().toString())));
            if (exportEdgeWeight) {
                writer.append(" " + edge.getWeight());
            }
            writer.append("\n");

            Progress.progress(progressTicket);
        }

        Progress.finish(progressTicket);
    }
}
