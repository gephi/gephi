/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.appearance;

import java.util.HashMap;
import java.util.Map;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.TransformerUI;
import static org.gephi.desktop.appearance.AppearanceUIController.ELEMENT_CLASSES;

/**
 *
 * @author mbastian
 */
public class AppearanceUIModel {

    protected final AppearanceUIController controller;
    protected final Map<String, Map<Category, TransformerUI>> selectedTransformerUI;
    protected final Map<String, Category> selectedCategory;
    protected String selectedElementClass = AppearanceUIController.NODE_ELEMENT;

    public AppearanceUIModel(AppearanceUIController controller, AppearanceModel model) {
        this.controller = controller;

        //Init categories
        selectedCategory = new HashMap<String, Category>();
        for (String ec : ELEMENT_CLASSES) {
            selectedCategory.put(ec, controller.getFirstCategory(ec));
        }

        //Init transformers
        selectedTransformerUI = new HashMap<String, Map<Category, TransformerUI>>();
        for (String ec : ELEMENT_CLASSES) {
            Map<Category, TransformerUI> m = new HashMap<Category, TransformerUI>();
            selectedTransformerUI.put(ec, m);
            for (Category c : controller.getCategories(ec)) {
                m.put(c, controller.getFirstTransformerUI(ec, c));
            }
        }
    }

    public void select() {
    }

    public void unselect() {
    }

    public String getSelectedElementClass() {
        return selectedElementClass;
    }

    public Category getSelectedCategory() {
        return selectedCategory.get(selectedElementClass);
    }

    public TransformerUI getSelectedTransformerUI() {
        return selectedTransformerUI.get(selectedElementClass).get(getSelectedCategory());
    }

    protected void setSelectedElementClass(String selectedElementClass) {
        this.selectedElementClass = selectedElementClass;
    }

    public void setSelectedCategory(Category category) {
        selectedCategory.put(selectedElementClass, category);
    }

    protected void setSelectedTransformerUI(TransformerUI transformerUI) {
        selectedTransformerUI.get(selectedElementClass).put(getSelectedCategory(), transformerUI);
    }
}
