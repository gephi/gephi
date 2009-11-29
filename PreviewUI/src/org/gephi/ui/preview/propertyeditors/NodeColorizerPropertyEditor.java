package org.gephi.ui.preview.propertyeditors;

import java.awt.Component;

/**
 *
 * @author jeremy
 */
public class NodeColorizerPropertyEditor extends GenericColorizerPropertyEditor {

    @Override
    public boolean supportsNodeOriginalColorMode() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        ColorModePanel p;
        p = (ColorModePanel) super.getCustomEditor();
        p = new NodeOriginalColorModePanelDecorator(this, p);

        return p;
    }
}
