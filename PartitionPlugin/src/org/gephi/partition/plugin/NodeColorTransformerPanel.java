/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.partition.plugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.java.dev.colorchooser.ColorChooser;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;
import org.gephi.partition.spi.Transformer;
import org.gephi.ui.utils.PaletteUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeColorTransformerPanel extends javax.swing.JPanel {

    private NodeColorTransformer nodeColorTransformer;
    private Partition partition;
    private JPopupMenu popupMenu;

    public NodeColorTransformerPanel() {
        initComponents();
        createPopup();
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (nodeColorTransformer != null) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        table.setRowMargin(4);
        table.setRowHeight(18);
    }

    public void setup(Partition partition, Transformer transformer) {
        nodeColorTransformer = (NodeColorTransformer) transformer;
        if (nodeColorTransformer.getMap().isEmpty()) {
            List<Color> colors = PaletteUtils.getSequenceColors(partition.getPartsCount());
            int i = 0;
            for (Part p : partition.getParts()) {
                nodeColorTransformer.getMap().put(p.getValue(), colors.get(i));
                i++;
            }
        }
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        this.partition = partition;

        Part[] partsArray = partition.getParts();
        Arrays.sort(partsArray);

        //Model
        String[] columnNames = new String[]{"Color", "Partition", "Percentage"};
        DefaultTableModel model = new DefaultTableModel(columnNames, partsArray.length) {

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

        for (int i = 0; i < partsArray.length; i++) {
            final Part p = partsArray[partsArray.length - 1 - i];
            model.setValueAt(p.getValue(), i, 0);
            model.setValueAt(p.getDisplayName(), i, 1);
            String perc = "(" + formatter.format(p.getPercentage()) + ")";
            model.setValueAt(perc, i, 2);
        }
    }

    private void createPopup() {
        popupMenu = new JPopupMenu();
        JMenuItem randomizeItem = new JMenuItem(NbBundle.getMessage(NodeColorTransformerPanel.class, "NodeColorTransformerPanel.action.randomize"));
        randomizeItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                nodeColorTransformer.getMap().clear();
                setup(partition, nodeColorTransformer);
                revalidate();
                repaint();
            }
        });
        popupMenu.add(randomizeItem);
        JMenuItem allBlackItem = new JMenuItem(NbBundle.getMessage(NodeColorTransformerPanel.class, "NodeColorTransformerPanel.action.allBlacks"));
        allBlackItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (Entry<Object, Color> entry : nodeColorTransformer.getMap().entrySet()) {
                    entry.setValue(Color.BLACK);
                }
                setup(partition, nodeColorTransformer);
                revalidate();
                repaint();
            }
        });
        popupMenu.add(allBlackItem);
    }

    class ColorChooserRenderer extends JLabel implements TableCellRenderer {

        public ColorChooserRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color c = nodeColorTransformer.getMap().get(value);
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

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 6;
        }

        public int getIconHeight() {
            return 6;
        }
    }

    class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {

        private final ColorChooser delegate;
        Object currentValue;

        public ColorChooserEditor() {
            delegate = new ColorChooser();
            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent actionEvent) {
                    nodeColorTransformer.getMap().put(currentValue, delegate.getColor());
                }
            };
            delegate.addActionListener(actionListener);
        }

        public Object getCellEditorValue() {
            return currentValue;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            currentValue = value;
            return delegate;
        }
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

        table = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        table.setFont(table.getFont().deriveFont(table.getFont().getSize()-1f));
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        table.setOpaque(false);
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
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(table, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
