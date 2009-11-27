package org.gephi.ui.preview.propertyeditors;

import org.gephi.preview.api.Colorizer;

/**
 *
 * @author jeremy
 */
class EdgeB1ColorModePanelDecorator extends ColorModePanelDecorator {

    public EdgeB1ColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
        super(propertyEditor, decoratedPanel);
    }

    protected String getRadioButtonLabel() {
        return "Edge's Boundary 1";
    }

    protected boolean isSelectedRadioButton() {
        return factory.isEdgeB1ColorMode((Colorizer) propertyEditor.getValue());
    }

    protected Colorizer createColorizer() {
        return factory.createEdgeB1ColorMode();
    }
}
