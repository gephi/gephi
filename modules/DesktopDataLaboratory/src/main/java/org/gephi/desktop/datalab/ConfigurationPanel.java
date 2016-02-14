/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.desktop.datalab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.joda.time.DateTimeZone;
import org.openide.util.NbBundle;

/**
 * Configurations dialog for DataTableTopComponent
 *
 * @author Eduardo Ramos
 */
public class ConfigurationPanel extends javax.swing.JPanel {

    private final DataTableTopComponent dataTableTopComponent;
    private final GraphModel graphModel;

    /**
     * Creates new form ConfigurationPanel
     */
    public ConfigurationPanel(final DataTableTopComponent dataTableTopComponent, final GraphModel graphModel) {
        this.dataTableTopComponent = dataTableTopComponent;
        this.graphModel = graphModel;
        initComponents();

        for (TimeFormat tf : TimeFormat.values()) {
            timeFormatComboBox.addItem(new TimeFormatWrapper(tf));
        }
        timeFormatComboBox.setSelectedItem(new TimeFormatWrapper(graphModel.getTimeFormat()));

        timeFormatComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                graphModel.setTimeFormat(((TimeFormatWrapper) timeFormatComboBox.getSelectedItem()).timeFormat);
                dataTableTopComponent.refreshCurrentTable();
            }
        });

        onlyVisibleCheckBox.setSelected(dataTableTopComponent.isShowOnlyVisible());
        useSparklinesCheckBox.setSelected(dataTableTopComponent.isUseSparklines());
        timeIntervalsGraphicsCheckBox.setSelected(dataTableTopComponent.isTimeIntervalGraphics());
        showEdgesNodesLabelsCheckBox.setSelected(dataTableTopComponent.isShowEdgesNodesLabels());

        //Timezone:
        long currentTimestamp = System.currentTimeMillis();
        DateTimeZone currentTimeZone = graphModel.getTimeZone();

        buildTimeZoneList();
        timeZoneComboBox.setSelectedItem(new TimeZoneWrapper(currentTimeZone.toTimeZone(), currentTimestamp));

        timeZoneComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TimeZone selected = ((TimeZoneWrapper) timeZoneComboBox.getSelectedItem()).timeZone;
                graphModel.setTimeZone(DateTimeZone.forTimeZone(selected));
                dataTableTopComponent.refreshCurrentTable();
            }
        });

        //Time representation:
        for (TimeRepresentation tr : TimeRepresentation.values()) {
            timeRepresentationComboBox.addItem(new TimeRepresentationWrapper(tr));
        }
        timeRepresentationComboBox.setSelectedItem(new TimeRepresentationWrapper(graphModel.getConfiguration().getTimeRepresentation()));

        if (canChangeTimeRepresentation(graphModel)) {
            timeRepresentationComboBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Configuration c = graphModel.getConfiguration();
                    c.setTimeRepresentation(((TimeRepresentationWrapper) timeRepresentationComboBox.getSelectedItem()).timeRepresentation);
                    graphModel.setConfiguration(c);
                }
            });
        } else {
            timeRepresentationComboBox.setEnabled(false);
            timeRepresentationComboBox.setToolTipText(NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeRepresentation.disabled.tooltip"));
        }
    }

    private boolean canChangeTimeRepresentation(GraphModel graphModel) {
        if (graphModel.getGraph().getNodeCount() > 0) {
            return false;//Graph has to be empty
        }

        //Also there cannot be any column apart from the basic ones:
        for (Column column : graphModel.getNodeTable()) {
            String id = column.getId();
            if (!id.equalsIgnoreCase("Id") && !id.equalsIgnoreCase("Label") && !id.equalsIgnoreCase("timeset")) {
                return false;
            }
        }

        for (Column column : graphModel.getEdgeTable()) {
            String id = column.getId();
            if (!id.equalsIgnoreCase("Id") && !id.equalsIgnoreCase("Label") && !id.equalsIgnoreCase("timeset") && !id.equalsIgnoreCase("Weight")) {
                return false;
            }
        }

        return true;
    }

    private void buildTimeZoneList() {
        long currentTimestamp = System.currentTimeMillis();
        for (String id : TimeZone.getAvailableIDs()) {
            timeZoneComboBox.addItem(new TimeZoneWrapper(TimeZone.getTimeZone(id), currentTimestamp));
        }
    }

    class TimeFormatWrapper {

        private final TimeFormat timeFormat;

        public TimeFormatWrapper(TimeFormat timeFormat) {
            this.timeFormat = timeFormat;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.timeFormat != null ? this.timeFormat.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TimeFormatWrapper other = (TimeFormatWrapper) obj;
            if (this.timeFormat != other.timeFormat) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeFormat." + timeFormat.name());
        }
    }

    class TimeRepresentationWrapper {

        private final TimeRepresentation timeRepresentation;

        public TimeRepresentationWrapper(TimeRepresentation timeRepresentation) {
            this.timeRepresentation = timeRepresentation;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.timeRepresentation != null ? this.timeRepresentation.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TimeRepresentationWrapper other = (TimeRepresentationWrapper) obj;
            if (this.timeRepresentation != other.timeRepresentation) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeRepresentation." + timeRepresentation.name());
        }
    }

    class TimeZoneWrapper {

        private final TimeZone timeZone;
        private final long currentTimestamp;

        public TimeZoneWrapper(TimeZone timeZone, long currentTimestamp) {
            this.timeZone = timeZone;
            this.currentTimestamp = currentTimestamp;
        }

        private String getTimeZoneText() {
            int offset = timeZone.getOffset(currentTimestamp);
            long hours = TimeUnit.MILLISECONDS.toHours(offset);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(offset)
                    - TimeUnit.HOURS.toMinutes(hours);
            minutes = Math.abs(minutes);

            if (hours >= 0) {
                return String.format("%s (GMT+%d:%02d)", timeZone.getID(), hours, minutes);
            } else {
                return String.format("%s (GMT%d:%02d)", timeZone.getID(), hours, minutes);
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.timeZone != null ? this.timeZone.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TimeZoneWrapper other = (TimeZoneWrapper) obj;
            if (this.timeZone != other.timeZone && (this.timeZone == null || !this.timeZone.equals(other.timeZone))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return getTimeZoneText();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        onlyVisibleCheckBox = new javax.swing.JCheckBox();
        useSparklinesCheckBox = new javax.swing.JCheckBox();
        showEdgesNodesLabelsCheckBox = new javax.swing.JCheckBox();
        timeIntervalsGraphicsCheckBox = new javax.swing.JCheckBox();
        timeFormatComboBox = new javax.swing.JComboBox();
        timeFormatLabel = new javax.swing.JLabel();
        timeZoneLabel = new javax.swing.JLabel();
        timeZoneComboBox = new javax.swing.JComboBox();
        timeRepresentationLabel = new javax.swing.JLabel();
        timeRepresentationComboBox = new javax.swing.JComboBox();

        onlyVisibleCheckBox.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.onlyVisibleCheckBox.text")); // NOI18N
        onlyVisibleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlyVisibleCheckBoxActionPerformed(evt);
            }
        });

        useSparklinesCheckBox.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.useSparklinesCheckBox.text")); // NOI18N
        useSparklinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useSparklinesCheckBoxActionPerformed(evt);
            }
        });

        showEdgesNodesLabelsCheckBox.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.showEdgesNodesLabelsCheckBox.text")); // NOI18N
        showEdgesNodesLabelsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showEdgesNodesLabelsCheckBoxActionPerformed(evt);
            }
        });

        timeIntervalsGraphicsCheckBox.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeIntervalsGraphicsCheckBox.text")); // NOI18N
        timeIntervalsGraphicsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeIntervalsGraphicsCheckBoxActionPerformed(evt);
            }
        });

        timeFormatLabel.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeFormatLabel.text")); // NOI18N

        timeZoneLabel.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeZoneLabel.text")); // NOI18N

        timeRepresentationLabel.setText(org.openide.util.NbBundle.getMessage(ConfigurationPanel.class, "ConfigurationPanel.timeRepresentationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(showEdgesNodesLabelsCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(timeIntervalsGraphicsCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(useSparklinesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                    .addComponent(onlyVisibleCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timeZoneLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timeRepresentationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeRepresentationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(timeFormatComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(timeZoneComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(onlyVisibleCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useSparklinesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(timeIntervalsGraphicsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showEdgesNodesLabelsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeFormatLabel)
                    .addComponent(timeFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeZoneLabel)
                    .addComponent(timeZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeRepresentationLabel)
                    .addComponent(timeRepresentationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void onlyVisibleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlyVisibleCheckBoxActionPerformed
        dataTableTopComponent.setShowOnlyVisible(onlyVisibleCheckBox.isSelected());
    }//GEN-LAST:event_onlyVisibleCheckBoxActionPerformed

    private void useSparklinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useSparklinesCheckBoxActionPerformed
        dataTableTopComponent.setUseSparklines(useSparklinesCheckBox.isSelected());
    }//GEN-LAST:event_useSparklinesCheckBoxActionPerformed

    private void timeIntervalsGraphicsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeIntervalsGraphicsCheckBoxActionPerformed
        dataTableTopComponent.setTimeIntervalGraphics(timeIntervalsGraphicsCheckBox.isSelected());
    }//GEN-LAST:event_timeIntervalsGraphicsCheckBoxActionPerformed

    private void showEdgesNodesLabelsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showEdgesNodesLabelsCheckBoxActionPerformed
        dataTableTopComponent.setShowEdgesNodesLabels(showEdgesNodesLabelsCheckBox.isSelected());
    }//GEN-LAST:event_showEdgesNodesLabelsCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox onlyVisibleCheckBox;
    private javax.swing.JCheckBox showEdgesNodesLabelsCheckBox;
    private javax.swing.JComboBox timeFormatComboBox;
    private javax.swing.JLabel timeFormatLabel;
    private javax.swing.JCheckBox timeIntervalsGraphicsCheckBox;
    private javax.swing.JComboBox timeRepresentationComboBox;
    private javax.swing.JLabel timeRepresentationLabel;
    private javax.swing.JComboBox timeZoneComboBox;
    private javax.swing.JLabel timeZoneLabel;
    private javax.swing.JCheckBox useSparklinesCheckBox;
    // End of variables declaration//GEN-END:variables
}
