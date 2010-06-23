package org.gephi.preview.propertyeditors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.Icon;
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
        if (factory.isCustomColorMode((Colorizer) propertyEditor.getValue())) {
            customColorButton = new JColorButton(((GenericColorizer) propertyEditor.getValue()).getAwtColor(), customColorButton);
        } else {
            customColorButton = new JColorButton(Color.BLACK, customColorButton);
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

    private static class JColorButton extends JButton {

        public static String EVENT_COLOR = "color";
        private Color color;
        private Component parent;
        private final static int ICON_WIDTH = 16;
        private final static int ICON_HEIGHT = 16;
        private final static Color DISABLED_BORDER = new Color(200, 200, 200);
        private final static Color DISABLED_FILL = new Color(220, 220, 220);

        public JColorButton(Color originalColor, Component parent) {
            this.parent = parent;
            this.color = originalColor;
            setIcon(new Icon() {

                public int getIconWidth() {
                    return ICON_WIDTH;
                }

                public int getIconHeight() {
                    return ICON_HEIGHT;
                }

                public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (c.isEnabled()) {
                        g.setColor(Color.BLACK);
                        g.drawRect(x + 2, y + 2, ICON_WIDTH - 5, ICON_HEIGHT - 5);
                        if (color != null) {
                            g.setColor(color);
                            g.fillRect(x + 3, y + 3, ICON_WIDTH - 6, ICON_HEIGHT - 6);
                        }
                    } else {
                        g.setColor(DISABLED_BORDER);
                        g.drawRect(x + 2, y + 2, ICON_WIDTH - 5, ICON_HEIGHT - 5);
                        g.setColor(DISABLED_FILL);
                        g.fillRect(x + 3, y + 3, ICON_WIDTH - 6, ICON_HEIGHT - 6);
                    }

                }
            });

            addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Color newColor = JColorChooser.showDialog(JColorButton.this.parent, "Choose Color", color);
                    if (newColor != null) {
                        setColor(newColor);
                    }
                }
            });

        }

        public void setColor(Color color) {
            if (color != this.color || (color != null && !color.equals(this.color))) {
                Color oldColor = this.color;
                this.color = color;
                firePropertyChange(EVENT_COLOR, oldColor, color);
                repaint();
            }
        }

        public Color getColor() {
            return color;
        }

        public float[] getColorArray() {
            return new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f};
        }
    }
}
