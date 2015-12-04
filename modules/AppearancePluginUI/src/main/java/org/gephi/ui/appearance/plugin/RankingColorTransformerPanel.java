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
package org.gephi.ui.appearance.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.ui.components.PaletteIcon;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.gephi.ui.components.gradientslider.MultiThumbSlider;
import org.gephi.utils.PaletteUtils;
import org.gephi.utils.PaletteUtils.Palette;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class RankingColorTransformerPanel extends javax.swing.JPanel {

    private RankingElementColorTransformer colorTransformer;
    private GradientSlider gradientSlider;
    private final RecentPalettes recentPalettes;

    public RankingColorTransformerPanel() {
        initComponents();
        this.recentPalettes = new RecentPalettes();

        //Init slider
        gradientSlider = new GradientSlider(GradientSlider.HORIZONTAL);
        gradientSlider.putClientProperty("GradientSlider.includeOpacity", "false");
        gradientSlider.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (colorTransformer != null
                        && ((!gradientSlider.isValueAdjusting() && evt.getPropertyName().equals(MultiThumbSlider.VALUES_PROPERTY))
                        || (evt.getPropertyName().equals(MultiThumbSlider.ADJUST_PROPERTY) && evt.getNewValue().equals(Boolean.FALSE)))) {
                    Color[] colors = gradientSlider.getColors();
                    float[] positions = gradientSlider.getThumbPositions();

                    if (!Arrays.equals(positions, colorTransformer.getColorPositions()) || !Arrays.deepEquals(colors, colorTransformer.getColors())) {
                        colorTransformer.setColors(Arrays.copyOf(colors, colors.length));
                        colorTransformer.setColorPositions(Arrays.copyOf(positions, positions.length));
                        addRecentPalette();
                    }
//                prepareGradientTooltip();
                }
            }
        });
        gradientPanel.add(gradientSlider, BorderLayout.CENTER);

        //Color Swatch
        colorSwatchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JPopupMenu popupMenu = getPalettePopupMenu();
                popupMenu.show(colorSwatchToolbar, -popupMenu.getPreferredSize().width, 0);
            }
        });
    }

    public void setup(RankingFunction function) {
        colorTransformer = (RankingElementColorTransformer) function.getTransformer();

        float[] positionsStart = colorTransformer.getColorPositions();
        Color[] colorsStart = colorTransformer.getColors();

        //Gradient
        gradientSlider.setValues(positionsStart, colorsStart);

//        prepareGradientTooltip();
        //Context
//        setComponentPopupMenu(getPalettePopupMenu());
    }

//    private void prepareGradientTooltip() {
//        StringBuilder sb = new StringBuilder();
//        final double min = ((Number) ranking.unNormalize(colorTransformer.getLowerBound())).doubleValue();
//        final double max = ((Number) ranking.unNormalize(colorTransformer.getUpperBound())).doubleValue();
//        final double range = max - min;
//        float[] positions = gradientSlider.getThumbPositions();
//        for (int i = 0; i < positions.length - 1; i++) {
//            sb.append(min + range * positions[i]);
//            sb.append(", ");
//        }
//        sb.append(min + range * positions[positions.length - 1]);
//        gradientSlider.setToolTipText(sb.toString());
//    }
    private JPopupMenu getPalettePopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenu defaultMenu = new JMenu(NbBundle.getMessage(RankingColorTransformerPanel.class, "PalettePopup.default"));
        for (Palette p : PaletteUtils.getSequencialPalettes()) {
            final Palette p3 = PaletteUtils.get3ClassPalette(p);
            JMenuItem item = new JMenuItem(new PaletteIcon(p3.getColors()));
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(p3.getPositions(), p3.getColors());
                }
            });
            defaultMenu.add(item);
        }
        for (Palette p : PaletteUtils.getDivergingPalettes()) {
            final Palette p3 = PaletteUtils.get3ClassPalette(p);
            JMenuItem item = new JMenuItem(new PaletteIcon(p3.getColors()));
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(p3.getPositions(), p3.getColors());
                }
            });
            defaultMenu.add(item);
        }
        popupMenu.add(defaultMenu);

        //Invert
        JMenuItem invertItem = new JMenuItem(NbBundle.getMessage(RankingColorTransformerPanel.class, "PalettePopup.invert"));
        invertItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gradientSlider.setValues(invert(gradientSlider.getThumbPositions()), invert(gradientSlider.getColors()));
            }
        });
        popupMenu.add(invertItem);

        //Recent
        JMenu recentMenu = new JMenu(NbBundle.getMessage(RankingColorTransformerPanel.class, "PalettePopup.recent"));
        for (final RankingElementColorTransformer.LinearGradient gradient : recentPalettes.getPalettes()) {
            JMenuItem item = new JMenuItem(new PaletteIcon(gradient.getColors()));
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(gradient.getPositions(), gradient.getColors());
                }
            });
            recentMenu.add(item);
        }
        popupMenu.add(recentMenu);

        return popupMenu;
    }

    private void addRecentPalette() {
        RankingElementColorTransformer.LinearGradient gradient = colorTransformer.getLinearGradient();
        recentPalettes.add(gradient);
    }

    private Color[] invert(Color[] source) {
        int len = source.length;
        Color[] res = new Color[len];
        for (int i = 0; i < len; i++) {
            res[i] = source[len - 1 - i];
        }
        return res;
    }

    private float[] invert(float[] source) {
        int len = source.length;
        float[] res = new float[len];
        for (int i = 0; i < len; i++) {
            res[i] = 1 - source[len - 1 - i];
        }

        return res;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelColor = new javax.swing.JLabel();
        gradientPanel = new javax.swing.JPanel();
        colorSwatchToolbar = new javax.swing.JToolBar();
        colorSwatchButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(225, 114));

        labelColor.setText(org.openide.util.NbBundle.getMessage(RankingColorTransformerPanel.class, "RankingColorTransformerPanel.labelColor.text")); // NOI18N

        gradientPanel.setOpaque(false);
        gradientPanel.setLayout(new java.awt.BorderLayout());

        colorSwatchToolbar.setFloatable(false);
        colorSwatchToolbar.setRollover(true);
        colorSwatchToolbar.setOpaque(false);

        colorSwatchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/appearance/plugin/resources/color-swatch.png"))); // NOI18N
        colorSwatchButton.setFocusable(false);
        colorSwatchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        colorSwatchButton.setIconTextGap(0);
        colorSwatchButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        colorSwatchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        colorSwatchToolbar.add(colorSwatchButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelColor)
                .addGap(18, 18, 18)
                .addComponent(gradientPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(colorSwatchToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorSwatchToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelColor, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gradientPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(88, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colorSwatchButton;
    private javax.swing.JToolBar colorSwatchToolbar;
    private javax.swing.JPanel gradientPanel;
    private javax.swing.JLabel labelColor;
    // End of variables declaration//GEN-END:variables
}
