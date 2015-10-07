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
package org.gephi.desktop.tools;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Mathieu Bastian
 */
public class MouseSelectionPopupPanel extends javax.swing.JPanel {

    private ChangeListener changeListener;

    /** Creates new form MouseSelectionPopupPanel */
    public MouseSelectionPopupPanel() {
        initComponents();

        diameterSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    fireChangeEvent(source);
                }
            }
        });

        proportionnalZoomCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent(proportionnalZoomCheckbox);
            }
        });
    }

    public boolean isProportionnalToZoom() {
        return proportionnalZoomCheckbox.isSelected();
    }

    public void setProportionnalToZoom(boolean proportionnalToZoom) {
        proportionnalZoomCheckbox.setSelected(proportionnalToZoom);
    }

    public int getDiameter() {
        return diameterSlider.getValue();
    }

    public void setDiameter(int diameter) {
        diameterSlider.setValue(diameter);
    }

    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    private void fireChangeEvent(Object source) {
        if (changeListener != null) {
            ChangeEvent changeEvent = new ChangeEvent(source);
            changeListener.stateChanged(changeEvent);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        labelDiameter = new javax.swing.JLabel();
        diameterSlider = new javax.swing.JSlider();
        labelValue = new javax.swing.JLabel();
        proportionnalZoomCheckbox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        labelDiameter.setText(org.openide.util.NbBundle.getMessage(MouseSelectionPopupPanel.class, "MouseSelectionPopupPanel.labelDiameter.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 8, 0);
        add(labelDiameter, gridBagConstraints);

        diameterSlider.setMaximum(1000);
        diameterSlider.setMinimum(1);
        diameterSlider.setValue(1);
        diameterSlider.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(diameterSlider, gridBagConstraints);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, diameterSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), labelValue, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 8, 5);
        add(labelValue, gridBagConstraints);

        proportionnalZoomCheckbox.setText(org.openide.util.NbBundle.getMessage(MouseSelectionPopupPanel.class, "MouseSelectionPopupPanel.proportionnalZoomCheckbox.text")); // NOI18N
        proportionnalZoomCheckbox.setFocusable(false);
        proportionnalZoomCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 0);
        add(proportionnalZoomCheckbox, gridBagConstraints);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider diameterSlider;
    private javax.swing.JLabel labelDiameter;
    private javax.swing.JLabel labelValue;
    private javax.swing.JCheckBox proportionnalZoomCheckbox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
