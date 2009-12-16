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
package org.gephi.layout.api;

import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Mathieu Bastian
 */
public final class LayoutProperty {

    protected Property property;
    protected String category;

    LayoutProperty(Property property, String category) {
        this.property = property;
        this.category = category;
    }

    public Property getProperty() {
        return property;
    }

    public String getCategory() {
        return category;
    }

    public static LayoutProperty createProperty(Object obj, Class valueType, String propertyName, String propertyCategory, String propertyDescription, String getMethod, String setMethod) throws NoSuchMethodException {
        Property property = new PropertySupport.Reflection(
                obj, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);

        return new LayoutProperty(property, propertyCategory);
    }
}
