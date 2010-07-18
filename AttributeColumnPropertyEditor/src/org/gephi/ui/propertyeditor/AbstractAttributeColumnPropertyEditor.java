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
package org.gephi.ui.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
abstract class AbstractAttributeColumnPropertyEditor extends PropertyEditorSupport {

    public enum EditorClass {

        NODE, EDGE, NODEEDGE
    };

    public enum AttributeTypeClass {

        ALL, NUMBER, STRING
    };
    private AttributeColumn[] columns;
    private AttributeColumn selectedColumn;
    private EditorClass editorClass = EditorClass.NODE;
    private AttributeTypeClass attributeTypeClass = AttributeTypeClass.ALL;

    protected AbstractAttributeColumnPropertyEditor(EditorClass editorClass) {
        this.editorClass = editorClass;
    }

    protected AbstractAttributeColumnPropertyEditor(EditorClass editorClass, AttributeTypeClass attributeClass) {
        this.editorClass = editorClass;
        this.attributeTypeClass = attributeClass;
    }

    protected AttributeColumn[] getColumns() {
        List<AttributeColumn> cols = new ArrayList<AttributeColumn>();
        AttributeModel model = Lookup.getDefault().lookup(AttributeController.class).getModel();
        if (model != null) {
            if (editorClass.equals(EditorClass.NODE) || editorClass.equals(EditorClass.NODEEDGE)) {
                for (AttributeColumn column : model.getNodeTable().getColumns()) {
                    if (attributeTypeClass.equals(AttributeTypeClass.NUMBER) && isNumberColumn(column)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.ALL)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(attributeTypeClass.STRING) && isStringColumn(column)) {
                        cols.add(column);
                    }
                }
            }
            if (editorClass.equals(EditorClass.EDGE) || editorClass.equals(EditorClass.NODEEDGE)) {
                for (AttributeColumn column : model.getEdgeTable().getColumns()) {
                    if (attributeTypeClass.equals(AttributeTypeClass.NUMBER) && isNumberColumn(column)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.ALL)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(attributeTypeClass.STRING) && isStringColumn(column)) {
                        cols.add(column);
                    }
                }
            }
        }
        return cols.toArray(new AttributeColumn[0]);
    }

    @Override
    public String[] getTags() {
        columns = getColumns();
        //selectedColumn = columns[0];
        String[] tags = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            tags[i] = columns[i].getTitle();
        }
        return tags;
    }

    @Override
    public Object getValue() {
        return selectedColumn;
    }

    @Override
    public void setValue(Object value) {
        AttributeColumn column = (AttributeColumn) value;
        this.selectedColumn = column;
    }

    @Override
    public String getAsText() {
        if (selectedColumn == null) {
            return "---";
        }
        return selectedColumn.getTitle();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        for (AttributeColumn c : columns) {
            if (c.getTitle().equals(text)) {
                this.selectedColumn = c;
            }
        }
    }

    public boolean isNumberColumn(AttributeColumn column) {
        AttributeType type = column.getType();
        if (type == AttributeType.DOUBLE
                || type == AttributeType.FLOAT
                || type == AttributeType.INT
                || type == AttributeType.LONG) {
            return true;
        }
        return false;
    }

    public boolean isStringColumn(AttributeColumn column) {
        AttributeType type = column.getType();
        if (type == AttributeType.STRING) {
            return true;
        }
        return false;
    }
}
