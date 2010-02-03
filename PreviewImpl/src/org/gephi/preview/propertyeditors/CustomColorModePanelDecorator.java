package org.gephi.preview.propertyeditors;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.gephi.preview.api.Colorizer;
import org.gephi.preview.api.GenericColorizer;
import org.gephi.ui.components.JColorButton;

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
        if (factory.isCustomColorMode((Colorizer) propertyEditor.getValue())) {
            customColorButton = new JColorButton(((GenericColorizer) propertyEditor.getValue()).getAwtColor());
        } else {
            customColorButton = new JColorButton(Color.BLACK);
        }


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
        customColorButton.addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                Color newColor = (Color) evt.getNewValue();
                propertyEditor.setValue(factory.createCustomColorMode(newColor));
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
        return "Custom";
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
