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
package org.gephi.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jdesktop.swingx.JXBusyLabel;

/**
 *
 * @author Mathieu Bastian
 */
public class JLazyComboBox extends JComboBox {

    public static String BUSY_LABEL = "BusyLabel";

    public JLazyComboBox() {
        putClientProperty(BUSY_LABEL, "Fetching data...");
        //setPrototypeDisplayValue("test");

        addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (!(dataModel instanceof LazyComboBoxModel)) {
                    return;
                }
                if (((LazyComboBoxModel) dataModel).needRefresh() && !((BusyRenderer) renderer).isBusy()) {
                    ((BusyRenderer) renderer).setBusy(true);
                    Thread t = new Thread(new Runnable() {

                        public void run() {
                            ((LazyComboBoxModel) dataModel).populate();
                            updatePopupSelection();
                        }
                    });
                    t.start();
                }
            }

            private void updatePopupSelection() {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        ((BusyRenderer) renderer).setBusy(false);
                        setModel(((LazyComboBoxModel) dataModel));
                        setPopupVisible(false);
                        setPopupVisible(true);
                    }
                });
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        setRenderer(new BusyRenderer(renderer));
    }

    private class BusyRenderer extends JXBusyLabel implements ListCellRenderer {

        private ListCellRenderer delegate;

        public BusyRenderer(ListCellRenderer delegate) {
            super(new Dimension(16, 16));
            setText((String) JLazyComboBox.this.getClientProperty(BUSY_LABEL));
            this.delegate = delegate;
        }

        @Override
        protected void frameChanged() {
            JLazyComboBox.this.revalidate();
            JLazyComboBox.this.repaint();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isBusy()) {
                return this;
            }
            Component comp = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            return comp;
        }
    }

    public static abstract class LazyComboBoxModel extends DefaultComboBoxModel {

        private boolean reset = true;

        public LazyComboBoxModel() {
            setSelectedItem(getInitialSelection());
        }

        void populate() {
            final Object[] list = loadItems();
            reset = false;
            removeAllElements();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    addElement(getInitialSelection());
                    for (Object o : list) {
                        addElement(o);
                    }
                }
            });
        }

        boolean needRefresh() {
            return reset;
        }

        @Override
        public Object getSelectedItem() {
            return getSize() == 0 ? getInitialSelection() : super.getSelectedItem();
        }

        public void setReset(boolean reset) {
            this.reset = reset;
        }

        protected abstract Object[] loadItems();

        protected abstract Object getInitialSelection();
    }
}
