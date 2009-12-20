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
package org.gephi.tool.edit;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.StringList;
import org.gephi.graph.api.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class EditNode extends AbstractNode {

    private PropertySet[] propertySets;
    private Node node;

    public EditNode(Node node) {
        super(Children.LEAF);
        this.node = node;
        setName(node.getNodeData().getLabel());
    }

    @Override
    public PropertySet[] getPropertySets() {
        if (propertySets == null) {
            try {
                Sheet.Set set = Sheet.createPropertiesSet();
                set.setDisplayName(node.getNodeData().getLabel());

                AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                for (AttributeValue value : row.getValues()) {
                    AttributeValueWrapper wrap = new AttributeValueWrapper(row, value.getColumn().getIndex());
                    AttributeType type = value.getColumn().getType();
                    Property p = new PropertySupport.Reflection(wrap, type.getType(), "getValue" + type.getType().getSimpleName(), "setValue" + type.getType().getSimpleName());
                    p.setDisplayName(value.getColumn().getTitle());
                    p.setName(value.getColumn().getId());
                    set.put(p);
                }

                propertySets = new PropertySet[]{set};
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return propertySets;
    }

    public static class AttributeValueWrapper {

        private AttributeRow row;
        private int index;

        public AttributeValueWrapper(AttributeRow row, int index) {
            this.row = row;
            this.index = index;
        }

        public String getValueString() {
            return (String) row.getValue(index);
        }

        public void setValueString(String object) {
            row.setValue(index, object);
        }

        public Double getValueDouble() {
            return (Double) row.getValue(index);
        }

        public void setValueDouble(Double object) {
            row.setValue(index, object);
        }

        public Float getValueFloat() {
            return (Float) row.getValue(index);
        }

        public void setValueFloat(Float object) {
            row.setValue(index, object);
        }

        public Integer getValueInteger() {
            return (Integer) row.getValue(index);
        }

        public void setValueInteger(Integer object) {
            row.setValue(index, object);
        }

        public Boolean getValueBoolean() {
            return (Boolean) row.getValue(index);
        }

        public void setValueBoolean(Boolean object) {
            row.setValue(index, object);
        }

        public Long getValueLong() {
            return (Long) row.getValue(index);
        }

        public void setValueLong(Long object) {
            row.setValue(index, object);
        }

        public StringList getValueStringList() {
            return (StringList) row.getValue(index);
        }

        public void setValueStringList(StringList object) {
            row.setValue(index, object);
        }
    }
}
