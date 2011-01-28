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
    private SelectionBar selectionBar;

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

            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
                selectionBar.setEnabled(enabled);
            }
        });

    }
}
