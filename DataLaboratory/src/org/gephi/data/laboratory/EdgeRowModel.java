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
package org.gephi.data.laboratory;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Edge;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeRowModel implements RowModel {

    private Column[] columns;

    public EdgeRowModel(AttributeColumn[] attributeColumns)
    {
        columns = new Column[attributeColumns.length+3];

        //Properties
        columns[0] = new Column(Column.PropertyColumn.ID);
        columns[1] = new Column(Column.PropertyColumn.SOURCE);
        columns[2] = new Column(Column.PropertyColumn.TARGET);

        //Attributes
        int count=3;
        for(int i=0;i<attributeColumns.length;i++)
        {
            AttributeColumn attCol = attributeColumns[i];
            columns[count] = new Column(attCol);
            count++;
        }
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueFor(Object edge, int column) {
        return columns[column].getValue((Edge)edge);
    }

    public Class getColumnClass(int column) {
        return columns[column].getColumnClass();
    }

    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public void setValueFor(Object node, int column, Object value) {
    }

    public String getColumnName(int column) {
        return columns[column].getName();
    }

    private static class Column {

        public enum PropertyColumn {

            ID,SOURCE,TARGET
        };
        private AttributeColumn attributeColumn;
        private PropertyColumn propertyColumn;

        public Column(AttributeColumn attributeColumn) {
            this.attributeColumn = attributeColumn;
        }

        public Column(PropertyColumn propertyColumn) {
            this.propertyColumn = propertyColumn;
        }

        public Object getValue(Edge edge) {
           if (attributeColumn != null) {
                if(edge.getEdgeData().getAttributes()!=null)
                {
                    AttributeRow row = ((AttributeRow)edge.getEdgeData().getAttributes());
                    return row.getValue(attributeColumn);
                }
            } else if (propertyColumn == PropertyColumn.ID) {
                return "";
            } else if (propertyColumn == PropertyColumn.SOURCE) {
                return ""+edge.getSource().getNodeData().getId();
            } else if (propertyColumn == PropertyColumn.TARGET) {
                return ""+edge.getTarget().getNodeData().getId();
            }
            return null;
        }

        public Class getColumnClass() {
            if (attributeColumn != null) {
                return attributeColumn.getAttributeType().getType();
            } else if (propertyColumn == PropertyColumn.ID) {
                return String.class;
            } else if (propertyColumn == PropertyColumn.SOURCE) {
                return String.class;
            } else if (propertyColumn == PropertyColumn.TARGET) {
                return String.class;
            }
            return String.class;
        }

        public String getName() {
            if (attributeColumn != null) {
                return attributeColumn.getTitle();
            } else if (propertyColumn == PropertyColumn.ID) {
                return "ID";
            } else if (propertyColumn == PropertyColumn.SOURCE) {
                return "Source";
            } else if (propertyColumn == PropertyColumn.TARGET) {
                return "Target";
            }
            return "null";
        }
    }
}
