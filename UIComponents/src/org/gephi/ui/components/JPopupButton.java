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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Mathieu Bastian
 */
public class JPopupButton extends JButton {

    private ArrayList<JPopupButtonItem> items;
    private JPopupButtonItem selectedItem;
    private ChangeListener listener;

    public JPopupButton() {

        items = new ArrayList<JPopupButtonItem>();
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = createPopup();
                menu.show(JPopupButton.this, 0, getHeight());
            }
        });
    }

    public JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        for (final JPopupButtonItem item : items) {
            JRadioButtonMenuItem r = new JRadioButtonMenuItem(item.object.toString(), item.icon, item == selectedItem);
            r.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (item != selectedItem) {
                        selectedItem = item;
                        fireChangeEvent();
                    }
                }
            });
            menu.add(r);
        }
        return menu;
    }

    public void addItem(Object object, Icon icon) {
        items.add(new JPopupButtonItem(object, icon));
    }

    public void setSelectedItem(Object item) {
        for (JPopupButtonItem i : items) {
            if (i.object == item) {
                selectedItem = i;
                return;
            }
        }
        throw new IllegalArgumentException("This elemen doesn't exist.");
    }

    public Object getSelectedItem() {
        return selectedItem.object;
    }

    public void setChangeListener(ChangeListener changeListener) {
        listener = changeListener;
    }

    private void fireChangeEvent() {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(selectedItem.object));
        }
    }

    private class JPopupButtonItem {

        private final Object object;
        private final Icon icon;

        public JPopupButtonItem(Object object, Icon icon) {
            this.object = object;
            this.icon = icon;
        }
    }
}
