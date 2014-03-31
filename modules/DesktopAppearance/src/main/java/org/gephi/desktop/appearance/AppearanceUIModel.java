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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.appearance.spi.TransformerUI;
import static org.gephi.desktop.appearance.AppearanceUIController.ELEMENT_CLASSES;

/**
 *
 * @author mbastian
 */
public class AppearanceUIModel {

    protected final AppearanceUIController controller;
    protected final AppearanceModel appearanceModel;
    protected final Map<String, Map<TransformerCategory, TransformerUI>> selectedTransformerUI;
    protected final Map<String, Map<TransformerUI, Function>> selectedFunction;
    protected final Map<String, TransformerCategory> selectedCategory;
    protected final Map<String, Map<TransformerCategory, AutoAppyTransformer>> selectedAutoTransformer;
    protected String selectedElementClass = AppearanceUIController.NODE_ELEMENT;
    protected Transformer selectedTransformer;

    public AppearanceUIModel(AppearanceUIController controller, AppearanceModel model) {
        this.controller = controller;
        this.appearanceModel = model;

        //Init maps
        selectedCategory = new HashMap<String, TransformerCategory>();
        selectedTransformerUI = new HashMap<String, Map<TransformerCategory, TransformerUI>>();
        selectedFunction = new HashMap<String, Map<TransformerUI, Function>>();
        selectedAutoTransformer = new HashMap<String, Map<TransformerCategory, AutoAppyTransformer>>();

        //Init selected
        for (String ec : ELEMENT_CLASSES) {
            initSelectedTransformerUIs(ec);
            refreshSelectedFunctions(ec);
        }
    }

    private void initSelectedTransformerUIs(String elementClass) {
        Map<TransformerCategory, TransformerUI> newMap = new HashMap<TransformerCategory, TransformerUI>();
        for (Function func : elementClass.equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions() : appearanceModel.getEdgeFunctions()) {
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
        selectedTransformerUI.put(elementClass, newMap);
        selectedFunction.put(elementClass, new HashMap<TransformerUI, Function>());
        selectedAutoTransformer.put(elementClass, new HashMap<TransformerCategory, AutoAppyTransformer>());
    }

    private void refreshSelectedFunctions(String elementClass) {
        Set<Function> functionSet = new HashSet<Function>();
        for (Function func : elementClass.equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions() : appearanceModel.getEdgeFunctions()) {
            TransformerUI ui = func.getUI();
            if (ui != null) {
                functionSet.add(func);
            }
        }

        for (Function func : functionSet) {
            Function oldFunc = selectedFunction.get(elementClass).get(func.getUI());
            if (oldFunc == null || !functionSet.contains(oldFunc)) {
                selectedFunction.get(elementClass).put(func.getUI(), func);
            }
        }
    }

    public boolean refreshSelectedFunction() {
        Function sFunction = getSelectedFunction();
        if (sFunction != null && sFunction.isAttribute()) {
            for (Function func : getSelectedElementClass().equals(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions() : appearanceModel.getEdgeFunctions()) {
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
        List<Function> functions = new ArrayList<Function>();
        for (Function func : selectedElementClass.equalsIgnoreCase(AppearanceUIController.NODE_ELEMENT) ? appearanceModel.getNodeFunctions() : appearanceModel.getEdgeFunctions()) {
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
        this.selectedElementClass = selectedElementClass;
    }

    protected void setSelectedCategory(TransformerCategory category) {
        selectedCategory.put(selectedElementClass, category);
    }

    protected void setSelectedTransformerUI(TransformerUI transformerUI) {
        selectedTransformerUI.get(selectedElementClass).put(getSelectedCategory(), transformerUI);
    }

    protected void setSelectedFunction(Function function) {
        selectedFunction.get(selectedElementClass).put(getSelectedTransformerUI(), function);
    }
//    protected void setSelectedTransformerUI(TransformerUI transformerUI) {
//        selectedTransformerUI.get(selectedElementClass).put(getSelectedCategory(), transformerUI);
//        if (transformerUI instanceof SimpleTransformerUI) {
//            if (selectedTransformer != null) {
//                unsetupTransformer(selectedTransformer);
//            }
//            selectedTransformer = controller.appearanceController.getTransformer(transformerUI);
//            setupTransformer(selectedTransformer);
//        } else {
//            //TODO
//        }
//    }
//    public void setupTransformer(Transformer transformer) {
//        for (Method m : transformer.getClass().getMethods()) {
//            if (isSetter(m)) {
//                Class paramClass = m.getParameterTypes()[0];
//                if (paramClass.isPrimitive() || Serializable.class.isAssignableFrom(paramClass)) {
//                    System.out.println("Setting to method " + m.getName());
//                }
//            }
//        }
//    }
//
//    public void unsetupTransformer(Transformer transformer) {
//        for (Method m : transformer.getClass().getMethods()) {
//            if (isGetter(m)) {
//                Class returnClass = m.getReturnType();
//                if (returnClass.isPrimitive() || Serializable.class.isAssignableFrom(returnClass)) {
//                    try {
//                        Object res = m.invoke(transformer);
//                        if (res != null) {
//                            System.out.println("Extracted " + res + "  from method " + m.getName());
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    public static boolean isGetter(Method method) {
//        if (Modifier.isPublic(method.getModifiers())
//                && method.getParameterTypes().length == 0) {
//            if (method.getName().matches("^get[A-Z].*")
//                    && !method.getReturnType().equals(void.class)) {
//                return true;
//            }
//            if (method.getName().matches("^is[A-Z].*")
//                    && method.getReturnType().equals(boolean.class)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean isSetter(Method method) {
//        return Modifier.isPublic(method.getModifiers())
//                && method.getReturnType().equals(void.class)
//                && method.getParameterTypes().length == 1
//                && method.getName().matches("^set[A-Z].*");
//    }
}
