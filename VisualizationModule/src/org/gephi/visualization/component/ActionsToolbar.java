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
package org.gephi.visualization.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ActionsToolbar extends JToolBar {

    //Settings
    private Color color = Color.BLACK;

    public ActionsToolbar() {
        initDesign();
        initContent();
    }

    private void initContent() {

        //Center on graph
        final JButton centerOnGraphButton = new JButton();
        centerOnGraphButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "ActionsToolbar.centerOnGraph"));
        centerOnGraphButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnGraph.png")));
        centerOnGraphButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                VizController.getInstance().getGraphIO().centerOnGraph();
            }
        });
        add(centerOnGraphButton);

        //Center on zero
        final JButton centerOnZeroButton = new JButton();
        centerOnZeroButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "ActionsToolbar.centerOnZero"));
        centerOnZeroButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnZero.png")));
        centerOnZeroButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                VizController.getInstance().getGraphIO().centerOnZero();
            }
        });
        add(centerOnZeroButton);

        //Reset colors
        JColorButton resetColorButton = new JColorButton(color, true);
        resetColorButton.setToolTipText(NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetColors"));
        resetColorButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.out.println("reset");
            }
        });
        add(resetColorButton);
    }

    private void initDesign() {
        setFloatable(false);
        setOrientation(JToolBar.VERTICAL);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setBorder(BorderFactory.createEmptyBorder(0, 2, 15, 2));
    }

    public void setEnable(boolean enabled) {
        for (Component c : getComponents()) {
            c.setEnabled(enabled);
        }
    }

    @Override
    public Component add(Component comp) {
        if (comp instanceof JButton) {
            UIUtils.fixButtonUI((JButton) comp);
        }
        return super.add(comp);
    }
}
