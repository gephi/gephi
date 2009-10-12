package org.gephi.ui.preview.propertyeditor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.GroupLayout;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.gephi.preview.api.color.colorizer.Colorizer;
import org.gephi.preview.api.color.colorizer.ColorizerFactory;

/**
 *
 * @author jeremy
 */
abstract class ColorModePanelDecorator extends ColorModePanel {

	protected final ColorModePanel decoratedPanel;
	protected final ColorizerFactory factory;

	public ColorModePanelDecorator(AbstractColorizerPropertyEditor propertyEditor, ColorModePanel decoratedPanel) {
		super(propertyEditor);
		this.decoratedPanel = decoratedPanel;
		factory = propertyEditor.getColorizerFactory();

		setPanelContent();
	}

	@Override
	public void addRadioButton(JRadioButton radioButton) {
		decoratedPanel.addRadioButton(radioButton);
	}

	protected void setPanelContent() {
		// radio button
		JRadioButton radioButton = new JRadioButton();
		radioButton.setText(getRadioButtonLabel());
		addRadioButton(radioButton);

		// initialization
		radioButton.setSelected(isSelectedRadioButton());

		// listener
		radioButton.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					propertyEditor.setValue(createColorizer());
				}
			}
		});

		// panel layout
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup().
				addComponent(radioButton).
				addComponent(decoratedPanel)));
		layout.setVerticalGroup(layout.createSequentialGroup().
				addComponent(radioButton).
				addPreferredGap(ComponentPlacement.RELATED).
				addComponent(decoratedPanel));
	}

	protected abstract String getRadioButtonLabel();

	protected abstract boolean isSelectedRadioButton();

	protected abstract Colorizer createColorizer();
}
