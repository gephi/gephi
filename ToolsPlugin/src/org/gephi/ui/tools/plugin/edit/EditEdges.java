/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.ui.tools.plugin.edit;

import java.awt.Color;
import java.util.EnumSet;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * PropertySheet that allows to edit one or more edges.
 * If multiple node edition mode is used at first all values will be shown as blank
 * but will change with the editions and all edges will be set the values that the user inputs.
 * @author Mathieu Bastian
 */
public class EditEdges extends AbstractNode {

    private PropertySet[] propertySets;
    private Edge[] edges;
    private boolean multipleEdges;
    private TimeFormat currentTimeFormat=TimeFormat.DOUBLE;

    /**
     * Single edge edition mode will always be enabled with this single node constructor
     * @param edge
     */
    public EditEdges(Edge edge) {
        super(Children.LEAF);
        this.edges = new Edge[]{edge};
        setName(edge.getEdgeData().getLabel());
        multipleEdges = false;
    }

    /**
     * If the edges array has more than one element, multiple edges edition mode will be enabled.
     * @param edges
     */
    public EditEdges(Edge[] edges) {
        super(Children.LEAF);
        this.edges = edges;
        multipleEdges = edges.length > 1;
        if (multipleEdges) {
            setName(NbBundle.getMessage(EditEdges.class, "EditEdges.multiple.elements"));
        } else {
            setName(edges[0].getEdgeData().getLabel());
        }
    }

    @Override
    public PropertySet[] getPropertySets() {
        propertySets = new PropertySet[]{prepareEdgesProperties(), prepareEdgesAttributes()};
        return propertySets;
    }

