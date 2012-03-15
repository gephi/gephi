/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.data.attributes;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.model.IndexedAttributeModel;
import org.gephi.data.attributes.model.TemporaryAttributeModel;
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
public class AttributeControllerImpl implements AttributeController {

    private ProjectController projectController;

    public AttributeControllerImpl() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                AttributeModel m = workspace.getLookup().lookup(AttributeModel.class);
                if (m == null) {
                    workspace.add(new IndexedAttributeModel());
                }
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

    public synchronized AttributeModel getModel() {
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

    public synchronized AttributeModel getModel(Workspace workspace) {
        AttributeModel model = workspace.getLookup().lookup(AttributeModel.class);
        if (model != null) {
            return model;
        }
        model = new IndexedAttributeModel();
        workspace.add(model);
        return model;
    }

    public AttributeModel newModel() {
        TemporaryAttributeModel model = new TemporaryAttributeModel();
        return model;
    }
}
