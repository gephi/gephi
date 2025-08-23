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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class SelectionToolbar extends JToolBar implements VisualizationPropertyChangeListener {

    private final JToggleButton mouseButton;
    private final JToggleButton rectangleButton;
    private final JToggleButton panButton;
    private final ButtonGroup buttonGroup;
    private final VisualizationController visualizationController;

    public SelectionToolbar() {
        this.visualizationController = Lookup.getDefault().lookup(VisualizationController.class);

        // Design
        setFloatable(false);
        setOrientation(JToolBar.VERTICAL);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        // Buttons
        buttonGroup = new ButtonGroup();
        mouseButton =
            new JToggleButton(ImageUtilities.loadImageIcon("VisualizationImpl/mouse.svg", false));
        mouseButton.setToolTipText(NbBundle.getMessage(SelectionToolbar.class, "SelectionToolbar.mouse.tooltip"));
        mouseButton.addActionListener(e -> visualizationController.setDirectMouseSelection());
        mouseButton.setFocusPainted(false);
        add(mouseButton);

        Icon icon = ImageUtilities.loadImageIcon("VisualizationImpl/rectangle.svg", false);

        rectangleButton = new JToggleButton(icon);
        rectangleButton
            .setToolTipText(NbBundle.getMessage(SelectionToolbar.class, "SelectionToolbar.rectangle.tooltip"));
        rectangleButton.addActionListener(e -> visualizationController.setRectangleSelection());
        rectangleButton.setFocusPainted(false);
        add(rectangleButton);

        panButton =
            new JToggleButton(ImageUtilities.loadImageIcon("VisualizationImpl/pan.svg", false));
        panButton.setToolTipText(NbBundle.getMessage(SelectionToolbar.class, "SelectionToolbar.pan.tooltip"));
        panButton.addActionListener(e -> {
            if (panButton.isSelected()) {
                visualizationController.disableSelection();
            }
        });
        panButton.setFocusPainted(false);
        add(panButton);
        addSeparator();

        // Disable
        for (Component c : getComponents()) {
            c.setEnabled(false);
        }
    }

    public void setup(VisualisationModel vizModel) {
        setEnabled(true);
        visualizationController.addPropertyChangeListener(this);
        refresh(vizModel);
    }

    public void unsetup(VisualisationModel vizModel) {
        setEnabled(false);
        visualizationController.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("selection")) {
            refresh(model);
        }
    }

    private void refresh(VisualisationModel vizModel) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (vizModel.isCustomSelection() || vizModel.isNodeSelection()) {
                    buttonGroup.clearSelection();
                } else if (!vizModel.isSelectionEnabled()) {
                    if (!buttonGroup.isSelected(panButton.getModel())) {
                        buttonGroup.setSelected(panButton.getModel(), true);
                    }
                } else if (vizModel.isDirectMouseSelection()) {
                    if (!buttonGroup.isSelected(mouseButton.getModel())) {
                        buttonGroup.setSelected(mouseButton.getModel(), true);
                    }
                } else if (vizModel.isRectangleSelection()) {
                    if (!buttonGroup.isSelected(rectangleButton.getModel())) {
                        buttonGroup.setSelected(rectangleButton.getModel(), true);
                    }
                }
            }
        });
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
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
