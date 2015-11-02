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
package org.gephi.desktop.statistics;

import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.lib.validation.PositiveNumberValidator;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicSettingsPanel extends javax.swing.JPanel {

    private TimeUnit windowTimeUnit = TimeUnit.DAYS;
    private TimeUnit tickTimeUnit = TimeUnit.DAYS;
    private Interval bounds = null;

    public DynamicSettingsPanel() {
        initComponents();

        //Load timeunit's combo
        windowTimeUnitCombo.setModel(getTimeUnitModel());
        tickTimeUnitCombo.setModel(getTimeUnitModel());

        //Rich tooltip
        windowInfoLabel.addMouseListener(new MouseAdapter() {

            RichTooltip richTooltip;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (windowInfoLabel.isEnabled()) {
                    richTooltip = buildTooltip();
                    richTooltip.showTooltip(windowInfoLabel, e.getLocationOnScreen());
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (richTooltip != null) {
                    richTooltip.hideTooltip();
                    richTooltip = null;
                }
            }
        });
    }
    
    public void setup(DynamicStatistics dynamicStatistics) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        TimeFormat timeFormat = graphModel.getTimeFormat();

        //Bounds
        bounds = dynamicStatistics.getBounds();
        if (bounds == null) {
            bounds = graphModel.getTimeBoundsVisible();
        }
        String boundsStr = timeFormat.print(bounds.getLow())+" - "+timeFormat.print(bounds.getHigh());
        currentIntervalLabel.setText(boundsStr);

        //TimeUnit
        if (timeFormat.equals(TimeFormat.DOUBLE)) {
            windowTimeUnitCombo.setVisible(false);
            tickTimeUnitCombo.setVisible(false);
        }

        //Set latest selected item
        if (!timeFormat.equals(TimeFormat.DOUBLE)) {
            loadDefaultTimeUnits();
        }

        //Window and tick
        double initValue = 0.;
        if(bounds.getHigh() - bounds.getLow() > 1) {
            initValue = 1.;
        }
        if (timeFormat.equals(TimeFormat.DOUBLE)) {
            windowTextField.setText(initValue + "");
            tickTextField.setText(initValue + "");
        } else {
            windowTextField.setText("" + windowTimeUnit.convert((long) initValue, TimeUnit.MILLISECONDS));
            tickTextField.setText("" + tickTimeUnit.convert((long) initValue, TimeUnit.MILLISECONDS));
        }

        //Add listeners
        windowTimeUnitCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getItem() != windowTimeUnitCombo.getSelectedItem()) {
                    refreshWindowTimeUnit();
                }
            }
        });

        tickTimeUnitCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getItem() != tickTimeUnitCombo.getSelectedItem()) {
                    refreshTickTimeUnit();
                }
            }
        });
    }

    public void unsetup(DynamicStatistics dynamicStatistics) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        TimeFormat timeFormat = graphModel.getTimeFormat();
        
        //Bounds is the same
        dynamicStatistics.setBounds(bounds);

        //Window
        double window;
        if (timeFormat == TimeFormat.DOUBLE) {
            window = Double.parseDouble(windowTextField.getText());
        } else {
            TimeUnit timeUnit = getSelectedTimeUnit(windowTimeUnitCombo.getModel());
            window = getTimeInMilliseconds(windowTextField.getText(), timeUnit);
        }
        dynamicStatistics.setWindow(window);

        //Tick
        double tick;
        if (timeFormat == TimeFormat.DOUBLE) {
            tick = Double.parseDouble(tickTextField.getText());
        } else {
            TimeUnit timeUnit = getSelectedTimeUnit(tickTimeUnitCombo.getModel());
            tick = getTimeInMilliseconds(tickTextField.getText(), timeUnit);
        }
        dynamicStatistics.setTick(tick);

        //Save latest selected item
        if (timeFormat != TimeFormat.DOUBLE) {
            saveDefaultTimeUnits();
        }
    }

    public void createValidation(ValidationGroup group) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        TimeFormat timeFormat = graphModel.getTimeFormat();
        
        if (timeFormat == TimeFormat.DOUBLE) {
            group.add(windowTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                    Validators.numberRange(Double.MIN_VALUE, (bounds.getHigh() - bounds.getLow())));
            group.add(tickTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                    Validators.numberRange(Double.MIN_VALUE, (bounds.getHigh() - bounds.getLow())));
        } else {
            //TODO validation with dates
            group.add(windowTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                    new PositiveNumberValidator(),
                    new DateRangeValidator(windowTimeUnitCombo.getModel()));
            group.add(tickTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                    new PositiveNumberValidator(),
                    new DateRangeValidator(tickTimeUnitCombo.getModel()),
                    new TickUnderWindowValidator(timeFormat != TimeFormat.DOUBLE));
        }
    }
    private final String DAYS = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.TimeUnit.DAYS");
    private final String HOURS = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.TimeUnit.HOURS");
    private final String MILLISECONDS = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.TimeUnit.MILLISECONDS");
    private final String MINUTES = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.TimeUnit.MINUTES");
    private final String SECONDS = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.TimeUnit.SECONDS");

    private ComboBoxModel getTimeUnitModel() {
        return new DefaultComboBoxModel(new String[]{DAYS, HOURS, MILLISECONDS, MINUTES, SECONDS});
    }

    private TimeUnit getSelectedTimeUnit(ComboBoxModel comboBoxModel) {
        if (comboBoxModel.getSelectedItem().equals(DAYS)) {
            return TimeUnit.DAYS;
        } else if (comboBoxModel.getSelectedItem().equals(HOURS)) {
            return TimeUnit.HOURS;
        } else if (comboBoxModel.getSelectedItem().equals(MILLISECONDS)) {
            return TimeUnit.MILLISECONDS;
        } else if (comboBoxModel.getSelectedItem().equals(MINUTES)) {
            return TimeUnit.MINUTES;
        } else if (comboBoxModel.getSelectedItem().equals(SECONDS)) {
            return TimeUnit.SECONDS;
        }
        return null;
    }

    private double getTimeInMilliseconds(String text, TimeUnit timeUnit) {
        Integer t = Integer.parseInt(text);
        return TimeUnit.MILLISECONDS.convert(t, timeUnit);
    }

    private void refreshWindowTimeUnit() {
        TimeUnit tu = getSelectedTimeUnit(windowTimeUnitCombo.getModel());
        try {
            Integer value = Integer.parseInt(windowTextField.getText());
            long newValue = tu.convert(value, windowTimeUnit);
            windowTextField.setText("" + newValue);
            windowTimeUnit = tu;
        } catch (Exception e) {
        }
    }

    private void refreshTickTimeUnit() {
        TimeUnit tu = getSelectedTimeUnit(tickTimeUnitCombo.getModel());
        try {
            Integer value = Integer.parseInt(tickTextField.getText());
            long newValue = tu.convert(value, tickTimeUnit);
            tickTextField.setText("" + newValue);
            tickTimeUnit = tu;
        } catch (Exception e) {
        }
    }

    private void loadDefaultTimeUnits() {
        String windowDuration = NbPreferences.forModule(DynamicSettingsPanel.class).get("DynamicSettingsPanel_window_timeunit", windowTimeUnit.name());
        windowTimeUnit = TimeUnit.valueOf(windowDuration);
        String tickDuration = NbPreferences.forModule(DynamicSettingsPanel.class).get("DynamicSettingsPanel_tick_timeunit", tickTimeUnit.name());
        tickTimeUnit = TimeUnit.valueOf(tickDuration);
        windowTimeUnitCombo.setSelectedItem(getTimeUnit(windowTimeUnit));
        tickTimeUnitCombo.setSelectedItem(getTimeUnit(tickTimeUnit));
    }

    private void saveDefaultTimeUnits() {
        NbPreferences.forModule(DynamicSettingsPanel.class).put("DynamicSettingsPanel_window_timeunit", windowTimeUnit.name());
        NbPreferences.forModule(DynamicSettingsPanel.class).put("DynamicSettingsPanel_tick_timeunit", tickTimeUnit.name());
    }

    private String getTimeUnit(TimeUnit timeUnit) {
        if (timeUnit.equals(TimeUnit.DAYS)) {
            return DAYS;
        } else if (timeUnit.equals(TimeUnit.HOURS)) {
            return HOURS;
        } else if (timeUnit.equals(TimeUnit.MILLISECONDS)) {
            return MILLISECONDS;
        } else if (timeUnit.equals(TimeUnit.MINUTES)) {
            return MINUTES;
        } else if (timeUnit.equals(TimeUnit.SECONDS)) {
            return SECONDS;
        }
        return null;
    }

    private RichTooltip buildTooltip() {
        String name = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.infoLabel.name");
        String description = NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.infoLabel.description");
        RichTooltip richTooltip = new RichTooltip(name, description);
        Image image = ImageUtilities.loadImage("org/gephi/desktop/statistics/resources/infolabel_details.png");

        richTooltip.setMainImage(image);
        return richTooltip;
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

        jPanel1 = new javax.swing.JPanel();
        labelCurrentTimeline = new javax.swing.JLabel();
        currentIntervalLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tickTextField = new javax.swing.JTextField();
        windowTextField = new javax.swing.JTextField();
        windowTimeUnitCombo = new javax.swing.JComboBox();
        tickTimeUnitCombo = new javax.swing.JComboBox();
        windowInfoLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.jPanel1.border.title"))); // NOI18N

        labelCurrentTimeline.setText(org.openide.util.NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.labelCurrentTimeline.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(DynamicSettingsPanel.class, "DynamicSettingsPanel.jLabel2.text")); // NOI18N

        tickTextField.setName("tick"); // NOI18N

        windowTextField.setName("window"); // NOI18N

        windowInfoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/statistics/resources/info.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tickTextField)
                            .addComponent(windowTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tickTimeUnitCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(windowTimeUnitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelCurrentTimeline)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(currentIntervalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                        .addComponent(windowInfoLabel)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelCurrentTimeline)
                            .addComponent(currentIntervalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(windowTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(windowTimeUnitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(tickTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tickTimeUnitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(windowInfoLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel currentIntervalLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelCurrentTimeline;
    private javax.swing.JTextField tickTextField;
    private javax.swing.JComboBox tickTimeUnitCombo;
    private javax.swing.JLabel windowInfoLabel;
    private javax.swing.JTextField windowTextField;
    private javax.swing.JComboBox windowTimeUnitCombo;
    // End of variables declaration//GEN-END:variables

    private class DateRangeValidator implements Validator<String> {

        private ComboBoxModel combo;

        public DateRangeValidator(ComboBoxModel comboBoxModel) {
            this.combo = comboBoxModel;
        }

        public boolean validate(Problems prblms, String string, String t) {
            Integer i = 0;
            try {
                i = Integer.parseInt(t);
            } catch (NumberFormatException e) {
                return false;
            }
            TimeUnit tu = getSelectedTimeUnit(combo);
            long timeInMilli = (long) getTimeInMilliseconds(t, tu);
            long limit = (long) (bounds.getHigh() - bounds.getLow());
            if (i < 1 || timeInMilli > limit) {
                String message = NbBundle.getMessage(DynamicSettingsPanel.class,
                        "DateRangeValidator.NotInRange", i, 1, tu.convert(limit, TimeUnit.MILLISECONDS));
                prblms.add(message);
                return false;
            }
            return true;
        }
    }

    private class TickUnderWindowValidator implements Validator<String> {

        private boolean dates;

        public TickUnderWindowValidator(boolean dates) {
            this.dates = dates;
        }

        public boolean validate(Problems prblms, String string, String t) {
            if (dates) {
                Integer tick = 0;
                Integer window = 0;
                try {
                    tick = Integer.parseInt(t);
                    window = Integer.parseInt(windowTextField.getText());
                } catch (NumberFormatException e) {
                    return false;
                }
                TimeUnit tu = getSelectedTimeUnit(tickTimeUnitCombo.getModel());
                long tickInMilli = (long) getTimeInMilliseconds(t, tu);
                tu = getSelectedTimeUnit(windowTimeUnitCombo.getModel());
                long windowInMilli = (long) getTimeInMilliseconds(windowTextField.getText(), tu);
                if (tickInMilli > windowInMilli) {
                    String message = NbBundle.getMessage(DynamicSettingsPanel.class,
                            "TickUnderWindowValidator.OverWindow");
                    prblms.add(message);
                    return false;
                }
            } else {
                Double tick = 0.;
                Double window = 0.;
                try {
                    tick = Double.parseDouble(t);
                    window = Double.parseDouble(windowTextField.getText());
                } catch (NumberFormatException e) {
                    return false;
                }
                if (tick > window) {
                    String message = NbBundle.getMessage(DynamicSettingsPanel.class,
                            "TickUnderWindowValidator.OverWindow");
                    prblms.add(message);
                    return false;
                }
            }
            return true;
        }
    }

    public static JPanel createCounpoundPanel(DynamicSettingsPanel dynamicPanel, JPanel innerPanel) {
        JPanel result = new JPanel();

        java.awt.GridBagConstraints gridBagConstraints;

        result.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        result.add(dynamicPanel, gridBagConstraints);

        if (innerPanel != null) {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            result.add(innerPanel, gridBagConstraints);
        }

        //Validation
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(result);
        ValidationGroup group = validationPanel.getValidationGroup();
        dynamicPanel.createValidation(group);

        return validationPanel;
    }
}
