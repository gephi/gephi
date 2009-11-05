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
package org.gephi.partition;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.api.TransformerBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionControllerImpl implements PartitionController {

    private PartitionModelImpl model;
    private boolean refreshPartitions = true;

    public PartitionControllerImpl() {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final WorkspaceDataKey<PartitionModel> key = Lookup.getDefault().lookup(PartitionModelWorkspaceDataProvider.class).getWorkspaceDataKey();
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                model = (PartitionModelImpl) workspace.getWorkspaceData().getData(key);
                refreshPartitions = true;
            }

            public void unselect(Workspace workspace) {
                model = null;
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            model = (PartitionModelImpl) pc.getCurrentWorkspace().getWorkspaceData().getData(key);
        }
    }

    public void setSelectedPartition(Partition partition) {
        if (model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING) {
            if (partition != null && !PartitionFactory.isPartitionBuilt(partition)) {
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                PartitionFactory.buildNodePartition((NodePartition) partition, graphModel.getGraphVisible());
            }
            model.setNodePartition(partition);
        } else {
            if (partition != null && !PartitionFactory.isPartitionBuilt(partition)) {
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                PartitionFactory.buildEdgePartition((EdgePartition) partition, graphModel.getGraphVisible());
            }
            model.setEdgePartition(partition);
        }
    }

    public void setSelectedPartitioning(int partitioning) {
        model.setSelectedPartitioning(partitioning);
    }

    public void setSelectedTransformerBuilder(TransformerBuilder builder) {
        if (model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING) {
            model.setNodeBuilder(builder);
        } else {
            model.setEdgeBuilder(builder);
        }
    }

    public void refreshPartitions() {
        if (refreshPartitions) {
            refreshPartitions = false;
            AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

            //Nodes
            List<NodePartition> nodePartitions = new ArrayList<NodePartition>();
            AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
            for (AttributeColumn column : nodeClass.getAttributeColumns()) {
                if (PartitionFactory.isNodePartitionColumn(column, graphModel.getGraphVisible())) {
                    nodePartitions.add(PartitionFactory.createNodePartition(column));
                }
            }
            model.setNodePartitions(nodePartitions.toArray(new NodePartition[0]));

            //Edges
            List<EdgePartition> edgePartitions = new ArrayList<EdgePartition>();
            AttributeClass edgeClass = ac.getTemporaryAttributeManager().getEdgeClass();
            for (AttributeColumn column : edgeClass.getAttributeColumns()) {
                if (PartitionFactory.isEdgePartitionColumn(column, graphModel.getGraphVisible())) {
                    edgePartitions.add(PartitionFactory.createEdgePartition(column));
                }
            }
            model.setEdgePartitions(edgePartitions.toArray(new EdgePartition[0]));
        }
    }
}
