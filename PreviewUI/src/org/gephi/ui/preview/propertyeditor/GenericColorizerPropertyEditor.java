package org.gephi.ui.preview.propertyeditor;

import java.awt.Component;

/**
 *
 * @author jeremy
 */
public class GenericColorizerPropertyEditor extends AbstractColorizerPropertyEditor {

    @Override
    public boolean supportsCustomColorMode() {
        return true;
    }

	@Override
	public Component getCustomEditor() {
		ColorModePanel p;
		p = new ColorModePanel();
		p = new CustomColorModePanelDecorator(this, p);
		
		return p;
	}
}
