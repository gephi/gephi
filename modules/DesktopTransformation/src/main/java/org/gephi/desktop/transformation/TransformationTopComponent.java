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

import javax.swing.ImageIcon;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.transformation.api.TransformationController;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.transformation//Transformation//EN",
    autostore = false)
@TopComponent.Description(preferredID = "TransformationTopComponent",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.transformation.TransformationTopComponent")
@ActionReference(path = "Menu/Window", position = 750)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TransformationTopComponent",
    preferredID = "TransformationTopComponent")
public final class TransformationTopComponent extends TopComponent {

    private final ImageIcon icon_mirror_y = ImageUtilities.loadImageIcon(
            "DesktopLayout/transformations/mirror_yaxis.svg", false);
    private final ImageIcon icon_mirror_x = ImageUtilities.loadImageIcon(
            "DesktopLayout/transformations/mirror_xaxis.svg", false);

    private final ImageIcon icon_rotate_right_1deg =
        ImageUtilities.loadImageIcon("DesktopLayout/transformations/rotate_right_1deg.svg", false);
    private final ImageIcon icon_rotate_left_1deg =
        ImageUtilities.loadImageIcon("DesktopLayout/transformations/rotate_left_1deg.svg", false);

    private final ImageIcon icon_rotate_right_45deg =
        ImageUtilities.loadImageIcon("DesktopLayout/transformations/rotate_right_45deg.svg", false);
    private final ImageIcon icon_rotate_left_45deg =
        ImageUtilities.loadImageIcon("DesktopLayout/transformations/rotate_left_45deg.svg", false);

    private final ImageIcon icon_scale_expand =
        ImageUtilities.loadImageIcon("DesktopLayout/transformations/scale_expand.svg", false);
    private final ImageIcon icon_scale_reduce =
        ImageUtilities.loadImageIcon("DesktopLayout/transformations/scale_reduce.svg", false);

    private final TransformationController transformationController;

