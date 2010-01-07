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
package org.gephi.filters.spi;

import javax.swing.Icon;

/**
 *
 * @author Mathieu Bastian
 */
public final class Category {

    private String name;
    private Icon icon;
    private Category parent;

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    public Category(String name, Icon icon, Category parent) {
        this.name = name;
        this.icon = icon;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return icon;
    }

    public Category getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Category) {
            Category cat = (Category) obj;
            if (cat.icon == icon && (cat.name == name || cat.name.equals(name)) && (cat.parent == parent || cat.parent.equals(parent))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.icon != null ? this.icon.hashCode() : 0);
        hash = 29 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        return hash;
    }
}
