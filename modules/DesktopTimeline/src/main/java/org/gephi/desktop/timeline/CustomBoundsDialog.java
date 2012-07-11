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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.JTextField;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public class CustomBoundsDialog extends javax.swing.JPanel {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
    private TimelineModel model;
    private TimelineController controller;

    public CustomBoundsDialog() {
        initComponents();

        resetDefaultsDate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setDefaults();
            }
        });
    }

    public void setDefaults() {
        if (model.getTimeFormat().equals(TimeFormat.DATE)) {
            Date min = DynamicUtilities.getDateFromDouble(model.getMin());
            Date max = DynamicUtilities.getDateFromDouble(model.getMax());
            Date from = DynamicUtilities.getDateFromDouble(model.getMin());
            Date to = DynamicUtilities.getDateFromDouble(model.getMax());

            minTextField.setText(DATE_FORMAT.format(min));
            maxTextField.setText(DATE_FORMAT.format(max));
            startTextField.setText(DATE_FORMAT.format(from));
            endTextField.setText(DATE_FORMAT.format(to));
        } else if (model.getTimeFormat().equals(TimeFormat.DATETIME)) {
            Date min = DynamicUtilities.getDateFromDouble(model.getMin());
            Date max = DynamicUtilities.getDateFromDouble(model.getMax());
            Date from = DynamicUtilities.getDateFromDouble(model.getMin());
            Date to = DynamicUtilities.getDateFromDouble(model.getMax());

            minTextField.setText(DATETIME_FORMAT.format(min));
            maxTextField.setText(DATETIME_FORMAT.format(max));
            startTextField.setText(DATETIME_FORMAT.format(from));
            endTextField.setText(DATETIME_FORMAT.format(to));
        } else {
            NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
            f.setGroupingUsed(false);
            f.setMaximumFractionDigits(20);
            minTextField.setText(f.format(model.getMin()));
            maxTextField.setText(f.format(model.getMax()));
            startTextField.setText(f.format(model.getMin()));
            endTextField.setText(f.format(model.getMax()));
        }
    }

    public void setup(TimelineModel timelineModel) {
        this.model = timelineModel;
        this.controller = Lookup.getDefault().lookup(TimelineController.class);
        setDefaults();
        
        if (model.getTimeFormat().equals(TimeFormat.DATE)) {
            Date min = DynamicUtilities.getDateFromDouble(model.getCustomMin());
            Date max = DynamicUtilities.getDateFromDouble(model.getCustomMax());
            Date from = DynamicUtilities.getDateFromDouble(model.getIntervalStart());
            Date to = DynamicUtilities.getDateFromDouble(model.getIntervalEnd());

            minTextField.setText(DATE_FORMAT.format(min));
            maxTextField.setText(DATE_FORMAT.format(max));
            startTextField.setText(DATE_FORMAT.format(from));
            endTextField.setText(DATE_FORMAT.format(to));
        } else if (model.getTimeFormat().equals(TimeFormat.DATETIME)) {
            Date min = DynamicUtilities.getDateFromDouble(model.getCustomMin());
            Date max = DynamicUtilities.getDateFromDouble(model.getCustomMax());
            Date from = DynamicUtilities.getDateFromDouble(model.getIntervalStart());
            Date to = DynamicUtilities.getDateFromDouble(model.getIntervalEnd());

            minTextField.setText(DATETIME_FORMAT.format(min));
            maxTextField.setText(DATETIME_FORMAT.format(max));
            startTextField.setText(DATETIME_FORMAT.format(from));
            endTextField.setText(DATETIME_FORMAT.format(to));
        } else {
            NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
            f.setGroupingUsed(false);
            f.setMaximumFractionDigits(20);
            minTextField.setText(f.format(model.getCustomMin()));
            maxTextField.setText(f.format(model.getCustomMax()));
            startTextField.setText(f.format(model.getIntervalStart()));
            endTextField.setText(f.format(model.getIntervalEnd()));
        }
    }

    public void unsetup() {
        if (model.getTimeFormat().equals(TimeFormat.DATE)) {
            try {
                double min = DynamicUtilities.getDoubleFromDate(DATE_FORMAT.parse(minTextField.getText()));
                double max = DynamicUtilities.getDoubleFromDate(DATE_FORMAT.parse(maxTextField.getText()));
                double start = DynamicUtilities.getDoubleFromDate(DATE_FORMAT.parse(startTextField.getText()));
                double end = DynamicUtilities.getDoubleFromDate(DATE_FORMAT.parse(endTextField.getText()));
                start = Math.max(min, start);
                end = Math.min(max, end);
                controller.setCustomBounds(min, max);
                controller.setInterval(start, end);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (model.getTimeFormat().equals(TimeFormat.DATETIME)) {
            try {
                double min = DynamicUtilities.getDoubleFromDate(DATETIME_FORMAT.parse(minTextField.getText()));
                double max = DynamicUtilities.getDoubleFromDate(DATETIME_FORMAT.parse(maxTextField.getText()));
                double start = DynamicUtilities.getDoubleFromDate(DATETIME_FORMAT.parse(startTextField.getText()));
                double end = DynamicUtilities.getDoubleFromDate(DATETIME_FORMAT.parse(endTextField.getText()));
                start = Math.max(min, start);
                end = Math.min(max, end);
                controller.setCustomBounds(min, max);
                controller.setInterval(start, end);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
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

        public boolean validate(Problems prblms, String string, String t) {
            double thisDate;
            double otherDate;
            if (model.getTimeFormat().equals(TimeFormat.DATE)) {
                try {
                    thisDate = DynamicUtilities.getDoubleFromDate(DATE_FORMAT.parse(t));
                    otherDate = DynamicUtilities.getDoubleFromDate(DATE_FORMAT.parse(other.getText()));
                    double minDate = max ? otherDate : thisDate;
                    double maxDate = max ? thisDate : otherDate;
                    if(minDate < model.getMin()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.min"));
                        return false;
                    }
                    if(maxDate > model.getMax()) {
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
                } catch (ParseException ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", DATE_FORMAT.toPattern()));
                    return false;
                }
            } else if (model.getTimeFormat().equals(TimeFormat.DATETIME)) {
                try {
                    thisDate = DynamicUtilities.getDoubleFromDate(DATETIME_FORMAT.parse(t));
                    otherDate = DynamicUtilities.getDoubleFromDate(DATETIME_FORMAT.parse(other.getText()));
                    double minDate = max ? otherDate : thisDate;
                    double maxDate = max ? thisDate : otherDate;
                    if(minDate < model.getMin()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.min"));
                        return false;
                    }
                    if(maxDate > model.getMax()) {
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
                } catch (ParseException ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", DATETIME_FORMAT.toPattern()));
                    return false;
                }
            } else {
                try {
                    thisDate = Double.parseDouble(t);
                    otherDate = Double.parseDouble(other.getText());
                    double minDate = max ? otherDate : thisDate;
                    double maxDate = max ? thisDate : otherDate;
                    if(minDate < model.getMin()) {
                        prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.TimeValidator.min"));
                        return false;
                    }
                    if(maxDate > model.getMax()) {
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

        public boolean validate(Problems prblms, String string, String t) {
            if (model.getTimeFormat().equals(TimeFormat.DATE)) {
                try {
                    DATE_FORMAT.parse(t);
                } catch (ParseException ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", DATE_FORMAT.toPattern()));
                    return false;
                }
            } else if (model.getTimeFormat().equals(TimeFormat.DATETIME)) {
                try {
                    DATETIME_FORMAT.parse(t);
                } catch (ParseException ex) {
                    prblms.add(NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.FormatValidator.date", DATETIME_FORMAT.toPattern()));
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
