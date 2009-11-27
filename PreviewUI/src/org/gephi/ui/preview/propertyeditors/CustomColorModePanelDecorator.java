package org.gephi.ui.preview.propertyeditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.gephi.preview.api.Colorizer;
import org.gephi.preview.api.GenericColorizer;

/**
 *
 * @author jeremy
 */
class CustomColorModePanelDecorator extends ColorModePanelDecorator {

    private JButton customColorButton;

    public CustomColorModePanelDecorator(final AbstractColorizerPropertyEditor propertyEditor, final ColorModePanel decoratedPanel) {
        super(propertyEditor, decoratedPanel);
    }

    @Override
    protected void setPanelContent() {
        // radio button
        JRadioButton radioButton = new JRadioButton();
        radioButton.setText(getRadioButtonLabel());
        this.addRadioButton(radioButton);

        // color chooser button
        customColorButton = new JButton();
        customColorButton.setText("Choose Color");

        // initialization
        radioButton.setSelected(isSelectedRadioButton());
        customColorButton.setEnabled(isSelectedRadioButton());

        // radio button listener
        radioButton.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                customColorButton.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                propertyEditor.setValue(createColorizer());
            }
        });

        // button listener
        customColorButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                java.awt.Color newColor = JColorChooser.showDialog(
                        decoratedPanel,
                        "Choose Color",
                        ((GenericColorizer) propertyEditor.getValue()).getAwtColor());
                if (null != newColor) {
                    propertyEditor.setValue(factory.createCustomColorMode(newColor));
                }
            }
        });

        // panel layout
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup().
                addGroup(layout.createParallelGroup().
                addGroup(layout.createSequentialGroup().
                addComponent(radioButton).
                addPreferredGap(ComponentPlacement.RELATED).
                addComponent(customColorButton)).
                addComponent(decoratedPanel)));
        layout.setVerticalGroup(layout.createSequentialGroup().
                addGroup(layout.createParallelGroup().
                addComponent(radioButton).
                addComponent(customColorButton)).
                addPreferredGap(ComponentPlacement.RELATED).
                addComponent(decoratedPanel));
    }

    @Override
    protected String getRadioButtonLabel() {
        return "Custom Color";
    }

    @Override
    protected boolean isSelectedRadioButton() {
        return factory.isCustomColorMode((Colorizer) propertyEditor.getValue());
    }

    @Override
    protected Colorizer createColorizer() {
        return factory.createCustomColorMode(0, 0, 0);
    }
}
