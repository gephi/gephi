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

package org.gephi.desktop.transformation;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.transformation.api.TransformationController;
import org.gephi.ui.utils.UIUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.transformation//Transformation//EN",
        autostore = false)
@TopComponent.Description(preferredID = "TransformationTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.transformation.TransformationTopComponent")
@ActionReference(path = "Menu/Window", position = 700)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TransformationTopComponent",
        preferredID = "TransformationTopComponent")
public final class TransformationTopComponent extends TopComponent {

    private final ImageIcon icon_mirror_y = ImageUtilities.loadImageIcon("DesktopTransform/drawing-geometry-symmetry-direction-interaction-move-svgrepo-com.svg", false);
    private final ImageIcon icon_mirror_x = ImageUtilities.loadImageIcon("DesktopTransform/measuring-symmetry-tool-direction-interaction-move-svgrepo-com.svg", false);

    private final ImageIcon icon_rotate_right = ImageUtilities.loadImageIcon("DesktopTransform/rotate-right-svgrepo-com.svg", false);
    private final ImageIcon icon_rotate_left = ImageUtilities.loadImageIcon("DesktopTransform/rotate-left-svgrepo-com.svg", false);

    private final ImageIcon icon_scale_expand = ImageUtilities.loadImageIcon("DesktopTransform/scale-expand-svgrepo-com.svg", false);
    private final ImageIcon icon_scale_reduce = ImageUtilities.loadImageIcon("DesktopTransform/scale-reduce-svgrepo-com.svg", false);

    private final TransformationController transformationController;

    public TransformationTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TransformationTopComponent.class, "CTL_TransformationTopComponent"));

        this.transformationController = Lookup.getDefault().lookup(TransformationController.class);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        if (UIUtils.isAquaLookAndFeel()) {

        }


        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {

                refreshModel();
            }

            @Override
            public void unselect(Workspace workspace) {

            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {

                refreshModel();
            }
        });

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController.getCurrentWorkspace() != null) {

        }
        refreshModel();
    }

    private void refreshModel() {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panel_mirror = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        button_mirror_y = new javax.swing.JButton(this.icon_mirror_y);
        button_mirror_x = new javax.swing.JButton(this.icon_mirror_x);
        panel_rotate = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        button_rotate_left = new javax.swing.JButton(this.icon_rotate_left);
        button_rotate_right = new javax.swing.JButton(this.icon_rotate_right);
        panel_scale = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        button_expand = new javax.swing.JButton(this.icon_scale_expand);
        button_reduce = new javax.swing.JButton(this.icon_scale_reduce);

        setLayout(new java.awt.BorderLayout());

        panel_mirror.setName(""); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.jLabel1.text")); // NOI18N
        panel_mirror.add(jLabel1);

        org.openide.awt.Mnemonics.setLocalizedText(button_mirror_y, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.button_mirror_y.text")); // NOI18N
        button_mirror_y.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_mirror_yActionPerformed(evt);
            }
        });
        jPanel5.add(button_mirror_y);

        org.openide.awt.Mnemonics.setLocalizedText(button_mirror_x, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.button_mirror_x.text")); // NOI18N
        button_mirror_x.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_mirror_xActionPerformed(evt);
            }
        });
        jPanel5.add(button_mirror_x);

        panel_mirror.add(jPanel5);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.jLabel2.text")); // NOI18N
        panel_rotate.add(jLabel2);

        org.openide.awt.Mnemonics.setLocalizedText(button_rotate_left, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.button_rotate_left.text")); // NOI18N
        button_rotate_left.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_rotate_leftActionPerformed(evt);
            }
        });
        jPanel6.add(button_rotate_left);

        org.openide.awt.Mnemonics.setLocalizedText(button_rotate_right, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.button_rotate_right.text")); // NOI18N
        button_rotate_right.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_rotate_rightActionPerformed(evt);
            }
        });
        jPanel6.add(button_rotate_right);

        panel_rotate.add(jPanel6);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.jLabel3.text")); // NOI18N
        panel_scale.add(jLabel3);

        org.openide.awt.Mnemonics.setLocalizedText(button_expand, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.button_expand.text")); // NOI18N
        button_expand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_expandActionPerformed(evt);
            }
        });
        panel_scale.add(button_expand);

        org.openide.awt.Mnemonics.setLocalizedText(button_reduce, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.button_reduce.text")); // NOI18N
        button_reduce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_reduceActionPerformed(evt);
            }
        });
        panel_scale.add(button_reduce);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panel_mirror, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(panel_rotate, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(panel_scale, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(panel_mirror, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panel_rotate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panel_scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void button_mirror_yActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_mirror_yActionPerformed


        transformationController.mirror_y();        // TODO add your handling code here:
    }//GEN-LAST:event_button_mirror_yActionPerformed

    private void button_mirror_xActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_mirror_xActionPerformed

        transformationController.mirror_x();
    }//GEN-LAST:event_button_mirror_xActionPerformed

    private void button_rotate_leftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rotate_leftActionPerformed


        transformationController.rotate_left();
    }//GEN-LAST:event_button_rotate_leftActionPerformed

    private void button_rotate_rightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rotate_rightActionPerformed


        transformationController.rotate_right();
    }//GEN-LAST:event_button_rotate_rightActionPerformed

    private void button_expandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_expandActionPerformed


        transformationController.extend();
    }//GEN-LAST:event_button_expandActionPerformed

    private void button_reduceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_reduceActionPerformed

        transformationController.reduce();
    }//GEN-LAST:event_button_reduceActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_expand;
    private javax.swing.JButton button_mirror_x;
    private javax.swing.JButton button_mirror_y;
    private javax.swing.JButton button_reduce;
    private javax.swing.JButton button_rotate_left;
    private javax.swing.JButton button_rotate_right;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel panel_mirror;
    private javax.swing.JPanel panel_rotate;
    private javax.swing.JPanel panel_scale;

    // End of variables declaration//GEN-END:variables
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
