package org.gephi.ui.preview.propertyeditor;

import org.gephi.preview.api.color.colorizer.Colorizer;

/**
 *
 * @author jeremy
 */
class ParentNodeColorModePanelDecorator extends ColorModePanelDecorator {

	public ParentNodeColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
		super(propertyEditor, decoratedPanel);
	}

	protected String getRadioButtonLabel() {
		return "Parent Node Color";
	}

	protected boolean isSelectedRadioButton() {
		return factory.isParentNodeColorMode((Colorizer) propertyEditor.getValue());
	}

	@Override
	protected Colorizer createColorizer() {
		return factory.createParentNodeColorMode();
	}
}
