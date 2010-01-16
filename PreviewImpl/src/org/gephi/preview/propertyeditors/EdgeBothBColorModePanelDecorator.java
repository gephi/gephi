package org.gephi.preview.propertyeditors;

import org.gephi.preview.api.Colorizer;

/**
 *
 * @author jeremy
 */
class EdgeBothBColorModePanelDecorator extends ColorModePanelDecorator {

    public EdgeBothBColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
        super(propertyEditor, decoratedPanel);
    }

    protected String getRadioButtonLabel() {
        return "Edge's Both Boundaries";
    }

    protected boolean isSelectedRadioButton() {
        return factory.isEdgeBothBColorMode((Colorizer) propertyEditor.getValue());
    }

    protected Colorizer createColorizer() {
        return factory.createEdgeBothBColorMode();
    }
}
