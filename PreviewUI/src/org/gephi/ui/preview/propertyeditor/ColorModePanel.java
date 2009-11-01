package org.gephi.ui.preview.propertyeditor;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author jeremy
 */
class ColorModePanel extends JPanel {

	protected AbstractColorizerPropertyEditor propertyEditor;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	public ColorModePanel() {
	}

	public ColorModePanel(final AbstractColorizerPropertyEditor propertyEditor) {
		this.propertyEditor = propertyEditor;
	}

	public void addRadioButton(JRadioButton radioButton) {
		buttonGroup.add(radioButton);
	}
}
