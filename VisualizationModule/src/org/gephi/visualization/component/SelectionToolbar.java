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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.selection.SelectionManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class SelectionToolbar extends JToolBar {

    private ButtonGroup buttonGroup;

    public SelectionToolbar() {
        initDesign();
        buttonGroup = new ButtonGroup();
        initContent();
    }

    private void initContent() {

        //Mouse
        final JToggleButton mouseButton = new JToggleButton(new ImageIcon(getClass().getResource("/org/gephi/visualization/component/mouse.png")));
        mouseButton.setToolTipText(NbBundle.getMessage(SelectionToolbar.class, "SelectionToolbar.mouse.tooltip"));
        mouseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mouseButton.isSelected()) {
                    VizController.getInstance().getSelectionManager().setDirectMouseSelection();
                }
            }
        });
        add(mouseButton);

        //Rectangle
        final JToggleButton rectangleButton = new JToggleButton(new ImageIcon(getClass().getResource("/org/gephi/visualization/component/rectangle.png")));
        rectangleButton.setToolTipText(NbBundle.getMessage(SelectionToolbar.class, "SelectionToolbar.rectangle.tooltip"));
        rectangleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (rectangleButton.isSelected()) {
                    VizController.getInstance().getSelectionManager().setRectangleSelection();
                }
            }
        });
        add(rectangleButton);

        //Drag
        final JToggleButton dragButton = new JToggleButton(new ImageIcon(getClass().getResource("/org/gephi/visualization/component/hand.png")));
        dragButton.setToolTipText(NbBundle.getMessage(SelectionToolbar.class, "SelectionToolbar.drag.tooltip"));
        dragButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (dragButton.isSelected()) {
                    VizController.getInstance().getSelectionManager().setDraggingMouseSelection();
                }
            }
        });
        add(dragButton);
        addSeparator();

        buttonGroup.setSelected(rectangleButton.getModel(), VizController.getInstance().getVizConfig().isRectangleSelection());
        buttonGroup.setSelected(mouseButton.getModel(), !VizController.getInstance().getVizConfig().isRectangleSelection());
        buttonGroup.setSelected(dragButton.getModel(), VizController.getInstance().getVizConfig().isDraggingEnable());

        //Init events
        VizController.getInstance().getSelectionManager().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                SelectionManager selectionManager = VizController.getInstance().getSelectionManager();
                if (selectionManager.isBlocked()) {
                    buttonGroup.clearSelection();
                } else if (!selectionManager.isSelectionEnabled()) {
                    buttonGroup.clearSelection();
                } else if (selectionManager.isDirectMouseSelection()) {
                    if (!buttonGroup.isSelected(mouseButton.getModel())) {
                        buttonGroup.setSelected(mouseButton.getModel(), true);
                    }
                }
            }
        });
    }

    private void initDesign() {
        setFloatable(false);
        setOrientation(JToolBar.VERTICAL);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
    }

    @Override
    public Component add(Component comp) {
        if (comp instanceof JButton) {
            UIUtils.fixButtonUI((JButton) comp);
        }
        if (comp instanceof AbstractButton) {
            buttonGroup.add((AbstractButton) comp);
        }

        return super.add(comp);
    }
}
