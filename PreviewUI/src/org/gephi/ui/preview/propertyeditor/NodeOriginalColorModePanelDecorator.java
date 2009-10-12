package org.gephi.ui.preview.propertyeditor;

import org.gephi.preview.api.color.colorizer.Colorizer;

/**
 *
 * @author jeremy
 */
class NodeOriginalColorModePanelDecorator extends ColorModePanelDecorator {

	public NodeOriginalColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
		super(propertyEditor, decoratedPanel);
	}

	protected String getRadioButtonLabel() {
		return "Original Color";
	}

	protected boolean isSelectedRadioButton() {
		return factory.isNodeOriginalColorMode((Colorizer) propertyEditor.getValue());
	}

	protected Colorizer createColorizer() {
		return factory.createNodeOriginalColorMode();
	}
}
