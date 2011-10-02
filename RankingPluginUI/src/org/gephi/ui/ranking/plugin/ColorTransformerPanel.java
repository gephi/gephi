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
package org.gephi.ui.ranking.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.RankingEvent;
import org.gephi.ranking.api.RankingListener;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.api.Transformer;
import org.gephi.ui.components.JRangeSlider;
import org.gephi.ui.components.PaletteIcon;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.gephi.utils.PaletteUtils;
import org.gephi.utils.PaletteUtils.Palette;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Mathieu Bastian
 */
public class ColorTransformerPanel extends javax.swing.JPanel {

    private static final int SLIDER_MAXIMUM = 100;
    private AbstractColorTransformer colorTransformer;
    private GradientSlider gradientSlider;
    private final RecentPalettes recentPalettes;
    private final Ranking ranking;

    public ColorTransformerPanel(final Transformer transformer, Ranking ranking) {
        initComponents();

        final String POSITIONS = "ColorTransformerPanel_" + transformer.getClass().getSimpleName() + "_positions";
        final String COLORS = "ColorTransformerPanel_" + transformer.getClass().getSimpleName() + "_colors";

        colorTransformer = (AbstractColorTransformer) transformer;
        this.ranking = ranking;
        this.recentPalettes = new RecentPalettes();

        float[] positionsStart = colorTransformer.getColorPositions();
        Color[] colorsStart = colorTransformer.getColors();

        try {
            positionsStart = deserializePositions(NbPreferences.forModule(ColorTransformerPanel.class).getByteArray(POSITIONS, serializePositions(positionsStart)));
            colorsStart = deserializeColors(NbPreferences.forModule(ColorTransformerPanel.class).getByteArray(COLORS, serializeColors(colorsStart)));
            colorTransformer.setColorPositions(positionsStart);
            colorTransformer.setColors(colorsStart);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Gradient
        gradientSlider = new GradientSlider(GradientSlider.HORIZONTAL, positionsStart, colorsStart);
        gradientSlider.putClientProperty("GradientSlider.includeOpacity", "false");
        gradientSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                Color[] colors = gradientSlider.getColors();
                float[] positions = gradientSlider.getThumbPositions();
                colorTransformer.setColors(Arrays.copyOf(colors, colors.length));
                colorTransformer.setColorPositions(Arrays.copyOf(positions, positions.length));
                try {
                    NbPreferences.forModule(ColorTransformerPanel.class).putByteArray(POSITIONS, serializePositions(positions));
                    NbPreferences.forModule(ColorTransformerPanel.class).putByteArray(COLORS, serializeColors(colors));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                prepareGradientTooltip();
            }
        });
        gradientPanel.add(gradientSlider, BorderLayout.CENTER);

        //Range
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        slider.setMinimum(0);
        slider.setMaximum(SLIDER_MAXIMUM);
        slider.setValue(0);
        slider.setUpperValue(SLIDER_MAXIMUM);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JRangeSlider source = (JRangeSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    setRangeValues();
                }
                prepareGradientTooltip();
            }
        });
        refreshRangeValues();
        prepareGradientTooltip();

        //Context
