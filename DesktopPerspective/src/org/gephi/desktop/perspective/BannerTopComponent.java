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
package org.gephi.desktop.perspective;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.gephi.desktop.perspective.spi.Perspective;
import org.gephi.desktop.perspective.spi.PerspectiveMember;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponentGroup;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.perspective//Banner//EN",
autostore = false)
public final class BannerTopComponent extends TopComponent {

    private static BannerTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "BannerTopComponent";
    private String selectedPerspective;
    private transient JToggleButton[] buttons;

    public BannerTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(BannerTopComponent.class, "CTL_BannerTopComponent"));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        addGroupTabs();

        logoButton.addActionListener(new ActionListener() {

            @Override
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

        setPreferredSize(new Dimension(100, 30));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, 30);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(32000, 30);
    }

    private void addGroupTabs() {
        final Perspective[] perspectives = Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0]);
        final PerspectiveMember[] members = Lookup.getDefault().lookupAll(PerspectiveMember.class).toArray(new PerspectiveMember[0]);

        buttons = new JPerspectiveButton[perspectives.length];
        int i = 0;

        //Add tabs
        for (final Perspective perspective : perspectives) {
            JPerspectiveButton toggleButton = new JPerspectiveButton(perspective.getDisplayName(), perspective.getIcon());
            toggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //Close other perspective
                    for (Perspective g : perspectives) {
                        if (g != perspective) {
                            TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(g.getName());
                            tpg.close();
                        }
                    }

                    //Close members
                    Perspective closingPerspective = getPerspective(selectedPerspective);
                    for (PerspectiveMember member : members) {
                        if (member.close(closingPerspective)) {
                            if (member instanceof TopComponent) {
                                ((TopComponent) member).close();
                            }
                        }
                    }

                    //Open perspective
                    TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(perspective.getName());
                    tpg.open();

                    //Open members
                    for (PerspectiveMember member : members) {
                        if (member.open(perspective)) {
                            if (member instanceof TopComponent) {
                                ((TopComponent) member).open();
                            }
                        }
                    }

                    selectedPerspective = perspective.getName();
                }
            });
            perspectivesButtonGroup.add(toggleButton);
            buttonsPanel.add(toggleButton);
            buttons[i++] = toggleButton;
        }

        refreshSelectedPerspective();
    }

    //Not working
    /*public void reset() {
    refreshSelectedPerspective();
    for (final Perspective group : Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0])) {
    TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(group.getName());
    if (group.getName().equals(selectedPerspective)) {
    tpg.open();
    } else {
    tpg.close();
    }
    }
    }*/
    private void refreshSelectedPerspective() {
        Perspective[] perspectives = Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0]);
        if (selectedPerspective == null) {
            perspectivesButtonGroup.setSelected(buttons[0].getModel(), true);
            selectedPerspective = perspectives[0].getName();
        } else {
            for (int j = 0; j < perspectives.length; j++) {
                String groupName = perspectives[j].getName();
                if (selectedPerspective.equals(groupName)) {
                    perspectivesButtonGroup.setSelected(buttons[j].getModel(), true);
                }
            }
        }
    }

    private Perspective getPerspective(String name) {
        for (Perspective p : Lookup.getDefault().lookupAll(Perspective.class)) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        perspectivesButtonGroup = new javax.swing.ButtonGroup();
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

        logoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/perspective/resources/logo_std.png"))); // NOI18N
        logoButton.setToolTipText(org.openide.util.NbBundle.getMessage(BannerTopComponent.class, "BannerTopComponent.logoButton.toolTipText")); // NOI18N
        logoButton.setBorder(null);
        logoButton.setBorderPainted(false);
        logoButton.setContentAreaFilled(false);
        logoButton.setFocusPainted(false);
        logoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logoButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/perspective/resources/logo_glow.png"))); // NOI18N
        logoButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/perspective/resources/logo_glow.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        mainPanel.add(logoButton, gridBagConstraints);

        groupsPanel.setBackground(new java.awt.Color(255, 255, 255));
        groupsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonsPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setPreferredSize(new java.awt.Dimension(10, 25));
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(buttonsPanel, gridBagConstraints);

        bannerBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/perspective/resources/bannerback.png"))); // NOI18N
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

    private void logoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_logoButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerBackground;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JButton logoButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.ButtonGroup perspectivesButtonGroup;
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
        p.setProperty("selectedGroup", selectedPerspective);
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
        selectedPerspective = p.getProperty("selectedGroup");
        refreshSelectedPerspective();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private static class JPerspectiveButton extends JToggleButton {

        public JPerspectiveButton(String text, Icon icon) {
            setText(text);
            setBorder(null);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

            if (UIUtils.isWindowsLookAndFeel()) {
                setIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/vista-enabled.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setRolloverIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/vista-mousover.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/vista-selected.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
            } else if (UIUtils.isAquaLookAndFeel()) {
                setIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/aqua-enabled.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setRolloverIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/aqua-mouseover.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/aqua-selected.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
            } else {
                setIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/nimbus-enabled.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setRolloverIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/nimbus-mouseover.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
                setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/gephi/desktop/perspective/resources/nimbus-selected.png"),
                        ImageUtilities.icon2Image(icon), 6, 3)));
            }
        }
    }
}
