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
package org.gephi.filters;

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterQueryImpl extends AbstractQueryImpl {

    private Parameters[] parameters;
    private Filter filter;
    private String name;

    public FilterQueryImpl(Filter filter) {
        this.filter = filter;
        this.name = filter.getName();
        updateParameters();
    }

    public void updateParameters() {
        FilterProperty[] properties = filter.getProperties();
        parameters = new Parameters[properties == null ? 0 : properties.length];
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                parameters[i] = new Parameters(i, properties[i].getValue());
            }
        }
    }

    @Override
    public int getChildrenSlotsCount() {
        return 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public int getPropertiesCount() {
        return parameters.length;
    }

    public String getPropertyName(int index) {
        return parameters[index].getKey();
    }

    public Object getPropertyValue(int index) {
        return parameters[index].getValue();
    }

    public Filter getFilter() {
        return filter;
    }

    private class Parameters {

        private int index;
        private Object value;

        public Parameters(int index, Object value) {
            this.index = index;
            this.value = value;
        }

        public String getKey() {
            return filter.getProperties()[index].getName();
        }

        public Object getValue() {
            return value;
        }
    }
}
