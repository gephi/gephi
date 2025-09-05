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

package org.gephi.ui.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ImageUtilities;

/**
 * @author Mathieu Bastian
 */
public class JPopupButton extends JButton {

    private final ArrayList<JPopupButtonItem> items;
    private JPopupButtonItem selectedItem;
    private ChangeListener listener;

    public JPopupButton() {
        items = new ArrayList<>();
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = createPopup();
                menu.show(JPopupButton.this, 0, getHeight());
            }
        });
    }

    @Override
    public void setIcon(Icon defaultIcon) {
        IconWithArrow iconWithArrow = new IconWithArrow(defaultIcon, false);
        super.setIcon(iconWithArrow);

        // Ensure a proper disabled icon is available (grays out both base icon and arrow)
        Icon disabled = UIManager.getLookAndFeel().getDisabledIcon(this, iconWithArrow);
        if (disabled == null) {
            disabled = new ImageIcon(GrayFilter.createDisabledImage(ImageUtilities.icon2Image(iconWithArrow)));
        }
        super.setDisabledIcon(disabled);
        super.setDisabledSelectedIcon(disabled);
    }

    public JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        for (final JPopupButtonItem item : items) {
            final JRadioButtonMenuItem r = new JRadioButtonMenuItem(item.toString(), item.icon, item == selectedItem);
            r.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (item != selectedItem) {
                        selectedItem = item;
                        fireChangeEvent();
                    }
                }
            });

            // Use background highlight for hover/armed state to match platform look
            r.setOpaque(true);
            final Color defaultBackground = UIManager.getColor("MenuItem.background") != null
                    ? UIManager.getColor("MenuItem.background") : r.getBackground();
            final Color selectionBackground = UIManager.getColor("MenuItem.selectionBackground") != null
                    ? UIManager.getColor("MenuItem.selectionBackground") : defaultBackground;

            // Persist highlight for the currently selected item
            if (item == selectedItem) {
                r.setBackground(selectionBackground);
            }

            // Foreground color should reflect selection/hover state
            final Color defaultForeground = UIManager.getColor("MenuItem.foreground") != null
                    ? UIManager.getColor("MenuItem.foreground") : r.getForeground();
            final Color selectionForeground = UIManager.getColor("MenuItem.selectionForeground") != null
                    ? UIManager.getColor("MenuItem.selectionForeground") : defaultForeground;
            r.setForeground(item == selectedItem ? selectionForeground : defaultForeground);
            r.setSelected(item == selectedItem);

            r.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    boolean armed = r.getModel().isArmed();
                    boolean active = armed || item == selectedItem;
                    r.setBackground(active ? selectionBackground : defaultBackground);
                    r.setForeground(active ? selectionForeground : defaultForeground);
                }
            });
            menu.add(r);
        }
        return menu;
    }

    public void addItem(Object object, Icon icon) {
        items.add(new JPopupButtonItem(object, icon, null));
    }

    public void addItem(Object object, Icon icon, String displayString) {
        items.add(new JPopupButtonItem(object, icon, displayString));
    }

    public Object getSelectedItem() {
        return selectedItem.object;
    }

    public void setSelectedItem(Object item) {
        for (JPopupButtonItem i : items) {
            if (i.object == item) {
                selectedItem = i;
                return;
            }
        }
        throw new IllegalArgumentException("This element doesn't exist.");
    }

    public void setChangeListener(ChangeListener changeListener) {
        listener = changeListener;
    }

    private void fireChangeEvent() {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(selectedItem.object));
        }
    }

    private static class JPopupButtonItem {

        private final Object object;
        private final Icon icon;
        private final String displayString;

        public JPopupButtonItem(Object object, Icon icon, String displayString) {
            this.object = object;
            this.icon = icon;
            this.displayString = displayString;
        }

        @Override
        public String toString() {
            if (displayString != null) {
                return displayString;
            } else {
                return object.toString();
            }
        }
    }
}
