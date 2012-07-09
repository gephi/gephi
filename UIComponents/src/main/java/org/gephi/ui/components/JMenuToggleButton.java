/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
