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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;


//@author S. Aubrecht
class JMenuToggleButton extends JToggleButton {

    private boolean mouseInArrowArea = false;

    /** Creates a new instance of MenuToggleButton */
    public JMenuToggleButton(final Icon regIcon, Icon rollOverIcon, int arrowWidth) {
        assert null != regIcon;
        assert null != rollOverIcon;
        final Icon lineIcon = new LineIcon(rollOverIcon, arrowWidth);
        setIcon(regIcon);
        setRolloverIcon(lineIcon);
        setRolloverSelectedIcon(lineIcon);
        setFocusable(false);

        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseMoved(MouseEvent e) {
                mouseInArrowArea = isInArrowArea(e.getPoint());
                setRolloverIcon(mouseInArrowArea ? regIcon : lineIcon);
                setRolloverSelectedIcon(mouseInArrowArea ? regIcon : lineIcon);
            }
        });

        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (isInArrowArea(e.getPoint())) {
                    JPopupMenu popup = getPopupMenu();
                    if (null != popup) {
                        popup.show(JMenuToggleButton.this, 0, getHeight());
                    }
                }
            }

            public void mouseEntered(MouseEvent e) {
                mouseInArrowArea = isInArrowArea(e.getPoint());
                setRolloverIcon(mouseInArrowArea ? regIcon : lineIcon);
                setRolloverSelectedIcon(mouseInArrowArea ? regIcon : lineIcon);
            }

            public void mouseExited(MouseEvent e) {
                mouseInArrowArea = false;
                setRolloverIcon(regIcon);
                setRolloverSelectedIcon(regIcon);
            }
        });

        setModel(new Model());
    }

    protected JPopupMenu getPopupMenu() {
        return null;
    }

    private boolean isInArrowArea(Point p) {
        return p.getLocation().x >= getWidth() - 3 - 2 - getInsets().right;
    }

    private static class LineIcon implements Icon {

        private Icon origIcon;
        private int arrowWidth;

        public LineIcon(Icon origIcon, int arrowWidth) {
            this.origIcon = origIcon;
            this.arrowWidth = arrowWidth;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            origIcon.paintIcon(c, g, x, y);

            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            g.drawLine(x + origIcon.getIconWidth() - arrowWidth - 2, y,
                    x + origIcon.getIconWidth() - arrowWidth - 2, y + getIconHeight());
            g.setColor(UIManager.getColor("controlShadow")); //NOI18N
            g.drawLine(x + origIcon.getIconWidth() - arrowWidth - 3, y,
                    x + origIcon.getIconWidth() - arrowWidth - 3, y + getIconHeight());
        }

        public int getIconWidth() {
            return origIcon.getIconWidth();
        }

        public int getIconHeight() {
            return origIcon.getIconHeight();
        }
    }

    private class Model extends JToggleButton.ToggleButtonModel {

        public void setPressed(boolean b) {
            if (mouseInArrowArea) {
                return;
            }
            super.setPressed(b);
        }
    }
}
