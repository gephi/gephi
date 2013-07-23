/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
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
    protected final Map<String, Map<Category, Set<TransformerUI>>> transformers;
    //Architecture
    private final Set<AppearanceUIModelListener> listeners;
    //Model
    private AppearanceUIModel model;

    public AppearanceUIController() {
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                AppearanceUIModel oldModel = model;
                model = workspace.getLookup().lookup(AppearanceUIModel.class);
                if (model == null) {
                    AppearanceModel appearanceModel = ac.getModel(workspace);
                    model = new AppearanceUIModel(AppearanceUIController.this, appearanceModel);
                    workspace.add(model);
                }
                model.select();
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
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(AppearanceUIModel.class);
            if (model == null) {
                AppearanceModel appearanceModel = ac.getModel(pc.getCurrentWorkspace());
                model = new AppearanceUIModel(this, appearanceModel);
                pc.getCurrentWorkspace().add(model);
            }
        }

        listeners = Collections.synchronizedSet(new HashSet<AppearanceUIModelListener>());

        transformers = new HashMap<String, Map<Category, Set<TransformerUI>>>();
        for (String ec : ELEMENT_CLASSES) {
            transformers.put(ec, new LinkedHashMap<Category, Set<TransformerUI>>());
        }

        //Register transformers
        Collection<? extends TransformerUI> trs = Lookup.getDefault().lookupAll(TransformerUI.class);
        for (TransformerUI t : trs) {
            for (Category c : t.getCategories()) {
                Transformer transformer = ac.getTransformer(t);
                if (transformer != null) {
                    if (c.isNode()) {
                        Set<TransformerUI> uis = transformers.get(NODE_ELEMENT).get(c);
                        if (uis == null) {
                            uis = new LinkedHashSet<TransformerUI>();
                            transformers.get(NODE_ELEMENT).put(c, uis);
                        }
                        uis.add(t);
                    }
                    if (c.isEdge()) {
                        Set<TransformerUI> uis = transformers.get(EDGE_ELEMENT).get(c);
                        if (uis == null) {
                            uis = new LinkedHashSet<TransformerUI>();
                            transformers.get(EDGE_ELEMENT).put(c, uis);
                        }
                        uis.add(t);
                    }
                }
            }
        }
    }

    public Collection<Category> getCategories(String elementClass) {
        return transformers.get(elementClass).keySet();
    }

    public Collection<TransformerUI> getTransformerUIs(String elementClass, Category category) {
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

    public void setSelectedCategory(Category category) {
        if (model != null) {
            Category oldValue = model.getSelectedCategory();
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
                model.setSelectedTransformerUI(ui);
                firePropertyChangeEvent(AppearanceUIModelEvent.SELECTED_TRANSFORMER_UI, oldValue, ui);
            }
        }
    }

    protected Category getFirstCategory(String elementClass) {
        return transformers.get(elementClass).keySet().toArray(new Category[0])[0];
    }

    protected TransformerUI getFirstTransformerUI(String elementClass, Category category) {
        Map<Category, Set<TransformerUI>> e = transformers.get(elementClass);
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
        AppearanceUIModelEvent event = new AppearanceUIModelEvent(model, propertyName, oldValue, newValue);
        for (AppearanceUIModelListener listener : listeners) {
            listener.propertyChange(event);
        }
    }
}
