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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.jdesktop.swingx.JXHyperlink;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
public final class WelcomeTopComponent extends JPanel {

    private static WelcomeTopComponent instance;
    public static final String STARTUP_PREF = "WelcomeScreen_Open_Startup";
    private static final String GEPHI_EXTENSION = "gephi";
    private static final Object LINK_PATH = new Object();
    private Action openAction;

    public static synchronized WelcomeTopComponent getInstance() {
        if (instance == null) {
            instance = new WelcomeTopComponent();
        }
        return instance;
    }

    private WelcomeTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(WelcomeTopComponent.class, "CTL_WelcomeTopComponent"));

        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        initAction();
        loadMRU();
        loadSamples();
        loadPrefs();
    }

    private void closeDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Container container = WelcomeTopComponent.this;
                for (; !(container instanceof JDialog);) {
                    container = container.getParent();
                }
                container.setVisible(false);
            }
        });
    }

    private void initAction() {
        openAction = new AbstractAction("", ImageUtilities.loadImageIcon("org/gephi/desktop/welcome/resources/gephifile20.png", false)) {

            @Override
            public void actionPerformed(ActionEvent e) {
                JXHyperlink link = (JXHyperlink) e.getSource();
                File file = (File) link.getClientProperty(LINK_PATH);
                FileObject fileObject = FileUtil.toFileObject(file);
                if (fileObject.hasExt(GEPHI_EXTENSION)) {
                    ProjectControllerUI pc = Lookup.getDefault().lookup(ProjectControllerUI.class);
                    try {
                        pc.openProject(file);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.openGephiError"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                } else {
                    ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                    if (importController.getImportController().isFileSupported(FileUtil.toFile(fileObject))) {
                        importController.importFile(fileObject);
                    }
                }
                closeDialog();
            }
        };
        newProjectLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectControllerUI pc = Lookup.getDefault().lookup(ProjectControllerUI.class);
                pc.newProject();
                closeDialog();
            }
        });
        openFileLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectControllerUI pc = Lookup.getDefault().lookup(ProjectControllerUI.class);
                pc.openFile();
                closeDialog();
            }
        });
    }

    private void loadMRU() {
        net.miginfocom.swing.MigLayout migLayout1 = new net.miginfocom.swing.MigLayout();
        migLayout1.setColumnConstraints("[pref]");
        recentPanel.setLayout(migLayout1);

        MostRecentFiles mru = Lookup.getDefault().lookup(MostRecentFiles.class);
        for (String filePath : mru.getMRUFileList()) {
            JXHyperlink fileLink = new JXHyperlink(openAction);
            File file = new File(filePath);
            if (file.exists()) {
                fileLink.setText(file.getName());
                fileLink.putClientProperty(LINK_PATH, file);
                recentPanel.add(fileLink, "wrap");
            }
        }
    }

    private void loadSamples() {
        net.miginfocom.swing.MigLayout migLayout1 = new net.miginfocom.swing.MigLayout();
        migLayout1.setColumnConstraints("[pref]");
        samplesPanel.setLayout(migLayout1);

        String[] samplePath = new String[3];
        samplePath[0] = "/org/gephi/desktop/welcome/samples/Les Miserables.gexf";
        samplePath[1] = "/org/gephi/desktop/welcome/samples/Java.gexf";
        samplePath[2] = "/org/gephi/desktop/welcome/samples/Power Grid.gml";

        String[] sampleTooltip = new String[3];
        sampleTooltip[0] = "Coappearance Network of Characters in 'Les Miserables' (D. E. Knuth)";
        sampleTooltip[1] = "Java Programming Language Dependency graph (V. Batagelj)";
        sampleTooltip[2] = "Topology of the Western States Power Grid of the US (D. Watts & S. Strogatz)";

        try {
            for (int i = 0; i < samplePath.length; i++) {
                final String s = samplePath[i];
                String tooltip = sampleTooltip[i];

                String fileName = s.substring(s.lastIndexOf('/') + 1, s.length());
                final String importer = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
                JXHyperlink fileLink = new JXHyperlink(new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(s);
                        ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                        importController.importStream(stream, importer);
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
        Boolean openStartup = NbPreferences.forModule(WelcomeTopComponent.class).getBoolean(STARTUP_PREF, Boolean.TRUE);
        openOnStartupCheckbox.setSelected(openStartup);
        openOnStartupCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                NbPreferences.forModule(WelcomeTopComponent.class).putBoolean(STARTUP_PREF, openOnStartupCheckbox.isSelected());
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

        header = new org.jdesktop.swingx.JXHeader();
        mainPanel = new javax.swing.JPanel();
        labelRecent = new javax.swing.JLabel();
        recentPanel = new javax.swing.JPanel();
        labelNew = new javax.swing.JLabel();
        newProjectLink = new org.jdesktop.swingx.JXHyperlink();
        labelSamples = new javax.swing.JLabel();
        samplesPanel = new javax.swing.JPanel();
        openFileLink = new org.jdesktop.swingx.JXHyperlink();
        southPanel = new javax.swing.JPanel();
        openOnStartupCheckbox = new javax.swing.JCheckBox();

        setOpaque(true);
        setPreferredSize(new java.awt.Dimension(679, 379));
        setLayout(new java.awt.BorderLayout());

        header.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/welcome/resources/logo_transparent_small.png"))); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.header.title")); // NOI18N
        header.setTitleFont(header.getTitleFont().deriveFont(header.getTitleFont().getSize()+4f));
        header.setTitleForeground(new java.awt.Color(39, 119, 198));
        header.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
        add(header, java.awt.BorderLayout.PAGE_START);

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));

        labelRecent.setFont(labelRecent.getFont().deriveFont(labelRecent.getFont().getStyle() | java.awt.Font.BOLD, labelRecent.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(labelRecent, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.labelRecent.text")); // NOI18N

        recentPanel.setOpaque(false);

        labelNew.setFont(labelNew.getFont().deriveFont(labelNew.getFont().getStyle() | java.awt.Font.BOLD, labelNew.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(labelNew, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.labelNew.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(newProjectLink, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.newProjectLink.text")); // NOI18N

        labelSamples.setFont(labelSamples.getFont().deriveFont(labelSamples.getFont().getStyle() | java.awt.Font.BOLD, labelSamples.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(labelSamples, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.labelSamples.text")); // NOI18N

        samplesPanel.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(openFileLink, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.openFileLink.text")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelRecent))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(samplesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelNew)
                            .addComponent(labelSamples)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(openFileLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(newProjectLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(62, 62, 62)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelNew)
                    .addComponent(labelRecent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(newProjectLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openFileLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelSamples)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(samplesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                    .addComponent(recentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(mainPanel, java.awt.BorderLayout.CENTER);

        southPanel.setBackground(new java.awt.Color(255, 255, 255));
        southPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(openOnStartupCheckbox, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.openOnStartupCheckbox.text")); // NOI18N
        southPanel.add(openOnStartupCheckbox);

        add(southPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXHeader header;
    private javax.swing.JLabel labelNew;
    private javax.swing.JLabel labelRecent;
    private javax.swing.JLabel labelSamples;
    private javax.swing.JPanel mainPanel;
    private org.jdesktop.swingx.JXHyperlink newProjectLink;
    private org.jdesktop.swingx.JXHyperlink openFileLink;
    private javax.swing.JCheckBox openOnStartupCheckbox;
    private javax.swing.JPanel recentPanel;
    private javax.swing.JPanel samplesPanel;
    private javax.swing.JPanel southPanel;
    // End of variables declaration//GEN-END:variables
}
