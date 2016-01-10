/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JTextField;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.TimeFormat;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.joda.time.format.ISODateTimeFormat;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public class CustomBoundsDialog extends javax.swing.JPanel {

    private TimelineModel model;
    private TimelineController controller;

    public CustomBoundsDialog() {
        initComponents();

        resetDefaultsDate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setDefaults();
            }
        });
    }

    public void setDefaults() {
        switch (model.getTimeFormat()) {
            case DATE:
                minTextField.setText(AttributeUtils.printDate(model.getMin()));
                maxTextField.setText(AttributeUtils.printDate(model.getMax()));
                startTextField.setText(AttributeUtils.printDate(model.getMin()));
                endTextField.setText(AttributeUtils.printDate(model.getMax()));
                break;
            case DATETIME:
                minTextField.setText(AttributeUtils.printDateTime(model.getMin()));
                maxTextField.setText(AttributeUtils.printDateTime(model.getMax()));
                startTextField.setText(AttributeUtils.printDateTime(model.getMin()));
                endTextField.setText(AttributeUtils.printDateTime(model.getMax()));
                break;
            default:
                NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
                f.setGroupingUsed(false);
                f.setMaximumFractionDigits(20);
                minTextField.setText(f.format(model.getMin()));
                maxTextField.setText(f.format(model.getMax()));
                startTextField.setText(f.format(model.getMin()));
                endTextField.setText(f.format(model.getMax()));
                break;
        }
    }

    public void setup(TimelineModel timelineModel) {
        this.model = timelineModel;
        this.controller = Lookup.getDefault().lookup(TimelineController.class);
        setDefaults();

        switch (model.getTimeFormat()) {
            case DATE:
                minTextField.setText(AttributeUtils.printDate(model.getCustomMin()));
                maxTextField.setText(AttributeUtils.printDate(model.getCustomMax()));
                startTextField.setText(AttributeUtils.printDate(model.getIntervalStart()));
                endTextField.setText(AttributeUtils.printDate(model.getIntervalEnd()));
                break;
            case DATETIME:
                minTextField.setText(AttributeUtils.printDateTime(model.getCustomMin()));
                maxTextField.setText(AttributeUtils.printDateTime(model.getCustomMax()));
                startTextField.setText(AttributeUtils.printDateTime(model.getIntervalStart()));
                endTextField.setText(AttributeUtils.printDateTime(model.getIntervalEnd()));
                break;
            default:
                NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
                f.setGroupingUsed(false);
                f.setMaximumFractionDigits(20);
                minTextField.setText(f.format(model.getCustomMin()));
                maxTextField.setText(f.format(model.getCustomMax()));
                startTextField.setText(f.format(model.getIntervalStart()));
                endTextField.setText(f.format(model.getIntervalEnd()));
                break;
        }
    }

    public void unsetup() {
        if (model.getTimeFormat().equals(TimeFormat.DATE) || model.getTimeFormat().equals(TimeFormat.DATETIME)) {
            double min = AttributeUtils.parseDateTime(minTextField.getText());
            double max = AttributeUtils.parseDateTime(maxTextField.getText());
            double start = AttributeUtils.parseDateTime(startTextField.getText());
            double end = AttributeUtils.parseDateTime(endTextField.getText());
            start = Math.max(min, start);
            end = Math.min(max, end);
            controller.setCustomBounds(min, max);
            controller.setInterval(start, end);
        } else {
            double min = Double.parseDouble(minTextField.getText());
            double max = Double.parseDouble(maxTextField.getText());
            double start = Double.parseDouble(startTextField.getText());
            double end = Double.parseDouble(endTextField.getText());
            start = Math.max(min, start);
            end = Math.min(max, end);
            controller.setCustomBounds(min, max);
            controller.setInterval(start, end);
        }
    }

    public void createValidation(ValidationGroup group, ValidationPanel panel) {
        group.add(minTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                new FormatValidator(), new TimeValidator(maxTextField, false));
        group.add(maxTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                new FormatValidator(), new TimeValidator(minTextField, true));
        group.add(startTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                new FormatValidator(), new TimeValidator(endTextField, false));
        group.add(endTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                new FormatValidator(), new TimeValidator(startTextField, true));
    }

    public static ValidationPanel createValidationPanel(CustomBoundsDialog panel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(panel);
        ValidationGroup group = validationPanel.getValidationGroup();
        panel.createValidation(group, validationPanel);
        return validationPanel;
    }

    private class TimeValidator implements Validator<String> {

        private final JTextField other;
        private boolean max;

        public TimeValidator(JTextField other, boolean max) {
            this.other = other;
            this.max = max;
        }

        @Override
        public boolean validate(Problems prblms, String string, String t) {
            double thisDate;
            double otherDate;
            if (model.getTimeFormat().equals(TimeFormat.DATE) || model.getTimeFormat().equals(TimeFormat.DATETIME)) {
                try {
                    thisDate = AttributeUtils.parseDateTime(t);
                    otherDate = AttributeUtils.parseDateTime(other.getText());
                    double minDate = max ? otherDate : thisDate;
                    double maxDate = max ? thisDate : otherDate;
                    if (minDate < model.getMin()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.min"));
                        return false;
                    }
                    if (maxDate > model.getMax()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.max"));
                        return false;
                    }
                    if (minDate >= maxDate) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator"));
                        return false;
                    } else if (maxDate <= minDate) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator"));
                        return false;
                    }
                } catch (Exception ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", ISODateTimeFormat.dateTime()));
                    return false;
                }

            } else {
                try {
                    thisDate = Double.parseDouble(t);
                    otherDate = Double.parseDouble(other.getText());
                    double minDate = max ? otherDate : thisDate;
                    double maxDate = max ? thisDate : otherDate;
                    if (minDate < model.getMin()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.min"));
                        return false;
                    }
                    if (maxDate > model.getMax()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.max"));
                        return false;
                    }
                    if (minDate >= maxDate) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator"));
                        return false;
                    } else if (maxDate <= minDate) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator"));
                        return false;
                    }
                } catch (Exception e) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.double"));
                    return false;
                }
            }
            return true;
        }
    }

    private class FormatValidator implements Validator<String> {

        @Override
        public boolean validate(Problems prblms, String string, String t) {
            if (model.getTimeFormat().equals(TimeFormat.DATE)) {
                try {
                    AttributeUtils.parseDateTime(t);
                } catch (Exception ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", ISODateTimeFormat.date()));
                    return false;
                }
            } else if (model.getTimeFormat().equals(TimeFormat.DATETIME)) {
                try {
                    AttributeUtils.parseDateTime(t);
                } catch (Exception ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", ISODateTimeFormat.dateTime()));
                    return false;
                }
            } else {
                try {
                    Double.parseDouble(t);
                } catch (Exception e) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.double"));
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleHeader = new org.jdesktop.swingx.JXHeader();
        labelBounds = new javax.swing.JLabel();
        labelMinDate = new javax.swing.JLabel();
        labelMaxDate = new javax.swing.JLabel();
        labelIntervalDate = new javax.swing.JLabel();
        labelStartDate = new javax.swing.JLabel();
        labelEndDate = new javax.swing.JLabel();
        resetDefaultsDate = new javax.swing.JButton();
        minTextField = new javax.swing.JTextField();
        maxTextField = new javax.swing.JTextField();
        startTextField = new javax.swing.JTextField();
        endTextField = new javax.swing.JTextField();

        titleHeader.setDescription(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.titleHeader.description")); // NOI18N
        titleHeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/custom_bounds.png"))); // NOI18N
        titleHeader.setTitle(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.titleHeader.title")); // NOI18N

        labelBounds.setFont(labelBounds.getFont().deriveFont(labelBounds.getFont().getStyle() | java.awt.Font.BOLD));
        labelBounds.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.labelBounds.text")); // NOI18N

        labelMinDate.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.labelMinDate.text")); // NOI18N

        labelMaxDate.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.labelMaxDate.text")); // NOI18N

        labelIntervalDate.setFont(labelIntervalDate.getFont().deriveFont(labelIntervalDate.getFont().getStyle() | java.awt.Font.BOLD));
        labelIntervalDate.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.labelIntervalDate.text")); // NOI18N

        labelStartDate.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.labelStartDate.text")); // NOI18N

        labelEndDate.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.labelEndDate.text")); // NOI18N

        resetDefaultsDate.setText(NbBundle.getMessage (TimelineTopComponent.class, "CustomBoundsDialog.resetDefaultsDate.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titleHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(resetDefaultsDate))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(labelMinDate)
                                    .addComponent(labelStartDate))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(47, 47, 47)
                                        .addComponent(labelMaxDate))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(startTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(labelEndDate)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(endTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(labelIntervalDate)
                            .addComponent(labelBounds))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(titleHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(labelBounds)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMinDate)
                    .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelMaxDate)
                    .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(labelIntervalDate)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelStartDate)
                    .addComponent(labelEndDate)
                    .addComponent(endTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(resetDefaultsDate)
                .addContainerGap(43, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField endTextField;
    private javax.swing.JLabel labelBounds;
    private javax.swing.JLabel labelEndDate;
    private javax.swing.JLabel labelIntervalDate;
    private javax.swing.JLabel labelMaxDate;
    private javax.swing.JLabel labelMinDate;
    private javax.swing.JLabel labelStartDate;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JTextField minTextField;
    private javax.swing.JButton resetDefaultsDate;
    private javax.swing.JTextField startTextField;
    private org.jdesktop.swingx.JXHeader titleHeader;
    // End of variables declaration//GEN-END:variables
}
