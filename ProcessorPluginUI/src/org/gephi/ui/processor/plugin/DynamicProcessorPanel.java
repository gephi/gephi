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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.datatype.DatatypeConfigurationException;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.io.processor.plugin.DynamicProcessor;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Exceptions;
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
        initLastFrame();

        dateRadio.setSelected(processor.isDateMode());
        if (processor.isDateMode()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                datePicker.setDate(sdf.parse(processor.getDate()));
            } catch (Exception e) {
                datePicker.setDate(new Date());
            }
        } else {
            //Timestamp
        }
    }

    public void unsetup(DynamicProcessor processor) {
        processor.setDateMode(dateRadio.isSelected());
        if (dateRadio.isSelected()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String begin = sdf.format(datePicker.getDate());
            processor.setDate(begin);
        } else {
            processor.setDate(timestampField.getText());
        }
    }

    private void initLastFrame() {
        if (Double.isInfinite(lastFrame)) {
            lastFrameLabel.setText("None");
        } else {
            lastFrameLabel.setText(Double.toString(lastFrame));
            lastFrameLabel.setText(DynamicUtilities.getXMLDateStringFromDouble(lastFrame));
        }
    }

    public static ValidationPanel createValidationPanel(DynamicProcessorPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();


        group.add(innerPanel.timestampField, Validators.merge(new LastFrameValidator(innerPanel), new Validator<String>() {

            @Override
            public boolean validate(Problems problems, String compName, String model) {
                if (model.isEmpty()) {
                    return true;
                }
                return Validators.REQUIRE_VALID_NUMBER.validate(problems, "time stamp", model);
            }
        }));

        final DatePickerValidationListener datePickerValidationListener = new DatePickerValidationListener(innerPanel);
        group.add(datePickerValidationListener);

        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ("date".equals(e.getPropertyName())) {
                    datePickerValidationListener.dateEvent();
                }
            }
        };
        innerPanel.datePicker.addPropertyChangeListener(listener);


        return validationPanel;
    }

    private static class DatePickerValidationListener extends ValidationListener {

        LastFrameValidator lastFrameValidator;

        public DatePickerValidationListener(DynamicProcessorPanel panel) {
            lastFrameValidator = new LastFrameValidator((panel));
        }

        @Override
        protected boolean validate(Problems problems) {
            return lastFrameValidator.validate(problems, "", "");
        }

        public void dateEvent() {
            this.validate();
        }
    }

    private static class LastFrameValidator implements Validator<String> {

        private DynamicProcessorPanel panel;

        public LastFrameValidator(DynamicProcessorPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (Double.isInfinite(panel.lastFrame)) {
                return true;
            }
            if (panel.dateRadio.isSelected()) {
                //Date
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String begin = sdf.format(panel.datePicker.getDate());
				double d = DynamicUtilities.getDoubleFromXMLDateString(begin);
				if (d <= panel.lastFrame) {
					problems.add("The new date must be later than the last current date");
					return false;
				}
            } else {
                if (model.isEmpty()) {
                    return true;
                }
                try {
                    Double.parseDouble(panel.timestampField.getText());
                } catch (Exception e) {
                    return false;
                }
                if (Double.parseDouble(panel.timestampField.getText()) <= panel.lastFrame) {
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

        header.setDescription(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.header.description")); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(DynamicProcessorPanel.class, "DynamicProcessorPanel.header.title")); // NOI18N

        buttongroup.add(dateRadio);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(header, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelLastFrame)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lastFrameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(labelDate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(datePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dateRadio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(labelTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(timestampField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeStampRadio))
                .addContainerGap(185, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLastFrame)
                    .addComponent(lastFrameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(dateRadio)
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
                .addContainerGap(46, Short.MAX_VALUE))
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
    private javax.swing.JLabel labelTime;
    private javax.swing.JLabel lastFrameLabel;
    private javax.swing.JRadioButton timeStampRadio;
    private javax.swing.JTextField timestampField;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
