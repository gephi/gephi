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
package org.gephi.ui.workspace;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.ui.components.JPopupPane;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceUISelectorPanel extends javax.swing.JPanel implements ChangeListener {

    private JPopupPane pane;
    private Workspace workspace;

    /** Creates new form WorkspaceUISelectorPanel */
    public WorkspaceUISelectorPanel() {
        initComponents();
        workspaceLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        workspaceLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                WorkspaceUISelectorPopupContent content = new WorkspaceUISelectorPopupContent();
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                if (pc.getCurrentProject() == null) {
                    return;
                }
                for (Workspace w : pc.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces()) {
                    content.addListComponent(new WorkspacePanePanel(w));
                }
                pane = new JPopupPane(WorkspaceUISelectorPanel.this, content);
                pane.showPopupPane();
            }
        });

        leftArrowButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Workspace sel = getPrecedentWorkspace(workspace);
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                if (pc.getCurrentWorkspace() != sel) {
                    pc.openWorkspace(sel);
                }
            }
        });

        rightArrowButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Workspace sel = getNextWorkspace(workspace);
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                if (pc.getCurrentWorkspace() != sel) {
                    pc.openWorkspace(sel);
                }
            }
        });

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentWorkspace() != null) {
            setSelectedWorkspace(pc.getCurrentWorkspace());
        } else {
            noSelectedWorkspace();
        }
    }

    public void setSelectedWorkspace(Workspace workspace) {
        if (this.workspace != null) {
            this.workspace.getLookup().lookup(WorkspaceInformation.class).removeChangeListener(this);
        }
        workspaceLabel.setFont(new java.awt.Font("Tahoma", 0, 11));
        workspaceLabel.setText(workspace.getLookup().lookup(WorkspaceInformation.class).getName());
        workspaceLabel.setEnabled(true);
        leftArrowButton.setEnabled(getPrecedentWorkspace(workspace) != null);
        rightArrowButton.setEnabled(getNextWorkspace(workspace) != null);
        if (pane != null && pane.isPopupShown()) {
            pane.hidePopup();
        }
        this.workspace = workspace;
        this.workspace.getLookup().lookup(WorkspaceInformation.class).addChangeListener(this);
    }

    public void noSelectedWorkspace() {
        workspaceLabel.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        workspaceLabel.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.workspaceLabel.text")); // NOI18N
        workspaceLabel.setEnabled(false);
        leftArrowButton.setEnabled(false);
        rightArrowButton.setEnabled(false);
        if (workspace != null) {
            workspace.getLookup().lookup(WorkspaceInformation.class).removeChangeListener(this);
        }
        workspace = null;
    }

    public void refreshList() {
        if (workspace != null) {
            leftArrowButton.setEnabled(getPrecedentWorkspace(workspace) != null);
            rightArrowButton.setEnabled(getNextWorkspace(workspace) != null);
        }
    }

    public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                workspaceLabel.setText(workspace.getLookup().lookup(WorkspaceInformation.class).getName());
            }
        });
    }

    private Workspace getPrecedentWorkspace(Workspace workspace) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace prec = null;
        Workspace[] workspaces = pc.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces();
        for (Workspace w : workspaces) {
            if (w == workspace) {
                break;
            }
            prec = w;
        }
        return prec;
    }

    private Workspace getNextWorkspace(Workspace workspace) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace next = null;
        Workspace[] workspaces = pc.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces();
        for (Workspace w : workspaces) {
            if (next == workspace) {
                return w;
            }
            next = w;
        }
        return null;
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

        workspaceButtonsBar = new javax.swing.JToolBar();
        leftArrowButton = new javax.swing.JButton();
        rightArrowButton = new javax.swing.JButton();
        workspaceLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JToolBar.Separator();

        setLayout(new java.awt.GridBagLayout());

        workspaceButtonsBar.setFloatable(false);
        workspaceButtonsBar.setRollover(true);

        leftArrowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/workspace/resources/leftArrow.png"))); // NOI18N
        leftArrowButton.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.leftArrowButton.text")); // NOI18N
        leftArrowButton.setToolTipText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.leftArrowButton.toolTipText")); // NOI18N
        leftArrowButton.setEnabled(false);
        leftArrowButton.setFocusable(false);
        leftArrowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        leftArrowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        workspaceButtonsBar.add(leftArrowButton);

        rightArrowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/workspace/resources/rightArrow.png"))); // NOI18N
        rightArrowButton.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.rightArrowButton.text")); // NOI18N
        rightArrowButton.setToolTipText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.rightArrowButton.toolTipText")); // NOI18N
        rightArrowButton.setEnabled(false);
        rightArrowButton.setFocusable(false);
        rightArrowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rightArrowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        workspaceButtonsBar.add(rightArrowButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(workspaceButtonsBar, gridBagConstraints);

        workspaceLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        workspaceLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/workspace/resources/workspace.png"))); // NOI18N
        workspaceLabel.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.workspaceLabel.text")); // NOI18N
        workspaceLabel.setIconTextGap(7);
        workspaceLabel.setMaximumSize(new java.awt.Dimension(300, 16));
        workspaceLabel.setPreferredSize(new java.awt.Dimension(300, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        add(workspaceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(jSeparator2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JButton leftArrowButton;
    private javax.swing.JButton rightArrowButton;
    private javax.swing.JToolBar workspaceButtonsBar;
    private javax.swing.JLabel workspaceLabel;
    // End of variables declaration//GEN-END:variables
}
