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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 *
 * @author Mathieu Bastian
 */
public class PaletteIcon implements Icon {

    private static int COLOR_WIDTH = 13;
    private static int COLOR_HEIGHT = 13;
    private static Color BORDER_COLOR = new Color(0x444444);
    private Color[] colors;

    public PaletteIcon(Color[] colors) {
        this.colors = colors;
    }

    public int getIconWidth() {
        return COLOR_WIDTH * colors.length;
    }

    public int getIconHeight() {
        return COLOR_HEIGHT + 2;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {

        for (int i = 0; i < colors.length; i++) {
            g.setColor(BORDER_COLOR);
            g.drawRect(x + 2 + i * COLOR_WIDTH, y, COLOR_WIDTH, COLOR_HEIGHT);
            g.setColor(colors[i]);
            g.fillRect(x + 2 + i * COLOR_WIDTH + 1, y + 1, COLOR_WIDTH - 1, COLOR_HEIGHT - 1);
        }
    }
}
