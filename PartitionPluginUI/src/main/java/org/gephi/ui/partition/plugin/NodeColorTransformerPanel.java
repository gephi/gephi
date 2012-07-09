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
package org.gephi.ui.partition.plugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.partition.spi.Transformer;
import org.gephi.utils.PaletteUtils;
import org.openide.util.ImageUtilities;
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

    public void setup(Partition partition, Transformer transformer, boolean color) {
        nodeColorTransformer = (NodeColorTransformer) transformer;
        if (color) {
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

        for (int j = 0; j < partsArray.length; j++) {
            final Part p = partsArray[partsArray.length - 1 - j];
            model.setValueAt(p.getValue(), j, 0);
            model.setValueAt(p.getDisplayName(), j, 1);
            String perc = "(" + formatter.format(p.getPercentage()) + ")";
            model.setValueAt(perc, j, 2);
        }
    }

    private void createPopup() {
        popupMenu = new JPopupMenu();
        JMenuItem randomizeItem = new JMenuItem(NbBundle.getMessage(NodeColorTransformerPanel.class, "NodeColorTransformerPanel.action.randomize"));
        randomizeItem.setIcon(ImageUtilities.loadImageIcon("org/gephi/ui/partition/plugin/resources/randomize.png", false));
        randomizeItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                nodeColorTransformer.getMap().clear();
                setup(partition, nodeColorTransformer, true);
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
                setup(partition, nodeColorTransformer, false);
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
            delegate.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
                        nodeColorTransformer.getMap().put(currentValue, (Color) evt.getNewValue());
                    }
                }
            });
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
