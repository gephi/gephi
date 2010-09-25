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

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Martin  Škurla
 */
public abstract class AttributeUtils {

    public abstract boolean isNodeColumn(AttributeColumn column);

    public abstract boolean isEdgeColumn(AttributeColumn column);

    public abstract boolean isColumnOfType(AttributeColumn column, AttributeType type);

    public abstract boolean areAllColumnsOfType(AttributeColumn[] columns, AttributeType type);

    public abstract boolean areAllColumnsOfSameType(AttributeColumn[] columns);

    public abstract boolean isStringColumn(AttributeColumn column);

    public abstract boolean areAllStringColumns(AttributeColumn[] columns);

    public abstract boolean isNumberColumn(AttributeColumn column);

    public abstract boolean areAllNumberColumns(AttributeColumn[] columns);

    public abstract boolean isNumberListColumn(AttributeColumn column);

    public abstract boolean areAllNumberListColumns(AttributeColumn[] columns);

    public abstract boolean isNumberOrNumberListColumn(AttributeColumn column);

    public abstract boolean areAllNumberOrNumberListColumns(AttributeColumn[] columns);

    public abstract boolean isDynamicNumberColumn(AttributeColumn column);

    public abstract boolean areAllDynamicNumberColumns(AttributeColumn[] columns);

    public abstract AttributeColumn[] getNumberColumns(AttributeTable table);

    public abstract AttributeColumn[] getStringColumns(AttributeTable table);

    @SuppressWarnings("rawtypes")
    public abstract Comparable getMin(AttributeColumn column, Comparable[] values);

    @SuppressWarnings("rawtypes")
    public abstract Comparable getMax(AttributeColumn column, Comparable[] values);

    public static synchronized AttributeUtils getDefault() {
        return Lookup.getDefault().lookup(AttributeUtils.class);
    }

    /**
     * Used for export (writes XML date strings).
     *
     * @param d a double to convert from
     *
     * @return an XML date string.
     *
     * @throws IllegalArgumentException if {@code d} is infinite.
     */
    public static String getXMLDateStringFromDouble(double d) {
        try {
            DatatypeFactory dateFactory = DatatypeFactory.newInstance();
            if (d == Double.NEGATIVE_INFINITY) {
                return "-Infinity";
            } else if (d == Double.POSITIVE_INFINITY) {
                return "Infinity";
            }
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis((long) d);
            return dateFactory.newXMLGregorianCalendar(gc).toXMLFormat().substring(0, 23);
        } catch (DatatypeConfigurationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }
}
