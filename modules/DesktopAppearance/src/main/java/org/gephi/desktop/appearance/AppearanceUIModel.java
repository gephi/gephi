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

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.AttributeFunction;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.appearance.spi.TransformerUI;
import static org.gephi.desktop.appearance.AppearanceUIController.ELEMENT_CLASSES;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.ui.appearance.plugin.category.DefaultCategory;
import org.openide.util.Lookup;

/**
 *
 * @author mbastian
 */
public class AppearanceUIModel {

    protected final AppearanceUIController controller;
    protected final AppearanceModel appearanceModel;
    protected final GraphController graphController;
    protected final Map<String, Map<TransformerCategory, TransformerUI>> selectedTransformerUI;
    protected final Map<String, Map<TransformerUI, Function>> selectedFunction;
    protected final Map<String, TransformerCategory> selectedCategory;
    protected final Map<String, Map<TransformerCategory, AutoAppyTransformer>> selectedAutoTransformer;
    protected final Map<Function, Map<String, Object>> savedProperties;
    protected String selectedElementClass = AppearanceUIController.NODE_ELEMENT;

    public AppearanceUIModel(AppearanceUIController controller, AppearanceModel model) {
        this.controller = controller;
        this.appearanceModel = model;
        this.graphController = Lookup.getDefault().lookup(GraphController.class);

        //Init maps
        selectedCategory = new HashMap<>();
        selectedTransformerUI = new HashMap<>();
        selectedFunction = new HashMap<>();
        selectedAutoTransformer = new HashMap<>();
        savedProperties = new HashMap<>();

        //Init selected
        for (String ec : ELEMENT_CLASSES) {
            initSelectedTransformerUIs(ec);
            refreshSelectedFunctions(ec);
        }
    }

    private void initSelectedTransformerUIs(String elementClass) {
        Graph graph = graphController.getGraphModel(appearanceModel.getWorkspace()).getGraph();
        Map<TransformerCategory, TransformerUI> newMap = new HashMap<>();
        for (Function func : elementClass.equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions(graph) : appearanceModel.getEdgeFunctions(graph)) {
            TransformerUI ui = func.getUI();
            if (ui != null) {
                TransformerCategory cat = ui.getCategory();
                if (!newMap.containsKey(cat)) {
                    newMap.put(cat, ui);
                }

                if (!selectedCategory.containsKey(elementClass)) {
                    selectedCategory.put(elementClass, cat);
                }
            }
        }

        //Prefer color to start
        if (newMap.containsKey(DefaultCategory.COLOR)) {
            selectedCategory.put(elementClass, DefaultCategory.COLOR);
        }

        selectedTransformerUI.put(elementClass, newMap);
        selectedFunction.put(elementClass, new HashMap<TransformerUI, Function>());
        selectedAutoTransformer.put(elementClass, new HashMap<TransformerCategory, AutoAppyTransformer>());
    }

    private void refreshSelectedFunctions(String elementClass) {
        Set<Function> functionSet = new HashSet<>();
        Graph graph = graphController.getGraphModel(appearanceModel.getWorkspace()).getGraph();
        for (Function func : elementClass.equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions(graph) : appearanceModel.getEdgeFunctions(graph)) {
            TransformerUI ui = func.getUI();
            if (ui != null) {
                functionSet.add(func);
            }
        }

        for (Function func : functionSet) {
            Function oldFunc = selectedFunction.get(elementClass).get(func.getUI());
            if (oldFunc == null || !functionSet.contains(oldFunc)) {
                if (func.isSimple()) {
                    selectedFunction.get(elementClass).put(func.getUI(), func);
                }
            }
        }
    }

    public synchronized Function replaceSelectedFunction() {
        Graph graph = graphController.getGraphModel(appearanceModel.getWorkspace()).getGraph();
        Function sFunction = getSelectedFunction();
        if (sFunction instanceof AttributeFunction) {
            AttributeFunction af = (AttributeFunction) sFunction;
            for (Function func : getSelectedElementClass().equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions(graph) : appearanceModel.getEdgeFunctions(graph)) {
                if (func instanceof AttributeFunction) {
                    if (af.getColumn().equals(((AttributeFunction) func).getColumn())
                            && sFunction.getUI().getCategory().equals(func.getUI().getCategory())) {
                        return func;
                    }
                }
            }
        }
        return null;
    }

    public synchronized boolean refreshSelectedFunction() {
        Graph graph = graphController.getGraphModel(appearanceModel.getWorkspace()).getGraph();
        Function sFunction = getSelectedFunction();
        if (sFunction != null && sFunction.isAttribute()) {
            for (Function func : getSelectedElementClass().equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions(graph) : appearanceModel.getEdgeFunctions(graph)) {
                if (func.equals(sFunction)) {
                    return false;
                }

            }
        }
        return true;
    }

    public void select() {
    }

    public void unselect() {
    }

    public boolean isLocalScale() {
        return appearanceModel.isLocalScale();
    }

    public void saveTransformerProperties() {
        Function func = getSelectedFunction();
        if (func != null) {
            Transformer transformer = func.getTransformer();
            Map<String, Object> props = savedProperties.get(func);
            if (props == null) {
                props = new HashMap<>();
                savedProperties.put(func, new HashMap<String, Object>());
            }

            for (Map.Entry<String, Method[]> entry : getProperties(transformer).entrySet()) {
                String name = entry.getKey();
                Method getMethod = entry.getValue()[0];
                try {
                    Object o = getMethod.invoke(transformer);
                    props.put(name, o);
                } catch (Exception ex) {
                }
            }
        }
    }

