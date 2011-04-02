/*
Copyright 2008-2011 Gephi
Authors : Cezary Bartosiak
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
package org.gephi.ui.statistics.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;

/**
 * A base class for all dynamic panels.
 *
 * @author Cezary Bartosiak
 */
public class DynamicPanel extends javax.swing.JPanel {

    /** Creates new form DynamicPanel */
    public DynamicPanel() {
        initComponents();
    }

    public TimeInterval getTimeInterval() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String begin = sdf.format(beginDatePicker.getDate());
        String end = sdf.format(endDatePicker.getDate());
        int beginHour = (Integer) beginHourSpinner.getValue();
        int beginMinute = (Integer) beginMinuteSpinner.getValue();
        int beginSecond = (Integer) beginSecondSpinner.getValue();
        int endHour = (Integer) endHourSpinner.getValue();
        int endMinute = (Integer) endMinuteSpinner.getValue();
        int endSecond = (Integer) endSecondSpinner.getValue();
        begin += "T" + (beginHour < 10 ? "0" + beginHour : beginHour);
        begin += ":" + (beginMinute < 10 ? "0" + beginMinute : beginMinute);
        begin += ":" + (beginSecond < 10 ? "0" + beginSecond : beginSecond);
        end += "T" + (endHour < 10 ? "0" + endHour : endHour);
        end += ":" + (endMinute < 10 ? "0" + endMinute : endMinute);
        end += ":" + (endSecond < 10 ? "0" + endSecond : endSecond);

