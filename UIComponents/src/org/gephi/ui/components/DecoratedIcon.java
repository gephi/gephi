/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.components;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Decorated icon. A (smaller) decoration icon is placed at the top right.
 * 
 * @author Mathieu Bastian
 */
public class DecoratedIcon implements Icon {

    private final Icon orig;
    private final Icon decoration;
    private final DecorationController decorationController;

    public DecoratedIcon(Icon orig, Icon decoration) {
        this.orig = orig;
        this.decoration = decoration;
        this.decorationController = null;
    }

    public DecoratedIcon(Icon orig, Icon decoration, DecorationController decorationController) {
        this.orig = orig;
        this.decoration = decoration;
        this.decorationController = decorationController;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        orig.paintIcon(c, g, x, y);
        if (decorationController == null || decorationController.isDecorated()) {
            decoration.paintIcon(c, g, x + orig.getIconWidth() - decoration.getIconWidth(), y);
        }
    }

    public int getIconWidth() {
        return orig.getIconWidth();
    }

    public int getIconHeight() {
        return orig.getIconHeight();
    }

    public static interface DecorationController {

        public boolean isDecorated();
    }
}
