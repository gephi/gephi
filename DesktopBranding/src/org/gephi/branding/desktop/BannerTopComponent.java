/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.gephi.branding.desktop.group.ComponentGroup;
import org.gephi.branding.desktop.group.LaboratoryGroup;
import org.gephi.branding.desktop.group.OverviewGroup;
import org.gephi.branding.desktop.group.PreviewGroup;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponentGroup;

@ConvertAsProperties(dtd = "-//org.gephi.branding.desktop//Banner//EN",
autostore = false)
public final class BannerTopComponent extends TopComponent {

    private static BannerTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "BannerTopComponent";
    private String selectedGroup;
    private transient ComponentGroup[] groups;
    private transient JToggleButton[] buttons;

    public BannerTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(BannerTopComponent.class, "CTL_BannerTopComponent"));
        setToolTipText(NbBundle.getMessage(BannerTopComponent.class, "HINT_BannerTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        addGroupTabs();

        logoButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    try {
                        java.net.URI uri = new java.net.URI("http://gephi.org");
                        desktop.browse(uri);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, 10);
    }

    private void addGroupTabs() {

        groups = new ComponentGroup[3];
        groups[0] = new OverviewGroup();
        groups[1] = new LaboratoryGroup();
        groups[2] = new PreviewGroup();

        //Sort by priority
        Arrays.sort(groups, new Comparator<ComponentGroup>() {

            public int compare(ComponentGroup o1, ComponentGroup o2) {
                return ((Integer) o2.getPriority()).compareTo((Integer) o1.getPriority());
            }
        });
        buttons = new JGroupButton[groups.length];
        int i = 0;

        //Add tabs
        for (final ComponentGroup group : groups) {
            JGroupButton toggleButton = new JGroupButton(group.getDisplayName(), group.getIcon());
            toggleButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    for (ComponentGroup g : groups) {
                        if (g != group) {
                            TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(g.getGroupName());
                            tpg.close();
                        }
                    }
                    //Open selected
                    TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(group.getGroupName());
                    tpg.open();
                    selectedGroup = group.getGroupName();
                }
            });
            groupsButtonGroup.add(toggleButton);
            buttonsPanel.add(toggleButton);
            buttons[i++] = toggleButton;
        }

        refreshSelectedGroup();
    }

    private void refreshSelectedGroup() {
        if (selectedGroup == null) {
            groupsButtonGroup.setSelected(buttons[0].getModel(), true);
            selectedGroup = groups[0].getDisplayName();
        } else {
            for (int j = 0; j < groups.length; j++) {
                String groupName = groups[j].getGroupName();
                if (selectedGroup.equals(groupName)) {
                    groupsButtonGroup.setSelected(buttons[j].getModel(), true);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        groupsButtonGroup = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        logoButton = new javax.swing.JButton();
        groupsPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        bannerBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setLayout(new java.awt.BorderLayout());

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        logoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/branding/desktop/resources/logo_std.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(logoButton, org.openide.util.NbBundle.getMessage(BannerTopComponent.class, "BannerTopComponent.logoButton.text")); // NOI18N
        logoButton.setToolTipText(org.openide.util.NbBundle.getMessage(BannerTopComponent.class, "BannerTopComponent.logoButton.toolTipText")); // NOI18N
        logoButton.setBorder(null);
        logoButton.setBorderPainted(false);
        logoButton.setContentAreaFilled(false);
        logoButton.setFocusPainted(false);
        logoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logoButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/branding/desktop/resources/logo_glow.png"))); // NOI18N
        logoButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/branding/desktop/resources/logo_glow.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        mainPanel.add(logoButton, gridBagConstraints);

        groupsPanel.setBackground(new java.awt.Color(255, 255, 255));
        groupsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonsPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(buttonsPanel, gridBagConstraints);

        bannerBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/branding/desktop/resources/bannerback.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bannerBackground, org.openide.util.NbBundle.getMessage(BannerTopComponent.class, "BannerTopComponent.bannerBackground.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(bannerBackground, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(groupsPanel, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerBackground;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.ButtonGroup groupsButtonGroup;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JButton logoButton;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized BannerTopComponent getDefault() {
        if (instance == null) {
            instance = new BannerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the BannerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized BannerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(BannerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof BannerTopComponent) {
            return (BannerTopComponent) win;
        }
        Logger.getLogger(BannerTopComponent.class.getName()).warning(
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
        p.setProperty("selectedGroup", selectedGroup);
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
        selectedGroup = p.getProperty("selectedGroup");
        refreshSelectedGroup();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private static class JGroupButton extends JToggleButton {

        public JGroupButton(String text, Icon icon) {
            setText(text);
            setBorder(null);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

            if (UIUtils.isWindowsLookAndFeel()) {
                setIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/branding/desktop/resources/vista-enabled.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setRolloverIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/branding/desktop/resources/vista-mousover.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/branding/desktop/resources/vista-selected.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
            }
        }
    }
}
