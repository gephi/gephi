package org.gephi.ui.preview;

import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.api.color.colorizer.Colorizer;
import org.gephi.preview.api.color.colorizer.ColorizerFactory;
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

    /*
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

        if (isSupported(ColorizerType.NODE_ORIGINAL)) {
            generateGenericComponent(ColorizerType.NODE_ORIGINAL, "Original Color", hg, vg, bg);
        }

        if (isSupported(ColorizerType.PARENT_NODE)) {
            generateGenericComponent(ColorizerType.PARENT_NODE, "Parent Node Color", hg, vg, bg);
        }

        if (isSupported(ColorizerType.EDGE_B1)) {
            generateGenericComponent(ColorizerType.EDGE_B1, "Edge Boundary 1 Color", hg, vg, bg);
        }

        if (isSupported(ColorizerType.EDGE_B2)) {
            generateGenericComponent(ColorizerType.EDGE_B2, "Edge Boundary 2 Color", hg, vg, bg);
        }

        if (isSupported(ColorizerType.EDGE_BOTH)) {
            generateGenericComponent(ColorizerType.EDGE_BOTH, "Edge Both Boundaries Color", hg, vg, bg);
        }

        if (isSupported(ColorizerType.PARENT_EDGE)) {
            generateGenericComponent(ColorizerType.PARENT_EDGE, "Parent Edge Color", hg, vg, bg);
        }

        if (isSupported(ColorizerType.CUSTOM)) {
            // radio button
            JRadioButton radioButton = new JRadioButton();
            radioButton.setText("Custom Color");
            bg.add(radioButton);

            // color chooser button
            final JButton customColorButton = new JButton();
            customColorButton.setText("Choose Color");

            // initialization
            if (getColorizerTypeValue() == ColorizerType.CUSTOM) {
                radioButton.setSelected(true);
                customColorButton.setEnabled(true);
            } else {
                customColorButton.setEnabled(false);
            }

            // radio button listener
            radioButton.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    customColorButton.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                    ColorizerType t = ColorizerType.CUSTOM;
                    t.setCustomColor(java.awt.Color.BLACK);
                    setValue(createColorizer(t));
                }
            });

            // button listener
            customColorButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ColorizerType initialType = getColorizerTypeValue();
                    java.awt.Color initialColor = initialType == ColorizerType.CUSTOM ? initialType.getCustomColor() : java.awt.Color.BLACK;
                    java.awt.Color newColor = JColorChooser.showDialog(panel, "Choose Color", initialColor);
                    if (newColor != null) {
                        ColorizerType t = ColorizerType.CUSTOM;
                        t.setCustomColor(newColor);
                        setValue(createColorizer(t));
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

    private void generateGenericComponent(final ColorizerType type, final String label, final ParallelGroup hg, final SequentialGroup vg, final ButtonGroup bg) {
        // radio button
        JRadioButton radioButton = new JRadioButton();
        radioButton.setText(label);
        bg.add(radioButton);

        // initialization
        radioButton.setSelected(getColorizerTypeValue() == type);

        // listener
        radioButton.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setValue(createColorizer(type));
                }
            }
        });

        // positioning
        hg.addComponent(radioButton);
        vg.addComponent(radioButton);
    }
*/
}
