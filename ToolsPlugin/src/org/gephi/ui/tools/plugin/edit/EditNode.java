/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.ui.tools.plugin.edit;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.BigDecimalList;
import org.gephi.data.attributes.type.BigIntegerList;
import org.gephi.data.attributes.type.BooleanList;
import org.gephi.data.attributes.type.ByteList;
import org.gephi.data.attributes.type.CharacterList;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.data.attributes.type.IntegerList;
import org.gephi.data.attributes.type.LongList;
import org.gephi.data.attributes.type.ShortList;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
        propertySets = new PropertySet[]{prepareNodeProperties(), prepareNodeAttributes()};
        return propertySets;
    }

    /**
     * Prepare set of attributes of the node.
     * @return Set of these attributes
     */
    private Sheet.Set prepareNodeAttributes() {
        try {
            Sheet.Set set = new Sheet.Set();
            set.setName("attributes");
            set.setDisplayName(NbBundle.getMessage(EditNode.class, "Node.attributes.text", node.getNodeData().getLabel()));

            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            for (AttributeValue value : row.getValues()) {
                AttributeValueWrapper wrap = new AttributeValueWrapper(row, value.getColumn().getIndex());
                AttributeType type = value.getColumn().getType();
                Property p;
                if (value.getColumn().getOrigin() != AttributeOrigin.COMPUTED && value.getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex()) {
                    if (!AttributeValueWrapper.notSupportedTypes.contains(type)) {//The AttributeType can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type.getType(), "getValue" + type.getType().getSimpleName(), "setValue" + type.getType().getSimpleName());
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", "setValue" + type.getType().getSimpleName());
                    }
                } else {
                    //Not editable because it is computed value or the node/edge id:
                    if (!AttributeValueWrapper.notSupportedTypes.contains(type)) {//The AttributeType can be edited by default:
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
     * Prepare set of editable properties of the node: size, position.
     * @return Set of these properties
     */
    private Sheet.Set prepareNodeProperties() {
        try {
            Sheet.Set set = new Sheet.Set();
            set.setName("properties");
            set.setDisplayName(NbBundle.getMessage(EditNode.class, "Node.properties.text", node.getNodeData().getLabel()));
            NodeData data = node.getNodeData();

            Property p;
            //Size:
            p = new PropertySupport.Reflection(data, Float.TYPE, "getSize", "setSize");
            p.setDisplayName(NbBundle.getMessage(EditNode.class, "Property.size.text"));
            p.setName("size");
            set.put(p);

            //All position coordinates:
            set.put(buildGeneralPositionProperty(data, "x"));
            set.put(buildGeneralPositionProperty(data, "y"));
            set.put(buildGeneralPositionProperty(data, "z"));

            //Color:
            p = new PropertySupport.Reflection(this, Color.class, "getNodeColor", "setNodeColor");
            p.setDisplayName(NbBundle.getMessage(EditNode.class, "Property.color.text"));
            p.setName("color");
            set.put(p);

            return set;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public Color getNodeColor() {
        NodeData data = node.getNodeData();
        return new Color(data.r(), data.g(), data.b(), data.alpha());
    }

    public void setNodeColor(Color c) {
        NodeData data = node.getNodeData();
        data.setR(c.getRed() / 255f);
        data.setG(c.getGreen() / 255f);
        data.setB(c.getBlue() / 255f);
        data.setAlpha(c.getAlpha() / 255f);
    }

    /**
     * Used to build property for each position coordinate (x,y,z) in the same way.
     * @return Property for that coordinate
     */
    private Property buildGeneralPositionProperty(NodeData data, String coordinate) throws NoSuchMethodException {
        //Position:
        Property p = new PropertySupport.Reflection(data, Float.TYPE, coordinate, "set" + coordinate.toUpperCase());
        p.setDisplayName(NbBundle.getMessage(EditNode.class, "Property.position.text", coordinate));
        p.setName(coordinate);
        return p;
    }

    public static class AttributeValueWrapper {

        /**
         * These AttributeTypes are not supported by default by netbeans property editor.
         * We will use attributes of these types as Strings and parse them.
         */
        private static EnumSet<AttributeType> notSupportedTypes = EnumSet.of(
                AttributeType.BIGINTEGER,
                AttributeType.BIGDECIMAL,
                AttributeType.TIME_INTERVAL,
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
                AttributeType.LIST_STRING);
        private AttributeRow row;
        private int index;

        public AttributeValueWrapper(AttributeRow row, int index) {
            this.row = row;
            this.index = index;
        }

        private String convertToStringIfNotNull() {
            Object value = row.getValue(index);
            if (value != null) {
                return value.toString();
            } else {
                return null;
            }
        }

        public Byte getValueByte() {
            return (Byte) row.getValue(index);
        }

        public void setValueByte(Byte object) {
            row.setValue(index, object);
        }

        public Short getValueShort() {
            return (Short) row.getValue(index);
        }

        public void setValueShort(Short object) {
            row.setValue(index, object);
        }

        public Character getValueCharacter() {
            return (Character) row.getValue(index);
        }

        public void setValueCharacter(Character object) {
            row.setValue(index, object);
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

        /****** Next types are not supported by property editors by default so they are used and parsed as Strings ******/
        public String getValueAsString() {
            return convertToStringIfNotNull();
        }

        public void setValueBigInteger(String object) {
            row.setValue(index, new BigInteger(object));
        }

        public void setValueBigDecimal(String object) {
            row.setValue(index, new BigDecimal(object));
        }

        public void setValueTimeInterval(String object) {
            row.setValue(index, new TimeInterval(object));
        }

        public void setValueByteList(String object) {
            row.setValue(index, new ByteList(object));
        }

        public void setValueShortList(String object) {
            row.setValue(index, new ShortList(object));
        }

        public void setValueIntegerList(String object) {
            row.setValue(index, new IntegerList(object));
        }

        public void setValueLongList(String object) {
            row.setValue(index, new LongList(object));
        }

        public void setValueFloatList(String object) {
            row.setValue(index, new FloatList(object));
        }

        public void setValueDoubleList(String object) {
            row.setValue(index, new DoubleList(object));
        }

        public void setValueBooleanList(String object) {
            row.setValue(index, new BooleanList(object));
        }

        public void setValueCharacterList(String object) {
            row.setValue(index, new CharacterList(object));
        }

        public void setValueStringList(String object) {
            row.setValue(index, new StringList(object));
        }

        public void setValueBigIntegerList(String object) {
            row.setValue(index, new BigIntegerList(object));
        }

        public void setValueBigDecimalList(String object) {
            row.setValue(index, new BigDecimalList(object));
        }
    }
}
