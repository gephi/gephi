package org.gephi.ui.preview.propertyeditors;

import org.gephi.preview.api.Colorizer;

/**
 *
 * @author jeremy
 */
class EdgeB2ColorModePanelDecorator extends ColorModePanelDecorator {

    public EdgeB2ColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
        super(propertyEditor, decoratedPanel);
    }

    protected String getRadioButtonLabel() {
        return "Edge's Boundary 2";
    }

    protected boolean isSelectedRadioButton() {
        return factory.isEdgeB2ColorMode((Colorizer) propertyEditor.getValue());
    }

    protected Colorizer createColorizer() {
        return factory.createEdgeB2ColorMode();
    }
}
