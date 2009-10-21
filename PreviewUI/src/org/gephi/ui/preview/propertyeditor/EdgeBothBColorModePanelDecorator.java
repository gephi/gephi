package org.gephi.ui.preview.propertyeditor;

import org.gephi.preview.api.color.colorizer.Colorizer;

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
