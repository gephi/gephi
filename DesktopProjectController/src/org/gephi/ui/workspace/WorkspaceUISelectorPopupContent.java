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
package org.gephi.ui.workspace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceUISelectorPopupContent extends JPanel {

    public WorkspaceUISelectorPopupContent() {
        GridLayout grid = new GridLayout(0, 1);
        grid.setHgap(0);
        grid.setVgap(0);
        setLayout(grid);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void addListComponent(JComponent lst) {
        if (getComponentCount() > 0) {
            JComponent previous = (JComponent) getComponent(getComponentCount() - 1);
            previous.setBorder(new BottomLineBorder());
        }
        lst.setBorder(BorderFactory.createEmptyBorder());
        add(lst);
    }

    private static class BottomLineBorder implements Border {

        private Insets ins = new Insets(0, 0, 1, 0);
        private Color col = new Color(221, 229, 248);

        public BottomLineBorder() {
        }

        public Insets getBorderInsets(Component c) {
            return ins;
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(col);
            g.drawRect(x, y + height - 2, width, 1);
            g.setColor(old);
        }
    }
}
