/*
Copyright 2008-2010 Gephi
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

package org.gephi.filters.plugin.attribute;


import java.util.List;


class ComparableArrayConverter {
    private ComparableArrayConverter() {}

    @SuppressWarnings("rawtypes")
    public static Comparable[] convert(Object[] objectArray) {
        Comparable[] comparableArray = new Comparable [objectArray.length];
        for (int index = 0; index < comparableArray.length; index++)
            comparableArray[index] = (Comparable) objectArray[index];

        return comparableArray;
    }

    @SuppressWarnings("rawtypes")
    public static Comparable[] convert(List<? extends Object> objectList) {
        Comparable[] compatableArray = new Comparable [objectList.size()];
        for (int index = 0; index < compatableArray.length; index++)
            compatableArray[index] = (Comparable) objectList.get(index);

        return compatableArray;
    }
}
