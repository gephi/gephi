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
package org.gephi.ui.workspace;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class WorkspaceUISelectorPanel extends javax.swing.JPanel {

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
        workspaceLabel.setFont(new java.awt.Font("Tahoma", 0, 11));
        workspaceLabel.setText(workspace.getLookup().lookup(WorkspaceInformation.class).getName());
        workspaceLabel.setEnabled(true);
        leftArrowButton.setEnabled(getPrecedentWorkspace(workspace) != null);
        rightArrowButton.setEnabled(getNextWorkspace(workspace) != null);
        if (pane != null && pane.isPopupShown()) {
            pane.hidePopup();
        }
        this.workspace = workspace;
    }

    public void noSelectedWorkspace() {
        workspaceLabel.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        workspaceLabel.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.workspaceLabel.text")); // NOI18N
        workspaceLabel.setEnabled(false);
        leftArrowButton.setEnabled(false);
        rightArrowButton.setEnabled(false);
        workspace = null;
    }

    public void refreshList() {
        leftArrowButton.setEnabled(getPrecedentWorkspace(workspace) != null);
        rightArrowButton.setEnabled(getNextWorkspace(workspace) != null);
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

        jToolBar1 = new javax.swing.JToolBar();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        workspaceLabel = new javax.swing.JLabel();
        leftArrowButton = new javax.swing.JButton();
        rightArrowButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();

        setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.add(jSeparator1);

        workspaceLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/workspace/resources/workspace.png"))); // NOI18N
        workspaceLabel.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.workspaceLabel.text")); // NOI18N
        workspaceLabel.setIconTextGap(7);
        workspaceLabel.setMaximumSize(new java.awt.Dimension(120, 16));
        workspaceLabel.setPreferredSize(new java.awt.Dimension(100, 16));
        jToolBar1.add(workspaceLabel);

        leftArrowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/workspace/resources/leftArrow.png"))); // NOI18N
        leftArrowButton.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.leftArrowButton.text")); // NOI18N
        leftArrowButton.setToolTipText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.leftArrowButton.toolTipText")); // NOI18N
        leftArrowButton.setEnabled(false);
        leftArrowButton.setFocusable(false);
        leftArrowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        leftArrowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(leftArrowButton);

        rightArrowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/workspace/resources/rightArrow.png"))); // NOI18N
        rightArrowButton.setText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.rightArrowButton.text")); // NOI18N
        rightArrowButton.setToolTipText(org.openide.util.NbBundle.getMessage(WorkspaceUISelectorPanel.class, "WorkspaceUISelectorPanel.rightArrowButton.toolTipText")); // NOI18N
        rightArrowButton.setEnabled(false);
        rightArrowButton.setFocusable(false);
        rightArrowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rightArrowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(rightArrowButton);
        jToolBar1.add(jSeparator2);

        add(jToolBar1, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton leftArrowButton;
    private javax.swing.JButton rightArrowButton;
    private javax.swing.JLabel workspaceLabel;
    // End of variables declaration//GEN-END:variables
}
