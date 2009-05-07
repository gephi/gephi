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
import org.gephi.data.laboratory.NodeTreeModel.TreeNode;
import org.gephi.graph.api.Node;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeRowModel implements RowModel {

    private Column[] columns;

    public NodeRowModel(AttributeColumn[] attributeColumns)
    {
        columns = new Column[attributeColumns.length+1];

        //Properties
        columns[0] = new Column(Column.PropertyColumn.ID);

        //Attributes
        int count=1;
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

    public Object getValueFor(Object node, int column) {
        TreeNode treeNode = (TreeNode)node;
        return columns[column].getValue(treeNode.getNode());
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

            ID
        };
        private AttributeColumn attributeColumn;
        private PropertyColumn propertyColumn;

        public Column(AttributeColumn attributeColumn) {
            this.attributeColumn = attributeColumn;
        }

        public Column(PropertyColumn propertyColumn) {
            this.propertyColumn = propertyColumn;
        }

        public Object getValue(Node node) {
            if (attributeColumn != null) {
                return ((AttributeRow)node.getAttributes()).getValue(attributeColumn);
            } else if (propertyColumn == PropertyColumn.ID) {
                return "" + node.getIndex();
            }
            return null;
        }

        public Class getColumnClass() {
            if (attributeColumn != null) {
                return attributeColumn.getAttributeType().getType();
            } else if (propertyColumn == PropertyColumn.ID) {
                return String.class;
            }
            return String.class;
        }

        public String getName() {
            if (attributeColumn != null) {
                return attributeColumn.getTitle();
            } else if (propertyColumn == PropertyColumn.ID) {
                return "ID";
            }
            return "null";
        }
    }
}
