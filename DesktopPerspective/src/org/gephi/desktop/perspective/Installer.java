/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.perspective;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // Init Banner
        initBanner();
    }

    private void initBanner() {
        //This would be too late:
        //WindowManager.getDefault().invokeWhenUIReady(new Runnable() {});
        //Therefore use this:
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //Get the main window of the NetBeans Platform:
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                //Get our custom main toolbar:
                JComponent toolbar = new BannerComponent();
                //Set the new layout of our root pane:
                frame.getRootPane().setLayout(new BannerRootPanelLayout(toolbar));
                //Install a new toolbar component into the layered pane
                //of the main frame on layer 0:
                toolbar.putClientProperty(JLayeredPane.LAYER_PROPERTY, 0);
                frame.getRootPane().getLayeredPane().add(toolbar, 0);
            }
        });
    }
}
