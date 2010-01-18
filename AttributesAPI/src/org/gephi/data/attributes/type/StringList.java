/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Sebastien Heymann
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
package org.gephi.data.attributes.type;

import java.util.Arrays;
import org.gephi.data.attributes.api.AttributeType;

/**
 * Complex type that define a list of string items. Can be created from a string
 * array or by using separators.
 * <p>
 * String list is useful when, for a particular type, the number of string
 * that define an element is not known by advance.
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 * @see AttributeType
 */
public final class StringList {

    private final String[] list;
    private volatile int hashCode = 0;      //Cache hashcode

    /**
     * Create a new string list with the given items.
     *
     * @param list      the list of string items
     */
    public StringList(String[] list) {
        if (list == null) {
            throw new NullPointerException();
        }
        this.list = Arrays.copyOf(list, list.length);
    }

    /**
     * Create a new string list with items found using given separators.
     *
     * @param value     a string with separators defined in <code>separator</code>
     * @param separator the separators chars that are to be used to split
     *                  <code>value</code>
     */
    public StringList(String value, String separator) {
        if (value == null || separator == null) {
            throw new NullPointerException();
        }

        this.list = value.split(separator);
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].trim();
        }
    }

    /**
     * Create a new string list with items found in the given value. Default
     * separators <code>,|;</code> are used to split the string in a list.
     *
     * @param value     a string with default separators
     */
    public StringList(String value) {
        if (value == null) {
            throw new NullPointerException();
        }

        this.list = value.split(",|;");
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].trim();
        }
    }

    /**
     * Returns the size of the string list.
     *
     * @return          the size of the list
     */
    public int size() {
        return list.length;
    }

    /**
     * Returns the item at the specified <code>index</code>. May return
     * <code>null</code> if <code>index</code> is out of range.
     *
     * @param index     the position in the string list
     * @return          the item at the specified position, or <code>null</code>
     */
    public String getString(int index) {
        if (index >= list.length) {
            return null;
        }
        return list[index];
    }

    /**
     * Returns <code>true</code> if any item in the list is <b>equal</b> to
     * <code>value</code>.
     *
     * @param value     the item that is to be queried
     * @return          <code>true</code> if the string list contains this value,
     *                  <code>false</code> otherwise
     */
    public boolean contains(String value) {
        for (int i = 0; i < list.length; i++) {
            if (value.equals(list[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the items in the order they were inserted, separated by comas.
     *
     * @return          the items separated by comas
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            builder.append(list[i]);
            builder.append(',');
        }
        if (list.length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StringList)) {
            return false;
        }
        StringList s = (StringList) obj;
        if (s.list.length != this.list.length) {
            return false;
        }
        for (int i = 0; i < list.length; i++) {
            if (this.list[i] != s.list[i]) {
                if (!this.list[i].equals(s.list[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int hash = 7;
            for (int i = 0; i < list.length; i++) {
                hash = 53 * hash + (this.list[i] != null ? this.list[i].hashCode() : 0);
            }
            hashCode = hash;
        }
        return hashCode;
    }
}
