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
package org.gephi.desktop.ranking;

import java.awt.Component;
import org.gephi.ranking.spi.TransformerUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.gephi.ranking.api.Transformer;
import org.gephi.ui.components.DecoratedIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingToolbar extends JToolBar implements PropertyChangeListener {

    private final RankingUIController controller;
    private RankingUIModel model;
    private final List<ButtonGroup> buttonGroups = new ArrayList<ButtonGroup>();

    public RankingToolbar(RankingUIController controller) {
        this.controller = controller;
        initComponents();
    }

    public void refreshModel(RankingUIModel model) {
        if (this.model != null) {
            this.model.removePropertyChangeListener(this);
        }
        this.model = model;
        if (model != null) {
            model.addPropertyChangeListener(this);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                initTransformersUI();

                if (RankingToolbar.this.model != null) {
                    refreshTransformers();

                    //Select the right element group
                    refreshSelectedElmntGroup(RankingToolbar.this.model.getCurrentElementType());
                } else {
                    elementGroup.clearSelection();
                }
            }
        });
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(RankingUIModel.CURRENT_ELEMENT_TYPE)) {
            refreshSelectedElmntGroup((String) pce.getNewValue());
        }
        if (pce.getPropertyName().equals(RankingUIModel.CURRENT_TRANSFORMER)
                || pce.getPropertyName().equals(RankingUIModel.CURRENT_ELEMENT_TYPE)) {
            refreshTransformers();
        }
        if (pce.getPropertyName().equalsIgnoreCase(RankingUIModel.START_AUTO_TRANSFORMER)
                || pce.getPropertyName().equalsIgnoreCase(RankingUIModel.STOP_AUTO_TRANSFORMER)) {
            refreshDecoratedIcons();
        }
    }

    private void refreshTransformers() {
        //Select the right transformer
        int index = 0;
        for (String elmtType : controller.getElementTypes()) {
            ButtonGroup g = buttonGroups.get(index);
            boolean active = model == null ? false : model.getCurrentElementType().equals(elmtType);
            g.clearSelection();
            Transformer t = model.getCurrentTransformer(elmtType);
            String selected = model == null ? "" : controller.getUI(t).getDisplayName();
            for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                AbstractButton btn = btns.nextElement();
                btn.setVisible(active);
                if (btn.getName().equals(selected)) {
                    g.setSelected(btn.getModel(), true);
                }
            }
            index++;
        }
    }

    private void refreshDecoratedIcons() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                int index = 0;
                for (String elmtType : controller.getElementTypes()) {
                    ButtonGroup g = buttonGroups.get(index++);
                    boolean active = model == null ? false : model.getCurrentElementType().equals(elmtType);
                    if (active) {
                        for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                            btns.nextElement().repaint();
                        }
                    }
                }
            }
        });
    }

    private void refreshSelectedElmntGroup(String selected) {
        ButtonModel buttonModel = null;
        Enumeration<AbstractButton> en = elementGroup.getElements();
        for (String elmtType : controller.getElementTypes()) {
            if (elmtType.equals(selected)) {
                buttonModel = en.nextElement().getModel();
                break;
            }
            en.nextElement();
        }
        elementGroup.setSelected(buttonModel, true);
    }

    private void initTransformersUI() {
        //Clear precent buttons
        for (ButtonGroup bg : buttonGroups) {
            for (Enumeration<AbstractButton> btns = bg.getElements(); btns.hasMoreElements();) {
                AbstractButton btn = btns.nextElement();
                remove(btn);
            }
        }
        buttonGroups.clear();
        if (model != null) {
            //Add transformers buttons, separate them by element group
            for (String elmtType : controller.getElementTypes()) {
                ButtonGroup buttonGroup = new ButtonGroup();
                for (final Transformer t : model.getTransformers(elmtType)) {
                    TransformerUI u = controller.getUI(t);
                    if (u != null) {
                        //Build button
                        Icon icon = u.getIcon();
                        DecoratedIcon decoratedIcon = getDecoratedIcon(icon, t);
                        JToggleButton btn = new JToggleButton(decoratedIcon);
                        btn.setToolTipText(u.getDisplayName());
                        btn.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                model.setCurrentTransformer(t);
                            }
                        });
                        btn.setName(u.getDisplayName());
                        btn.setFocusPainted(false);
                        buttonGroup.add(btn);
                        add(btn);
                    }
                }
                buttonGroups.add(buttonGroup);
            }
        }
    }

    private void initComponents() {
        elementGroup = new javax.swing.ButtonGroup();
        for (final String elmtType : controller.getElementTypes()) {

            JToggleButton btn = new JToggleButton();
            btn.setFocusPainted(false);
            String btnLabel = elmtType;
            try {
                btnLabel = NbBundle.getMessage(RankingToolbar.class, "RankingToolbar." + elmtType + ".label");
            } catch (MissingResourceException e) {
            }
            btn.setText(btnLabel);
            btn.setEnabled(false);
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    model.setCurrentElementType(elmtType);
                }
            });
            elementGroup.add(btn);
            add(btn);
        }
        box = new javax.swing.JLabel();

        setFloatable(false);
        setRollover(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        setBorder(b);

        addSeparator();

        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        add(box);
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
    private javax.swing.JLabel box;
    private javax.swing.ButtonGroup elementGroup;

    private DecoratedIcon getDecoratedIcon(Icon icon, final Transformer transformer) {
        Icon decoration = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/gephi/desktop/ranking/resources/chain.png", false));
        return new DecoratedIcon(icon, decoration, new DecoratedIcon.DecorationController() {

            public boolean isDecorated() {
                return model != null && model.isAutoTransformer(transformer);
            }
        });
    }
}
