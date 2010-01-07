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

import java.lang.reflect.InvocationTargetException;
import org.gephi.filters.api.FilterController;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public final class FilterProperty {

    protected Property property;
    protected Filter filter;

    FilterProperty(Filter filter) {
        this.filter = filter;
    }

    public String getName() {
        return property.getDisplayName();
    }

    public Object getValue() {
        try {
            return property.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Property getProperty() {
        return property;
    }

    public Filter getFilter() {
        return filter;
    }

    public static FilterProperty createProperty(Filter filter, Class valueType, String propertyName, String getMethod, String setMethod) throws NoSuchMethodException {
        final FilterProperty filterProperty = new FilterProperty(filter);
        Property property = new PropertySupport.Reflection(
                filter, valueType, getMethod, setMethod) {

            @Override
            public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                super.setValue(val);
                Lookup.getDefault().lookup(FilterController.class).propertyChanged(filterProperty);
            }
        };
        property.setName(propertyName);
        filterProperty.property = property;

        return filterProperty;
    }

    public static FilterProperty createProperty(Filter filter, Class valueType, String fieldName) throws NoSuchMethodException {
        if (valueType == Boolean.class) {
            valueType = boolean.class;
        }
        final FilterProperty filterProperty = new FilterProperty(filter);
        Property property = new PropertySupport.Reflection(filter, valueType, fieldName) {

            @Override
            public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                super.setValue(val);
                Lookup.getDefault().lookup(FilterController.class).propertyChanged(filterProperty);
            }
        };
        property.setName(fieldName);
        filterProperty.property = property;

        return filterProperty;
    }
}
