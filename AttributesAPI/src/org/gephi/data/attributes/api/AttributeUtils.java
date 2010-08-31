/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla
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
package org.gephi.data.attributes.api;

import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Martin  Škurla
 */
public abstract class AttributeUtils {

    public abstract boolean isNodeColumn(AttributeColumn column);

    public abstract boolean isEdgeColumn(AttributeColumn column);

    public abstract boolean isStringColumn(AttributeColumn column);

    public abstract boolean isNumberColumn(AttributeColumn column);

    public abstract AttributeColumn[] getNumberColumns(AttributeTable table);

    public abstract AttributeColumn[] getStringColumns(AttributeTable table);

    @SuppressWarnings("rawtypes")
    public abstract Comparable getMin(AttributeColumn column, Comparable[] values);

    @SuppressWarnings("rawtypes")
    public abstract Comparable getMax(AttributeColumn column, Comparable[] values);

    public static synchronized AttributeUtils getDefault() {
        return Lookup.getDefault().lookup(AttributeUtils.class);
    }
}
