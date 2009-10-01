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

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
public final class StringList {

    private final String[] list;
    private volatile int hashCode = 0;      //Cache hashcode

    public StringList(String[] list) {
        if (list == null) {
            throw new NullPointerException();
        }
        this.list = Arrays.copyOf(list, list.length);
    }

    public StringList(String value, String separator) {
        if (value == null || separator == null) {
            throw new NullPointerException();
        }

        this.list = value.split(separator);
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].trim();
        }
    }

    public StringList(String value) {
        if (value == null) {
            throw new NullPointerException();
        }

        this.list = value.split(",|;");
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].trim();
        }
    }

    public int size() {
        return list.length;
    }

    public String getString(int index) {
        if (index >= list.length) {
            return null;
        }
        return list[index];
    }

    public boolean contains(String value) {
        for (int i = 0; i < list.length; i++) {
            if(value.equals(list[i])) {
                return true;
            }
        }
        return false;
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
