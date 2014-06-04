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

            @Override
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

                @Override
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
