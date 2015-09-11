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
package org.gephi.desktop.appearance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.TableObserver;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = AppearanceUIController.class)
public class AppearanceUIController {

    //Classes
    protected static final String NODE_ELEMENT = "nodes";
    protected static final String EDGE_ELEMENT = "edges";
    protected static final String[] ELEMENT_CLASSES = {NODE_ELEMENT, EDGE_ELEMENT};
    //Transformers
    protected final Map<String, Map<TransformerCategory, Set<TransformerUI>>> transformers;
    //Architecture
    protected final AppearanceController appearanceController;
    private final Set<AppearanceUIModelListener> listeners;
    //Model
    private AppearanceUIModel model;
    //Observer
    private ColumnObserver tableObserver;

    public AppearanceUIController() {
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                AppearanceUIModel oldModel = model;
                model = workspace.getLookup().lookup(AppearanceUIModel.class);
                if (model == null) {
                    AppearanceModel appearanceModel = appearanceController.getModel(workspace);
                    model = new AppearanceUIModel(AppearanceUIController.this, appearanceModel);
                    workspace.add(model);
                }
                model.select();
                if (tableObserver != null) {
                    tableObserver.destroy();
                }
                tableObserver = new ColumnObserver(workspace);
                tableObserver.start();

                firePropertyChangeEvent(AppearanceUIModelEvent.MODEL, oldModel, model);
            }

            @Override
            public void unselect(Workspace workspace) {
                if (model != null) {
                    model.unselect();
                }
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                AppearanceUIModel oldModel = model;
                model = null;
                firePropertyChangeEvent(AppearanceUIModelEvent.MODEL, oldModel, model);
                if (tableObserver != null) {
                    tableObserver.destroy();
                }
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(AppearanceUIModel.class);
            if (model == null) {
                AppearanceModel appearanceModel = appearanceController.getModel(pc.getCurrentWorkspace());
                model = new AppearanceUIModel(this, appearanceModel);
                pc.getCurrentWorkspace().add(model);
            }
        }

        listeners = Collections.synchronizedSet(new HashSet<AppearanceUIModelListener>());

        transformers = new HashMap<String, Map<TransformerCategory, Set<TransformerUI>>>();
        for (String ec : ELEMENT_CLASSES) {
            transformers.put(ec, new LinkedHashMap<TransformerCategory, Set<TransformerUI>>());
        }

