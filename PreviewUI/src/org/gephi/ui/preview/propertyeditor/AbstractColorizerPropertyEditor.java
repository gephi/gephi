package org.gephi.ui.preview.propertyeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.gephi.preview.api.color.colorizer.Colorizer;
import org.gephi.preview.api.color.colorizer.ColorizerFactory;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.openide.util.Lookup;

/**
 *
 * @author jeremy
 */
public abstract class AbstractColorizerPropertyEditor extends PropertyEditorSupport {

    private final ColorizerFactory colorizerFactory = Lookup.getDefault().lookup(ColorizerFactory.class);

    @Override
    public String getAsText() {
         Colorizer c = (Colorizer) getValue();
         return c.toString();
    }

    @Override
    public void setAsText(String s) {

        if (supportsCustomColorMode() && colorizerFactory.matchCustomColorMode(s)) {
            Pattern p = Pattern.compile("\\w+\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
            Matcher m = p.matcher(s);
            if (m.lookingAt()) {
                int r = Integer.valueOf(m.group(1));
                int g = Integer.valueOf(m.group(2));
                int b = Integer.valueOf(m.group(3));

                setValue(colorizerFactory.createCustomColorMode(r, g, b));
            }
        }
        else if (supportsNodeOriginalColorMode() && colorizerFactory.matchNodeOriginalColorMode(s)) {
            setValue(colorizerFactory.createNodeOriginalColorMode());
        }
        else if (supportsParentNodeColorMode() && colorizerFactory.matchParentNodeColorMode(s)) {
            setValue(colorizerFactory.createParentNodeColorMode());
        }
    }

    public boolean supportsCustomColorMode() {
        return false;
    }

    public boolean supportsNodeOriginalColorMode() {
        return false;
    }

    public boolean supportsParentNodeColorMode() {
        return false;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        final JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        ParallelGroup hg = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup vg = layout.createSequentialGroup();
        ButtonGroup bg = new ButtonGroup();

        layout.setHorizontalGroup(layout.createSequentialGroup().
                addContainerGap().
                addGroup(hg).
                addContainerGap());
        layout.setVerticalGroup(layout.createSequentialGroup().
                addContainerGap().
                addGroup(vg).
                addContainerGap());

        if (supportsNodeOriginalColorMode()) {
            // radio button
			JRadioButton radioButton = new JRadioButton();
			radioButton.setText("Original Color");
			bg.add(radioButton);

			// initialization
			radioButton.setSelected(colorizerFactory.isNodeOriginalColorMode((Colorizer) getValue()));

			// listener
			radioButton.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setValue(colorizerFactory.createNodeOriginalColorMode());
					}
				}
			});

			// positioning
			hg.addComponent(radioButton);
			vg.addComponent(radioButton);
        }

        if (supportsParentNodeColorMode()) {
            // radio button
			JRadioButton radioButton = new JRadioButton();
			radioButton.setText("Parent Node Color");
			bg.add(radioButton);

			// initialization
			radioButton.setSelected(colorizerFactory.isNodeOriginalColorMode((Colorizer) getValue()));

			// listener
			radioButton.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setValue(colorizerFactory.createParentNodeColorMode());
					}
				}
			});

			// positioning
			hg.addComponent(radioButton);
			vg.addComponent(radioButton);
        }

        if (supportsCustomColorMode()) {
            // radio button
            JRadioButton radioButton = new JRadioButton();
            radioButton.setText("Custom Color");
            bg.add(radioButton);

            // color chooser button
            final JButton customColorButton = new JButton();
            customColorButton.setText("Choose Color");

            // initialization
            if (colorizerFactory.isCustomColorMode((Colorizer) getValue())) {
                radioButton.setSelected(true);
                customColorButton.setEnabled(true);
            } else {
                customColorButton.setEnabled(false);
            }

            // radio button listener
            radioButton.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    customColorButton.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                    setValue(colorizerFactory.createCustomColorMode(0, 0, 0));
                }
            });

            // button listener
            customColorButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    java.awt.Color newColor = JColorChooser.showDialog(
							panel,
							"Choose Color",
							((GenericColorizer) getValue()).getAwtColor());
                    if (null != newColor) {
                        setValue(colorizerFactory.createCustomColorMode(newColor));
                    }
                }
            });

            // positioning
            hg.addGroup(layout.createSequentialGroup().
                    addComponent(radioButton).
                    addPreferredGap(radioButton, customColorButton, ComponentPlacement.RELATED).
                    addComponent(customColorButton));
            vg.addGroup(layout.createParallelGroup().
                    addComponent(radioButton).
                    addComponent(customColorButton));
        }

        return panel;
    }
}
