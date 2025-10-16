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

package org.gephi.visualization.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.gephi.desktop.visualization.selection.SelectionPropertiesToolbar;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class PropertiesBar extends JPanel {

    private final SelectionPropertiesToolbar selectionBar;
    private final JLabel fpsLabel;
    private volatile boolean fpsThreadRunning = false;
    private Thread fpsThread;

    public PropertiesBar() {
        super(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(true);
        fpsLabel = new JLabel();
        leftPanel.add(getFpsPanel(), BorderLayout.WEST);
        leftPanel.add(selectionBar = new SelectionPropertiesToolbar(), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
        setOpaque(true);
    }

    public void setup(VisualisationModel vizModel) {
        selectionBar.setup(vizModel);
        startFpsThread();
    }

    public void unsetup() {
        selectionBar.unsetup();
        stopFpsThread();
        SwingUtilities.invokeLater(() -> fpsLabel.setText(""));
    }

    public void addToolsPropertiesBar(JComponent component) {
        add(component, BorderLayout.CENTER);
    }

    private JComponent getFpsPanel() {
        int logoWidth = 27;
        int logoHeight = 28;
        if (UIUtils.isAquaLookAndFeel()) {
            logoWidth = 34;
        }

        JPanel c = new JPanel(new BorderLayout());
        fpsLabel.setText("");
        fpsLabel.setFont(new java.awt.Font("Lucida Grande", 0, 8));
        fpsLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        c.add(fpsLabel, BorderLayout.CENTER);
        c.setPreferredSize(new Dimension(logoWidth, logoHeight));
        return c;
    }

    private void startFpsThread() {
        if (fpsThreadRunning) {
            return;
        }
        fpsThreadRunning = true;
        fpsThread = new Thread(() -> {
            final VisualizationController controller = Lookup.getDefault().lookup(VisualizationController.class);
            while (fpsThreadRunning) {
                VisualisationModel model = controller.getModel();
                String text = "";
                if (model != null) {
                    text = String.valueOf(model.getFps());
                }
                final String fpsText = text;
                SwingUtilities.invokeLater(() -> {
                    if (fpsLabel != null) {
                        fpsLabel.setText(fpsText);
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Refresh FPS Label");
        fpsThread.setDaemon(true);
        fpsThread.start();
    }

    private void stopFpsThread() {
        fpsThreadRunning = false;
        if (fpsThread != null) {
            fpsThread.interrupt();
            fpsThread = null;
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            for (Component c : getComponents()) {
                c.setEnabled(enabled);
            }
            selectionBar.setEnabled(enabled);
        });
    }
}
