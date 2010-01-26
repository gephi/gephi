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
package org.gephi.ui.filters.plugin.partition;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicListUI;
import org.gephi.filters.plugin.partition.PartitionBuilder.PartitionFilter;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionPanel extends javax.swing.JPanel {

    private PartitionFilter filter;

    public PartitionPanel() {
        initComponents();
        setMinimumSize(new Dimension(50, 90));
        final ListCellRenderer renderer = new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {

                final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                PartWrapper pw = (PartWrapper) value;
                if (pw.isEnabled()) {
                    label.setEnabled(true);
                    label.setIcon(pw.icon);
                } else {
                    label.setEnabled(false);
                    label.setDisabledIcon(pw.disabledIcon);
                }
                label.setFont(label.getFont().deriveFont(10f));
                label.setIconTextGap(6);
                setOpaque(false);
                setForeground(list.getForeground());
                setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                return label;
            }
        };
        list.setCellRenderer(renderer);
        MouseListener mouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                PartWrapper pw = (PartWrapper) list.getModel().getElementAt(index);
                pw.setEnabled(!pw.isEnabled());
                list.repaint();
            }
        };
        list.addMouseListener(mouseListener);
    }

    public void setup(final PartitionFilter filter) {
        this.filter = filter;
        final Partition partition = filter.getCurrentPartition();
        final DefaultListModel model = new DefaultListModel();
        if (partition != null) {
            Part[] parts = partition.getParts();
            for (int i = 0; i < parts.length; i++) {
                PartWrapper pw = new PartWrapper(parts[i], parts[i].getColor());
                model.add(i, pw);
            }
        }
        list.setModel(model);
    }

    private static class PartWrapper {

        private final Part part;
        private final PaletteIcon icon;
        private final PaletteIcon disabledIcon;
        private boolean enabled;

        public PartWrapper(Part part, Color color) {
            this.part = part;
            this.icon = new PaletteIcon(color);
            this.disabledIcon = new PaletteIcon();
        }

        public PaletteIcon getIcon() {
            return icon;
        }

        public Part getPart() {
            return part;
        }

        @Override
        public String toString() {
            return part.getDisplayName();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static void computeListSize(final JList list) {
        if (list.getUI() instanceof BasicListUI) {
            final BasicListUI ui = (BasicListUI) list.getUI();

            try {
                final Method method = BasicListUI.class.getDeclaredMethod("updateLayoutState");
                method.setAccessible(true);
                method.invoke(ui);
                list.revalidate();
                list.repaint();
            } catch (final SecurityException e) {
                e.printStackTrace();
            } catch (final NoSuchMethodException e) {
                e.printStackTrace();
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            } catch (final InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static class PaletteIcon implements Icon {

        private final int COLOR_WIDTH;
        private final int COLOR_HEIGHT;
        private final Color BORDER_COLOR;
        private final Color color;

        public PaletteIcon(Color color) {
            this.color = color;
            BORDER_COLOR = new Color(0x444444);
            COLOR_WIDTH = 11;
            COLOR_HEIGHT = 11;
        }

        public PaletteIcon() {
            this.color = new Color(0xDDDDDD);
            BORDER_COLOR = new Color(0x999999);
            COLOR_WIDTH = 11;
            COLOR_HEIGHT = 11;
        }

        public int getIconWidth() {
            return COLOR_WIDTH;
        }

        public int getIconHeight() {
            return COLOR_HEIGHT + 2;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(BORDER_COLOR);
            g.drawRect(x + 2, y, COLOR_WIDTH, COLOR_HEIGHT);
            g.setColor(color);
            g.fillRect(x + 2 + 1, y + 1, COLOR_WIDTH - 1, COLOR_HEIGHT - 1);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);

        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setOpaque(false);
        jScrollPane1.setViewportView(list);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list;
    // End of variables declaration//GEN-END:variables
}
