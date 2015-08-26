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
package org.gephi.desktop.banner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;

//Author Chris from pinkmatter - RibbonRootPaneLayout
class BannerRootPanelLayout implements LayoutManager2 {

    private JComponent _toolbar;

    public BannerRootPanelLayout(JComponent toolbar) {
        _toolbar = toolbar;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        int contentWidth = 0;
        int menuWidth = 0;
        int height = 0;

        JRootPane rootPane = (JRootPane) parent;
//        hideMenu(rootPane);

        Insets insets = parent.getInsets();
        height += insets.top + insets.bottom;

        Dimension contentSize;
        if (rootPane.getContentPane() != null) {
            contentSize = rootPane.getContentPane().getPreferredSize();
        } else {
            contentSize = rootPane.getSize();
        }
        contentWidth = contentSize.width;
        height += contentSize.height;

        if (rootPane.getJMenuBar() != null && rootPane.getJMenuBar().isVisible()) {
            Dimension menuSize = rootPane.getJMenuBar().getPreferredSize();
            height += menuSize.height;
            menuWidth = menuSize.width;
        }

        return new Dimension(Math.max(contentWidth, menuWidth) + insets.left + insets.right, height);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        int contentWidth = 0;
        int menuWidth = 0;
        int height = 0;

        Insets insets = parent.getInsets();
        height += insets.top + insets.bottom;

        JRootPane rootPane = (JRootPane) parent;

        Dimension contentSize;
        if (rootPane.getContentPane() != null) {
            contentSize = rootPane.getContentPane().getMinimumSize();
        } else {
            contentSize = rootPane.getSize();
        }
        contentWidth = contentSize.width;
        height += contentSize.height;

        if (rootPane.getJMenuBar() != null && rootPane.getJMenuBar().isVisible()) {
            Dimension menuSize = rootPane.getJMenuBar().getMinimumSize();
            height += menuSize.height;
            menuWidth = menuSize.width;
        }

        return new Dimension(Math.max(contentWidth, menuWidth) + insets.left + insets.right, height);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void layoutContainer(Container parent) {
        JRootPane rootPane = (JRootPane) parent;
//        hideMenu(rootPane);
        Rectangle bounds = rootPane.getBounds();
        Insets insets = rootPane.getInsets();
        int y = insets.top;
        int x = insets.left;
        int w = bounds.width - insets.right - insets.left;
        int h = bounds.height - insets.top - insets.bottom;

        if (rootPane.getLayeredPane() != null) {
            rootPane.getLayeredPane().setBounds(x, y, w, h);
        }

        if (rootPane.getGlassPane() != null) {
            rootPane.getGlassPane().setBounds(x, y, w, h);
        }

        if (rootPane.getJMenuBar() != null && rootPane.getJMenuBar().isVisible()) {
            JMenuBar menu = rootPane.getJMenuBar();
            Dimension size = menu.getPreferredSize();
            menu.setBounds(x, y, w, size.height);
            y += size.height;
        }


        if (_toolbar != null) {
            Dimension size = _toolbar.getPreferredSize();
            _toolbar.setBounds(x, y, w, size.height);
            y += size.height;
        }

        if (rootPane.getContentPane() != null) {
            int height = h - y;
            if (height < 0) {
                height = 0;
            }
            rootPane.getContentPane().setBounds(x, y, w, height);
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        System.out.println(comp);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.0f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.0f;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    private static void hideMenu(JRootPane rootPane) {
        JMenuBar menu = rootPane.getJMenuBar();
        if (menu != null) {
            menu.setVisible(false);
        }
    }
}
