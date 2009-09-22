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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.selection.SelectionManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Mathieu Bastian
 */
public class PropertiesBar extends JPanel {

    private JPanel propertiesBar;

    public PropertiesBar() {
        super(new BorderLayout());
        add(getFullScreenIcon(), BorderLayout.WEST);
        add(new SelectionBar(), BorderLayout.CENTER);
    }

    public void select(JPanel propertiesBar) {
        this.propertiesBar = propertiesBar;
        propertiesBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(propertiesBar, BorderLayout.EAST);
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
        fullScreenButton.setToolTipText("FullScreen");
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

    private static class SelectionBar extends JPanel {

        private JLabel statusLabel;

        public SelectionBar() {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            statusLabel = new JLabel();
            add(statusLabel);
            add(new JSeparator(SwingConstants.VERTICAL));
            VizController.getInstance().getSelectionManager().addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    refresh();
                }
            });
            refresh();
        }

        public void refresh() {
            SelectionManager manager = VizController.getInstance().getSelectionManager();
            if (manager.isSelectionEnabled()) {
                if (manager.isRectangleSelection()) {
                    statusLabel.setText("Rectangle selection");
                } else if (manager.isDirectMouseSelection()) {
                    statusLabel.setText("Mouse selection");
                } else if (manager.isDraggingEnabled()) {
                    statusLabel.setText("Dragging");
                }
            } else {
                statusLabel.setText("No selection");
            }
        }
    }
}
