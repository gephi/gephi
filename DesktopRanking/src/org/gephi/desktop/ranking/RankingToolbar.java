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
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.gephi.ranking.api.Transformer;
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
                    //Select the right transformer
                    int index = 0;
                    for (String elmtType : controller.getElementTypes()) {
                        ButtonGroup g = buttonGroups.get(index);
                        boolean active = RankingToolbar.this.model == null ? false : RankingToolbar.this.model.getCurrentElementType().equals(elmtType);
                        g.clearSelection();
                        String selected = RankingToolbar.this.model == null ? "" : controller.getUI(RankingToolbar.this.model.getCurrentTransformer(elmtType)).getDisplayName();
                        for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                            AbstractButton btn = btns.nextElement();
                            btn.setVisible(active);
                            if (btn.getName().equals(selected)) {
                                g.setSelected(btn.getModel(), true);
                            }
                        }
                        index++;
                    }

                    //Select the right element group
                    ButtonModel buttonModel = null;
                    Enumeration<AbstractButton> en = elementGroup.getElements();
                    for (String elmtType : controller.getElementTypes()) {
                        if (elmtType.equals(RankingToolbar.this.model.getCurrentElementType())) {
                            buttonModel = en.nextElement().getModel();
                            break;
                        }
                        en.nextElement();
                    }
                    elementGroup.setSelected(buttonModel, true);
                } else {
                    elementGroup.clearSelection();
                }
            }
        });
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(RankingUIModel.CURRENT_ELEMENT_TYPE)) {
            ButtonModel buttonModel = null;
            Enumeration<AbstractButton> en = elementGroup.getElements();
            for (String elmtType : controller.getElementTypes()) {
                if (elmtType.equals((String) pce.getNewValue())) {
                    buttonModel = en.nextElement().getModel();
                    break;
                }
                en.nextElement();
            }
            elementGroup.setSelected(buttonModel, true);
        }
        if (pce.getPropertyName().equals(RankingUIModel.CURRENT_TRANSFORMER)
                || pce.getPropertyName().equals(RankingUIModel.CURRENT_ELEMENT_TYPE)) {
            String selectedTransformer = controller.getUI((Transformer) model.getCurrentTransformer()).getDisplayName();
            int index = 0;
            for (String elmtType : controller.getElementTypes()) {
                ButtonGroup g = buttonGroups.get(index);
                boolean active = model.getCurrentElementType().equals(elmtType);
                if (active) {
                    for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                        AbstractButton btn = btns.nextElement();
                        if (btn.getName().equals(selectedTransformer)) {
                            g.setSelected(btn.getModel(), true);
                        }
                    }
                }
            }
        }
    }

    private void initTransformersUI() {
        //Clear precent buttons
        for (ButtonGroup bg : buttonGroups) {
            for (Enumeration<AbstractButton> btns = bg.getElements(); btns.hasMoreElements();) {
                AbstractButton btn = btns.nextElement();
                remove(btn);
            }
        }
        if (model != null) {
            //Add transformers buttons, separate them by element group
            for (String elmtType : controller.getElementTypes()) {
                ButtonGroup buttonGroup = new ButtonGroup();
                for (final Transformer t : model.getTransformers(elmtType)) {
                    TransformerUI u = controller.getUI(t);
                    if (u != null) {
                        //Build button
                        JToggleButton btn = new JToggleButton(u.getIcon());
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
}
