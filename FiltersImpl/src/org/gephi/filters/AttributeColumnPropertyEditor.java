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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeColumnPropertyEditor extends PropertyEditorSupport {
    
    private AttributeColumn column;

    @Override
    public void setValue(Object value) {
        this.column = (AttributeColumn) column;
    }

    @Override
    public Object getValue() {
        return column;
    }

    @Override
    public String getAsText() {
        if (column != null) {
            AttributeModel model = Lookup.getDefault().lookup(AttributeController.class).getModel();
            if (model.getNodeTable().hasColumn(column.getTitle())) {
                return "NODE*-*" + column.getId() + "*-*" + column.getType().toString();
            } else if (model.getEdgeTable().hasColumn(column.getTitle())) {
                return "EDGE*-*" + column.getId() + "*-*" + column.getType().toString();
            }
        }
        return "null";

    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!text.equals("null")) {
            AttributeModel model = Lookup.getDefault().lookup(AttributeController.class).getModel();
            String[] arr = text.split(" - ");
            if (arr[0].equals("NODE")) {
                column = model.getNodeTable().getColumn(arr[1], AttributeType.valueOf(arr[2]));
            } else if (arr[0].equals("EDGE")) {
                column = model.getEdgeTable().getColumn(arr[1], AttributeType.valueOf(arr[2]));
            }
        }
    }
}
