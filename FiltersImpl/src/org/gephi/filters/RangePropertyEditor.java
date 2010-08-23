/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.filters;

import java.beans.PropertyEditorSupport;
import org.gephi.filters.api.Range;

/**
 *
 * @author Mathieu Bastian
 */
public class RangePropertyEditor extends PropertyEditorSupport {

    private Range range;

    @Override
    public void setValue(Object value) {
        this.range = (Range) value;
    }

    @Override
    public Object getValue() {
        return range;
    }

    @Override
    public String getAsText() {
        if (range != null) {
            return range.getRangeType().getSimpleName() + " - " + range.toString();
        } else {
            return "null";
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!text.equals("null")) {
            String[] arr = text.split(" - ");
            if (arr[0].equals("Float")) {
                range = new Range(Float.parseFloat(arr[1]), Float.parseFloat(arr[2]));
            } else if (arr[0].equals("Double")) {
                range = new Range(Double.parseDouble(arr[1]), Double.parseDouble(arr[2]));
            } else if (arr[0].equals("Integer")) {
                range = new Range(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
            } else if (arr[0].equals("Long")) {
                range = new Range(Long.parseLong(arr[1]), Long.parseLong(arr[2]));
            }
        }
    }
}
