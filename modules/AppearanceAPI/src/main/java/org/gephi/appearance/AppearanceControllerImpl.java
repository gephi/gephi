/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = AppearanceController.class)
public class AppearanceControllerImpl implements AppearanceController {

    private AppearanceModelImpl model;

    public AppearanceControllerImpl() {
        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(AppearanceModelImpl.class);
                if (model == null) {
                    model = new AppearanceModelImpl(workspace);
                    workspace.add(model);
                }
//                model.select();
            }

            @Override
            public void unselect(Workspace workspace) {
//                model.unselect();
                model = null;
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(AppearanceModelImpl.class);
            if (model == null) {
                model = new AppearanceModelImpl(pc.getCurrentWorkspace());
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    @Override
    public void transform(Function function) {
        if (model != null) {
            GraphModel graphModel = model.getGraphModel();
            Graph graph = graphModel.getGraphVisible();
            ElementIterable<? extends Element> iterable;
            if (function.getElementClass().equals(Node.class)) {
                iterable = graph.getNodes();
            } else {
                iterable = graph.getEdges();
            }
            try {
                for (Element element : iterable) {
                    function.transform(element, graph);
                }
            } catch (Exception e) {
                iterable.doBreak();
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public AppearanceModelImpl getModel() {
        return model;
    }

    @Override
    public AppearanceModelImpl getModel(Workspace workspace) {
        AppearanceModelImpl m = workspace.getLookup().lookup(AppearanceModelImpl.class);
        if (m == null) {
            m = new AppearanceModelImpl(workspace);
            workspace.add(m);
        }
        return m;
    }

    @Override
    public Transformer getTransformer(TransformerUI ui) {
        Class<? extends Transformer> transformerClass = ui.getTransformerClass();
        Transformer transformer = Lookup.getDefault().lookup(transformerClass);
        if (transformer != null) {
            return transformer;
        }
        return null;
    }

    @Override
    public void setUseLocalScale(boolean useLocalScale) {
        if (model != null) {
            model.setLocalScale(useLocalScale);
        }
    }
}