        //Register transformers
        Map<Class, Transformer> tMap = new HashMap<Class, Transformer>();
        for (Transformer t : Lookup.getDefault().lookupAll(Transformer.class)) {
            tMap.put(t.getClass(), t);
        }
        for (TransformerUI ui : Lookup.getDefault().lookupAll(TransformerUI.class)) {
            Transformer t = tMap.get(ui.getTransformerClass());
            if (t != null) {
                TransformerCategory c = ui.getCategory();
                if (t.isNode()) {
                    Set<TransformerUI> uis = transformers.get(NODE_ELEMENT).get(c);
                    if (uis == null) {
                        uis = new LinkedHashSet<TransformerUI>();
                        transformers.get(NODE_ELEMENT).put(c, uis);
                    }
                    uis.add(ui);
                }
                if (t.isEdge()) {
                    Set<TransformerUI> uis = transformers.get(EDGE_ELEMENT).get(c);
                    if (uis == null) {
                        uis = new LinkedHashSet<TransformerUI>();
                        transformers.get(EDGE_ELEMENT).put(c, uis);
                    }
                    uis.add(ui);
                }
            }
        }
    }

    public Collection<TransformerCategory> getCategories(String elementClass) {
        return transformers.get(elementClass).keySet();
    }

    public Collection<TransformerUI> getTransformerUIs(String elementClass, TransformerCategory category) {
        return transformers.get(elementClass).get(category);
    }

    public AppearanceUIModel getModel() {
        return model;
    }

    public AppearanceUIModel getModel(Workspace workspace) {
        AppearanceUIModel m = workspace.getLookup().lookup(AppearanceUIModel.class);
        if (m == null) {
            AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class);
            AppearanceModel appearanceModel = ac.getModel(workspace);
            m = new AppearanceUIModel(this, appearanceModel);
            workspace.add(m);
        }
        return m;
    }

    public void setSelectedElementClass(String elementClass) {
        if (!elementClass.equals(NODE_ELEMENT) && !elementClass.equals(EDGE_ELEMENT)) {
            throw new RuntimeException("Element class has to be " + NODE_ELEMENT + " or " + EDGE_ELEMENT);
        }
        if (model != null) {
            String oldValue = model.getSelectedElementClass();
            if (!oldValue.equals(elementClass)) {
                model.setSelectedElementClass(elementClass);
                firePropertyChangeEvent(AppearanceUIModelEvent.SELECTED_ELEMENT_CLASS, oldValue, elementClass);
            }
        }
    }

    public void setSelectedCategory(TransformerCategory category) {
        if (model != null) {
            TransformerCategory oldValue = model.getSelectedCategory();
            if (!oldValue.equals(category)) {
                model.setSelectedCategory(category);
                firePropertyChangeEvent(AppearanceUIModelEvent.SELECTED_CATEGORY, oldValue, category);
            }
        }
    }

    public void setSelectedTransformerUI(TransformerUI ui) {
        if (model != null) {
            TransformerUI oldValue = model.getSelectedTransformerUI();
            if (!oldValue.equals(ui)) {
                model.setAutoApply(false);
                model.setSelectedTransformerUI(ui);
                firePropertyChangeEvent(AppearanceUIModelEvent.SELECTED_TRANSFORMER_UI, oldValue, ui);
            }
        }
    }

    public void setSelectedFunction(Function function) {
        if (model != null) {
            Function oldValue = model.getSelectedFunction();
            if ((oldValue == null && function != null) || (oldValue != null && function == null) || (function != null && oldValue != null && !oldValue.equals(function))) {
                model.setAutoApply(false);
                model.setSelectedFunction(function);
                firePropertyChangeEvent(AppearanceUIModelEvent.SELECTED_FUNCTION, oldValue, function);
            }
        }
    }

    public void setAutoApply(boolean autoApply) {
        if (model != null) {
            model.setAutoApply(autoApply);
            firePropertyChangeEvent(AppearanceUIModelEvent.SET_AUTO_APPLY, !autoApply, autoApply);
        }
    }

    public void startAutoApply() {
        if (model != null) {
            AutoAppyTransformer aat = model.getAutoAppyTransformer();
            if (aat != null) {
                aat.start();
                firePropertyChangeEvent(AppearanceUIModelEvent.START_STOP_AUTO_APPLY, false, true);
            }
        }
    }

    public void stopAutoApply() {
        if (model != null) {
            AutoAppyTransformer aat = model.getAutoAppyTransformer();
            if (aat != null) {
                aat.stop();
                firePropertyChangeEvent(AppearanceUIModelEvent.START_STOP_AUTO_APPLY, true, false);
            }
        }
    }

    public AppearanceController getAppearanceController() {
        return appearanceController;
    }

    protected TransformerCategory getFirstCategory(String elementClass) {
        return transformers.get(elementClass).keySet().toArray(new TransformerCategory[0])[0];
    }

    protected TransformerUI getFirstTransformerUI(String elementClass, TransformerCategory category) {
        Map<TransformerCategory, Set<TransformerUI>> e = transformers.get(elementClass);
        return e.get(category).toArray(new TransformerUI[0])[0];
    }

    public void addPropertyChangeListener(AppearanceUIModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(AppearanceUIModelListener listener) {
        listeners.remove(listener);
    }

    protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        AppearanceUIModelEvent event = new AppearanceUIModelEvent(this, propertyName, oldValue, newValue);
        for (AppearanceUIModelListener listener : listeners) {
            listener.propertyChange(event);
        }
    }

    private class ColumnObserver extends TimerTask {

        private final GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        private static final int INTERVAL = 500;
        private final Timer timer;
        private final TableObserver nodeObserver;
        private final TableObserver edgeObserver;

        public ColumnObserver(Workspace workspace) {
            timer = new Timer("RankingColumnObserver", true);
            nodeObserver = gc.getGraphModel(workspace).getNodeTable().createTableObserver(false);
            edgeObserver = gc.getGraphModel(workspace).getEdgeTable().createTableObserver(false);
        }

        @Override
        public void run() {
            if (nodeObserver.hasTableChanged() || edgeObserver.hasTableChanged()) {
                Function oldValue = model.getSelectedFunction();
                model.refreshSelectedFunction();
                Function newValue = model.getSelectedFunction();
                firePropertyChangeEvent(AppearanceUIModelEvent.SELECTED_FUNCTION, oldValue, newValue);
            }
        }

        public void start() {
            timer.schedule(this, INTERVAL, INTERVAL);
        }

        public void stop() {
            timer.cancel();
        }

        public void destroy() {
            stop();
            nodeObserver.destroy();
            edgeObserver.destroy();
        }
    }
}
