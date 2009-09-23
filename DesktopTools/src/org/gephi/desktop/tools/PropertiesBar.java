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
package org.gephi.desktop.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Mathieu Bastian
 */
public class PropertiesBar extends JPanel {

    private JPanel propertiesBar;

    public PropertiesBar() {
        super(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(getFullScreenIcon(), BorderLayout.WEST);
        leftPanel.add(new SelectionBar(), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
    }

    public void select(JPanel propertiesBar) {
        this.propertiesBar = propertiesBar;
        propertiesBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(propertiesBar, BorderLayout.CENTER);
        revalidate();
    }

    public void unselect() {
        if (propertiesBar != null) {
            remove(propertiesBar);
            revalidate();
            propertiesBar = null;
        }
    }

    private JButton getFullScreenIcon() {
        JButton fullScreenButton = new JButton();
        fullScreenButton.setIcon(new ImageIcon(getClass().getResource("/org/gephi/desktop/tools/gephilogo_std.png")));
        fullScreenButton.setRolloverEnabled(true);
        fullScreenButton.setRolloverIcon(new ImageIcon(getClass().getResource("/org/gephi/desktop/tools/gephilogo_glow.png")));
        fullScreenButton.setToolTipText(NbBundle.getMessage(PropertiesBar.class, "PropertiesBar.fullScreenButton.tooltip"));
        fullScreenButton.setBorderPainted(false);
        fullScreenButton.setContentAreaFilled(false);
        fullScreenButton.setBorder(BorderFactory.createEmptyBorder());
        fullScreenButton.setPreferredSize(new Dimension(27, 28));
        fullScreenButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Lookup lookup = Lookups.forPath("org-gephi-desktop-tools/Actions/ToggleFullScreenAction");
                for (Action a : lookup.lookupAll(Action.class)) {
                    a.actionPerformed(null);
                }
            }
        });
        return fullScreenButton;
    }
}
