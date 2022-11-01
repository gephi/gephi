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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewMouseEvent;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.spi.MouseResponsiveRenderer;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.Controller;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Controller.class)
public class PreviewControllerImpl implements PreviewController, Controller<PreviewModelImpl> {

    //Registered renderers
    private Renderer[] registeredRenderers = null;
    private Boolean anyPluginRendererRegistered = null;
    private boolean mousePressed = false;

    @Override
    public PreviewModelImpl newModel(Workspace workspace) {
        return new PreviewModelImpl(workspace);
    }

    @Override
    public Class<PreviewModelImpl> getModelClass() {
        return PreviewModelImpl.class;
    }

    @Override
    public PreviewModelImpl getModel(Workspace workspace) {
        return Controller.super.getModel(workspace);
    }

    @Override
    public PreviewModelImpl getModel() {
        return Controller.super.getModel();
    }

    @Override
    public void refreshPreview() {
        refreshPreview(getModel().getWorkspace());
    }

    @Override
    public synchronized void refreshPreview(Workspace workspace) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        PreviewModelImpl previewModel = getModel(workspace);
        previewModel.clear();

        //Directed graph?
        previewModel.getProperties()
            .putValue(PreviewProperty.DIRECTED, graphModel.isDirected() || graphModel.isMixed());

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

        Renderer[] renderers;
        if (!mousePressed) {
            renderers = previewModel.getManagedEnabledRenderers();
        } else {
            ArrayList<Renderer> renderersList = new ArrayList<>();
            for (Renderer renderer : previewModel.getManagedEnabledRenderers()) {
                //Only mouse responsive renderers will be called while mouse is pressed
                if (renderer instanceof MouseResponsiveRenderer) {
                    renderersList.add(renderer);
                }
            }

            renderers = renderersList.toArray(new Renderer[0]);
        }

        if (renderers == null) {
            renderers = getRegisteredRenderers();
        }

        //Build items
        boolean globalCanvasSize = previewModel.isGlobalCanvasSize() && !graph.getView().isMainView();
        previewModel.buildAndLoadItems(renderers, globalCanvasSize ? graph.getModel().getGraph() : graph);

        //Pre process renderers
        Arrays.stream(renderers).forEachOrdered(r -> r.preProcess(previewModel));

        //Canvas size
        previewModel.updateCanvasSize(renderers);

        if (globalCanvasSize) {
            // Clear and rebuild with just the filtered graph
            previewModel.clear();
            previewModel.buildAndLoadItems(renderers, graph);

            //Pre process renderers
            Arrays.stream(renderers).forEachOrdered(r -> r.preProcess(previewModel));
        }

