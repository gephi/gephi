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
package org.gephi.desktop.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Mathieu Bastian
 */
public class PropertiesBar extends JPanel {

    private JPanel propertiesBar;
    private final SelectionBar selectionBar;

    public PropertiesBar() {
        super(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(getFullScreenIcon(), BorderLayout.WEST);
        leftPanel.add(selectionBar = new SelectionBar(), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
        setOpaque(false);
    }

    public void select(JPanel propertiesBar) {
        this.propertiesBar = propertiesBar;
        propertiesBar.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        add(propertiesBar, BorderLayout.CENTER);
        propertiesBar.setOpaque(false);
        for (Component c : propertiesBar.getComponents()) {
            if (c instanceof JPanel || c instanceof JToolBar) {
                ((JComponent) c).setOpaque(false);
            }
        }
        revalidate();
    }

    public void unselect() {
        if (propertiesBar != null) {
            remove(propertiesBar);
            revalidate();
            repaint();
            propertiesBar = null;
        }
    }

    private JComponent getFullScreenIcon() {
        int logoWidth = 27;
        int logoHeight = 28;
        //fullscreen icon size
        if (UIUtils.isAquaLookAndFeel()) {
            logoWidth = 34;
        }
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Color.WHITE);
        JButton fullScreenButton = new JButton();
        fullScreenButton.setIcon(new ImageIcon(getClass().getResource("/org/gephi/desktop/tools/gephilogo_std.png")));
        fullScreenButton.setRolloverEnabled(true);
        fullScreenButton.setRolloverIcon(new ImageIcon(getClass().getResource("/org/gephi/desktop/tools/gephilogo_glow.png")));
        fullScreenButton.setToolTipText(NbBundle.getMessage(PropertiesBar.class, "PropertiesBar.fullScreenButton.tooltip"));
        fullScreenButton.setBorderPainted(false);
        fullScreenButton.setContentAreaFilled(false);
        fullScreenButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fullScreenButton.setBorder(BorderFactory.createEmptyBorder());
        fullScreenButton.setPreferredSize(new Dimension(logoWidth, logoHeight));
        fullScreenButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Lookup lookup = Lookups.forPath("org-gephi-desktop-tools/Actions/ToggleFullScreenAction");
                for (Action a : lookup.lookupAll(Action.class)) {
                    a.actionPerformed(null);
                }
            }
        });
        c.add(fullScreenButton, BorderLayout.CENTER);
        return c;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
                selectionBar.setEnabled(enabled);
            }
        });

    }
}
