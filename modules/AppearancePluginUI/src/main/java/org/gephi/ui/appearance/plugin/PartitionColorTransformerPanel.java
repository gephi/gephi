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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.java.dev.colorchooser.ColorChooser;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.ui.appearance.plugin.palette.PaletteGeneratorPanel;
import org.gephi.ui.components.PaletteIcon;
import org.gephi.ui.utils.UIUtils;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTitledSeparator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionColorTransformerPanel extends javax.swing.JPanel {

    private PalettePopupButton palettePopupButton;
    private PartitionFunction function;
    private List<Object> values;

    public PartitionColorTransformerPanel() {
        initComponents();
        palettePopupButton = new PalettePopupButton();
        if (UIUtils.isAquaLookAndFeel()) {
            backPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }

    public JButton getPaletteButton() {
        return palettePopupButton;
    }

    public void setup(PartitionFunction function) {
        this.function = function;
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);

        values = new ArrayList();
        for (Object value : function.getPartition().getValues()) {
            values.add(value);
        }
        Collections.sort(values, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                float p1 = PartitionColorTransformerPanel.this.function.getPartition().percentage(o1);
                float p2 = PartitionColorTransformerPanel.this.function.getPartition().percentage(o2);
                return p1 > p2 ? -1 : p1 < p2 ? 1 : 0;
            }
        });

        //Model
        String[] columnNames = new String[]{"Color", "Partition", "Percentage"};
        DefaultTableModel model = new DefaultTableModel(columnNames, values.size()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        table.setModel(model);

        TableColumn partCol = table.getColumnModel().getColumn(1);
        partCol.setCellRenderer(new TextRenderer());

        TableColumn percCol = table.getColumnModel().getColumn(2);
        percCol.setCellRenderer(new TextRenderer());
        percCol.setPreferredWidth(60);
        percCol.setMaxWidth(60);

        TableColumn colorCol = table.getColumnModel().getColumn(0);
        colorCol.setCellEditor(new ColorChooserEditor());
        colorCol.setCellRenderer(new ColorChooserRenderer());
        colorCol.setPreferredWidth(16);
        colorCol.setMaxWidth(16);

        for (int j = 0; j < values.size(); j++) {
            Object value = values.get(j);
            String displayName = value == null ? "null" : value.toString();
            float percentage = function.getPartition().percentage(value);
            model.setValueAt(value, j, 0);
            model.setValueAt(displayName, j, 1);
            String perc = "(" + formatter.format(percentage) + ")";
            model.setValueAt(perc, j, 2);
        }
    }

    private void applyPalette(Palette palette) {
        PaletteManager.getInstance().addRecentPalette(palette);
        Color[] colors = palette.getColors();
        for (int i = 0; i < values.size(); i++) {
            Object val = values.get(i);
            Color col = colors[i];
            function.getPartition().setColor(val, col);
        }
        table.revalidate();
        table.repaint();
    }

    private void applyColor(Color col) {
        for (int i = 0; i < values.size(); i++) {
            Object val = values.get(i);
            function.getPartition().setColor(val, col);
        }
        table.revalidate();
        table.repaint();
    }

    class ColorChooserRenderer extends JLabel implements TableCellRenderer {

        public ColorChooserRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color c = function.getPartition().getColor(value);
            setBackground(c);
            return this;
        }
    }

    class TextRenderer extends JLabel implements TableCellRenderer {

        private EmptyIcon emptyIcon;

        public TextRenderer() {
            setFont(table.getFont());
            emptyIcon = new EmptyIcon();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((String) value);
            if (column == 1) {
                setIcon(emptyIcon);
            } else {
                setIcon(null);
            }
            return this;
        }
    }

    class EmptyIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return 6;
        }

        @Override
        public int getIconHeight() {
            return 6;
        }
    }

    class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {

        private final ColorChooser delegate;
        Object currentValue;

        public ColorChooserEditor() {
            delegate = new ColorChooser();
            delegate.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
                        function.getPartition().setColor(currentValue, (Color) evt.getNewValue());
                    }
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            currentValue = value;
            return delegate;
        }
    }

    class PalettePopupButton extends JXHyperlink {

        private final PaletteManager paletteManager;

        public PalettePopupButton() {
            setText(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PartitionColorTransformerPanel.paletteButton"));
            setClickedColor(new Color(0, 51, 255));
            setFocusPainted(false);
            setFocusable(false);
            paletteManager = PaletteManager.getInstance();

            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int size = function.getPartition().size();
                    JPopupMenu menu = createPopup(size);
                    menu.show(PalettePopupButton.this, 0, getHeight());
                }
            });
        }

        private JPopupMenu createPopup(final int colorsCount) {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JXTitledSeparator(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.recent")));
            Collection<Palette> recentPalettes = paletteManager.getRecentPalettes();
            if (recentPalettes.isEmpty()) {
                menu.add("<html><i>" + NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.norecent") + "</i></html>");
            } else {
                for (Palette pl : recentPalettes) {
                    menu.add(new PaletteMenuItem(pl, colorsCount));
                }
            }

            menu.add(new JXTitledSeparator(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.standard")));
            JMenu lightPalette = new JMenu(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.light"));
            for (Palette pl : paletteManager.getWhiteBackgroudPalette(colorsCount)) {
                lightPalette.add(new PaletteMenuItem(pl, colorsCount));
            }
            menu.add(lightPalette);

            JMenu darkPalette = new JMenu(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.dark"));
            for (Palette pl : paletteManager.getBlackBackgroudPalette(colorsCount)) {
                darkPalette.add(new PaletteMenuItem(pl, colorsCount));
            }
            menu.add(darkPalette);

            JMenuItem allBlack = new JMenuItem(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.allblack"));
            allBlack.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyColor(Color.BLACK);
                }
            });
            menu.add(allBlack);

            JMenuItem allWhite = new JMenuItem(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.allwhite"));
            allWhite.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyColor(Color.WHITE);
                }
            });
            menu.add(allWhite);

            JMenuItem generate = new JMenuItem(NbBundle.getMessage(PartitionColorTransformerPanel.class, "PalettePopup.generate"));
            generate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PaletteGeneratorPanel pgn = new PaletteGeneratorPanel();
                    pgn.setup(colorsCount);
                    NotifyDescriptor nd = new NotifyDescriptor(pgn,
                            NbBundle.getMessage(PartitionColorTransformerPanel.class, "PartitionColorTransformerPanel.generatePalettePanel.title"),
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.DEFAULT_OPTION, null, null);

                    if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                        Palette pl = pgn.getSelectedPalette();
                        if (pl != null) {
                            applyPalette(pl);
                        }
                    }
                }
            });
            menu.add(generate);

            return menu;
        }
    }

    class PaletteMenuItem extends JMenuItem implements ActionListener {

        private final Palette palette;

        public PaletteMenuItem(Palette palette, int max) {
            super(new PaletteIcon(palette.getColors(), max));
            this.palette = palette;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            applyPalette(palette);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        centerScrollPane = new javax.swing.JScrollPane();
        backPanel = new javax.swing.JPanel();
        table = new javax.swing.JTable();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        centerScrollPane.setBorder(null);
        centerScrollPane.setOpaque(false);
        centerScrollPane.setViewportView(null);

        backPanel.setLayout(new java.awt.GridBagLayout());

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        table.setOpaque(false);
        table.setRowHeight(18);
        table.setRowMargin(4);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setTableHeader(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        backPanel.add(table, gridBagConstraints);

        centerScrollPane.setViewportView(backPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(centerScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backPanel;
    private javax.swing.JScrollPane centerScrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
