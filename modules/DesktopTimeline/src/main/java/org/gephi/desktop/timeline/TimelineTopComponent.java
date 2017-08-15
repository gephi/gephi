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

import java.awt.CardLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelEvent;
import org.gephi.timeline.api.TimelineModelListener;
import org.gephi.ui.components.CloseButton;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component corresponding to the Timeline component
 *
 * @author Julian Bilcke, Daniel Bernardes
 */
//@ConvertAsProperties(dtd = "-//org.gephi.desktop.timeline//Timeline//EN",
//autostore = false)
//@TopComponent.Description(preferredID = "TimelineTopComponent",
//iconBase = "org/gephi/desktop/timeline/resources/icon.png",
//persistenceType = TopComponent.PERSISTENCE_ALWAYS)
//@TopComponent.Registration(mode = "timeline,ode", openAtStartup = false)
//@ActionID(category = "Window", id = "org.gephi.desktop.timeline.TimelineTopComponent")
//@ActionReference(path = "Menu/Window", position = 1200)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_TimelineTopComponent",
//preferredID = "TimelineTopComponent")
public final class TimelineTopComponent extends JPanel implements TimelineModelListener {

    private transient TimelineDrawer drawer;
    private transient TimelineModel model;
    private transient TimelineController controller;

    public TimelineTopComponent() {
        initComponents();
        if (UIUtils.isAquaLookAndFeel()) {
            containerPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
            enablePanel.setBackground(UIManager.getColor("NbExplorerView.background"));
            disabledTimeline.setBackground(UIManager.getColor("NbExplorerView.background"));
            controlPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
            innerPanel.setBackground(UIManager.getColor("NbTabControl.editorTabBackground"));
        }
        this.drawer = (TimelineDrawer) timelinePanel;

        //TopComponent
        setName(NbBundle.getMessage(TimelineTopComponent.class, "CTL_TimelineTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        //Model
        controller = Lookup.getDefault().lookup(TimelineController.class);
        controller.addListener(this);
        setup(controller.getModel());

        //Close button
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setEnabled(false);
                setTimeLineVisible(false);
            }
        });

