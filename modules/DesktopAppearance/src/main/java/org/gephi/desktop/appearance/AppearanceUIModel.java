/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.appearance;

import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.spi.TransformerUI;

/**
 *
 * @author mbastian
 */
public class AppearanceUIModel {

    protected String selectedElementClass = AppearanceUIController.NODE_ELEMENT;

    public AppearanceUIModel(AppearanceUIController controller, AppearanceModel model) {
    }

    public String getSelectedElementClass() {
        return selectedElementClass;
    }

    public void setSelectedElementClass(String selectedElementClass) {
        this.selectedElementClass = selectedElementClass;
    }

    public TransformerUI getCurrentTransformerUI(String selectedElementClass) {
        return null;
    }
}
