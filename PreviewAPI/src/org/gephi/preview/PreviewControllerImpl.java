/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
