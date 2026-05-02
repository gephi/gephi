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

import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class AnalyticsPanel extends JPanel {

    private final AnalyticsOptionsPanelController controller;
    private javax.swing.JCheckBox sendCrashReportsCheckBox;
    private javax.swing.JLabel crashReportsDescriptionLabel;
    private javax.swing.JCheckBox trackUsageCheckBox;
    private javax.swing.JLabel trackUsageDescriptionLabel;
    private javax.swing.JCheckBox doNotRemindCheckBox;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel titleLabel;

    AnalyticsPanel(AnalyticsOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        titleLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        sendCrashReportsCheckBox = new javax.swing.JCheckBox();
        crashReportsDescriptionLabel = new javax.swing.JLabel();
        trackUsageCheckBox = new javax.swing.JCheckBox();
        trackUsageDescriptionLabel = new javax.swing.JLabel();
        doNotRemindCheckBox = new javax.swing.JCheckBox();

        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD));
        titleLabel.setText(NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.title"));

        org.openide.awt.Mnemonics.setLocalizedText(sendCrashReportsCheckBox,
            NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.sendCrashReports.text"));
        sendCrashReportsCheckBox.addActionListener(e -> controller.changed());

        crashReportsDescriptionLabel.setForeground(new java.awt.Color(132, 132, 132));
        crashReportsDescriptionLabel.setFont(crashReportsDescriptionLabel.getFont().deriveFont(crashReportsDescriptionLabel.getFont().getSize() - 1f));
        crashReportsDescriptionLabel.setText(NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.sendCrashReports.description"));

        org.openide.awt.Mnemonics.setLocalizedText(trackUsageCheckBox,
            NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.trackUsage.text"));
        trackUsageCheckBox.addActionListener(e -> controller.changed());

        trackUsageDescriptionLabel.setForeground(new java.awt.Color(132, 132, 132));
        trackUsageDescriptionLabel.setFont(trackUsageDescriptionLabel.getFont().deriveFont(trackUsageDescriptionLabel.getFont().getSize() - 1f));
        trackUsageDescriptionLabel.setText(NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.trackUsage.description"));

        org.openide.awt.Mnemonics.setLocalizedText(doNotRemindCheckBox,
            NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.doNotRemind.text"));
        doNotRemindCheckBox.addActionListener(e -> controller.changed());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleLabel)
                    .addComponent(separator, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sendCrashReportsCheckBox)
                            .addComponent(crashReportsDescriptionLabel)
                            .addComponent(trackUsageCheckBox)
                            .addComponent(trackUsageDescriptionLabel)
                            .addComponent(doNotRemindCheckBox))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sendCrashReportsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(crashReportsDescriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(trackUsageCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trackUsageDescriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(doNotRemindCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    void load() {
        sendCrashReportsCheckBox.setSelected(
            NbPreferences.forModule(ReportController.class).getBoolean(ReportController.SEND_CRASH_REPORTS, ReportController.DEFAULT_SEND_CRASH_REPORTS));
        trackUsageCheckBox.setSelected(
            NbPreferences.forModule(ReportController.class).getBoolean(ReportController.TRACK_USAGE, ReportController.DEFAULT_TRACK_USAGE));
        doNotRemindCheckBox.setSelected(
            NbPreferences.forModule(ReportController.class).getBoolean(ReportController.DO_NOT_REMIND_ANALYTICS, false));
    }

    void store() {
        NbPreferences.forModule(ReportController.class).putBoolean(ReportController.SEND_CRASH_REPORTS, sendCrashReportsCheckBox.isSelected());
        NbPreferences.forModule(ReportController.class).putBoolean(ReportController.TRACK_USAGE, trackUsageCheckBox.isSelected());
        NbPreferences.forModule(ReportController.class).putBoolean(ReportController.DO_NOT_REMIND_ANALYTICS, doNotRemindCheckBox.isSelected());
    }

    boolean valid() {
        return true;
    }
}
