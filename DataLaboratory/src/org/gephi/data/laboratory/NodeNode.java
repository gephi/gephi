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
import java.util.Collection;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeNode extends AbstractNode {

    private Node graphNode;
    private Collection<? extends AttributeColumn> attributeColumns;

    public NodeNode(Node node, Collection<? extends AttributeColumn> attributeColumns) {
        super(((node == null) || (node != null && false)) ? (new NodeChildren(node,attributeColumns)) : Children.LEAF);
        this.graphNode = node;
        this.attributeColumns = attributeColumns;
    }

    @Override
    public String getDisplayName() {
        if (graphNode != null) {
            return graphNode.getLabel();
        }
        return "root";
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }
        ss.put(new IDProperty(graphNode));
        if (attributeColumns != null) {
            for (AttributeColumn col : attributeColumns) {
                ss.put(new AttributeProperty(col, graphNode));
            }
        }

        return s;

    }

    public class AttributeProperty extends PropertySupport.ReadOnly {

        private final Node node;

        @SuppressWarnings("unchecked")
        public AttributeProperty(AttributeColumn attColumn, Node node) {
            super(attColumn.getId(), String.class, attColumn.getTitle(), "Description tooltip");
            this.node = node;

            this.setValue("ComparableColumnTTV", Boolean.TRUE); // sortable
            this.setValue("SortingColumnTTV", Boolean.FALSE); // initially not sorted
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            //Get value
            return "rien";
        }
    }

    public class IDProperty extends PropertySupport.ReadOnly {

        private final Node node;

        @SuppressWarnings("unchecked")
        public IDProperty(Node node) {
            super("id", String.class, "ID", "ID tooltip");
            this.node = node;

            this.setValue("TreeColumnTTV", Boolean.TRUE); // main tree column
            this.setValue("ComparableColumnTTV", Boolean.TRUE); // sortable
            this.setValue("SortingColumnTTV", Boolean.FALSE); // initially not sorted
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            //return node.getLabel();
            return "id";
        }
    }
}
