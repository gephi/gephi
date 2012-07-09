/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.datalab;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.type.*;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.desktop.datalab.utils.SparkLinesRenderer;
import org.gephi.desktop.datalab.utils.TimeIntervalCellEditor;
import org.gephi.desktop.datalab.utils.TimeIntervalsRenderer;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.etable.QuickFilter;
import org.netbeans.swing.outline.*;
import org.openide.awt.MouseUtils;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDataTable {
    private boolean useSparklines = false;
    private boolean timeIntervalGraphics = false;
    private Outline outlineTable;
    private QuickFilter quickFilter;
    private Pattern pattern;
    private DataTablesModel dataTablesModel;
    private Node[] selectedNodes;
    private AttributeUtils attributeUtils;
    private AttributeColumnsController attributeColumnsController;
    private boolean refreshingTable = false;
    private AttributeColumn[] showingColumns = null;
    private static final int FAKE_COLUMNS_COUNT = 1;
    private SparkLinesRenderer sparkLinesRenderer;
    private TimeIntervalsRenderer timeIntervalsRenderer;
    private TimeIntervalCellEditor timeIntervalCellEditor;
    private TimeFormat currentTimeFormat;

    public NodeDataTable() {
        attributeUtils = AttributeUtils.getDefault();
        attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);

        outlineTable = new Outline();
        outlineTable.setColumnHidingAllowed(false);

        quickFilter = new QuickFilter() {

            public boolean accept(Object value) {
                if (value == null) {
                    value = "";
                }
                if (value instanceof ImmutableTreeNode) {
                    String label = ((ImmutableTreeNode) value).getNode().getNodeData().getLabel();
                    if (label != null) {
                        return pattern.matcher(label).find();
                    }
                    return false;
                }
                return pattern.matcher(value.toString()).find();
            }
        };

        outlineTable.addMouseListener(new PopupAdapter());
        prepareRenderers();
        //Add listener of table selection to refresh edit window when the selection changes (and if the table is not being refreshed):
        //Temporally disabled because the call to findInstance in EditWindowController seems to randomly and rarely create exceptions
//        outlineTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//
//            public void valueChanged(ListSelectionEvent e) {
//                if (!refreshingTable) {
//                    EditWindowController edc = Lookup.getDefault().lookup(EditWindowController.class);
//                    if (edc.isOpen()) {
//                        if (outlineTable.getSelectedRow() != -1) {
//                            edc.editNodes(getNodesFromSelectedRows());
//                        } else {
//                            edc.disableEdit();
//                        }
//                    }
//                }
//            }
//        });
        outlineTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
                    Node[] selectedNodes = getNodesFromSelectedRows();
                    if (selectedNodes.length > 0) {
                        NodesManipulator del = dlh.getNodesManipulatorByName("DeleteNodes");
                        if (del != null) {
                            del.setup(selectedNodes, null);
                            if (del.canExecute()) {
                                dlh.executeManipulator(del);
                            }
                        }
                    }
                }
            }
        });
    }

    private void prepareRenderers() {
        DynamicModel dm = Lookup.getDefault().lookup(DynamicController.class).getModel();
        outlineTable.setDefaultRenderer(NumberList.class, sparkLinesRenderer = new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicBigDecimal.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicBigInteger.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicByte.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicDouble.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicFloat.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicInteger.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicLong.class, new SparkLinesRenderer());
        outlineTable.setDefaultRenderer(DynamicShort.class, new SparkLinesRenderer());
        double min, max;
        if (dm != null) {
            min = dm.getMin();
            max = dm.getMax();
        } else {
            min = Double.NEGATIVE_INFINITY;
            max = Double.POSITIVE_INFINITY;
        }
        outlineTable.setDefaultRenderer(TimeInterval.class, timeIntervalsRenderer = new TimeIntervalsRenderer(min, max, timeIntervalGraphics));

        //Use default string editor for them:
        outlineTable.setDefaultEditor(NumberList.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicBigDecimal.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicBigInteger.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicByte.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicDouble.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicFloat.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicInteger.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicLong.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(DynamicShort.class, new DefaultCellEditor(new JTextField()));
        outlineTable.setDefaultEditor(TimeInterval.class, timeIntervalCellEditor=new TimeIntervalCellEditor(new JTextField()));
    }

    public Outline getOutlineTable() {
        return outlineTable;
    }

    public boolean setFilter(String regularExpr, int columnIndex) {
        if (selectedNodes == null) {
            selectedNodes = getNodesFromSelectedRows();
        }
        try {
            pattern = Pattern.compile(regularExpr, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return false;
        }
        if (regularExpr == null || regularExpr.isEmpty()) {
            outlineTable.unsetQuickFilter();
        } else {
            outlineTable.setQuickFilter(columnIndex, quickFilter);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setNodesSelection(selectedNodes); //Keep row selection before refreshing.
                selectedNodes = null;
            }
        });
        return true;
    }

    public void refreshModel(HierarchicalGraph graph, AttributeColumn[] cols, final DataTablesModel dataTablesModel) {
        showingColumns = cols;
        DynamicModel dm = Lookup.getDefault().lookup(DynamicController.class).getModel();
        if (dm != null) {
            timeIntervalsRenderer.setMinMax(dm.getMin(), dm.getMax());
            currentTimeFormat = dm.getTimeFormat();
            timeIntervalsRenderer.setTimeFormat(currentTimeFormat);
            timeIntervalCellEditor.setTimeFormat(currentTimeFormat);
            sparkLinesRenderer.setTimeFormat(currentTimeFormat);
        }
        timeIntervalsRenderer.setDrawGraphics(timeIntervalGraphics);
        refreshingTable = true;
        if (selectedNodes == null) {
            selectedNodes = getNodesFromSelectedRows();
        }
        NodeTreeModel nodeTreeModel = new NodeTreeModel(graph.wrapToTreeNode());
        final OutlineModel mdl = DefaultOutlineModel.createOutlineModel(nodeTreeModel, new NodeRowModel(cols), true);
        ((ETableColumnModel)outlineTable.getColumnModel()).clearSortedColumns();//Outline has problems when a sorted column in the previous model is not in the new model
        outlineTable.setRootVisible(false);
        outlineTable.setRenderDataProvider(new NodeRenderer());
        outlineTable.setModel(mdl);
        NodeDataTable.this.dataTablesModel = dataTablesModel;
        setNodesSelection(selectedNodes);//Keep row selection before refreshing.
        selectedNodes = null;
        refreshingTable = false;
    }

    public void setNodesSelection(Node[] nodes) {
        this.selectedNodes = nodes;//Keep this selection request to be able to apply nodes selection if the table is first refreshed later.
        if (selectedNodes != null) {
            HashSet<Node> nodesSet = new HashSet<Node>();
            nodesSet.addAll(Arrays.asList(selectedNodes));
            outlineTable.clearSelection();
            for (int i = 0; i < outlineTable.getRowCount(); i++) {
                if (nodesSet.contains(getNodeFromRow(i))) {
                    outlineTable.addRowSelectionInterval(i, i);
                }
            }
        }
    }

    public void scrollToFirstNodeSelected() {
        int row = outlineTable.getSelectedRow();
        if (row != -1) {
            Rectangle rect = outlineTable.getCellRect(row, 0, true);
            outlineTable.scrollRectToVisible(rect);
        }
    }

    public boolean hasData() {
        return outlineTable.getRowCount() > 0;
    }

    public boolean isUseSparklines() {
        return useSparklines;
    }

    public boolean isTimeIntervalGraphics() {
        return timeIntervalGraphics;
    }

    public void setTimeIntervalGraphics(boolean timeIntervalGraphics) {
        this.timeIntervalGraphics = timeIntervalGraphics;
    }

    public void setUseSparklines(boolean useSparklines) {
        this.useSparklines = useSparklines;
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
            if (parent instanceof TreeNode) {
                TreeNode node = (TreeNode) parent;
                return node.getChildAt(index);
            } else {
                return null;
            }
        }

        public int getChildCount(Object parent) {
            if (parent instanceof TreeNode) {
                TreeNode node = (TreeNode) parent;
                return node.getChildCount();
            } else {
                return 0;
            }
        }

        public boolean isLeaf(Object node) {
            if (node instanceof TreeNode) {
                TreeNode n = (TreeNode) node;
                return n.isLeaf();
            } else {
                return true;
            }
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

    private class NodeRowModel implements RowModel {

        private NodeDataColumn[] columns;
        private final int length;

        public NodeRowModel(AttributeColumn[] attributeColumns) {

            ArrayList<NodeDataColumn> cols = new ArrayList<NodeDataColumn>();
            for (AttributeColumn c : attributeColumns) {
                cols.add(new AttributeNodeDataColumn(c));
            }
            columns = cols.toArray(new NodeDataColumn[0]);
            length = columns.length;
        }

        public int getColumnCount() {
            return length;
        }

        public Object getValueFor(Object node, int column) {
            if (outOfBounds(column)) {
                return null;
            }
            ImmutableTreeNode treeNode = (ImmutableTreeNode) node;
            return columns[column].getValueFor(treeNode);
        }

        public Class getColumnClass(int column) {
            if (outOfBounds(column)) {
                return Object.class;
            }
            return columns[column].getColumnClass();
        }

        public boolean isCellEditable(Object node, int column) {
            if (outOfBounds(column)) {
                return false;
            }
            return columns[column].isEditable();
        }

        public void setValueFor(Object node, int column, Object value) {
            columns[column].setValueFor((ImmutableTreeNode) node, value);
        }

        public String getColumnName(int column) {
            if (outOfBounds(column)) {
                return null;
            }
            return columns[column].getColumnName();
        }

        private boolean outOfBounds(int position) {
            return position >= length || position < 0;
        }
    }

    private interface NodeDataColumn {

        public Class getColumnClass();

        public String getColumnName();

        public Object getValueFor(ImmutableTreeNode node);

        public void setValueFor(ImmutableTreeNode node, Object value);

        public boolean isEditable();
    }

    private class AttributeNodeDataColumn implements NodeDataColumn {

        private AttributeColumn column;

        public AttributeNodeDataColumn(AttributeColumn column) {
            this.column = column;
        }

        public Class getColumnClass() {
            if (useSparklines && attributeUtils.isNumberListColumn(column)) {
                return NumberList.class;
            } else if (useSparklines && attributeUtils.isDynamicNumberColumn(column)) {
                return column.getType().getType();
            } else if (column.getType() == AttributeType.TIME_INTERVAL) {
                return TimeInterval.class;
            } else if (attributeUtils.isNumberColumn(column)) {
                return column.getType().getType();//Number columns should not be treated as Strings because the sorting would be alphabetic instead of numeric
            } else if (column.getType() == AttributeType.BOOLEAN) {
                return Boolean.class;
            } else {
                return String.class;//Treat all columns as Strings. Also fix the fact that the table implementation does not allow to edit Character cells.
            }
        }

        public String getColumnName() {
            return column.getTitle();
        }

        public Object getValueFor(ImmutableTreeNode node) {
            Node graphNode = node.getNode();
            if (graphNode.getId() == -1) {
                return null;
            }
            Attributes row = graphNode.getNodeData().getAttributes();
            Object value = row.getValue(column.getIndex());

            if (useSparklines && (attributeUtils.isNumberListColumn(column) || attributeUtils.isDynamicNumberColumn(column))) {
                return value;
            } else if (column.getType() == AttributeType.TIME_INTERVAL) {
                return value;
            } else if (attributeUtils.isNumberColumn(column)) {
                return value;
            } else if (column.getType() == AttributeType.BOOLEAN) {
                return value;
            } else {
                //Show values as Strings like in Edit window and other parts of the program to be consistent
                if (value != null) {
                    if (value instanceof DynamicType) {//When type is dynamic, take care to show proper time format
                        return ((DynamicType) value).toString(currentTimeFormat == TimeFormat.DOUBLE);
                    } else {
                        return value.toString();
                    }
                } else {
                    return null;
                }
            }
        }

        public void setValueFor(ImmutableTreeNode node, Object value) {
            Node graphNode = node.getNode();
            attributeColumnsController.setAttributeValue(value, graphNode.getNodeData().getAttributes(), column);
        }

        public boolean isEditable() {
            return attributeColumnsController.canChangeColumnData(column);
        }
    }

    private static class NodeRenderer implements RenderDataProvider {

        @Override
        public java.awt.Color getBackground(Object o) {
            return null;
        }

        @Override
        public String getDisplayName(Object o) {
            if (o instanceof ImmutableTreeNode) {
                if (((ImmutableTreeNode) o).getNode().getNodeData().isFixed()) {
                    return "¤ " + ((ImmutableTreeNode) o).getNode().getNodeData().getLabel();//UI feedback when node is settled
                } else {
                    return ((ImmutableTreeNode) o).getNode().getNodeData().getLabel();
                }
            } else {
                return o.toString();
            }
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

    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        PopupAdapter() {
        }

        protected void showPopup(final MouseEvent e) {
            int selRow = outlineTable.rowAtPoint(e.getPoint());

            if (selRow != -1) {
                if (!outlineTable.getSelectionModel().isSelectedIndex(selRow)) {
                    outlineTable.getSelectionModel().clearSelection();
                    outlineTable.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
                final Point p = e.getPoint();
                new Thread(new Runnable() {

                    public void run() {
                        final JPopupMenu pop = createPopup(p);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                showPopup(p.x, p.y, pop);
                            }
                        });
                    }
                }).start();
            } else {
                outlineTable.getSelectionModel().clearSelection();
            }
            outlineTable.repaint();
            e.consume();
        }

        private void showPopup(int xpos, int ypos, final JPopupMenu popup) {
            if ((popup != null) && (popup.getSubElements().length > 0)) {
                final PopupMenuListener p = new PopupMenuListener() {

                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }

                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        popup.removePopupMenuListener(this);
                    }

                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                };
                popup.addPopupMenuListener(p);
                popup.show(outlineTable, xpos, ypos);
            }
        }

        private JPopupMenu createPopup(Point p) {
            final Node[] selectedNodes = getNodesFromSelectedRows();
            final Node clickedNode = getNodeFromRow(outlineTable.rowAtPoint(p));
            JPopupMenu contextMenu = new JPopupMenu();

            //First add nodes manipulators items:
            DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
            Integer lastManipulatorType = null;
            for (NodesManipulator nm : dlh.getNodesManipulators()) {
                nm.setup(selectedNodes, clickedNode);
                if (lastManipulatorType == null) {
                    lastManipulatorType = nm.getType();
                }
                if (lastManipulatorType != nm.getType()) {
                    contextMenu.addSeparator();
                }
                lastManipulatorType = nm.getType();
                if (nm.isAvailable()) {
                    contextMenu.add(PopupMenuUtils.createMenuItemFromNodesManipulator(nm, clickedNode, selectedNodes));
                }
            }

            //Add AttributeValues manipulators submenu:
            AttributeRow row = (AttributeRow) clickedNode.getNodeData().getAttributes();
            int realColumnIndex = outlineTable.convertColumnIndexToModel(outlineTable.columnAtPoint(p)) - FAKE_COLUMNS_COUNT;//Get real attribute column index not counting fake columns.
            if (realColumnIndex >= 0) {
                AttributeColumn column = showingColumns[realColumnIndex];
                if (column != null) {
                    contextMenu.add(PopupMenuUtils.createSubMenuFromRowColumn(row, column));
                }
            }
            return contextMenu;
        }
    }

    private Node getNodeFromRow(int rowIndex) {
        int row = outlineTable.convertRowIndexToModel(rowIndex);
        TreePath tp = outlineTable.getLayoutCache().getPathForRow(row);
        if (tp == null) {
            return null;
        }
        ImmutableTreeNode immutableTreeNode = (ImmutableTreeNode) tp.getLastPathComponent();
        return immutableTreeNode.getNode();
    }

    public Node[] getNodesFromSelectedRows() {
        int[] selectedRows = outlineTable.getSelectedRows();
        Node[] nodes = new Node[selectedRows.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = getNodeFromRow(selectedRows[i]);
        }
        return nodes;
    }
}
