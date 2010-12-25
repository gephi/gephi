/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.processor.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.io.processor.plugin.DynamicProcessor;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicProcessorPanel extends javax.swing.JPanel {

    private double lastFrame = Double.POSITIVE_INFINITY;

    public DynamicProcessorPanel() {
        initComponents();
    }

    public void setup(DynamicProcessor processor) {
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel dynamicModel = dynamicController.getModel();
        if (dynamicModel != null) {
            lastFrame = dynamicModel.getMax();
        }
        lastFrameLabel.setText("None");

        if (dynamicModel != null && !(dynamicModel.getMin() == Double.NEGATIVE_INFINITY && dynamicModel.getMax() == Double.POSITIVE_INFINITY)) {
            //Select only the current time format
            DynamicModel.TimeFormat timeFormat = dynamicModel.getTimeFormat();
            if (timeFormat.equals(DynamicModel.TimeFormat.DATE)) {
                dateRadio.setSelected(true);
                timeStampRadio.setEnabled(false);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    datePicker.setDate(sdf.parse(processor.getDate()));
                } catch (Exception e) {
                    datePicker.setDate(new Date());
                }
                lastFrameLabel.setText(DynamicUtilities.getXMLDateStringFromDouble(lastFrame));
            } else {
                timeStampRadio.setSelected(true);
                dateRadio.setEnabled(false);

                lastFrameLabel.setText(Double.toString(lastFrame));
            }
        }

        labelMatchingCheckbox.setSelected(processor.isLabelmatching());
    }

    public void unsetup(DynamicProcessor processor) {
        processor.setDateMode(dateRadio.isSelected());
        if (dateRadio.isSelected()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String begin = sdf.format(datePicker.getDate());
                processor.setDate(begin);
            } catch (Exception e) {
                //Exception catched later in processor
            }
        } else {
            processor.setDate(timestampField.getText());
        }
        processor.setLabelmatching(labelMatchingCheckbox.isSelected());
    }

    public static ValidationPanel createValidationPanel(DynamicProcessorPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        final FullValidationListener fullValidationListener = new FullValidationListener(innerPanel);
        group.add(fullValidationListener);

        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ("date".equals(e.getPropertyName())) {
                    fullValidationListener.event();
                }
            }
        };
        innerPanel.datePicker.addPropertyChangeListener(listener);

        innerPanel.timestampField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                fullValidationListener.event();
            }

            public void removeUpdate(DocumentEvent e) {
                fullValidationListener.event();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        innerPanel.dateRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fullValidationListener.event();
            }
        });

        innerPanel.timeStampRadio.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fullValidationListener.event();
            }
        });


        return validationPanel;
    }

    private static class FullValidationListener extends ValidationListener {

        FullValidator lastFrameValidator;

        public FullValidationListener(DynamicProcessorPanel panel) {
            lastFrameValidator = new FullValidator((panel));
        }

        @Override
        protected boolean validate(Problems problems) {
            return lastFrameValidator.validate(problems, "", "");
        }

        public void event() {
            this.validate();
        }
    }

    private static class FullValidator implements Validator<String> {

        private DynamicProcessorPanel panel;

        public FullValidator(DynamicProcessorPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (panel.dateRadio.isSelected()) {
                if (panel.datePicker.getDate() == null) {
                    problems.add("The date can't be empty");
                    return false;
                } else if (!Double.isInfinite(panel.lastFrame)) {
                    //Date
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String begin = sdf.format(panel.datePicker.getDate());
                    double d = DynamicUtilities.getDoubleFromXMLDateString(begin);
                    if (d <= panel.lastFrame) {
                        problems.add("The new date must be later than the last current date");
                        return false;
                    }
                }

            } else {
                String t = panel.timestampField.getText();
                if (t.isEmpty()) {
                    problems.add("The time stamp can't be empty");
                    return false;
                }
                try {
                    Double.parseDouble(panel.timestampField.getText());
                } catch (Exception e) {
                    problems.add("The time stamp must be a number");
                    return false;
                }
                if (!Double.isInfinite(panel.lastFrame) && Double.parseDouble(panel.timestampField.getText()) <= panel.lastFrame) {
                    problems.add("The new time stamp must be greater than the last frame");
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
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttongroup = new javax.swing.ButtonGroup();
        header = new org.jdesktop.swingx.JXHeader();
        dateRadio = new javax.swing.JRadioButton();
        labelDate = new javax.swing.JLabel();
        datePicker = new org.jdesktop.swingx.JXDatePicker();
        timeStampRadio = new javax.swing.JRadioButton();
        labelTime = new javax.swing.JLabel();
        timestampField = new javax.swing.JTextField();
        labelLastFrame = new javax.swing.JLabel();
        lastFrameLabel = new javax.swing.JLabel();
        labelMatchingCheckbox = new javax.swing.JCheckBox();

        header.setDescription(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.header.description")); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.header.title")); // NOI18N

        buttongroup.add(dateRadio);
        dateRadio.setSelected(true);
        dateRadio.setText(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.dateRadio.text")); // NOI18N

        labelDate.setText(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.labelDate.text")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, dateRadio, org.jdesktop.beansbinding.ELProperty.create("${selected}"), labelDate, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, dateRadio, org.jdesktop.beansbinding.ELProperty.create("${selected}"), datePicker, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttongroup.add(timeStampRadio);
        timeStampRadio.setText(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.timeStampRadio.text")); // NOI18N

        labelTime.setText(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.labelTime.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, timeStampRadio, org.jdesktop.beansbinding.ELProperty.create("${selected}"), labelTime, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, timeStampRadio, org.jdesktop.beansbinding.ELProperty.create("${selected}"), timestampField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        labelLastFrame.setFont(labelLastFrame.getFont().deriveFont(labelLastFrame.getFont().getStyle() | java.awt.Font.BOLD));
        labelLastFrame.setText(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.labelLastFrame.text")); // NOI18N

        labelMatchingCheckbox.setText(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.labelMatchingCheckbox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelMatchingCheckbox)
                .addContainerGap(285, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelLastFrame)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lastFrameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addGap(136, 136, 136))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(labelDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dateRadio)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(labelTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(timestampField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeStampRadio))
                .addContainerGap(205, Short.MAX_VALUE))
            .addComponent(header, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMatchingCheckbox)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelLastFrame)
                        .addGap(18, 18, 18)
                        .addComponent(dateRadio))
                    .addComponent(lastFrameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDate)
                    .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(timeStampRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTime)
                    .addComponent(timestampField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttongroup;
    private org.jdesktop.swingx.JXDatePicker datePicker;
    private javax.swing.JRadioButton dateRadio;
    private org.jdesktop.swingx.JXHeader header;
    private javax.swing.JLabel labelDate;
    private javax.swing.JLabel labelLastFrame;
    private javax.swing.JCheckBox labelMatchingCheckbox;
    private javax.swing.JLabel labelTime;
    private javax.swing.JLabel lastFrameLabel;
    private javax.swing.JRadioButton timeStampRadio;
    private javax.swing.JTextField timestampField;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
