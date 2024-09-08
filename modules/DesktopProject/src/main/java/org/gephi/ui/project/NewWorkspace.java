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

package org.gephi.ui.project;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.ui.utils.TimeRepresentationWrapper;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author mathieu.bastian
 */
public class NewWorkspace extends javax.swing.JPanel {

    private static final String TIME_REPRESENTATION_SAVED_PREFERENCES = "NewWorkspace_timerepresentation";

    /**
     * Creates new form NewWorkspace
     */
    public NewWorkspace() {
        initComponents();

        for (TimeRepresentation tr : TimeRepresentation.values()) {
            timeRepresentationComboBox.addItem(new TimeRepresentationWrapper(tr));
        }
    }

    public void setup() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Configuration defaultConfig = graphController.getDefaultConfigurationBuilder().build();

        // Load preference
        int savedPreference = NbPreferences.forModule(NewWorkspace.class).getInt(TIME_REPRESENTATION_SAVED_PREFERENCES, -1);
        if (savedPreference != -1) {
            timeRepresentationComboBox.setSelectedIndex(savedPreference);
        } else {
            timeRepresentationComboBox
                .setSelectedItem(new TimeRepresentationWrapper(defaultConfig.getTimeRepresentation()));
        }

        ProjectController controller = Lookup.getDefault().lookup(ProjectController.class);
        WorkspaceProvider workspaceProvider = controller.getCurrentProject().getLookup().lookup(WorkspaceProvider.class);
        String prefix = NbBundle.getMessage(NewWorkspace.class, "NewWorkspace.default.prefix");

        nameTextField.setText(prefix+" "+workspaceProvider.getNextWorkspaceId());
    }

    public void unsetup() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Configuration.Builder defaultConfig = graphController.getDefaultConfigurationBuilder();
        TimeRepresentation selected = ((TimeRepresentationWrapper)timeRepresentationComboBox.getSelectedItem()).getTimeRepresentation();

        Configuration configuration = defaultConfig.timeRepresentation(selected).build();

        ProjectController controller = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = controller.newWorkspace(controller.getCurrentProject(), configuration);
        controller.renameWorkspace(workspace, nameTextField.getText());

        // Save preference
        NbPreferences.forModule(NewWorkspace.class)
            .putInt(TIME_REPRESENTATION_SAVED_PREFERENCES, timeRepresentationComboBox.getSelectedIndex());
    }

    public static ValidationPanel createValidationPanel(NewWorkspace innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);
        ValidationGroup group = validationPanel.getValidationGroup();

        //Make sure components have names
        innerPanel.nameTextField.setName(innerPanel.labelName.getText().replace(":", ""));

        group.add(innerPanel.nameTextField, StringValidators.REQUIRE_NON_EMPTY_STRING);

        return validationPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameTextField = new javax.swing.JTextField();
        labelName = new javax.swing.JLabel();
        innerPanel = new javax.swing.JPanel();
        labelTimeRepresentation = new javax.swing.JLabel();
        timeRepresentationComboBox = new javax.swing.JComboBox<>();

        org.openide.awt.Mnemonics.setLocalizedText(labelName, org.openide.util.NbBundle.getMessage(NewWorkspace.class, "NewWorkspace.labelName.text")); // NOI18N

        innerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NewWorkspace.class, "NewWorkspace.innerPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelTimeRepresentation, org.openide.util.NbBundle.getMessage(NewWorkspace.class, "NewWorkspace.labelTimeRepresentation.text")); // NOI18N

        javax.swing.GroupLayout innerPanelLayout = new javax.swing.GroupLayout(innerPanel);
        innerPanel.setLayout(innerPanelLayout);
        innerPanelLayout.setHorizontalGroup(
            innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTimeRepresentation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(timeRepresentationComboBox, 0, 200, Short.MAX_VALUE)
                .addContainerGap())
        );
        innerPanelLayout.setVerticalGroup(
            innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTimeRepresentation)
                    .addComponent(timeRepresentationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(innerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameTextField)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(innerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel innerPanel;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelTimeRepresentation;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JComboBox<TimeRepresentationWrapper> timeRepresentationComboBox;
    // End of variables declaration//GEN-END:variables
}