//        setComponentPopupMenu(getPalettePopupMenu());
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    JPopupMenu popupMenu = getPalettePopupMenu();       
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    JPopupMenu popupMenu = getPalettePopupMenu();
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
        
        //Color Swatch
        colorSwatchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JPopupMenu popupMenu = getPalettePopupMenu();
                popupMenu.show(colorSwatchToolbar, -popupMenu.getPreferredSize().width, 0);
            }
        });

        //Listen to apply button to add recent palettes
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        RankingModel rankingModel = rankingController.getModel();
        rankingModel.addRankingListener(new RankingListener() {

            public void rankingChanged(RankingEvent event) {
                if (event.is(RankingEvent.EventType.APPLY_TRANSFORMER)) {
                    if (transformer == event.getTransformer()) {
                        addRecentPalette();
                    }
                }
            }
        });
    }

    private void prepareGradientTooltip() {
        StringBuilder sb = new StringBuilder();
        final double min = ((Number) ranking.unNormalize(colorTransformer.getLowerBound())).doubleValue();
        final double max = ((Number) ranking.unNormalize(colorTransformer.getUpperBound())).doubleValue();
        final double range = max - min;
        float[] positions = gradientSlider.getThumbPositions();
        for (int i = 0; i < positions.length - 1; i++) {
            sb.append(min + range * positions[i]);
            sb.append(", ");
        }
        sb.append(min + range * positions[positions.length - 1]);
        gradientSlider.setToolTipText(sb.toString());
    }

    private void setRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        float low = slider.getValue() / 100f;
        float high = slider.getUpperValue() / 100f;
        colorTransformer.setLowerBound(low);
        colorTransformer.setUpperBound(high);

        lowerBoundLabel.setText(ranking.unNormalize(colorTransformer.getLowerBound()).toString());
        upperBoundLabel.setText(ranking.unNormalize(colorTransformer.getUpperBound()).toString());
    }

    private void refreshRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        slider.setValue((int) (colorTransformer.getLowerBound() * 100f));
        slider.setUpperValue((int) (colorTransformer.getUpperBound() * 100f));

        lowerBoundLabel.setText(ranking.unNormalize(colorTransformer.getLowerBound()).toString());
        upperBoundLabel.setText(ranking.unNormalize(colorTransformer.getUpperBound()).toString());
    }

    private JPopupMenu getPalettePopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenu defaultMenu = new JMenu(NbBundle.getMessage(ColorTransformerPanel.class, "PalettePopup.default"));
        for (Palette p : PaletteUtils.getSequencialPalettes()) {
            final Palette p3 = PaletteUtils.get3ClassPalette(p);
            JMenuItem item = new JMenuItem(new PaletteIcon(p3.getColors()));
            item.addActionListener(new ActionListener() {

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

                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(p3.getPositions(), p3.getColors());
                }
            });
            defaultMenu.add(item);
        }
        popupMenu.add(defaultMenu);

        //Invert
        JMenuItem invertItem = new JMenuItem(NbBundle.getMessage(ColorTransformerPanel.class, "PalettePopup.invert"));
        invertItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gradientSlider.setValues(invert(gradientSlider.getThumbPositions()), invert(gradientSlider.getColors()));
            }
        });
        popupMenu.add(invertItem);

        //Recent
        JMenu recentMenu = new JMenu(NbBundle.getMessage(ColorTransformerPanel.class, "PalettePopup.recent"));
        for (final AbstractColorTransformer.LinearGradient gradient : recentPalettes.getPalettes()) {
            JMenuItem item = new JMenuItem(new PaletteIcon(gradient.getColors()));
            item.addActionListener(new ActionListener() {

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
        AbstractColorTransformer.LinearGradient gradient = colorTransformer.getLinearGradient();
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

    private byte[] serializePositions(float[] positions) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(positions);
        out.close();
        return bos.toByteArray();
    }

    private float[] deserializePositions(byte[] positions) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(positions);
        ObjectInputStream in = new ObjectInputStream(bis);
        float[] array = (float[]) in.readObject();
        in.close();
        return array;
    }

    private byte[] serializeColors(Color[] colors) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(colors);
        out.close();
        return bos.toByteArray();
    }

    private Color[] deserializeColors(byte[] colors) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(colors);
        ObjectInputStream in = new ObjectInputStream(bis);
        Color[] array = (Color[]) in.readObject();
        in.close();
        return array;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelColor = new javax.swing.JLabel();
        gradientPanel = new javax.swing.JPanel();
        rangeSlider = new JRangeSlider();
        labelRange = new javax.swing.JLabel();
        upperBoundLabel = new javax.swing.JLabel();
        lowerBoundLabel = new javax.swing.JLabel();
        colorSwatchToolbar = new javax.swing.JToolBar();
        colorSwatchButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(225, 114));

        labelColor.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.labelColor.text")); // NOI18N

        gradientPanel.setOpaque(false);
        gradientPanel.setLayout(new java.awt.BorderLayout());

        rangeSlider.setFocusable(false);
        rangeSlider.setOpaque(false);

        labelRange.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.labelRange.text")); // NOI18N

        upperBoundLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
        upperBoundLabel.setForeground(new java.awt.Color(102, 102, 102));
        upperBoundLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        upperBoundLabel.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.upperBoundLabel.text")); // NOI18N

        lowerBoundLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lowerBoundLabel.setForeground(new java.awt.Color(102, 102, 102));
        lowerBoundLabel.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.lowerBoundLabel.text")); // NOI18N

        colorSwatchToolbar.setFloatable(false);
        colorSwatchToolbar.setRollover(true);
        colorSwatchToolbar.setOpaque(false);

        colorSwatchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/ranking/plugin/resources/color-swatch.png"))); // NOI18N
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelColor)
                        .addGap(18, 18, 18)
                        .addComponent(gradientPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelRange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(lowerBoundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(upperBoundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(rangeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
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
                            .addComponent(gradientPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rangeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRange, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lowerBoundLabel)
                            .addComponent(upperBoundLabel))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colorSwatchButton;
    private javax.swing.JToolBar colorSwatchToolbar;
    private javax.swing.JPanel gradientPanel;
    private javax.swing.JLabel labelColor;
    private javax.swing.JLabel labelRange;
    private javax.swing.JLabel lowerBoundLabel;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JLabel upperBoundLabel;
    // End of variables declaration//GEN-END:variables
}
