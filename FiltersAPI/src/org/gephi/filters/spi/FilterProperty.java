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

import java.beans.PropertyEditor;
import org.gephi.filters.api.PropertyExecutor;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Properties for filters. All editable properties of a filter must be used
 * through this class, especially setting value should be done by using
 * {@link #setValue(java.lang.Object) }.
 * <p>
 * The role of this class is to define filter's properties in order value changes
 * can be tracked by the system, UI can be generated and values correctly saved
 * in projects file.
 *
 * @author Mathieu Bastian
 */
public final class FilterProperty {

    protected PropertySupport.Reflection property;
    protected Filter filter;
    protected PropertyExecutor propertyExecutor;

    FilterProperty(Filter filter) {
        this.filter = filter;
        propertyExecutor = Lookup.getDefault().lookup(PropertyExecutor.class);
    }

    /**
     * Returns property's name
     * @return      property's name
     */
    public String getName() {
        return property.getDisplayName();
    }

    /**
     * Returns property's value, can be <code>null</code>
     * @return      property's value
     */
    public Object getValue() {
        try {
            return property.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Set property's value. The type of <code>value</code> must match with this
     * property value type.
     * @param value the value that is to be set
     */
    public void setValue(Object value) {
        if (propertyExecutor != null) {
            propertyExecutor.setValue(this, value, new PropertyExecutor.Callback() {

                public void setValue(Object value) {
                    try {
                        property.setValue(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            try {
                property.setValue(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the <code>PropertyEditor</code> associated to the property value.
     * @return      the property editor
     */
    public PropertyEditor getPropertyEditor() {
        return property.getPropertyEditor();
    }

    /**
     * Sets the property editor class. The class must implement
     * {@link PropertyEditor}.
     * @param clazz the property editor class
     */
    public void setPropertyEditorClass(Class<? extends PropertyEditor> clazz) {
        property.setPropertyEditorClass(clazz);
    }

    /**
     * Returns the property's value type.
     * @return      the value type
     */
    public Class getValueType() {
        return property.getValueType();
    }

    /**
     * Returns the filter instance this property is associated to.
     * @return      the filter this property belongs to
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Create a property.
     * @param filter The filter instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static FilterProperty createProperty(Filter filter, Class valueType, String propertyName, String getMethod, String setMethod) throws NoSuchMethodException {
        final FilterProperty filterProperty = new FilterProperty(filter);
        PropertySupport.Reflection property = new PropertySupport.Reflection(filter, valueType, getMethod, setMethod);
        property.setName(propertyName);
        filterProperty.property = property;

        return filterProperty;
    }

    /**
     * Create a property.
     * @param filter The filter instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param filedName The Java field name of the property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static FilterProperty createProperty(Filter filter, Class valueType, String fieldName) throws NoSuchMethodException {
        if (valueType == Boolean.class) {
            valueType = boolean.class;
        }
        final FilterProperty filterProperty = new FilterProperty(filter);
        PropertySupport.Reflection property = new PropertySupport.Reflection(filter, valueType, fieldName);
        property.setName(fieldName);
        filterProperty.property = property;

        return filterProperty;
    }
}