        disableButon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setEnabled(false);
            }
        });

        //Enable button
        enableTimelineButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (model != null) {
                    controller.setEnabled(true);
                }
            }
        });

        columnsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Create popup
                JPopupMenu menu = new JPopupMenu();

                //Add columns
                String selectedColumn = model.getChart() != null ? model.getChart().getColumn() : null;

                //Dynamic columns
                String[] columns = controller.getDynamicGraphColumns();

                for (final String col : columns) {
                    boolean selected = (col == null ? selectedColumn == null : col.equals(selectedColumn));
                    JRadioButtonMenuItem item = new JRadioButtonMenuItem(col, selected);
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            controller.selectColumn(col);
                        }
                    });
                    menu.add(item);
                }

                //No columns message
                if (columns.length == 0) {
                    menu.add("<html><i>" + NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.charts.empty") + "</i></html>");
                }

                //Separator
                menu.add(new JSeparator());

                //Disable
                if (columns.length > 0) {
                    JMenuItem disableItem = new JMenuItem(NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.charts.disable"));
                    disableItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            controller.selectColumn(null);
                        }
                    });

                    menu.add(disableItem);
                    if (selectedColumn == null) {
                        disableItem.setEnabled(false);
                    }
                }

                menu.show(columnsButton, 0, -menu.getPreferredSize().height);
            }
        });

        settingsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Create popup
                JPopupMenu menu = new JPopupMenu();

                //Custom bounds
                Image customBoundsIcon = ImageUtilities.loadImage("org/gephi/desktop/timeline/resources/custom_bounds.png", false);
                JMenuItem customBoundsItem = new JMenuItem(NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.settings.setCustomBounds"),
                        ImageUtilities.image2Icon(customBoundsIcon));
                customBoundsItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CustomBoundsDialog d = new CustomBoundsDialog();
                        d.setup(model);
                        ValidationPanel validationPanel = CustomBoundsDialog.createValidationPanel(d);
                        String title = NbBundle.getMessage(CustomBoundsDialog.class, "CustomBoundsDialog.title");
                        final DialogDescriptor descriptor = new DialogDescriptor(validationPanel, title);
                        validationPanel.addChangeListener(new ChangeListener() {

                            @Override
                            public void stateChanged(ChangeEvent e) {
                                descriptor.setValid(!((ValidationPanel) e.getSource()).isProblem());
                            }
                        });
                        Object result = DialogDisplayer.getDefault().notify(descriptor);
                        if (result == NotifyDescriptor.OK_OPTION) {
                            d.unsetup();
                        }
                    }
                });
                menu.add(customBoundsItem);

                //Animation
                Image animationIcon = ImageUtilities.loadImage("org/gephi/desktop/timeline/resources/animation_settings.png", false);
                JMenuItem animationItem = new JMenuItem(NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.settings.setPlaySettings"),
                        ImageUtilities.image2Icon(animationIcon));
                animationItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PlaySettingsDialog d = new PlaySettingsDialog();
                        d.setup(model);
                        String title = NbBundle.getMessage(CustomBoundsDialog.class, "PlaySettingsDialog.title");
                        final DialogDescriptor descriptor = new DialogDescriptor(d, title);
                        Object result = DialogDisplayer.getDefault().notify(descriptor);
                        if (result == NotifyDescriptor.OK_OPTION) {
                            d.unsetup();
                        }
                    }
                });
                menu.add(animationItem);

                //Date format
                Image dateFormatIcon = ImageUtilities.loadImage("org/gephi/desktop/timeline/resources/time_format_small.png", false);
                JMenuItem dateFormatItem = new JMenuItem(NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.settings.setTimeFormat"),
                        ImageUtilities.image2Icon(dateFormatIcon));
                dateFormatItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TimeFormatDialog d = new TimeFormatDialog();
                        d.setup(model);
                        String title = NbBundle.getMessage(TimeFormatDialog.class, "TimeFormatDialog.title");
                        final DialogDescriptor descriptor = new DialogDescriptor(d, title);
                        Object result = DialogDisplayer.getDefault().notify(descriptor);
                        if (result == NotifyDescriptor.OK_OPTION) {
                            d.unsetup();
                        }
                    }
                });
                menu.add(dateFormatItem);

                menu.show(settingsButton, 0, -menu.getPreferredSize().height);
            }
        });

        playButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (model != null) {
                    //Bounds not set? Cannot play
                    if (model.getMin() == model.getIntervalStart()
                            && model.getMax() == model.getIntervalEnd()) {
                        String helpMessage = NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.playButton.intevalNotSet");
                        JOptionPane.showMessageDialog(null, helpMessage, helpMessage, JOptionPane.WARNING_MESSAGE);
                    }

                    if (model.isPlaying() && !playButton.isSelected()) {
                        controller.stopPlay();
                    } else if (!model.isPlaying() && playButton.isSelected()) {
                        controller.startPlay();
                    }
                }
            }
        });
    }

    private void setup(TimelineModel model) {
        this.model = model;
        if (model != null) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    drawer.setModel(TimelineTopComponent.this.model);
                    enableTimeline(TimelineTopComponent.this.model);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    drawer.setModel(TimelineTopComponent.this.model);
                    enableTimeline(null);
                }
            });
        }
    }

    @Override
    public void timelineModelChanged(TimelineModelEvent event) {
        if (event.getEventType().equals(TimelineModelEvent.EventType.MODEL)) {
            setup(event.getSource());
        } else if (event.getEventType().equals(TimelineModelEvent.EventType.ENABLED)) {
            enableTimeline(event.getSource());
        } else if (event.getEventType().equals(TimelineModelEvent.EventType.VALID_BOUNDS)) {
            enableTimeline(event.getSource());
        } else if (event.getEventType().equals(TimelineModelEvent.EventType.PLAY_START)) {
            setPlaying(true);
        } else if (event.getEventType().equals(TimelineModelEvent.EventType.PLAY_STOP)) {
            setPlaying(false);
        }
        drawer.consumeEvent(event);
    }

    private void enableTimeline(TimelineModel model) {
        CardLayout cardLayout = (CardLayout) containerPanel.getLayout();
        if (model == null) {
            cardLayout.show(containerPanel, "top");
            enableTimelineButton.setEnabled(false);
            setTimeLineVisible(false);
        } else if (!model.hasValidBounds()) {
            cardLayout.show(containerPanel, "disabled");
        } else if (model.isEnabled()) {
            cardLayout.show(containerPanel, "bottom");
            setTimeLineVisible(true);
        } else {
            cardLayout.show(containerPanel, "top");
            enableTimelineButton.setEnabled(true);
            setTimeLineVisible(true);
        }
    }

    private void setPlaying(final boolean playing) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                playButton.setSelected(playing);
            }
        });
    }

    public void setTimeLineVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (visible != TimelineTopComponent.this.isVisible()) {

                    TimelineTopComponent.this.setVisible(visible);
                    VizController.getInstance().getDrawable().reinitWindow();
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        containerPanel = new javax.swing.JPanel();
        enablePanel = new javax.swing.JPanel();
        toolbarEnable = new javax.swing.JToolBar();
        enableTimelineButton = new javax.swing.JButton();
        disabledTimeline = new javax.swing.JPanel();
        disabledTimelineLabel = new javax.swing.JLabel();
        innerPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        playButton = new javax.swing.JToggleButton();
        innerToolbar = new javax.swing.JToolBar(JToolBar.VERTICAL);
        disableButon = new javax.swing.JButton();
        columnsButton = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        timelinePanel = new org.gephi.desktop.timeline.TimelineDrawer();
        closeButton = new CloseButton();

        setMaximumSize(new java.awt.Dimension(32767, 68));
        setMinimumSize(new java.awt.Dimension(414, 68));
        setPreferredSize(new java.awt.Dimension(424, 68));
        setLayout(new java.awt.GridBagLayout());

        containerPanel.setLayout(new java.awt.CardLayout());

        enablePanel.setLayout(new java.awt.GridBagLayout());

        toolbarEnable.setFloatable(false);
        toolbarEnable.setRollover(true);
        toolbarEnable.setOpaque(false);

        enableTimelineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/activate.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(enableTimelineButton, NbBundle.getMessage (TimelineTopComponent.class, "TimelineTopComponent.enableTimelineButton.text")); // NOI18N
        enableTimelineButton.setToolTipText(org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.enableTimelineButton.toolTipText")); // NOI18N
        enableTimelineButton.setFocusable(false);
        enableTimelineButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        enableTimelineButton.setMargin(new java.awt.Insets(4, 6, 4, 6));
        toolbarEnable.add(enableTimelineButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        enablePanel.add(toolbarEnable, gridBagConstraints);

        containerPanel.add(enablePanel, "top");

        disabledTimeline.setLayout(new java.awt.GridBagLayout());

        disabledTimelineLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/activate.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(disabledTimelineLabel, NbBundle.getMessage (TimelineTopComponent.class, "TimelineTopComponent.disabledTimelineLabel.text")); // NOI18N
        disabledTimelineLabel.setEnabled(false);
        disabledTimeline.add(disabledTimelineLabel, new java.awt.GridBagConstraints());

        containerPanel.add(disabledTimeline, "disabled");

        innerPanel.setLayout(new java.awt.GridBagLayout());

        controlPanel.setLayout(new java.awt.GridBagLayout());

        playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/disabled.png"))); // NOI18N
        playButton.setToolTipText(org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.playButton.toolTipText")); // NOI18N
        playButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/disabled.png"))); // NOI18N
        playButton.setFocusable(false);
        playButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playButton.setRequestFocusEnabled(false);
        playButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/enabled.png"))); // NOI18N
        playButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        controlPanel.add(playButton, gridBagConstraints);

        innerToolbar.setBorder(null);
        innerToolbar.setFloatable(false);
        innerToolbar.setRollover(true);
        innerToolbar.setOpaque(false);

        disableButon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/cross.png"))); // NOI18N
        disableButon.setToolTipText(org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.disableButon.toolTipText")); // NOI18N
        disableButon.setFocusable(false);
        disableButon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        disableButon.setIconTextGap(0);
        disableButon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        innerToolbar.add(disableButon);

        columnsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/chart.png"))); // NOI18N
        columnsButton.setFocusable(false);
        columnsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        columnsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        innerToolbar.add(columnsButton);

        settingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/timeline/resources/settings.png"))); // NOI18N
        settingsButton.setFocusable(false);
        settingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsButton.setIconTextGap(0);
        settingsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        innerToolbar.add(settingsButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        controlPanel.add(innerToolbar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        innerPanel.add(controlPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        innerPanel.add(timelinePanel, gridBagConstraints);

        containerPanel.add(innerPanel, "bottom");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(containerPanel, gridBagConstraints);

        closeButton.setToolTipText(org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.closeButton.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(closeButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton columnsButton;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton disableButon;
    private javax.swing.JPanel disabledTimeline;
    private javax.swing.JLabel disabledTimelineLabel;
    private javax.swing.JPanel enablePanel;
    private javax.swing.JButton enableTimelineButton;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JToolBar innerToolbar;
    private javax.swing.JToggleButton playButton;
    private javax.swing.JButton settingsButton;
    private transient javax.swing.JPanel timelinePanel;
    private javax.swing.JToolBar toolbarEnable;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
