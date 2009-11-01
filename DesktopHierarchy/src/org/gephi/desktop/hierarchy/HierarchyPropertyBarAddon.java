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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.gephi.visualization.api.PropertiesBarAddon;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyPropertyBarAddon implements PropertiesBarAddon {

    public JPanel getPanel() {
        return new HierarchyAddonPanel();
    }

    private static class HierarchyAddonPanel extends JPanel {

        public HierarchyAddonPanel() {
            JButton btn = new JButton("Hierarchy");
            btn.setMargin(new Insets(0, 14, 0, 14));
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JPopupMenu menu = createPopup();
                    menu.show(HierarchyAddonPanel.this, HierarchyAddonPanel.this.getWidth() - menu.getPreferredSize().width, HierarchyAddonPanel.this.getHeight());
                }
            });
            add(btn);
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
