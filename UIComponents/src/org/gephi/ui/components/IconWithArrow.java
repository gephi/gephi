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
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

//author S. Aubrecht from org.openide.awt
public class IconWithArrow implements Icon {

    private static final String ARROW_IMAGE_NAME = "org/openide/awt/resources/arrow.png"; //NOI18N
    private Icon orig;
    private Icon arrow = ImageUtilities.image2Icon(ImageUtilities.loadImage(ARROW_IMAGE_NAME, false));
    private boolean paintRollOver;
    private static final int GAP = 6;

    /** Creates a new instance of IconWithArrow */
    public IconWithArrow(Icon orig, boolean paintRollOver) {
        this.orig = orig;
        this.paintRollOver = paintRollOver;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        int height = getIconHeight();
        orig.paintIcon(c, g, x, y + (height - orig.getIconHeight()) / 2);

        arrow.paintIcon(c, g, x + GAP + orig.getIconWidth(), y + (height - arrow.getIconHeight()) / 2);

        if (paintRollOver) {
            Color brighter = UIManager.getColor("controlHighlight"); //NOI18N
            Color darker = UIManager.getColor("controlShadow"); //NOI18N
            if (null == brighter || null == darker) {
                brighter = c.getBackground().brighter();
                darker = c.getBackground().darker();
            }
            if (null != brighter && null != darker) {
                g.setColor(brighter);
                g.drawLine(x + orig.getIconWidth() + 1, y,
                        x + orig.getIconWidth() + 1, y + getIconHeight());
                g.setColor(darker);
                g.drawLine(x + orig.getIconWidth() + 2, y,
                        x + orig.getIconWidth() + 2, y + getIconHeight());
            }
        }
    }

    public int getIconWidth() {
        return orig.getIconWidth() + GAP + arrow.getIconWidth();
    }

    public int getIconHeight() {
        return Math.max(orig.getIconHeight(), arrow.getIconHeight());
    }

    public static int getArrowAreaWidth() {
        return GAP / 2 + 5;
    }
}
