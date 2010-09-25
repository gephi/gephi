/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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
package org.gephi.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.gephi.timeline.spi.TimelineDrawer;
import org.gephi.timeline.api.TimelineAnimatorListener;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelEvent;
import org.gephi.timeline.api.TimelineModelListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 * 
 * @author Julian Bilcke
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.timeline//Timeline//EN",
autostore = false)
public final class TimelineTopComponent extends TopComponent implements TimelineAnimatorListener, TimelineModelListener {

    private static TimelineTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/gephi/desktop/timeline/resources/ui-status-bar.png";
    private static final String PREFERRED_ID = "TimelineTopComponent";
    private JPanel drawerPanel;
    private TimelineAnimatorImpl animator;
    private TimelineModel model;
    //MinMax
    private double min;
    private double max;

    public TimelineTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TimelineTopComponent.class, "CTL_TimelineTopComponent"));
//        setToolTipText(NbBundle.getMessage(TimelineTopComponent.class, "HINT_TimelineTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        //Drawer
        TimelineDrawer drawer = Lookup.getDefault().lookup(TimelineDrawer.class);
        drawerPanel = (JPanel) drawer;
        timelinePanel.add(drawerPanel);
        drawerPanel.setEnabled(false);

        //Animator
        animator = new TimelineAnimatorImpl();
        animator.addListener(this);

        //Button
        enableButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TimelineTopComponent.this.setEnabled(enableButton.isSelected());
                if (model != null) {
                    model.setEnabled(enableButton.isSelected());
                }
            }
        });
    }

    public void timelineModelChanged(TimelineModelEvent event) {
        setEnabled(event.getSource().isEnabled());
        TimelineDrawer drawer = (TimelineDrawer) drawerPanel;
        if (drawer.getModel() == null || drawer.getModel() != event.getSource()) {
            drawer.setModel(event.getSource());
        }
        if (model != event.getSource()) {
            model = event.getSource();
        }
        switch (event.getEventType()) {
            case MIN_CHANGED:
                setMin((Double) event.getData());
                break;
            case MAX_CHANGED:
                setMax((Double) event.getData());
                break;
            case VISIBLE_INTERVAL:
                break;
        }
    }

    private void setMin(double min) {
        if (this.min != min) {
            this.min = min;
            setTimeLineVisible(!Double.isInfinite(min));
        }

    }

    private void setMax(double max) {
        if (this.max != max) {
            this.max = max;
            setTimeLineVisible(!Double.isInfinite(max));
        }
    }

    private void setTimeLineVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (visible && !TimelineTopComponent.this.isOpened()) {
                    TimelineTopComponent.this.open();
                    TimelineTopComponent.this.requestActive();
                } else if (!visible && TimelineTopComponent.this.isOpened()) {
                    TimelineTopComponent.this.close();
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        enableButton = new javax.swing.JToggleButton();
        timelinePanel = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(2147483647, 27));
        setMinimumSize(new java.awt.Dimension(128, 16));
        setPreferredSize(new java.awt.Dimension(800, 24));
        setLayout(new java.awt.GridBagLayout());

        enableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/disabled.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(enableButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.enableButton.text")); // NOI18N
        enableButton.setFocusable(false);
        enableButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        enableButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/enabled.png"))); // NOI18N
        enableButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        enableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(enableButton, gridBagConstraints);

        timelinePanel.setEnabled(false);
        timelinePanel.setMinimumSize(new java.awt.Dimension(300, 28));
        timelinePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(timelinePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void enableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_enableButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton enableButton;
    private javax.swing.JPanel timelinePanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TimelineTopComponent getDefault() {
        if (instance == null) {
            instance = new TimelineTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the TimelineTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TimelineTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(TimelineTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TimelineTopComponent) {
            return (TimelineTopComponent) win;
        }
        Logger.getLogger(TimelineTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void setEnabled(boolean enable) {
        drawerPanel.setEnabled(enable);
        timelinePanel.setEnabled(enable);
        if (enableButton.isSelected() != enable) {
            enableButton.setSelected(enable);
        }
    }

    public void timelineAnimatorChanged(ChangeEvent event) {
        // check animator value, to update the buttons etc..
    }
}
