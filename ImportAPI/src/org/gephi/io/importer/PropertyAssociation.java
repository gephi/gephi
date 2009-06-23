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
package org.gephi.io.importer;

/**
 *
 * @author Mathieu Bastian
 */
public final class PropertyAssociation<Property> {

    private final Property property;
    private final String title;
    private volatile int hashCode = 0;      //Cache hashcode

    public PropertyAssociation(Property property, String title) {
        this.property = property;
        this.title = title;
    }

    public Property getProperty() {
        return property;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PropertyAssociation)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        PropertyAssociation foreign = (PropertyAssociation) obj;
        if (foreign.title.equals(title) && foreign.property.equals(property)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int res = 17;
            res = 37 * res + title.hashCode();
            res = 37 * res + property.hashCode();
            hashCode = res;
        }
        return hashCode;
    }
}
