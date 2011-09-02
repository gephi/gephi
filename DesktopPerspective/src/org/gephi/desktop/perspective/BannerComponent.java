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
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.gephi.desktop.perspective.spi.Perspective;
import org.gephi.desktop.perspective.spi.PerspectiveMember;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class BannerComponent extends javax.swing.JPanel {

    private String selectedPerspective;
    private transient JToggleButton[] buttons;

    public BannerComponent() {
        initComponents();

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

        //This defines the height of the banner bar - with an extra height on MacOS X
        setPreferredSize(new Dimension(100, 35 + (UIUtils.isAquaLookAndFeel() ? 10 : 0)));
    }

    private void addGroupTabs() {
        final Perspective[] perspectives = Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0]);

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

                    //Open perspective
                    TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(perspective.getName());
                    tpg.open();

                    PerspectiveMember[] members = Lookup.getDefault().lookupAll(PerspectiveMember.class).toArray(new PerspectiveMember[0]);

                    //Close members
                    Perspective closingPerspective = getPerspective(selectedPerspective);
                    for (PerspectiveMember member : members) {
                        if (member.close(closingPerspective)) {
                            if (member instanceof TopComponent) {
                                boolean closed = ((TopComponent) member).close();
                                //System.out.println("Close "+member+" : "+closed);
                            }
                        }
                    }

                    //Open members
                    for (PerspectiveMember member : members) {
                        if (member.open(perspective)) {
                            if (member instanceof TopComponent && !((TopComponent) member).isOpened()) {
                                ((TopComponent) member).open();
                                //System.out.println("Open "+member);
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
    @SuppressWarnings("unchecked")
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
        setLayout(new java.awt.BorderLayout());

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        logoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/perspective/resources/logo_std.png"))); // NOI18N
        logoButton.setToolTipText(org.openide.util.NbBundle.getMessage(BannerComponent.class, "BannerComponent.logoButton.toolTipText")); // NOI18N
        logoButton.setBorder(null);
        logoButton.setBorderPainted(false);
        logoButton.setContentAreaFilled(false);
        logoButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerBackground;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JButton logoButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.ButtonGroup perspectivesButtonGroup;
    // End of variables declaration//GEN-END:variables

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
