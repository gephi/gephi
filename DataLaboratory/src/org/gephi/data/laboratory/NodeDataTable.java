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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.etable.QuickFilter;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDataTable {

    private Outline outlineTable;
    private QuickFilter quickFilter;
    private Pattern pattern;
    private DataTablesModel dataTablesModel;

    public NodeDataTable() {
        outlineTable = new Outline();

        quickFilter = new QuickFilter() {

            public boolean accept(Object value) {
                if (value == null) {
                    return false;
                }
                if (value instanceof ImmutableTreeNode) {
                    return pattern.matcher(((ImmutableTreeNode) value).getNode().getNodeData().getLabel()).find();
                }
                return pattern.matcher(value.toString()).find();
            }
        };
    }

    public Outline getOutlineTable() {
        return outlineTable;
    }

    public boolean setFilter(String regularExpr, int columnIndex) {
        try {
            pattern = Pattern.compile(regularExpr, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return false;
        }
        outlineTable.setQuickFilter(columnIndex, quickFilter);
        return true;
    }

    public void refreshModel(HierarchicalGraph graph, AttributeColumn[] cols, final DataTablesModel dataTablesModel) {
        NodeTreeModel nodeTreeModel = new NodeTreeModel(graph.wrapToTreeNode());
        final OutlineModel mdl = DefaultOutlineModel.createOutlineModel(nodeTreeModel, new NodeRowModel(cols), true);
        outlineTable.setRootVisible(false);
        outlineTable.setRenderDataProvider(new NodeRenderer());

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    outlineTable.setModel(mdl);
                    NodeDataTable.this.dataTablesModel = dataTablesModel;
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static class NodeTreeModel implements TreeModel {

        private ImmutableTreeNode root;

        public NodeTreeModel(ImmutableTreeNode root) {
            this.root = root;
        }

        public Object getRoot() {
            return root;
        }

        public Object getChild(Object parent, int index) {
            TreeNode node = (TreeNode) parent;
            return node.getChildAt(index);
        }

        public int getChildCount(Object parent) {

            TreeNode node = (TreeNode) parent;
            return node.getChildCount();

        }

        public boolean isLeaf(Object node) {

            TreeNode n = (TreeNode) node;
            return n.isLeaf();

        }

        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        public int getIndexOfChild(Object parent, Object child) {
            if (parent == null || child == null) {
                return -1;
            }
            TreeNode node = (TreeNode) parent;
            return node.getIndex((TreeNode) child);
        }

        public void addTreeModelListener(TreeModelListener l) {
        }

        public void removeTreeModelListener(TreeModelListener l) {
        }
    }

    private static class NodeRowModel implements RowModel {

        private NodeDataColumn[] columns;

        public NodeRowModel(AttributeColumn[] attributeColumns) {

            ArrayList<NodeDataColumn> cols = new ArrayList<NodeDataColumn>();
            for (AttributeColumn c : attributeColumns) {
                cols.add(new AttributeNodeDataColumn(c));
            }
            columns = cols.toArray(new NodeDataColumn[0]);
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueFor(Object node, int column) {
            ImmutableTreeNode treeNode = (ImmutableTreeNode) node;
            return columns[column].getValueFor(treeNode);
        }

        public Class getColumnClass(int column) {
            return columns[column].getColumnClass();
        }

        public boolean isCellEditable(Object node, int column) {
            return columns[column].isEditable();
        }

        public void setValueFor(Object node, int column, Object value) {
            columns[column].setValueFor((ImmutableTreeNode) node, value);
        }

        public String getColumnName(int column) {
            return columns[column].getColumnName();
        }
    }

    private static interface NodeDataColumn {

        public Class getColumnClass();

        public String getColumnName();

        public Object getValueFor(ImmutableTreeNode node);

        public void setValueFor(ImmutableTreeNode node, Object value);

        public boolean isEditable();
    }

    private static class AttributeNodeDataColumn implements NodeDataColumn {

        private AttributeColumn column;

        public AttributeNodeDataColumn(AttributeColumn column) {
            this.column = column;
        }

        public Class getColumnClass() {
            return column.getType().getType();
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
            return column.getOrigin().equals(AttributeOrigin.DATA);
        }
    }

    private static class NodeRenderer implements RenderDataProvider {

        @Override
        public java.awt.Color getBackground(Object o) {
            return null;
        }

        @Override
        public String getDisplayName(Object o) {
            return ((ImmutableTreeNode) o).getNode().getNodeData().getLabel();
        }

        @Override
        public java.awt.Color getForeground(Object o) {
            return null;
        }

        @Override
        public javax.swing.Icon getIcon(Object o) {
            return null;

        }

        @Override
        public String getTooltipText(Object o) {
            return "";
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }
    }
}
