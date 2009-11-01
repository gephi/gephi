package org.gephi.ui.preview.propertyeditor;

import org.gephi.preview.api.color.colorizer.Colorizer;

/**
 *
 * @author jeremy
 */
class ParentColorModePanelDecorator extends ColorModePanelDecorator {

	public ParentColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
		super(propertyEditor, decoratedPanel);
	}

	protected String getRadioButtonLabel() {
		return "Parent Color";
	}

	protected boolean isSelectedRadioButton() {
		return factory.isParentColorMode((Colorizer) propertyEditor.getValue());
	}

	@Override
	protected Colorizer createColorizer() {
		return factory.createParentColorMode();
	}
}
