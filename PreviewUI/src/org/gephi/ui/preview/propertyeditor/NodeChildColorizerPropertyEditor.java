package org.gephi.ui.preview.propertyeditor;

import java.awt.Component;

/**
 *
 * @author jeremy
 */
public class NodeChildColorizerPropertyEditor extends GenericColorizerPropertyEditor {

    @Override
    public boolean supportsParentColorMode() {
        return true;
    }

	@Override
	public Component getCustomEditor() {
		ColorModePanel p;
		p = (ColorModePanel) super.getCustomEditor();
		p = new ParentColorModePanelDecorator(this, p);

		return p;
	}
}
