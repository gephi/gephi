/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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

package org.gephi.desktop.datalab;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Flamingo command buttons have no accessible context, which hides them from assistive technologies, and their UI
 * delegate does not paint keyboard focus. This subclass adds both. The button stays reachable with Tab but does not
 * take focus on mouse click, like the other toolbar buttons.
 */
class AccessibleCommandButton extends JCommandButton implements Accessible {

    AccessibleCommandButton(String text) {
        super(text);
        setRequestFocusEnabled(false);
    }

    AccessibleCommandButton(String text, ResizableIcon icon) {
        super(text, icon);
        setRequestFocusEnabled(false);
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleCommandButtonContext();
        }
        return accessibleContext;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hasFocus() && isEnabled()) {
            g.setColor(getForeground());
            g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
        }
    }

    @Override
    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        // Flamingo only repaints on focus gain, which would leave a stale ring behind
        repaint();
    }

    private class AccessibleCommandButtonContext extends AccessibleJComponent implements AccessibleAction {

        @Override
        public String getAccessibleName() {
            String name = super.getAccessibleName();
            return name != null ? name : getText();
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }

        @Override
        public int getAccessibleChildrenCount() {
            // Hide Flamingo's internal renderer pane, which holds an unnamed button
            return 0;
        }

        @Override
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        @Override
        public AccessibleAction getAccessibleAction() {
            return this;
        }

        @Override
        public int getAccessibleActionCount() {
            return 1;
        }

        @Override
        public String getAccessibleActionDescription(int i) {
            return i == 0 ? AccessibleAction.CLICK : null;
        }

        @Override
        public boolean doAccessibleAction(int i) {
            if (i != 0 || !isEnabled()) {
                return false;
            }
            if (getCommandButtonKind().hasAction()) {
                doActionClick();
            } else {
                doPopupClick();
            }
            return true;
        }
    }
}
