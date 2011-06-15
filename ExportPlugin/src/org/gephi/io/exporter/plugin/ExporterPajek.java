/*
Copyright 2008-2011 Gephi
Authors : Daniel Bernardes <daniel.bernardes@polytechnique.edu>
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
package org.gephi.io.exporter.plugin;

import java.io.Writer;
import java.util.HashMap;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
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
    
    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public boolean isExportVisible() {
        return exportVisible;
    }

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

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

    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        HierarchicalGraph graph = null;
        if (exportVisible) {
            graph = graphModel.getHierarchicalGraphVisible();
        } else {
            graph = graphModel.getHierarchicalGraph();
        }
        try {
            exportData(graph);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return !cancel;
    }

    private void exportData(HierarchicalGraph graph) throws Exception {
        int max = graph.getNodeCount(), i=1;
        HashMap<String, Integer> idx = new HashMap<String, Integer>(3*max/2+1);

        Progress.start(progressTicket, max);
        graph.readLock();

        writer.append("*Vertices " + max + "\n");

        for (Node node : graph.getNodes()) {
            writer.append(Integer.toString(i));
            writer.append(" \"" + node.getNodeData().getLabel() + "\"");
            if(exportPosition) {
                writer.append(" "+node.getNodeData().x()+" "+node.getNodeData().y()+" "+node.getNodeData().z());
            }
            writer.append("\n");
            idx.put(node.getNodeData().getId(), i++); // assigns Ids from the interval [1..max]
        }

        if (graph instanceof UndirectedGraph) {
            writer.append("*Edges\n");
        } else {
            writer.append("*Arcs\n");
        }

        for (Edge edge : graph.getEdgesAndMetaEdges()) {
            if (cancel) {
                break;
            }
            if (edge != null) {
                writer.append(Integer.toString(idx.get(edge.getSource().getNodeData().getId())) + " ");
                writer.append(Integer.toString(idx.get(edge.getTarget().getNodeData().getId())));
                if (exportEdgeWeight) {
                    writer.append(" " + edge.getWeight());
                }
                writer.append("\n");
            }

            Progress.progress(progressTicket);
        }

        graph.readUnlockAll();

        Progress.finish(progressTicket);
    }

}