        return new TimeInterval(
                DynamicUtilities.getDoubleFromXMLDateString(begin),
                DynamicUtilities.getDoubleFromXMLDateString(end));
    }

    public double getWindow() {
        TimeInterval timeInterval = getTimeInterval();
        return windowSlider.getValue() / 100.0 * (timeInterval.getHigh() - timeInterval.getLow());
    }

    public Estimator getEstimator() {
        switch (estimatorComboBox.getSelectedIndex()) {
            case 0: // FIRST
                return Estimator.FIRST;
            case 1: // LAST
                return Estimator.LAST;
            case 2: // MEDIAN
                return Estimator.MEDIAN;
            case 3: // MODE
                return Estimator.MODE;
            default:
                return Estimator.FIRST;
        }
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        try {
            String begin = DynamicUtilities.getXMLDateStringFromDouble(timeInterval.getLow()).replace('T', ' ').
                    substring(0, 19);
            String beginDate = begin.split(" ")[0];
            String beginTime = begin.split(" ")[1];
            String end = DynamicUtilities.getXMLDateStringFromDouble(timeInterval.getHigh()).replace('T', ' ').
                    substring(0, 19);
            String endDate = end.split(" ")[0];
            String endTime = end.split(" ")[1];

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            beginDatePicker.setDate(sdf.parse(beginDate));
            beginHourSpinner.setValue(Integer.parseInt(beginTime.split(":")[0]));
            beginMinuteSpinner.setValue(Integer.parseInt(beginTime.split(":")[1]));
            beginSecondSpinner.setValue(Integer.parseInt(beginTime.split(":")[2]));
            endDatePicker.setDate(sdf.parse(endDate));
            endHourSpinner.setValue(Integer.parseInt(endTime.split(":")[0]));
            endMinuteSpinner.setValue(Integer.parseInt(endTime.split(":")[1]));
            endSecondSpinner.setValue(Integer.parseInt(endTime.split(":")[2]));
        } catch (Exception ex) {
            beginDatePicker.setDate(new Date());
            beginHourSpinner.setValue(0);
            beginMinuteSpinner.setValue(0);
            beginSecondSpinner.setValue(0);
            endDatePicker.setDate(new Date());
            endHourSpinner.setValue(23);
            endMinuteSpinner.setValue(59);
            endSecondSpinner.setValue(59);
        }
    }

    public void setWindow(double window) {
        TimeInterval timeInterval = getTimeInterval();
        if (window > timeInterval.getHigh() - timeInterval.getLow() || Double.compare(window, 0.0) == 0) {
            windowSlider.setValue(100);
        } else {
            windowSlider.setValue((int) Math.round(window / (timeInterval.getHigh() - timeInterval.getLow()) * 100));
        }
    }

    public void setEstimator(Estimator estimator) {
        switch (estimator) {
            case FIRST:
                estimatorComboBox.setSelectedIndex(0);
                break;
            case LAST:
                estimatorComboBox.setSelectedIndex(1);
                break;
            case MEDIAN:
                estimatorComboBox.setSelectedIndex(2);
                break;
            case MODE:
                estimatorComboBox.setSelectedIndex(3);
                break;
            default:
                estimatorComboBox.setSelectedIndex(0);
                break;
        }
    }

    protected void setContent(JPanel panel, int height) {
        javax.swing.GroupLayout contentLayout = new javax.swing.GroupLayout(content);
        content.setLayout(contentLayout);
        contentLayout.setHorizontalGroup(
                contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE));
        contentLayout.setVerticalGroup(
                contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(panel, height, height, height));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        timeIntervalLabel = new javax.swing.JLabel();
        windowLabel = new javax.swing.JLabel();
        separatorTimeInterval1 = new javax.swing.JLabel();
        estimatorComboBox = new javax.swing.JComboBox();
        estimatorLabel = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        windowSlider = new javax.swing.JSlider();
        separator = new javax.swing.JSeparator();
        beginDatePicker = new org.jdesktop.swingx.JXDatePicker();
        endDatePicker = new org.jdesktop.swingx.JXDatePicker();
        beginHourSpinner = new javax.swing.JSpinner();
        beginMinuteSpinner = new javax.swing.JSpinner();
        beginSecondSpinner = new javax.swing.JSpinner();
        separatorTimeInterval2 = new javax.swing.JLabel();
        endHourSpinner = new javax.swing.JSpinner();
        endMinuteSpinner = new javax.swing.JSpinner();
        endSecondSpinner = new javax.swing.JSpinner();

        setPreferredSize(new java.awt.Dimension(419, 381));

        timeIntervalLabel.setText(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.timeIntervalLabel.text")); // NOI18N

        windowLabel.setText(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.windowLabel.text")); // NOI18N

        separatorTimeInterval1.setText(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.separatorTimeInterval1.text")); // NOI18N

        estimatorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "FIRST", "LAST", "MEDIAN", "MODE", " " }));

        estimatorLabel.setText(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.estimatorLabel.text")); // NOI18N

        javax.swing.GroupLayout contentLayout = new javax.swing.GroupLayout(content);
        content.setLayout(contentLayout);
        contentLayout.setHorizontalGroup(
            contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 419, Short.MAX_VALUE)
        );
        contentLayout.setVerticalGroup(
            contentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );

        windowSlider.setMajorTickSpacing(10);
        windowSlider.setMinorTickSpacing(1);
        windowSlider.setPaintLabels(true);
        windowSlider.setPaintTicks(true);
        windowSlider.setSnapToTicks(true);
        windowSlider.setValue(100);

        beginDatePicker.setMaximumSize(new java.awt.Dimension(150, 22));
        beginDatePicker.setMinimumSize(new java.awt.Dimension(150, 22));
        beginDatePicker.setPreferredSize(new java.awt.Dimension(150, 22));

        endDatePicker.setMaximumSize(new java.awt.Dimension(150, 22));
        endDatePicker.setMinimumSize(new java.awt.Dimension(150, 22));
        endDatePicker.setPreferredSize(new java.awt.Dimension(150, 22));

        beginHourSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));

        beginMinuteSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));

        beginSecondSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));

        separatorTimeInterval2.setText(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.separatorTimeInterval2.text")); // NOI18N

        endHourSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));

        endMinuteSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));

        endSecondSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(windowSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(estimatorLabel)
                .addContainerGap(326, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(estimatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(309, 309, 309))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(windowLabel)
                .addContainerGap(168, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(timeIntervalLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(beginHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(beginMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(beginSecondSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separatorTimeInterval2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(beginDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separatorTimeInterval1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(endHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endSecondSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(119, Short.MAX_VALUE))
            .addComponent(separator, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
            .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeIntervalLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(beginDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorTimeInterval1)
                    .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(beginHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(beginMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(beginSecondSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorTimeInterval2)
                    .addComponent(endHourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endMinuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endSecondSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(windowLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(windowSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(estimatorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(estimatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        timeIntervalLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.timeIntervalLabel.AccessibleContext.accessibleName")); // NOI18N
        windowLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.windowLabel.AccessibleContext.accessibleName")); // NOI18N
        separatorTimeInterval1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.jLabel1.AccessibleContext.accessibleName")); // NOI18N
        estimatorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DynamicPanel.class, "DynamicPanel.estimatorLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXDatePicker beginDatePicker;
    private javax.swing.JSpinner beginHourSpinner;
    private javax.swing.JSpinner beginMinuteSpinner;
    private javax.swing.JSpinner beginSecondSpinner;
    private javax.swing.JPanel content;
    private org.jdesktop.swingx.JXDatePicker endDatePicker;
    private javax.swing.JSpinner endHourSpinner;
    private javax.swing.JSpinner endMinuteSpinner;
    private javax.swing.JSpinner endSecondSpinner;
    private javax.swing.JComboBox estimatorComboBox;
    private javax.swing.JLabel estimatorLabel;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel separatorTimeInterval1;
    private javax.swing.JLabel separatorTimeInterval2;
    private javax.swing.JLabel timeIntervalLabel;
    private javax.swing.JLabel windowLabel;
    private javax.swing.JSlider windowSlider;
    // End of variables declaration//GEN-END:variables
}
