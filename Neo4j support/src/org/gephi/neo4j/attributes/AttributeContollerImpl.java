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
package org.gephi.neo4j.attributes;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.neo4j.attributes.model.IndexedAttributeModel;
import org.gephi.neo4j.attributes.model.TemporaryAttributeModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = AttributeController.class)
public class AttributeContollerImpl implements AttributeController {

    private ProjectController projectController;

    public AttributeContollerImpl() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new IndexedAttributeModel());
            }

            public void select(Workspace workspace) {
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
            }
        });
        if (projectController.getCurrentProject() != null) {
            for (Workspace workspace : projectController.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces()) {
                AttributeModel m = workspace.getLookup().lookup(AttributeModel.class);
                if (m == null) {
                    workspace.add(new IndexedAttributeModel());
                }
            }
        }
    }

    public AttributeModel getModel() {
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace != null) {
            AttributeModel model = workspace.getLookup().lookup(AttributeModel.class);
            if (model != null) {
                return model;
            }
            model = new IndexedAttributeModel();
            workspace.add(model);
            return model;
        }
        return null;
    }

    public AttributeModel newModel() {
        TemporaryAttributeModel model = new TemporaryAttributeModel();
        return model;
    }
}
