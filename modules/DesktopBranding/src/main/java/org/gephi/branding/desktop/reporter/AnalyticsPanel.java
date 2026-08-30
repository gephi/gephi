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
    private javax.swing.JCheckBox disableAllTrackingCheckBox;
    private javax.swing.JSeparator disableAllSeparator;
    private javax.swing.JCheckBox sendCrashReportsCheckBox;
    private javax.swing.JTextArea crashReportsDescriptionLabel;
    private javax.swing.JCheckBox usageAnalyticsCheckBox;
    private javax.swing.JTextArea usageAnalyticsDescriptionLabel;
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
        crashReportsDescriptionLabel = createDescriptionLabel();
        usageAnalyticsCheckBox = new javax.swing.JCheckBox();
        usageAnalyticsDescriptionLabel = createDescriptionLabel();
        disableAllSeparator = new javax.swing.JSeparator();
        disableAllTrackingCheckBox = new javax.swing.JCheckBox();

        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD));
        titleLabel.setText(NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.title"));

        org.openide.awt.Mnemonics.setLocalizedText(sendCrashReportsCheckBox,
            NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.sendCrashReports.text"));
        sendCrashReportsCheckBox.addActionListener(e -> controller.changed());

        crashReportsDescriptionLabel.setText(NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.sendCrashReports.description"));

        org.openide.awt.Mnemonics.setLocalizedText(usageAnalyticsCheckBox,
            NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.usageAnalytics.text"));
        usageAnalyticsCheckBox.addActionListener(e -> controller.changed());

        usageAnalyticsDescriptionLabel.setText(NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.usageAnalytics.description"));

        org.openide.awt.Mnemonics.setLocalizedText(disableAllTrackingCheckBox,
            NbBundle.getMessage(AnalyticsPanel.class, "AnalyticsPanel.disableAllTracking.text"));
        disableAllTrackingCheckBox.addActionListener(e -> {
            boolean disableAll = disableAllTrackingCheckBox.isSelected();
            if (disableAll) {
                sendCrashReportsCheckBox.setSelected(false);
                usageAnalyticsCheckBox.setSelected(false);
            }
            sendCrashReportsCheckBox.setEnabled(!disableAll);
            usageAnalyticsCheckBox.setEnabled(!disableAll);
            controller.changed();
        });

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
                            .addComponent(crashReportsDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(usageAnalyticsCheckBox)
                            .addComponent(usageAnalyticsDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(disableAllSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
                    .addComponent(disableAllTrackingCheckBox))
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
                .addComponent(crashReportsDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(usageAnalyticsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usageAnalyticsDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(disableAllSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disableAllTrackingCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    private static javax.swing.JTextArea createDescriptionLabel() {
        javax.swing.JTextArea textArea = new javax.swing.JTextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(null);
        textArea.setForeground(new java.awt.Color(132, 132, 132));
        textArea.setFont(textArea.getFont().deriveFont(textArea.getFont().getSize() - 1f));
        return textArea;
    }

    void load() {
        boolean disableAll = NbPreferences.forModule(ReportController.class)
            .getBoolean(ReportController.DISABLE_ALL_TRACKING, false);
        disableAllTrackingCheckBox.setSelected(disableAll);
        sendCrashReportsCheckBox.setSelected(
            NbPreferences.forModule(ReportController.class).getBoolean(ReportController.SEND_CRASH_REPORTS, ReportController.DEFAULT_SEND_CRASH_REPORTS));
        usageAnalyticsCheckBox.setSelected(
            NbPreferences.forModule(ReportController.class).getBoolean(ReportController.TRACK_USAGE, ReportController.DEFAULT_TRACK_USAGE));
        sendCrashReportsCheckBox.setEnabled(!disableAll);
        usageAnalyticsCheckBox.setEnabled(!disableAll);
    }

    void store() {
        boolean disableAll = disableAllTrackingCheckBox.isSelected();
        NbPreferences.forModule(ReportController.class).putBoolean(ReportController.DISABLE_ALL_TRACKING, disableAll);
        NbPreferences.forModule(ReportController.class).putBoolean(ReportController.SEND_CRASH_REPORTS,
            disableAll ? false : sendCrashReportsCheckBox.isSelected());
        NbPreferences.forModule(ReportController.class).putBoolean(ReportController.TRACK_USAGE,
            disableAll ? false : usageAnalyticsCheckBox.isSelected());
    }

    boolean valid() {
        return true;
    }
}
