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
package org.gephi.ui.control.visibility;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class VisibilityToolbar extends JToolBar {

    public static enum VisibilityDisplayMode {

        DETAILS, PIE
    };
    private ButtonGroup modeGroup = new ButtonGroup();
    private VisibilityDisplayMode visibilityDisplayMode = VisibilityDisplayMode.DETAILS;

    public VisibilityToolbar() {
        initDesign();

        //Details
        final JToggleButton detailsModeButton = new JToggleButton();
        detailsModeButton.setToolTipText(NbBundle.getMessage(VisibilityToolbar.class, "VisibilityToolbar.mode.details"));
        detailsModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/control/visibility/details.png")));
        detailsModeButton.setFocusable(false);
        modeGroup.add(detailsModeButton);
        modeGroup.setSelected(detailsModeButton.getModel(), true);
        detailsModeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (detailsModeButton.isSelected()) {
                    visibilityDisplayMode = VisibilityDisplayMode.DETAILS;
                    firePropertyChange("mode", null, visibilityDisplayMode);
                }
            }
        });

        //Pie
        final JToggleButton pieModeButton = new JToggleButton();
        pieModeButton.setToolTipText(NbBundle.getMessage(VisibilityToolbar.class, "VisibilityToolbar.mode.pie"));
        pieModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/control/visibility/pie.png")));
        pieModeButton.setFocusable(false);
        modeGroup.add(pieModeButton);
        pieModeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (pieModeButton.isSelected()) {
                    visibilityDisplayMode = VisibilityDisplayMode.PIE;
                    firePropertyChange("mode", null, visibilityDisplayMode);
                }
            }
        });

        add(new JSeparator(SwingConstants.VERTICAL), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.VERTICAL, new Insets(3, 4, 3, 4), 0, 0));
        add(detailsModeButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        add(pieModeButton,new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        add(new JSeparator(SwingConstants.VERTICAL), new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.VERTICAL, new Insets(3, 4, 3, 4), 0, 0));
        add(Box.createHorizontalGlue(),new GridBagConstraints(4, 0, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    public VisibilityDisplayMode getVisibilityDisplayMode() {
        return visibilityDisplayMode;
    }

    private void initDesign() {
        setLayout(new GridBagLayout());
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
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
