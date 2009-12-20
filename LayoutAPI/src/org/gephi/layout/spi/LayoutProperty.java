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
package org.gephi.layout.spi;

import java.beans.PropertyEditor;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;

/**
 * Properties for layout algorithms that are used by the UI to fill the property
 * sheet and thus allow user edit.
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

    /**
     * Return the underlying <code>Property</code>.
     * @return the instance of <code>Node.Property</code>
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Return the category of the property
     */
    public String getCategory() {
        return category;
    }

    /**
     * Create a property.
     * @param layout The layout instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyDescription A description string for the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static LayoutProperty createProperty(Layout layout, Class valueType, String propertyName, String propertyCategory, String propertyDescription, String getMethod, String setMethod) throws NoSuchMethodException {
        Property property = new PropertySupport.Reflection(
                layout, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);

        return new LayoutProperty(property, propertyCategory);
    }

    /**
     * Create a property, with a particular {@link PropertyEditor}. A particular
     * editor must be specified when the property type don't have a registered
     * editor class.
     * @param layout The layout instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyDescription A description string for the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @param editorClass A <code>PropertyEditor</code> class for the given type
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static LayoutProperty createProperty(Layout layout, Class valueType, String propertyName, String propertyCategory, String propertyDescription, String getMethod, String setMethod, Class<? extends PropertyEditor> editorClass) throws NoSuchMethodException {
        PropertySupport.Reflection property = new PropertySupport.Reflection(
                layout, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);
        property.setPropertyEditorClass(editorClass);

        return new LayoutProperty(property, propertyCategory);
    }
}
