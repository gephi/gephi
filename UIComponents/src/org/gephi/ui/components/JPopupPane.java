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
import javax.swing.BorderFactory;
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

        public Dimension getPreferredSize() {
            int count = view.getComponentCount();
            int height = count > 0 ? view.getComponent(0).getPreferredSize().height : 0;
            int offset = count > 3 ? height * 3 + 5 : (count * height) + 5;
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

        public Insets getBorderInsets(Component c) {
            return ins;
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(col);
            g.drawRect(x, y + height - 2, width, 1);
            g.setColor(old);
        }
    }

    private class HideAWTListener extends ComponentAdapter implements AWTEventListener, WindowStateListener {

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

        public void componentResized(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }

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

