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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

//Copied from org.netbeans.modules.progress.ui
//Author Milos Kleint (mkleint@netbeans.org)
public class JPopupPane {

    private HideAWTListener hideListener;
    private JComponent ancestor;
    private boolean showingPopup = false;
    private JPopupPaneComponent pane;
    private JWindow popupWindow;
    private JPanel view;

    public JPopupPane(JComponent ancestor, JPanel content) {
        this.ancestor = ancestor;
        this.view = content;
        hideListener = new HideAWTListener();
    }

    private class JPopupPaneComponent extends JScrollPane {

        public JPopupPaneComponent() {
            setName("jpopuppane");
            GridLayout grid = new GridLayout(0, 1);
            grid.setHgap(0);
            grid.setVgap(0);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            setViewportView(view);
            setFocusable(true);
            setRequestFocusEnabled(true);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }
        static final int ITEM_WIDTH = 400;

        @Override
        public Dimension getPreferredSize() {
            int count = view.getComponentCount();
            int height = count > 0 ? view.getComponent(0).getPreferredSize().height : 0;
            int offset = count > 6 ? height * 6 + 5 : (count * height) + 5;
            // 22 is the width of the additional scrollbar
            return new Dimension(count > 3 ? ITEM_WIDTH + 22
                    : ITEM_WIDTH + 2, offset);
        }

        private int findIndex(Component comp) {
            Component[] comps = view.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == comp) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class BottomLineBorder implements Border {

        private Insets ins = new Insets(0, 0, 1, 0);
        private Color col = new Color(221, 229, 248);

        public BottomLineBorder() {
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return ins;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(col);
            g.drawRect(x, y + height - 2, width, 1);
            g.setColor(old);
        }
    }

    private class HideAWTListener extends ComponentAdapter implements AWTEventListener, WindowStateListener {

        @Override
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent) aWTEvent;
                if (mv.getClickCount() > 0) {
                    if (!(aWTEvent.getSource() instanceof Component)) {
                        return;
                    }
                    Component comp = (Component) aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed("jpopuppane", comp); //NOI18N
                    Container barpar = SwingUtilities.getAncestorOfClass(ancestor.getClass(), comp);
                    if (par == null && barpar == null) {
                        hidePopup();
                    }
                }
            }
        }

        @Override
        public void windowStateChanged(WindowEvent windowEvent) {
            if (showingPopup) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();

                if (((oldState & Frame.ICONIFIED) == 0) &&
                        ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hidePopup();
//                } else if (((oldState & Frame.ICONIFIED) == Frame.ICONIFIED) &&
//                           ((newState & Frame.ICONIFIED) == 0 )) {
//                    //TODO remember we showed before and show again? I guess not worth the efford, not part of spec.
                }
            }

        }

        @Override
        public void componentResized(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }

        @Override
        public void componentMoved(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }
    }

    public void showPopupPane() {
        if (pane == null) {
            pane = new JPopupPaneComponent();
        }
        if (popupWindow == null) {
            popupWindow = new JWindow(WindowManager.getDefault().getMainWindow());
        }
        popupWindow.getContentPane().add(pane);
        showingPopup = true;

        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        WindowManager.getDefault().getMainWindow().addWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().addComponentListener(hideListener);
        resizePopup();
        popupWindow.setVisible(true);
        pane.requestFocus();
    }

    private void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0, 0);
        SwingUtilities.convertPointToScreen(point, ancestor);
        Dimension dim = popupWindow.getSize();
        Rectangle usableRect = Utilities.getUsableScreenBounds();
        int sepShift = 0;
        Point loc = new Point(point.x + ancestor.getSize().width - dim.width - sepShift - 5 * 2, point.y - dim.height - 5);
        if (!usableRect.contains(loc)) {
            loc = new Point(loc.x, point.y + 5 + ancestor.getSize().height);
        }
        popupWindow.setLocation(loc);
    }

    public void hidePopup() {
        if (popupWindow != null) {
//            popupWindow.getContentPane().removeAll();
            popupWindow.setVisible(false);
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeComponentListener(hideListener);
        showingPopup = false;
    }

    public boolean isPopupShown() {
        return showingPopup;
    }
}

