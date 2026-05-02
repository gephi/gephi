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

package org.gephi.desktop.welcome;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.jdesktop.swingx.JXHyperlink;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
public final class WelcomeTopComponent extends JPanel {

    public static final String STARTUP_PREF = "WelcomeScreen_Open_Startup";
    private static final Object LINK_PATH = new Object();
    private static WelcomeTopComponent instance;
    private Action openFileAction;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelNew;
    private javax.swing.JLabel labelProjects;
    private javax.swing.JLabel labelRecent;
    private javax.swing.JLabel labelSamples;
    private javax.swing.JPanel mainPanel;
    private org.jdesktop.swingx.JXHyperlink newProjectLink;
    private org.jdesktop.swingx.JXHyperlink openFileLink;
    private javax.swing.JCheckBox openOnStartupCheckbox;
    private javax.swing.JPanel projectsPanel;
    private javax.swing.JScrollPane projectsScrollPane;
    private javax.swing.JPanel recentPanel;
    private javax.swing.JPanel samplesPanel;
    private javax.swing.JPanel southPanel;
    // End of variables declaration//GEN-END:variables

    private WelcomeTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(WelcomeTopComponent.class, "CTL_WelcomeTopComponent"));

        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        initAction();
        loadProjects();
        loadMRU();
        loadSamples();
        loadPrefs();
    }

    public static synchronized WelcomeTopComponent getInstance() {
        if (instance == null) {
            instance = new WelcomeTopComponent();
        }
        return instance;
    }

    private void closeDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Container container = WelcomeTopComponent.this;
                for (; !(container instanceof JDialog); ) {
                    container = container.getParent();
                }
                container.setVisible(false);
            }
        });
    }

    private void initAction() {
        openFileAction = new AbstractAction("") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JXHyperlink link = (JXHyperlink) e.getSource();
                File file = (File) link.getClientProperty(LINK_PATH);
                Actions.forID("File", "org.gephi.desktop.project.actions.OpenFile").actionPerformed(
                    new ActionEvent(file, 0, null));
                closeDialog();
            }
        };
        newProjectLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Actions.forID("File", "org.gephi.desktop.project.actions.NewProject").actionPerformed(null);
                closeDialog();
            }
        });
        openFileLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Actions.forID("File", "org.gephi.desktop.project.actions.OpenFile").actionPerformed(null);
                closeDialog();
            }
        });
    }

    private void loadProjects() {
        net.miginfocom.swing.MigLayout migLayout1 = new net.miginfocom.swing.MigLayout("insets 6");
        migLayout1.setColumnConstraints("[pref]");
        projectsPanel.setLayout(migLayout1);

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        boolean hasProjects = false;
        for (Project project : pc.getAllProjects()) {
            if (project.hasFile()) {
                hasProjects = true;
                JLabel iconLabel = new JLabel(ImageUtilities.loadImageIcon("WelcomeScreen/gephifile20.svg", false));
                projectsPanel.add(iconLabel, "span 1 2, aligny top");

                JXHyperlink link = new JXHyperlink(openFileAction);
                link.setText(project.getName());
                link.setToolTipText(project.getFile().getPath());
                link.putClientProperty(LINK_PATH, project.getFile());
                projectsPanel.add(link, "wrap 0");

                JLabel fileLabel = new JLabel(project.getFile().getName());
                fileLabel.setFont(fileLabel.getFont().deriveFont(fileLabel.getFont().getSize() - 2f));
                fileLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
                projectsPanel.add(fileLabel, "gapleft 2, wrap 6");
            }
        }
        labelProjects.setVisible(hasProjects);
        projectsScrollPane.setVisible(hasProjects);
    }

    private void loadMRU() {
        net.miginfocom.swing.MigLayout migLayout1 = new net.miginfocom.swing.MigLayout("insets 6");
        migLayout1.setColumnConstraints("[pref]");
        recentPanel.setLayout(migLayout1);

        MostRecentFiles mru = Lookup.getDefault().lookup(MostRecentFiles.class);
        boolean hasRecent = false;
        int recentCount = 0;
        for (String filePath : mru.getMRUFileList()) {
            if (recentCount >= 3) {
                break;
            }
            JXHyperlink fileLink = new JXHyperlink(openFileAction);
            File file = new File(filePath);
            if (file.exists()) {
                hasRecent = true;
                recentCount++;
                fileLink.setText(file.getName());
                fileLink.setToolTipText(file.getPath());
                fileLink.putClientProperty(LINK_PATH, file);
                recentPanel.add(fileLink, "wrap");
            }
        }
        labelRecent.setVisible(hasRecent);
        recentPanel.setVisible(hasRecent);
    }

    private void loadSamples() {
        net.miginfocom.swing.MigLayout migLayout1 = new net.miginfocom.swing.MigLayout("insets 6");
        migLayout1.setColumnConstraints("[pref]");
        samplesPanel.setLayout(migLayout1);

        String[] samplePath = new String[4];
        samplePath[0] = "/org/gephi/desktop/welcome/samples/Les Miserables.gexf";
        samplePath[1] = "/org/gephi/desktop/welcome/samples/Java.gexf";
        samplePath[2] = "/org/gephi/desktop/welcome/samples/Power Grid.gml";
        samplePath[3] = "/org/gephi/desktop/welcome/samples/US Airports.gexf";

        String[] sampleTooltip = new String[4];
        sampleTooltip[0] = "Coappearance Network of Characters in 'Les Miserables' (D. E. Knuth)";
        sampleTooltip[1] = "Java Programming Language Dependency graph (V. Batagelj)";
        sampleTooltip[2] = "Topology of the Western States Power Grid of the US (D. Watts & S. Strogatz)";
        sampleTooltip[3] = "Example of a geographical network with latitude/longitude attributes";

        try {
            for (int i = 0; i < samplePath.length; i++) {
                final String s = samplePath[i];
                String tooltip = sampleTooltip[i];

                String fileName = s.substring(s.lastIndexOf('/') + 1);
                final String importer = fileName.substring(fileName.lastIndexOf('.'));
                final String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));
                JXHyperlink fileLink = new JXHyperlink(new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(s);
                        ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                        importController.importStream(stream, fileNameNoExt, importer);
                        closeDialog();
                    }
                });
                fileLink.setText(fileName);
                fileLink.setToolTipText(tooltip);
                fileLink.putClientProperty(LINK_PATH, importer);
                samplesPanel.add(fileLink, "wrap");
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadPrefs() {
        boolean openStartup = NbPreferences.forModule(WelcomeTopComponent.class).getBoolean(STARTUP_PREF, Boolean.TRUE);
        openOnStartupCheckbox.setSelected(openStartup);
        openOnStartupCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                NbPreferences.forModule(WelcomeTopComponent.class)
                    .putBoolean(STARTUP_PREF, openOnStartupCheckbox.isSelected());
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

        mainPanel = new javax.swing.JPanel();
        labelRecent = new javax.swing.JLabel();
        labelNew = new javax.swing.JLabel();
        newProjectLink = new org.jdesktop.swingx.JXHyperlink();
        labelSamples = new javax.swing.JLabel();
        samplesPanel = new javax.swing.JPanel();
        openFileLink = new org.jdesktop.swingx.JXHyperlink();
        labelProjects = new javax.swing.JLabel();
        projectsScrollPane = new javax.swing.JScrollPane();
        projectsPanel = new javax.swing.JPanel();
        recentPanel = new javax.swing.JPanel();
        southPanel = new javax.swing.JPanel();
        openOnStartupCheckbox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(679, 379));
        setLayout(new java.awt.BorderLayout());

        // Custom header: logo + title on the left, checkbox on the right
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new org.jdesktop.swingx.border.DropShadowBorder(),
            javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        javax.swing.JLabel logoLabel =
            new javax.swing.JLabel(ImageUtilities.loadImageIcon("WelcomeScreen/logo_transparent_small.svg", false));
        javax.swing.JLabel titleLabel = new javax.swing.JLabel(
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.header.title")); // NOI18N
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, titleLabel.getFont().getSize() + 4f));
        titleLabel.setForeground(new java.awt.Color(39, 119, 198));

        javax.swing.JPanel logoTitlePanel =
            new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.add(logoLabel);
        logoTitlePanel.add(titleLabel);

        org.openide.awt.Mnemonics.setLocalizedText(openOnStartupCheckbox,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.openOnStartupCheckbox.text")); // NOI18N
        javax.swing.JPanel checkboxPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        checkboxPanel.setOpaque(false);
        checkboxPanel.add(openOnStartupCheckbox);

        headerPanel.add(logoTitlePanel, java.awt.BorderLayout.WEST);
        headerPanel.add(checkboxPanel, java.awt.BorderLayout.EAST);
        add(headerPanel, java.awt.BorderLayout.PAGE_START);

        labelRecent.setFont(labelRecent.getFont()
            .deriveFont(labelRecent.getFont().getStyle() | java.awt.Font.BOLD, labelRecent.getFont().getSize() + 2));
        org.openide.awt.Mnemonics.setLocalizedText(labelRecent,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.labelRecent.text")); // NOI18N

        labelNew.setFont(labelNew.getFont()
            .deriveFont(labelNew.getFont().getStyle() | java.awt.Font.BOLD, labelNew.getFont().getSize() + 2));
        org.openide.awt.Mnemonics.setLocalizedText(labelNew,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.labelNew.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(newProjectLink,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.newProjectLink.text")); // NOI18N

        labelSamples.setFont(labelSamples.getFont()
            .deriveFont(labelSamples.getFont().getStyle() | java.awt.Font.BOLD, labelSamples.getFont().getSize() + 2));
        org.openide.awt.Mnemonics.setLocalizedText(labelSamples,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.labelSamples.text")); // NOI18N

        samplesPanel.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(openFileLink,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.openFileLink.text")); // NOI18N

        labelProjects.setFont(labelProjects.getFont()
            .deriveFont(labelProjects.getFont().getStyle() | java.awt.Font.BOLD,
                labelProjects.getFont().getSize() + 2));
        org.openide.awt.Mnemonics.setLocalizedText(labelProjects,
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.labelProjects.text")); // NOI18N

        projectsScrollPane.setBorder(null);
        projectsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout projectsPanelLayout = new javax.swing.GroupLayout(projectsPanel);
        projectsPanel.setLayout(projectsPanelLayout);
        projectsPanelLayout.setHorizontalGroup(
            projectsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 375, Short.MAX_VALUE)
        );
        projectsPanelLayout.setVerticalGroup(
            projectsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 168, Short.MAX_VALUE)
        );

        projectsScrollPane.setViewportView(projectsPanel);

        javax.swing.GroupLayout recentPanelLayout = new javax.swing.GroupLayout(recentPanel);
        recentPanel.setLayout(recentPanelLayout);
        recentPanelLayout.setHorizontalGroup(
            recentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );
        recentPanelLayout.setVerticalGroup(
            recentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(20, 20, 20)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(projectsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(labelProjects)
                                .addComponent(labelNew)
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addGroup(
                                        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(newProjectLink, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(openFileLink, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addGap(18, 18, 18)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(labelSamples)
                        .addComponent(labelRecent)
                        .addComponent(recentPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(samplesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 260,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelNew)
                        .addComponent(labelRecent))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(newProjectLink, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(openFileLink, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(labelProjects)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(projectsScrollPane))
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(recentPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(labelSamples)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(samplesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 106,
                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
        );

        add(mainPanel, java.awt.BorderLayout.CENTER);

        // Donate banner
        southPanel.setOpaque(true);
        southPanel.setBackground(new java.awt.Color(0xe2, 0xf7, 0xff));
        southPanel.setBorder(
            javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(220, 220, 220)));
        southPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.JLabel donateLabel = new javax.swing.JLabel(
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.donateLabel.text")); // NOI18N
        donateLabel.setFont(donateLabel.getFont().deriveFont(donateLabel.getFont().getStyle() | java.awt.Font.ITALIC));
        donateLabel.setForeground(new java.awt.Color(0x11, 0x9b, 0xd4));
        donateLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 0));

        javax.swing.JButton donateButton = new javax.swing.JButton(
            org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class,
                "WelcomeTopComponent.donateButton.text")); // NOI18N
        donateButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        donateButton.setBackground(new java.awt.Color(0x11, 0x9b, 0xd4));
        donateButton.setForeground(java.awt.Color.WHITE);
        donateButton.setFocusPainted(false);
        donateButton.setOpaque(true);
        donateButton.setFont(
            donateButton.getFont().deriveFont(java.awt.Font.BOLD, donateButton.getFont().getSize() + 1f));
        donateButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        donateButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 18, 8, 18));
        donateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(
                        "https://opencollective.com/gephi/donate?tags=gephi-desktop&interval=oneTime&amount=5"));
                } catch (Exception ex) {
                    org.openide.util.Exceptions.printStackTrace(ex);
                }
            }
        });

        javax.swing.JPanel donateButtonWrapper =
            new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 6));
        donateButtonWrapper.setOpaque(false);
        donateButtonWrapper.add(donateButton);

        southPanel.add(donateLabel, java.awt.BorderLayout.CENTER);
        southPanel.add(donateButtonWrapper, java.awt.BorderLayout.EAST);

        add(southPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
}