    public void loadTransformerProperties() {
        Function func = getSelectedFunction();
        if (func != null) {
            Transformer transformer = func.getTransformer();
            Map<String, Object> props = savedProperties.get(func);
            if (props != null) {
                for (Map.Entry<String, Method[]> entry : getProperties(transformer).entrySet()) {
                    String name = entry.getKey();
                    Object o = props.get(name);
                    if (o != null) {
                        Method setMethod = entry.getValue()[1];
                        try {
                            setMethod.invoke(transformer, o);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
    }

    public String getSelectedElementClass() {
        return selectedElementClass;
    }

    public TransformerCategory getSelectedCategory() {
        return selectedCategory.get(selectedElementClass);
    }

    public TransformerUI getSelectedTransformerUI() {
        return selectedTransformerUI.get(selectedElementClass).get(getSelectedCategory());
    }

    public Function getSelectedFunction() {
        return selectedFunction.get(selectedElementClass).get(getSelectedTransformerUI());
    }

    public AutoAppyTransformer getAutoAppyTransformer() {
        String elm = getSelectedElementClass();
        TransformerCategory ct = getSelectedCategory();
        if (ct != null) {
            return selectedAutoTransformer.get(elm).get(ct);
        }
        return null;
    }

    public Collection<Function> getFunctions() {
        Graph graph = graphController.getGraphModel(appearanceModel.getWorkspace()).getGraph();
        List<Function> functions = new ArrayList<>();
        for (Function func : selectedElementClass.equalsIgnoreCase(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions(graph) : appearanceModel.getEdgeFunctions(graph)) {
            TransformerUI ui = func.getUI();
            if (ui != null && ui.getDisplayName().equals(getSelectedTransformerUI().getDisplayName())) {
                if (ui.getCategory().equals(selectedCategory.get(selectedElementClass))) {
                    functions.add(func);
                }
            }
        }
        return functions;
    }

    protected void setAutoApply(boolean autoApply) {
        if (!autoApply) {
            AutoAppyTransformer aat = getAutoAppyTransformer();
            if (aat != null) {
                aat.stop();
            }
        }
        String elmt = getSelectedElementClass();
        TransformerCategory cat = getSelectedCategory();
        if (autoApply) {
            selectedAutoTransformer.get(elmt).put(cat, new AutoAppyTransformer(controller, getSelectedFunction()));
        } else {
            selectedAutoTransformer.get(elmt).put(cat, null);
        }
    }

    protected boolean isAttributeTransformerUI(TransformerUI ui) {
        Class transformerClass = ui.getTransformerClass();
        if (RankingTransformer.class.isAssignableFrom(transformerClass) || PartitionTransformer.class.isAssignableFrom(transformerClass)) {
            return true;
        }
        return false;
    }

    protected void setSelectedElementClass(String selectedElementClass) {
        saveTransformerProperties();
        this.selectedElementClass = selectedElementClass;
        loadTransformerProperties();
    }

    protected void setSelectedCategory(TransformerCategory category) {
        saveTransformerProperties();
        selectedCategory.put(selectedElementClass, category);
        loadTransformerProperties();
    }

    protected void setSelectedTransformerUI(TransformerUI transformerUI) {
        saveTransformerProperties();
        selectedTransformerUI.get(selectedElementClass).put(getSelectedCategory(), transformerUI);
        loadTransformerProperties();
    }

    protected void setSelectedFunction(Function function) {
        saveTransformerProperties();
        selectedFunction.get(selectedElementClass).put(getSelectedTransformerUI(), function);
        loadTransformerProperties();
    }

    private Map<String, Method[]> getProperties(Transformer transformer) {
        Map<String, Method[]> propertyMethods = new HashMap<>();

        for (Method m : transformer.getClass().getMethods()) {
            String name = m.getName();
            if (Modifier.isPublic(m.getModifiers())) {
                String propertyName = null;
                if (name.startsWith("get")) {
                    propertyName = name.substring(3);
                } else if (name.startsWith("set")) {
                    propertyName = name.substring(3);
                } else if (name.startsWith("is")) {
                    propertyName = name.substring(2);
                }
                Method[] ms = propertyMethods.get(propertyName);
                if (ms == null) {
                    ms = new Method[2];
                    propertyMethods.put(propertyName, ms);
                }
                if (name.startsWith("set")) {
                    ms[1] = m;
                } else {
                    ms[0] = m;
                }
            }
        }

        for (Iterator<Map.Entry<String, Method[]>> itr = propertyMethods.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<String, Method[]> entry = itr.next();
            Method get = entry.getValue()[0];
            Method set = entry.getValue()[1];
            if (!(get != null && set != null
                    && set.getParameterTypes().length == 1 && get.getParameterTypes().length == 0
                    && set.getParameterTypes()[0].equals(get.getReturnType())
                    && isSupportedPropertyType(get.getReturnType()))) {
                itr.remove();
            }
        }
        return propertyMethods;
    }

    private boolean isSupportedPropertyType(Class type) {
        if (type.isPrimitive()) {
            return true;
        } else if (type.isArray()) {
            Class cmp = type.getComponentType();
            if (cmp.isPrimitive() || cmp.equals(Color.class)) {
                return true;
            }
        } else if (type.equals(Color.class)) {
            return true;
        }
        return false;
    }
}
