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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import javax.swing.*;
import org.gephi.desktop.banner.perspective.spi.BottomComponent;
import org.gephi.perspective.api.PerspectiveController;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        //Initialize the perspective controller
        Lookup.getDefault().lookup(PerspectiveController.class);

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

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                Container contentPane = ((JRootPane) frame.getComponents()[0]).getContentPane();

                //Get the bottom component
                BottomComponent bottomComponentImpl = Lookup.getDefault().lookup(BottomComponent.class);
                JComponent bottomComponent = bottomComponentImpl != null ? bottomComponentImpl.getComponent() : null;

                JComponent statusLinePanel = null;
                JPanel childPanel = (JPanel) contentPane.getComponents()[1];
                JLayeredPane layeredPane = (JLayeredPane) childPanel.getComponents()[0];
                Container desktopPanel = (Container) layeredPane.getComponent(0);
                for (Component c : desktopPanel.getComponents()) {
                    if (c instanceof JPanel) {
                        JPanel cp = (JPanel) c;
                        for (Component cpnt : cp.getComponents()) {
                            if (cpnt.getName() != null && cpnt.getName().equals("statusLine")) {
                                statusLinePanel = (JComponent) cpnt;
                                break;
                            }
                        }
                    }
                }

                if (statusLinePanel != null) {
                    frame.getContentPane().remove(statusLinePanel);
                    JPanel southPanel = new JPanel(new BorderLayout());
                    southPanel.add(statusLinePanel, BorderLayout.SOUTH);
                    if (bottomComponent != null) {
                        bottomComponent.setVisible(false);
                        southPanel.add(bottomComponent, BorderLayout.CENTER);
                    }
                    frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
                }
            }
        });
    }
}
