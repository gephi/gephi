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
package org.gephi.desktop.hierarchy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.gephi.visualization.apiimpl.PropertiesBarAddon;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyPropertyBarAddon implements PropertiesBarAddon {

    public JComponent getComponent() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)) {

            @Override
            public void setEnabled(final boolean enabled) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        for (Component c : getComponents()) {
                            c.setEnabled(enabled);
                        }
                    }
                });
                setOpaque(enabled);
            }
        };
        panel.add(new HierarchyAddonButton());
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private static class HierarchyAddonButton extends JButton {

        public HierarchyAddonButton() {
            super("Hierarchy");
            setOpaque(false);
            setMargin(new Insets(0, 10, 0, 10));
            setFocusPainted(false);
            setPreferredSize(new Dimension(95, 28));
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/hierarchy/resources/bulb.png"))); // NOI18N
            setBorder(null);
            addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JPopupMenu menu = createPopup();
                    menu.show(HierarchyAddonButton.this, HierarchyAddonButton.this.getWidth() - menu.getPreferredSize().width, HierarchyAddonButton.this.getHeight());
                }
            });
        }

        private JPopupMenu createPopup() {
            HierarchyControlPanel controlPanel = new HierarchyControlPanel();
            controlPanel.setup();
            JPopupMenu menu = new JPopupMenu();
            menu.add(controlPanel);
            return menu;
        }
    }
}
