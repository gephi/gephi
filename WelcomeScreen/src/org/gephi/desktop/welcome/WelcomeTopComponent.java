/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.welcome;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.io.importer.api.ImportController;
import org.gephi.project.api.ProjectController;
import org.jdesktop.swingx.JXHyperlink;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.welcome//Welcome//EN",
autostore = false)
public final class WelcomeTopComponent extends TopComponent {

    public static final String STARTUP_PREF = "WelcomeScreen_Open_Startup";
    private static final String GEPHI_EXTENSION = "gephi";
    private static final Object LINK_PATH = new Object();
    private static WelcomeTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "WelcomeTopComponent";
    private Action openAction;

    public WelcomeTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(WelcomeTopComponent.class, "CTL_WelcomeTopComponent"));
        setToolTipText(NbBundle.getMessage(WelcomeTopComponent.class, "HINT_WelcomeTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        initAction();
        loadMRU();
        loadSamples();
        loadPrefs();
    }

    private void initAction() {
        openAction = new AbstractAction("", ImageUtilities.loadImageIcon("org/gephi/desktop/welcome/resources/gephifile20.png", false)) {

            public void actionPerformed(ActionEvent e) {
                JXHyperlink link = (JXHyperlink) e.getSource();
                File file = (File) link.getClientProperty(LINK_PATH);
                FileObject fileObject = FileUtil.toFileObject(file);
                if (fileObject.hasExt(GEPHI_EXTENSION)) {
                    ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                    pc.closeCurrentProject();
                    try {
                        DataObject doe = DataObject.find(fileObject);
                        pc.loadProject(doe);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.openGephiError"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                } else {
                    ImportController importController = Lookup.getDefault().lookup(ImportController.class);
                    if (importController.isFileSupported(fileObject)) {
                        importController.doImport(fileObject);
                    }
                }
                WelcomeTopComponent.this.close();
            }
        };
        newProjectLink.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                if (pc.canNewProject()) {
                    pc.newProject();
                }
                WelcomeTopComponent.this.close();
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

        String[] samplePath = new String[1];
        samplePath[0] = "/org/gephi/desktop/welcome/samples/Les Miserables.gexf";

        try {
            for (String s : samplePath) {
                final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(s);
                String fileName = s.substring(s.lastIndexOf('/') + 1, s.length());
                final String importer = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
                JXHyperlink fileLink = new JXHyperlink(new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
                        importController.doImport(stream, importer);
                        WelcomeTopComponent.this.close();
                    }
                });
                fileLink.setText(fileName);
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

            public void itemStateChanged(ItemEvent e) {
                NbPreferences.forModule(WelcomeTopComponent.class).putBoolean(STARTUP_PREF, openOnStartupCheckbox.isSelected());
            }
        });
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        southPanel = new javax.swing.JPanel();
        openOnStartupCheckbox = new javax.swing.JCheckBox();

        setOpaque(true);
        setLayout(new java.awt.BorderLayout());

        header.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/welcome/resources/logo_transparent_small.png"))); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.header.title")); // NOI18N
        header.setTitleFont(header.getTitleFont().deriveFont(header.getTitleFont().getSize()+4f));
        header.setTitleForeground(new java.awt.Color(39, 119, 198));
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

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelRecent)
                    .addComponent(recentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(newProjectLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labelNew)
                    .addComponent(labelSamples)
                    .addComponent(samplesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelNew)
                    .addComponent(labelRecent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(newProjectLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelSamples)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(samplesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                    .addComponent(recentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(mainPanel, java.awt.BorderLayout.CENTER);

        southPanel.setBackground(new java.awt.Color(255, 255, 255));
        southPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(openOnStartupCheckbox, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.openOnStartupCheckbox.text")); // NOI18N
        openOnStartupCheckbox.setOpaque(false);
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
    private javax.swing.JCheckBox openOnStartupCheckbox;
    private javax.swing.JPanel recentPanel;
    private javax.swing.JPanel samplesPanel;
    private javax.swing.JPanel southPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized WelcomeTopComponent getDefault() {
        if (instance == null) {
            instance = new WelcomeTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the WelcomeTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized WelcomeTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(WelcomeTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof WelcomeTopComponent) {
            return (WelcomeTopComponent) win;
        }
        Logger.getLogger(WelcomeTopComponent.class.getName()).warning(
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
}
