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

    public PartitionControllerImpl() {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final WorkspaceDataKey<PartitionModel> key = Lookup.getDefault().lookup(PartitionModelWorkspaceDataProvider.class).getWorkspaceDataKey();
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                model = (PartitionModelImpl)workspace.getWorkspaceData().getData(key);
            }

            public void unselect(Workspace workspace) {
                model = null;
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
            }
        });
        if(pc.getCurrentWorkspace()!=null) {
            model = (PartitionModelImpl)pc.getCurrentWorkspace().getWorkspaceData().getData(key);
        }
    }

    private void refreshModel() {
        
    }

    public void setSelectedPartition(Partition partition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelectedPartitioning(int partitioning) {
        model.setSelectedPartitioning(partitioning);
    }

    public void setSelectedTransformerBuilder(TransformerBuilder builder) {
        if(model.getSelectedPartitioning()==PartitionModel.NODE_PARTITIONING) {
            model.setNodeBuilder(builder);
        } else {
            model.setEdgeBuilder(builder);
        }
    }
}
