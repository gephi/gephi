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

import java.util.ArrayList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDataTable {

    private JXTreeTable treeTable;
    private PropertyNodeDataColumn[] propertiesColumns;

    public NodeDataTable() {
        treeTable = new JXTreeTable();
        treeTable.setRootVisible(false);
        treeTable.setHighlighters(HighlighterFactory.createAlternateStriping());

        propertiesColumns = new PropertyNodeDataColumn[2];

        propertiesColumns[0] = new PropertyNodeDataColumn("Label") {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(ImmutableTreeNode node) {
                Node graphNode = node.getNode();
                return graphNode.getNodeData().getLabel();
            }

            @Override
            public void setValueFor(ImmutableTreeNode node, Object value) {
                Node graphNode = node.getNode();
                graphNode.getNodeData().setLabel((String) value);
            }

            @Override
            public boolean isEditable() {
                return true;
            }
        };

        propertiesColumns[1] = new PropertyNodeDataColumn("ID") {

            @Override
            public Class getColumnClass() {
                return Integer.class;
            }

            @Override
            public Object getValueFor(ImmutableTreeNode node) {
                Node graphNode = node.getNode();
                return graphNode.getId();
            }
        };
    }

    public JXTreeTable getTreeTable() {
        return treeTable;
    }

    public void refreshModel(HierarchicalGraph graph, AttributeColumn[] cols) {
        ArrayList<NodeDataColumn> columns = new ArrayList<NodeDataColumn>();

        for (PropertyNodeDataColumn p : propertiesColumns) {
            columns.add(p);
        }

        for (AttributeColumn c : cols) {
            columns.add(new AttributeNodeDataColumn(c));
        }

        NodeDataTreeTableModel model = new NodeDataTreeTableModel(graph.wrapToTreeNode(), columns.toArray(new NodeDataColumn[0]));
        treeTable.setTreeTableModel(model);
    }

    private class NodeDataTreeTableModel implements TreeTableModel {

        private ImmutableTreeNode root;
        private NodeDataColumn[] columns;

        public NodeDataTreeTableModel(ImmutableTreeNode root, NodeDataColumn[] columns) {
            this.root = root;
            this.columns = columns;
        }

        public Class<?> getColumnClass(int arg0) {
            return columns[arg0].getColumnClass();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnName(int arg0) {
            return columns[arg0].getColumnName();
        }

        public int getHierarchicalColumn() {
            return 0;
        }

        public Object getValueAt(Object arg0, int arg1) {
            ImmutableTreeNode node = (ImmutableTreeNode) arg0;
            return columns[arg1].getValueFor(node);
        }

        public boolean isCellEditable(Object arg0, int arg1) {
            return columns[arg1].isEditable();
        }

        public void setValueAt(Object arg0, Object arg1, int arg2) {
            ImmutableTreeNode node = (ImmutableTreeNode) arg1;
            columns[arg2].setValueFor(node, arg0);
        }

        public Object getRoot() {
            return root;
        }

        public Object getChild(Object parent, int index) {
            return ((ImmutableTreeNode) parent).getChildAt(index);
        }

        public int getChildCount(Object parent) {
            return ((ImmutableTreeNode) parent).getChildCount();
        }

        public boolean isLeaf(Object node) {
            return ((ImmutableTreeNode) node).isLeaf();
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getIndexOfChild(Object parent, Object child) {
            return ((ImmutableTreeNode) parent).getIndex(((TreeNode) child));
        }

        public void addTreeModelListener(TreeModelListener l) {
        }

        public void removeTreeModelListener(TreeModelListener l) {
        }
    }

    private static interface NodeDataColumn {

        public Class getColumnClass();

        public String getColumnName();

        public Object getValueFor(ImmutableTreeNode node);

        public void setValueFor(ImmutableTreeNode node, Object value);

        public boolean isEditable();
    }

    private static class AttributeNodeDataColumn implements NodeDataTable.NodeDataColumn {

        private AttributeColumn column;

        public AttributeNodeDataColumn(AttributeColumn column) {
            this.column = column;
        }

        public Class getColumnClass() {
            return column.getAttributeType().getType();
        }

        public String getColumnName() {
            return column.getTitle();
        }

        public Object getValueFor(ImmutableTreeNode node) {
            Node graphNode = node.getNode();
            if (graphNode.getId() == -1) {
                return null;
            }
            return graphNode.getNodeData().getAttributes().getValue(column.getIndex());
        }

        public void setValueFor(ImmutableTreeNode node, Object value) {
            Node graphNode = node.getNode();
            graphNode.getNodeData().getAttributes().setValue(column.getIndex(), value);
        }

        public boolean isEditable() {
            return true;
        }
    }

    private static abstract class PropertyNodeDataColumn implements NodeDataTable.NodeDataColumn {

        private String name;

        public PropertyNodeDataColumn(String name) {
            this.name = name;
        }

        public abstract Class getColumnClass();

        public String getColumnName() {
            return name;
        }

        public abstract Object getValueFor(ImmutableTreeNode node);

        public void setValueFor(ImmutableTreeNode node, Object value) {
        }

        public boolean isEditable() {
            return false;
        }
    }
}
