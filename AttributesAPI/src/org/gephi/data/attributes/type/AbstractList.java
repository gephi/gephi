/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>
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

/**
 *
 * @author Martin Škurla
 * @param <T>
 */
public abstract class AbstractList<T> {

    public static final String DEFAULT_SEPARATOR = ",|;";
    protected final T[] list;
    private volatile int hashCode = 0;

    public AbstractList(String input, Class<T> finalType) {
        this(input, DEFAULT_SEPARATOR, finalType);
    }

    public AbstractList(String input, String separator, Class<T> finalType) {
        this(TypeConvertor.<T>createArrayFromString(input, separator, finalType));
    }

    public AbstractList(T[] array) {
        this.list = Arrays.copyOf(array, array.length);
    }

    public int size() {
        return list.length;
    }

    public T getItem(int index) {
        if (index >= list.length) {
            return null;
        }

        return list[index];
    }

    public boolean contains(T value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

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
        if (!(obj instanceof AbstractList<?>)) {
            return false;
        }

        AbstractList<?> s = (AbstractList<?>) obj;

        if (s.size() != this.size()) {
            return false;
        }

        for (int i = 0; i < list.length; i++) {
            if (this.getItem(i) != s.getItem(i)) {
                if (!this.getItem(i).equals(s.getItem(i))) {
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

