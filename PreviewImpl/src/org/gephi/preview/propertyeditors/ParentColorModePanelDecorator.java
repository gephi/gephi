package org.gephi.preview.propertyeditors;

import org.gephi.preview.api.Colorizer;

/**
 *
 * @author jeremy
 */
class ParentColorModePanelDecorator extends ColorModePanelDecorator {

    public ParentColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
        super(propertyEditor, decoratedPanel);
    }

    protected String getRadioButtonLabel() {
        return "Parent";
    }

    protected boolean isSelectedRadioButton() {
        return factory.isParentColorMode((Colorizer) propertyEditor.getValue());
    }

    @Override
    protected Colorizer createColorizer() {
        return factory.createParentColorMode();
    }
}
