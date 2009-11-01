package org.gephi.ui.preview.propertyeditor;

import java.awt.Component;

/**
 *
 * @author jeremy
 */
public class EdgeChildColorizerPropertyEditor extends GenericColorizerPropertyEditor {

    @Override
    public boolean supportsEdgeB1ColorMode() {
        return true;
    }

	@Override
    public boolean supportsEdgeB2ColorMode() {
        return true;
    }

	@Override
    public boolean supportsEdgeBothBColorMode() {
        return true;
    }

	@Override
	public Component getCustomEditor() {
		ColorModePanel p;
		p = (ColorModePanel) super.getCustomEditor();
		p = new EdgeBothBColorModePanelDecorator(this, p);
		p = new EdgeB2ColorModePanelDecorator(this, p);
		p = new EdgeB1ColorModePanelDecorator(this, p);
        p = new ParentColorModePanelDecorator(this, p);

		return p;
	}
}
