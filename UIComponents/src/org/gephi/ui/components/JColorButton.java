/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.components;

import com.bric.swing.ColorPicker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class JColorButton extends JButton {

    public static String EVENT_COLOR = "color";
    private Color color;
    private boolean includeOpacity;
    private final static int ICON_WIDTH = 16;
    private final static int ICON_HEIGHT = 16;
    private final static Color DISABLED_BORDER = new Color(200, 200, 200);
    private final static Color DISABLED_FILL = new Color(220, 220, 220);

    public JColorButton(Color originalColor) {
        this(originalColor, false, false);
    }

    public JColorButton(Color originalColor, boolean rightClick, boolean includeOpacity) {
        this.includeOpacity = includeOpacity;
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
        if (rightClick) {
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    if (SwingUtilities.isRightMouseButton(e)) {
                        Color newColor = ColorPicker.showDialog(WindowManager.getDefault().getMainWindow(), color, JColorButton.this.includeOpacity);
                        if (newColor != null) {
                            setColor(newColor);
                        }
                    }
                }
            });
        } else {
            addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Color newColor = ColorPicker.showDialog(WindowManager.getDefault().getMainWindow(), color, JColorButton.this.includeOpacity);
                    if (newColor != null) {
                        setColor(newColor);
                    }
                }
            });
        }
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

    public void setIncludeOpacity(boolean includeOpacity) {
        this.includeOpacity = includeOpacity;
    }
}
