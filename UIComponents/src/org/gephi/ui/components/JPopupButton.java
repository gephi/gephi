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
import javax.swing.JButton;
import javax.swing.JPopupMenu;

/**
 *
 * @author Mathieu Bastian
 */
public class JPopupButton extends JButton {

    public JPopupButton() {

        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = new JPopupMenu();
                createPopup(menu);
                menu.show(JPopupButton.this, 0, getHeight());
            }
        });
    }

    public void createPopup(JPopupMenu popupMenu) {
    }
}
