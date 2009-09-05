package org.gephi.ui.preview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditorSupport;
import java.util.HashSet;
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
import org.gephi.preview.api.color.Colorizer;
import org.gephi.preview.api.color.ColorizerType;

/**
 *
 * @author jeremy
 */
public abstract class AbstractColorizerPropertyEditor extends PropertyEditorSupport {

    private final HashSet<ColorizerType> supportedColorizerTypes = new HashSet<ColorizerType>();
    private static final String CUSTOM_ID = "custom";
    private static final String NODE_ORIGINAL_ID = "original";
    private static final String PARENT_NODE_ID = "parent";
    private static final String EDGE_B1_ID = "b1";
    private static final String EDGE_B2_ID = "b2";
    private static final String EDGE_BOTH_ID = "both";
    private static final String PARENT_EDGE_ID = "parent";

    public AbstractColorizerPropertyEditor() {
        setSupportedColorizerTypes();
    }

    protected void addSupportedColorizerType(ColorizerType type) {
        supportedColorizerTypes.add(type);
    }

    private boolean isSupported(ColorizerType type) {
        return supportedColorizerTypes.contains(type);
    }

    protected abstract void setSupportedColorizerTypes();

    protected abstract Colorizer createColorizer(ColorizerType type);

    protected ColorizerType getColorizerTypeValue() {
        Colorizer c = (Colorizer) getValue();
        return c.getColorizerType();
    }

    @Override
    public String getAsText() {
        ColorizerType t = getColorizerTypeValue();

        if (isSupported(t)) {
            switch (t) {
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported colorizer.");
                case CUSTOM:
                    return CUSTOM_ID + " [" + t.getCustomColorRed() + "," +
                            t.getCustomColorGreen() + "," +
                            t.getCustomColorBlue() + "]";
                case NODE_ORIGINAL:
                    return NODE_ORIGINAL_ID;
                case PARENT_NODE:
                    return PARENT_NODE_ID;
                case EDGE_B1:
                    return EDGE_B1_ID;
                case EDGE_B2:
                    return EDGE_B2_ID;
                case EDGE_BOTH:
                    return EDGE_BOTH_ID;
                case PARENT_EDGE:
                    return PARENT_EDGE_ID;
            }
        } else {
            throw new UnsupportedOperationException(
                    "Error while retrieving colorizer.");
        }
    }

    @Override
    public void setAsText(String s) {
        if (isSupported(ColorizerType.CUSTOM)) {
            Pattern p = Pattern.compile(CUSTOM_ID +
                    "\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
            Matcher m = p.matcher(s);
            if (m.lookingAt()) {
                int r = Integer.valueOf(m.group(1));
                int g = Integer.valueOf(m.group(2));
                int b = Integer.valueOf(m.group(3));

                ColorizerType t = ColorizerType.CUSTOM;
                t.setCustomColor(r, g, b);
                setValue(createColorizer(t));
            }
        }

        if (isSupported(ColorizerType.NODE_ORIGINAL)) {
            setGenericColorizer(ColorizerType.NODE_ORIGINAL, NODE_ORIGINAL_ID, s);
        }

        if (isSupported(ColorizerType.PARENT_NODE)) {
            setGenericColorizer(ColorizerType.PARENT_NODE, PARENT_NODE_ID, s);
        }

        if (isSupported(ColorizerType.EDGE_B1)) {
            setGenericColorizer(ColorizerType.EDGE_B1, EDGE_B1_ID, s);
        }

        if (isSupported(ColorizerType.EDGE_B2)) {
            setGenericColorizer(ColorizerType.EDGE_B2, EDGE_B2_ID, s);
        }

        if (isSupported(ColorizerType.EDGE_BOTH)) {
            setGenericColorizer(ColorizerType.EDGE_BOTH, EDGE_BOTH_ID, s);
        }

        if (isSupported(ColorizerType.PARENT_EDGE)) {
            setGenericColorizer(ColorizerType.PARENT_EDGE, PARENT_EDGE_ID, s);
        }
    }

    private void setGenericColorizer(ColorizerType type, String typeId, String s) {
        Pattern p = Pattern.compile("\\s*" + typeId + "\\s*");
        Matcher m = p.matcher(s);
        if (m.lookingAt()) {
            setValue(createColorizer(type));
        }
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
}