    /**
     * Prepare set of attributes of the edges.
     * @return Set of these attributes
     */
    private Sheet.Set prepareEdgesAttributes() {
        try {
            DynamicModel dm=Lookup.getDefault().lookup(DynamicController.class).getModel();
            if(dm!=null){
                currentTimeFormat=dm.getTimeFormat();
            }
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            Sheet.Set set = new Sheet.Set();
            set.setName("attributes");
            if (edges.length > 1) {
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.attributes.text.multiple"));
            } else {
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.attributes.text", edges[0].getEdgeData().getLabel()));
            }

            AttributeRow row = (AttributeRow) edges[0].getEdgeData().getAttributes();
            AttributeValueWrapper wrap;
            for (AttributeValue value : row.getValues()) {

                if (multipleEdges) {
                    wrap = new MultipleEdgesAttributeValueWrapper(edges, value.getColumn());
                } else {
                    wrap = new SingleEdgeAttributeValueWrapper(row, value.getColumn());
                }
                AttributeType type = value.getColumn().getType();
                Property p;
                if (ac.canChangeColumnData(value.getColumn())) {
                    //Editable column, provide "set" method:
                    if (!NotSupportedTypes.contains(type)) {//The AttributeType can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type.getType(), "getValue" + type.getType().getSimpleName(), "setValue" + type.getType().getSimpleName());
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", "setValueAsString");
                    }
                } else {
                    //Not editable column, do not provide "set" method:
                    if (!NotSupportedTypes.contains(type)) {//The AttributeType can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type.getType(), "getValue" + type.getType().getSimpleName(), null);
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", null);
                    }
                }
                p.setDisplayName(value.getColumn().getTitle());
                p.setName(value.getColumn().getId());
                set.put(p);
            }
            return set;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Prepare set of editable properties of the node(s): size, position.
     * @return Set of these properties
     */
    private Sheet.Set prepareEdgesProperties() {
        try {
            if (multipleEdges) {
                MultipleEdgesPropertiesWrapper EdgesWrapper = new MultipleEdgesPropertiesWrapper(edges);
                Sheet.Set set = new Sheet.Set();
                set.setName("properties");
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.properties.text.multiple"));

                Property p;

                //Color:
                p = new PropertySupport.Reflection(EdgesWrapper, Color.class, "getEdgesColor", "setEdgesColor");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.color.text"));
                p.setName("color");
                set.put(p);

                return set;
            } else {
                Edge edge = edges[0];
                Sheet.Set set = new Sheet.Set();
                set.setName("properties");
                set.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.properties.text", edge.getEdgeData().getLabel()));

                Property p;

                //Color:
                SingleEdgePropertiesWrapper EdgeWrapper = new SingleEdgePropertiesWrapper(edge);
                p = new PropertySupport.Reflection(EdgeWrapper, Color.class, "getEdgeColor", "setEdgeColor");
                p.setDisplayName(NbBundle.getMessage(EditEdges.class, "EditEdges.color.text"));
                p.setName("color");
                set.put(p);

                return set;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public class SingleEdgePropertiesWrapper {

        private Edge edge;

        public SingleEdgePropertiesWrapper(Edge Edge) {
            this.edge = Edge;
        }

        public Color getEdgeColor() {
            EdgeData data = edge.getEdgeData();
            if(data.r()<0||data.g()<0||data.b()<0||data.alpha()<0){
                return null;//Not specific color for edge
            }

            return new Color(data.r(), data.g(), data.b(), data.alpha());
        }

        public void setEdgeColor(Color c) {
            if (c != null) {
                EdgeData data = edge.getEdgeData();
                data.setR(c.getRed() / 255f);
                data.setG(c.getGreen() / 255f);
                data.setB(c.getBlue() / 255f);
                data.setAlpha(c.getAlpha() / 255f);
            }
        }
    }

    public class MultipleEdgesPropertiesWrapper {

        Edge[] edges;

        public MultipleEdgesPropertiesWrapper(Edge[] Edges) {
            this.edges = Edges;
        }
        //Methods and fields for multiple edges editing:
        private Color EdgesColor = null;

        public Color getEdgesColor() {
            return EdgesColor;
        }

        public void setEdgesColor(Color c) {
            if (c != null) {
                EdgesColor = c;
                EdgeData data;
                for (Edge edge : edges) {
                    data = edge.getEdgeData();
                    data.setR(c.getRed() / 255f);
                    data.setG(c.getGreen() / 255f);
                    data.setB(c.getBlue() / 255f);
                    data.setAlpha(c.getAlpha() / 255f);
                }
            }
        }
    }
    
    /**
     * These AttributeTypes are not supported by default by netbeans property editor.
     * We will use attributes of these types as Strings and parse them.
     */
    private static EnumSet<AttributeType> NotSupportedTypes = EnumSet.of(
            AttributeType.BIGINTEGER,
            AttributeType.BIGDECIMAL,
            AttributeType.LIST_BIGDECIMAL,
            AttributeType.LIST_BIGINTEGER,
            AttributeType.LIST_BOOLEAN,
            AttributeType.LIST_BYTE,
            AttributeType.LIST_CHARACTER,
            AttributeType.LIST_DOUBLE,
            AttributeType.LIST_FLOAT,
            AttributeType.LIST_INTEGER,
            AttributeType.LIST_LONG,
            AttributeType.LIST_SHORT,
            AttributeType.LIST_STRING,
            AttributeType.TIME_INTERVAL,
            AttributeType.DYNAMIC_BIGDECIMAL,
            AttributeType.DYNAMIC_BIGINTEGER,
            AttributeType.DYNAMIC_BOOLEAN,
            AttributeType.DYNAMIC_BYTE,
            AttributeType.DYNAMIC_CHAR,
            AttributeType.DYNAMIC_DOUBLE,
            AttributeType.DYNAMIC_FLOAT,
            AttributeType.DYNAMIC_INT,
            AttributeType.DYNAMIC_LONG,
            AttributeType.DYNAMIC_SHORT,
            AttributeType.DYNAMIC_STRING);

    public interface AttributeValueWrapper {

        public Byte getValueByte();

        public void setValueByte(Byte object);

        public Short getValueShort();

        public void setValueShort(Short object);

        public Character getValueCharacter();

        public void setValueCharacter(Character object);

        public String getValueString();

        public void setValueString(String object);

        public Double getValueDouble();

        public void setValueDouble(Double object);

        public Float getValueFloat();

        public void setValueFloat(Float object);

        public Integer getValueInteger();

        public void setValueInteger(Integer object);

        public Boolean getValueBoolean();

        public void setValueBoolean(Boolean object);

        public Long getValueLong();

        public void setValueLong(Long object);

        /****** Other types are not supported by property editors by default so they are used and parsed as Strings ******/
        public String getValueAsString();

        public void setValueAsString(String value);
    }

    public class SingleEdgeAttributeValueWrapper implements AttributeValueWrapper {

        private AttributeRow row;
        private AttributeColumn column;

        public SingleEdgeAttributeValueWrapper(AttributeRow row, AttributeColumn column) {
            this.row = row;
            this.column = column;
        }

        private String convertToStringIfNotNull() {
            Object value = row.getValue(column.getIndex());
            if (value != null) {
                if (value instanceof DynamicType) {
                    return ((DynamicType) value).toString(currentTimeFormat==TimeFormat.DOUBLE);
                } else {
                    return value.toString();
                }
            } else {
                return null;
            }
        }

        public Byte getValueByte() {
            return (Byte) row.getValue(column.getIndex());
        }

        public void setValueByte(Byte object) {
            row.setValue(column.getIndex(), object);
        }

        public Short getValueShort() {
            return (Short) row.getValue(column.getIndex());
        }

        public void setValueShort(Short object) {
            row.setValue(column.getIndex(), object);
        }

        public Character getValueCharacter() {
            return (Character) row.getValue(column.getIndex());
        }

        public void setValueCharacter(Character object) {
            row.setValue(column.getIndex(), object);
        }

        public String getValueString() {
            return (String) row.getValue(column.getIndex());
        }

        public void setValueString(String object) {
            row.setValue(column.getIndex(), object);
        }

        public Double getValueDouble() {
            return (Double) row.getValue(column.getIndex());
        }

        public void setValueDouble(Double object) {
            row.setValue(column.getIndex(), object);
        }

        public Float getValueFloat() {
            return (Float) row.getValue(column.getIndex());
        }

        public void setValueFloat(Float object) {
            row.setValue(column.getIndex(), object);
        }

        public Integer getValueInteger() {
            return (Integer) row.getValue(column.getIndex());
        }

        public void setValueInteger(Integer object) {
            row.setValue(column.getIndex(), object);
        }

        public Boolean getValueBoolean() {
            return (Boolean) row.getValue(column.getIndex());
        }

        public void setValueBoolean(Boolean object) {
            row.setValue(column.getIndex(), object);
        }

        public Long getValueLong() {
            return (Long) row.getValue(column.getIndex());
        }

        public void setValueLong(Long object) {
            row.setValue(column.getIndex(), object);
        }

        public String getValueAsString() {
            return convertToStringIfNotNull();
        }

        public void setValueAsString(String value) {
            row.setValue(column.getIndex(), column.getType().parse(value));
        }
    }

    public class MultipleEdgesAttributeValueWrapper implements AttributeValueWrapper {

        private Edge[] edges;
        private AttributeColumn column;
        private Object value;

        public MultipleEdgesAttributeValueWrapper(Edge[] edges, AttributeColumn column) {
            this.edges = edges;
            this.column = column;
            this.value = null;
        }

        private String convertToStringIfNotNull() {
            if (value != null) {
                if (value instanceof DynamicType) {
                    return ((DynamicType) value).toString(currentTimeFormat==TimeFormat.DOUBLE);
                } else {
                    return value.toString();
                }
            } else {
                return null;
            }
        }

        private void setValueToAllEdges(Object object) {
            this.value = object;
            for (Edge edge : edges) {
                edge.getEdgeData().getAttributes().setValue(column.getIndex(), value);
            }
        }

        public Byte getValueByte() {
            return (Byte) value;
        }

        public void setValueByte(Byte object) {
            setValueToAllEdges(object);
        }

        public Short getValueShort() {
            return (Short) value;
        }

        public void setValueShort(Short object) {
            setValueToAllEdges(object);
        }

        public Character getValueCharacter() {
            return (Character) value;
        }

        public void setValueCharacter(Character object) {
            setValueToAllEdges(object);
        }

        public String getValueString() {
            return (String) value;
        }

        public void setValueString(String object) {
            setValueToAllEdges(object);
        }

        public Double getValueDouble() {
            return (Double) value;
        }

        public void setValueDouble(Double object) {
            setValueToAllEdges(object);
        }

        public Float getValueFloat() {
            return (Float) value;
        }

        public void setValueFloat(Float object) {
            setValueToAllEdges(object);
        }

        public Integer getValueInteger() {
            return (Integer) value;
        }

        public void setValueInteger(Integer object) {
            setValueToAllEdges(object);
        }

        public Boolean getValueBoolean() {
            return (Boolean) value;
        }

        public void setValueBoolean(Boolean object) {
            setValueToAllEdges(object);
        }

        public Long getValueLong() {
            return (Long) value;
        }

        public void setValueLong(Long object) {
            setValueToAllEdges(object);
        }

        public String getValueAsString() {
            return convertToStringIfNotNull();
        }

        public void setValueAsString(String value) {
            setValueToAllEdges(column.getType().parse(value));
        }
    }
}
