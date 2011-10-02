/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview;

import java.awt.Dimension;
import java.awt.Point;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = PreviewController.class)
public class PreviewControllerImpl implements PreviewController {
    
    private PreviewModelImpl model;
    //Other controllers
    private final GraphController graphController;
    private final AttributeController attributeController;
    
    public PreviewControllerImpl() {
        graphController = Lookup.getDefault().lookup(GraphController.class);
        attributeController = Lookup.getDefault().lookup(AttributeController.class);

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            
            @Override
            public void initialize(Workspace workspace) {
            }
            
            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(PreviewModelImpl.class);
                if (model == null) {
                    model = new PreviewModelImpl(workspace);
                    workspace.add(model);
                }
            }
            
            @Override
            public void unselect(Workspace workspace) {
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
            model = pc.getCurrentWorkspace().getLookup().lookup(PreviewModelImpl.class);
            if (model == null) {
                model = new PreviewModelImpl(pc.getCurrentWorkspace());
                pc.getCurrentWorkspace().add(model);
            }
        }
    }
    
    @Override
    public void refreshPreview() {
        refreshPreview(model.getWorkspace());
    }
    
    @Override
    public synchronized void refreshPreview(Workspace workspace) {
        GraphModel graphModel = graphController.getModel(workspace);
        AttributeModel attributeModel = attributeController.getModel(model.getWorkspace());
        PreviewModelImpl previewModel = getModel(workspace);
        previewModel.clear();

        //Directed graph?
        previewModel.getProperties().putValue(PreviewProperty.DIRECTED, graphModel.isDirected() || graphModel.isMixed());

        //Graph
        Graph graph = graphModel.getGraphVisible();
        if (previewModel.getProperties().getFloatValue(PreviewProperty.VISIBILITY_RATIO) < 1f) {
            float visibilityRatio = previewModel.getProperties().getFloatValue(PreviewProperty.VISIBILITY_RATIO);
            GraphView reducedView = graphModel.copyView(graph.getView());
            graph = graphModel.getGraph(reducedView);
            Node[] nodes = graph.getNodes().toArray();
            for (int i = 0; i < nodes.length; i++) {
                float r = (float) i / (float) nodes.length;
                if (r > visibilityRatio) {
                    graph.removeNode(nodes[i]);
                }
            }
        }

        //Build items
        for (ItemBuilder b : Lookup.getDefault().lookupAll(ItemBuilder.class)) {
            try {
                Item[] items = b.getItems(graph, attributeModel);
                if (items != null) {
                    previewModel.loadItems(b.getType(), items);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

        //Destrow view
        if (previewModel.getProperties().getFloatValue(PreviewProperty.VISIBILITY_RATIO) < 1f) {
            graphModel.destroyView(graph.getView());
        }

        //Refresh dimensions
        updateDimensions(previewModel, previewModel.getItems(Item.NODE));


        //Pre process renderers
        for (Renderer r : Lookup.getDefault().lookupAll(Renderer.class).toArray(new Renderer[0])) {
            r.preProcess(model);
        }
    }
    
    public void updateDimensions(PreviewModelImpl model, Item[] nodeItems) {
        float margin = model.getProperties().getFloatValue(PreviewProperty.MARGIN);  //percentage
        float topLeftX = 0f;
        float topLeftY = 0f;
        float bottomRightX = 0f;
        float bottomRightY = 0f;
        
        for (Item nodeItem : nodeItems) {
            float x = (Float) nodeItem.getData("x");
            float y = (Float) nodeItem.getData("y");
            
            if (x < topLeftX) {
                topLeftX = x;
            }
            if (y < topLeftY) {
                topLeftY = y;
            }
            if (x > bottomRightX) {
                bottomRightX = x;
            }
            if (y > bottomRightY) {
                bottomRightY = y;
            }
        }

        float marginWidth = (bottomRightX - topLeftX) * (margin / 100f);
        float marginHeight = (bottomRightY - topLeftY) * (margin / 100f);
        topLeftX -= marginWidth;
        topLeftY -= marginHeight;
        bottomRightX += marginWidth;
        bottomRightY += marginHeight;
        model.setDimensions(new Dimension((int) (bottomRightX - topLeftX), (int) (bottomRightY - topLeftY)));
        model.setTopLeftPosition(new Point((int) topLeftX, (int) topLeftY));
    }
    
    @Override
    public void render(RenderTarget target) {
        render(target, getModel());
    }
    
    @Override
    public void render(RenderTarget target, Workspace workspace) {
        render(target, getModel(workspace));
    }
    
    private synchronized void render(RenderTarget target, PreviewModelImpl previewModel) {
        if (previewModel != null) {
            Renderer[] renderers = Lookup.getDefault().lookupAll(Renderer.class).toArray(new Renderer[0]);
            PreviewProperties properties = previewModel.getProperties();

            //Progress
            ProgressTicket progressTicket = null;
            if (target instanceof AbstractRenderTarget) {
                int tasks = 0;
                for (Renderer r : renderers) {
                    for (String type : previewModel.getItemTypes()) {
                        for (Item item : previewModel.getItems(type)) {
                            if (r.isRendererForitem(item, properties)) {
                                tasks++;
                            }
                        }
                    }
                }
                progressTicket = ((AbstractRenderTarget) target).getProgressTicket();
                Progress.switchToDeterminate(progressTicket, tasks);
            }


            //Render items
            for (Renderer r : renderers) {
                for (String type : previewModel.getItemTypes()) {
                    for (Item item : previewModel.getItems(type)) {
                        if (r.isRendererForitem(item, properties)) {
                            r.render(item, target, properties);
                            Progress.progress(progressTicket);
                            if (target instanceof AbstractRenderTarget) {
                                if (((AbstractRenderTarget) target).isCancelled()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public synchronized PreviewModelImpl getModel() {
        if (model == null) {
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            if (pc.getCurrentWorkspace() != null) {
                return getModel(pc.getCurrentWorkspace());
            }
        }
        return model;
    }
    
    @Override
    public synchronized PreviewModelImpl getModel(Workspace workspace) {
        PreviewModelImpl m = workspace.getLookup().lookup(PreviewModelImpl.class);
        if (m == null) {
            m = new PreviewModelImpl(workspace);
            workspace.add(m);
        }
        return m;
    }
    
    @Override
    public RenderTarget getRenderTarget(String name) {
        return getRenderTarget(name, getModel());
    }
    
    @Override
    public RenderTarget getRenderTarget(String name, Workspace workspace) {
        return getRenderTarget(name, getModel(workspace));
    }
    
    private synchronized RenderTarget getRenderTarget(String name, PreviewModel m) {
        if (m != null) {
            for (RenderTargetBuilder rtb : Lookup.getDefault().lookupAll(RenderTargetBuilder.class)) {
                if (rtb.getName().equals(name)) {
                    return rtb.buildRenderTarget(m);
                }
            }
        }
        return null;
    }
}
