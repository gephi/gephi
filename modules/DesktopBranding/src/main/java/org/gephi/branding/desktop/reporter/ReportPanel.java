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

package org.gephi.branding.desktop.reporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;

/**
 * @author Mathieu Bastian
 */
public class ReportPanel extends javax.swing.JPanel {

    private final Report report;
    private final ReportController reportController;
    private final Document document;
    private final boolean autoSend;

    private javax.swing.JLabel sentLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField summaryTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea problemArea;
    private javax.swing.JLabel labelGitHubUsername;
    private javax.swing.JTextField githubUsernameTextField;
    private javax.swing.JLabel helpLabel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton viewDataButton;

    public ReportPanel(Report report) {
        this.reportController = new ReportController();
        this.report = report;
        this.autoSend = NbPreferences.forModule(ReportController.class)
            .getBoolean(ReportController.SEND_CRASH_REPORTS, ReportController.DEFAULT_SEND_CRASH_REPORTS);

        // Populates system info if not already done (manual mode) and builds XML for View Data.
        this.document = reportController.buildReportDocument(report);

        initComponents();
        setup();

        viewDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                report.setUserDescription(problemArea.getText());
                report.setUserGitHubUsername(githubUsernameTextField.getText().trim());
                DialogDescriptor dd = new DialogDescriptor(new ViewDataPanel(document),
                    NbBundle.getMessage(ReportPanel.class, "ReportPanel.viewData.title"), true,
                    DialogDescriptor.DEFAULT_OPTION, null, null);
                DialogDisplayer.getDefault().notify(dd);
            }
        });
    }

    private void initComponents() {
        sentLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        summaryTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        problemArea = new javax.swing.JTextArea();
        labelGitHubUsername = new javax.swing.JLabel();
        githubUsernameTextField = new javax.swing.JTextField();
        helpLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        viewDataButton = new javax.swing.JButton();

        sentLabel.setFont(sentLabel.getFont().deriveFont(java.awt.Font.BOLD));
        sentLabel.setForeground(new java.awt.Color(39, 119, 198));
        String sentKey = autoSend ? "ReportPanel.autoSend.sentLabel.text" : "ReportPanel.manual.sentLabel.text";
        sentLabel.setText(NbBundle.getMessage(ReportPanel.class, sentKey));

        jLabel1.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.jLabel1.text"));

        summaryTextField.setEditable(false);

        jLabel2.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.jLabel2.text"));

        problemArea.setColumns(20);
        problemArea.setRows(5);
        jScrollPane1.setViewportView(problemArea);

        labelGitHubUsername.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelGitHubUsername.text"));

        helpLabel.setFont(helpLabel.getFont().deriveFont(helpLabel.getFont().getSize() - 1f));
        helpLabel.setForeground(new java.awt.Color(132, 132, 132));
        helpLabel.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.githubUsernameHelpLabel.text"));

        jLabel3.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.jLabel3.text"));

        viewDataButton.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.viewDataButton.text"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(summaryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelGitHubUsername)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(githubUsernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                    .addComponent(helpLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                        .addComponent(viewDataButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(summaryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGitHubUsername)
                    .addComponent(githubUsernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(helpLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(viewDataButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    private void setup() {
        summaryTextField.setText(report.getSummary());
        problemArea.setText(report.getUserDescription());
        // Pre-fill saved GitHub username if available
        String savedUsername = NbPreferences.forModule(ReportController.class).get("github_username", "");
        githubUsernameTextField.setText(savedUsername);
        report.setUserGitHubUsername(savedUsername);
    }

    public void showDialog() {
        String title = NbBundle.getMessage(ReportPanel.class,
            autoSend ? "ReportPanel.autoSend.dialog.title" : "ReportPanel.manual.dialog.title");
        String actionButton = NbBundle.getMessage(ReportPanel.class,
            autoSend ? "ReportPanel.autoSend.dialog.actionButton" : "ReportPanel.manual.dialog.actionButton");
        String dismissButton = NbBundle.getMessage(ReportPanel.class,
            autoSend ? "ReportPanel.autoSend.dialog.dismissButton" : "ReportPanel.manual.dialog.dismissButton");

        Object[] options = {actionButton, dismissButton};
        DialogDescriptor dd =
            new DialogDescriptor(this, title, true, options, dismissButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) == options[0]) {
            report.setUserDescription(problemArea.getText());
            report.setUserGitHubUsername(githubUsernameTextField.getText().trim());
            reportController.sendReport(report);
        }
    }
}
