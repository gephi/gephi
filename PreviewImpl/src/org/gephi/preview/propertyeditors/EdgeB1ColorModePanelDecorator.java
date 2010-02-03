package org.gephi.preview.propertyeditors;

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
        return "Source";
    }

    protected boolean isSelectedRadioButton() {
        return factory.isEdgeB1ColorMode((Colorizer) propertyEditor.getValue());
    }

    protected Colorizer createColorizer() {
        return factory.createEdgeB1ColorMode();
    }
}