    public TransformationTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TransformationTopComponent.class, "CTL_TransformationTopComponent"));

        this.transformationController = Lookup.getDefault().lookup(TransformationController.class);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);


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
                refreshModel();
            }

            @Override
            public void close(Workspace workspace) {

            }

            @Override
            public void disable() {

                refreshModel();
            }
        });


        refreshModel();


    }

    private void refreshModel() {
        boolean canBeActive = true;//  Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace() != null;

        buttonExpand.setEnabled(canBeActive);
        buttonReduce.setEnabled(canBeActive);
        buttonMirrorX.setEnabled(canBeActive);
        buttonMirrorY.setEnabled(canBeActive);
        buttonRotateLeft.setEnabled(canBeActive);
        buttonRotateRight.setEnabled(canBeActive);
        buttonRotateLeft45deg.setEnabled(canBeActive);
        buttonRotateRight45deg.setEnabled(canBeActive);
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
        buttonMirrorY = new javax.swing.JButton(this.icon_mirror_y);
        buttonMirrorX = new javax.swing.JButton(this.icon_mirror_x);
        panel_rotate = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        buttonRotateLeft45deg = new javax.swing.JButton(this.icon_rotate_left_45deg);
        buttonRotateLeft = new javax.swing.JButton(this.icon_rotate_left_1deg);
        buttonRotateRight = new javax.swing.JButton(this.icon_rotate_right_1deg);
        buttonRotateRight45deg = new javax.swing.JButton(this.icon_rotate_right_45deg);
        panel_scale = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        buttonExpand = new javax.swing.JButton(this.icon_scale_expand);
        buttonReduce = new javax.swing.JButton(this.icon_scale_reduce);

        setLayout(new java.awt.BorderLayout());

        panel_mirror.setName(""); // NOI18N
        panel_mirror.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.jLabel1.text")); // NOI18N
        panel_mirror.add(jLabel1);

        org.openide.awt.Mnemonics.setLocalizedText(buttonMirrorY, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonMirrorY.text")); // NOI18N
        buttonMirrorY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMirrorYActionPerformed(evt);
            }
        });
        jPanel5.add(buttonMirrorY);

        org.openide.awt.Mnemonics.setLocalizedText(buttonMirrorX, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonMirrorX.text")); // NOI18N
        buttonMirrorX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMirrorXActionPerformed(evt);
            }
        });
        jPanel5.add(buttonMirrorX);

        panel_mirror.add(jPanel5);

        panel_rotate.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.jLabel2.text")); // NOI18N
        panel_rotate.add(jLabel2);

        org.openide.awt.Mnemonics.setLocalizedText(buttonRotateLeft45deg, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonRotateLeft45deg.text")); // NOI18N
        buttonRotateLeft45deg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRotateLeft45degActionPerformed(evt);
            }
        });
        jPanel6.add(buttonRotateLeft45deg);

        org.openide.awt.Mnemonics.setLocalizedText(buttonRotateLeft, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonRotateLeft.text")); // NOI18N
        buttonRotateLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRotateLeftActionPerformed(evt);
            }
        });
        jPanel6.add(buttonRotateLeft);

        org.openide.awt.Mnemonics.setLocalizedText(buttonRotateRight, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonRotateRight.text")); // NOI18N
        buttonRotateRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRotateRightActionPerformed(evt);
            }
        });
        jPanel6.add(buttonRotateRight);

        org.openide.awt.Mnemonics.setLocalizedText(buttonRotateRight45deg, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonRotateRight45deg.text")); // NOI18N
        buttonRotateRight45deg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRotateRight45degActionPerformed(evt);
            }
        });
        jPanel6.add(buttonRotateRight45deg);

        panel_rotate.add(jPanel6);

        panel_scale.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.jLabel3.text")); // NOI18N
        panel_scale.add(jLabel3);

        org.openide.awt.Mnemonics.setLocalizedText(buttonExpand, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonExpand.text")); // NOI18N
        buttonExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExpandActionPerformed(evt);
            }
        });
        panel_scale.add(buttonExpand);

        org.openide.awt.Mnemonics.setLocalizedText(buttonReduce, org.openide.util.NbBundle.getMessage(TransformationTopComponent.class, "TransformationTopComponent.buttonReduce.text")); // NOI18N
        buttonReduce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonReduceActionPerformed(evt);
            }
        });
        panel_scale.add(buttonReduce);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel_mirror, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panel_rotate, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panel_scale, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(panel_mirror, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel_rotate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addComponent(panel_scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMirrorYActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_mirror_yActionPerformed


        transformationController.mirrorY();        // TODO add your handling code here:
    }//GEN-LAST:event_button_mirror_yActionPerformed

    private void buttonMirrorXActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_mirror_xActionPerformed

        transformationController.mirrorX();
    }//GEN-LAST:event_button_mirror_xActionPerformed

    private void buttonRotateLeftActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rotate_leftActionPerformed


        transformationController.rotateLeft1Deg();
    }//GEN-LAST:event_button_rotate_leftActionPerformed

    private void buttonRotateRightActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rotate_rightActionPerformed


        transformationController.rotateRight1Deg();
    }//GEN-LAST:event_button_rotate_rightActionPerformed

    private void buttonExpandActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_expandActionPerformed


        transformationController.extend();
    }//GEN-LAST:event_button_expandActionPerformed

    private void buttonReduceActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_reduceActionPerformed

        transformationController.reduce();
    }//GEN-LAST:event_button_reduceActionPerformed

    private void buttonRotateLeft45degActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rotate_left_45degActionPerformed
        transformationController.rotateLeft45Deg();
    }//GEN-LAST:event_button_rotate_left_45degActionPerformed

    private void buttonRotateRight45degActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_rotate_right_45degActionPerformed
        transformationController.rotateRight45Deg();
    }//GEN-LAST:event_button_rotate_right_45degActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonExpand;
    private javax.swing.JButton buttonMirrorX;
    private javax.swing.JButton buttonMirrorY;
    private javax.swing.JButton buttonReduce;
    private javax.swing.JButton buttonRotateLeft;
    private javax.swing.JButton buttonRotateLeft45deg;
    private javax.swing.JButton buttonRotateRight;
    private javax.swing.JButton buttonRotateRight45deg;
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
