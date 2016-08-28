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
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicListUI;
import org.gephi.appearance.api.Partition;
import org.gephi.filters.plugin.partition.PartitionBuilder.PartitionFilter;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionPanel extends javax.swing.JPanel {

    private PartitionFilter filter;
    private JPopupMenu popupMenu;

    public PartitionPanel() {
        initComponents();
        setMinimumSize(new Dimension(50, 90));

        //List renderer
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

        //List click
        MouseListener mouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index == -1) {
                        return;
                    }
                    PartWrapper pw = (PartWrapper) list.getModel().getElementAt(index);
                    boolean set = !pw.isEnabled();
                    pw.setEnabled(set);
                    if (set) {
                        filter.addPart(pw.getPart());
                    } else {
                        filter.removePart(pw.getPart());
                    }
                    list.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (filter != null) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        list.addMouseListener(mouseListener);

        //Popup
        createPopup();
    }

    public void setup(final PartitionFilter filter) {
        this.filter = filter;
        final Partition partition = filter.getPartition();
        if (partition != null) {
            refresh(partition, filter.getParts());
        }
    }

    private void refresh(Partition partition, Set<Object> currentParts) {
        final DefaultListModel model = new DefaultListModel();

        int i = 0;
        for (Object p : partition.getSortedValues()) {
            PartWrapper pw = new PartWrapper(p, partition.percentage(p), partition.getColor(p));
            pw.setEnabled(currentParts.contains(p));
            model.add(i++, pw);
        }
        list.setModel(model);
    }

    private static class PartWrapper {

        private final Object part;
        private final float percentage;
        private final PaletteIcon icon;
        private final PaletteIcon disabledIcon;
        private boolean enabled = false;
        private static final NumberFormat FORMATTER = NumberFormat.getPercentInstance();

        public PartWrapper(Object part, float percentage, Color color) {
            this.part = part;
            this.percentage = percentage;
            this.icon = new PaletteIcon(color);
            this.disabledIcon = new PaletteIcon();
            FORMATTER.setMaximumFractionDigits(2);
        }

        public PaletteIcon getIcon() {
            return icon;
        }

        public Object getPart() {
            return part;
        }

        @Override
        public String toString() {
            String percentageStr = FORMATTER.format(percentage / 100f);
            return (part == null ? "null" : part.toString()) + " (" + percentageStr + ")";
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private void createPopup() {
        popupMenu = new JPopupMenu();
        JMenuItem refreshItem = new JMenuItem(NbBundle.getMessage(PartitionPanel.class, "PartitionPanel.action.refresh"));
        refreshItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setup(filter);
            }
        });
        popupMenu.add(refreshItem);
        JMenuItem selectItem = new JMenuItem(NbBundle.getMessage(PartitionPanel.class, "PartitionPanel.action.selectall"));
        selectItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filter.selectAll();
                refresh(filter.getPartition(), new HashSet<>(filter.getParts()));
            }
        });
        popupMenu.add(selectItem);
        JMenuItem unselectItem = new JMenuItem(NbBundle.getMessage(PartitionPanel.class, "PartitionPanel.action.unselectall"));
        unselectItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filter.unselectAll();
                refresh(filter.getPartition(), new HashSet<>());
            }
        });
        popupMenu.add(unselectItem);
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

        @Override
        public int getIconWidth() {
            return COLOR_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return COLOR_HEIGHT + 2;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x + 2, y, COLOR_WIDTH, COLOR_HEIGHT);
            g.setColor(BORDER_COLOR);
            g.drawRect(x + 2, y, COLOR_WIDTH, COLOR_HEIGHT);

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

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