        //Destroy view
        if (previewModel.getProperties().getFloatValue(PreviewProperty.VISIBILITY_RATIO) < 1f) {
            graphModel.destroyView(graph.getView());
        }
    }

    @Override
    public void render(RenderTarget target) {
        PreviewModelImpl m = getModel();
        render(target, m.getManagedEnabledRenderers(), m);
    }

    @Override
    public void render(RenderTarget target, Workspace workspace) {
        PreviewModelImpl m = getModel(workspace);
        render(target, m.getManagedEnabledRenderers(), m);
    }

    @Override
    public void render(RenderTarget target, Renderer[] renderers) {
        render(target, renderers, getModel());
    }

    @Override
    public void render(RenderTarget target, Renderer[] renderers, Workspace workspace) {
        render(target, renderers != null ? renderers : getModel(workspace).getManagedEnabledRenderers(),
            getModel(workspace));
    }

    private synchronized void render(RenderTarget target, Renderer[] renderers, PreviewModelImpl previewModel) {
        if (previewModel != null) {
            PreviewProperties properties = previewModel.getProperties();

            //Progress
            ProgressTicket progressTicket = null;
            if (target instanceof AbstractRenderTarget) {
                int tasks = 0;
                for (Renderer r : renderers) {
                    if (!mousePressed || r instanceof MouseResponsiveRenderer) {
                        tasks++;
                        for (String type : previewModel.getItemTypes()) {
                            for (Item item : previewModel.getItems(type)) {
                                if (r.isRendererForitem(item, properties)) {
                                    tasks++;
                                }
                            }
                        }
                    }
                }
                progressTicket = ((AbstractRenderTarget) target).getProgressTicket();
                Progress.switchToDeterminate(progressTicket, tasks);
            }


            //Render items
            for (Renderer r : renderers) {
                if (!mousePressed || r instanceof MouseResponsiveRenderer) {
                    for (String type : previewModel.getItemTypes()) {
                        for (Item item : previewModel.getItems(type)) {
                            if (r.isRendererForitem(item, properties)) {
                                try {
                                    r.render(item, target, properties);
                                } catch (Exception e) {
                                    Exceptions.printStackTrace(e);
                                }
                                Progress.progress(progressTicket);
                                if (target instanceof AbstractRenderTarget) {
                                    if (((AbstractRenderTarget) target).isCancelled()) {
                                        Progress.finish(progressTicket);
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    // Call post-process
                    r.postProcess(previewModel, target, properties);
                    Progress.progress(progressTicket);
                }
            }

            Progress.finish(progressTicket);
        }
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

    @Override
    public Renderer[] getRegisteredRenderers() {
        if (registeredRenderers == null) {
            LinkedHashMap<String, Renderer> renderers = new LinkedHashMap<>();
            for (Renderer r : Lookup.getDefault().lookupAll(Renderer.class)) {
                renderers.put(r.getClass().getName(), r);
            }

            for (Renderer r : renderers.values().toArray(new Renderer[0])) {
                Class superClass = r.getClass().getSuperclass();
                if (superClass != null && superClass.getName().startsWith("org.gephi.preview.plugin.renderers.")) {
                    //Replace default renderer with plugin by removing it
                    renderers.remove(superClass.getName());
                }
            }

            registeredRenderers = renderers.values().toArray(new Renderer[0]);
        }
        return registeredRenderers;
    }

    @Override
    public boolean isAnyPluginRendererRegistered() {
        if (anyPluginRendererRegistered == null) {
            anyPluginRendererRegistered = false;
            for (Renderer renderer : getRegisteredRenderers()) {
                if (!renderer.getClass().getName().startsWith("org.gephi.preview.plugin.renderers.")) {
                    anyPluginRendererRegistered = true;
                    break;
                }
            }
        }
        return anyPluginRendererRegistered;
    }

    @Override
    public boolean sendMouseEvent(PreviewMouseEvent event) {
        return sendMouseEvent(event, Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace());
    }

    @Override
    public boolean sendMouseEvent(PreviewMouseEvent event, Workspace workspace) {
        if (workspace == null) {
            return false;
        }

        PreviewModel previewModel = getModel(workspace);

        //Avoid drag events arriving to listeners if they did not consume previous press event.
        if ((event.type != PreviewMouseEvent.Type.DRAGGED && event.type != PreviewMouseEvent.Type.RELEASED) ||
            mousePressed) {
            for (PreviewMouseListener listener : previewModel.getEnabledMouseListeners()) {
                switch (event.type) {
                    case CLICKED:
                        listener.mouseClicked(event, previewModel.getProperties(), workspace);
                        break;
                    case PRESSED:
                        mousePressed = true;
                        listener.mousePressed(event, previewModel.getProperties(), workspace);
                        break;
                    case DRAGGED:
                        listener.mouseDragged(event, previewModel.getProperties(), workspace);
                        break;
                    case RELEASED:
                        mousePressed = false;
                        listener.mouseReleased(event, previewModel.getProperties(), workspace);
                }
                if (event.isConsumed()) {
                    return true;
                }
            }
        }

        mousePressed = false;//Avoid drag events arriving to listeners if they did not consume previous press event.
        return false;
    }

    @Override
    public void setGlobalCanvasSize(boolean globalCanvasSize) {
        PreviewModelImpl model = getModel();
        if (model != null) {
            model.setGlobalCanvasSize(globalCanvasSize);
        }
    }
}
