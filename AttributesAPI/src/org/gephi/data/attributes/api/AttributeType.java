/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Cezary Bartosiak
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
package org.gephi.data.attributes.api;

import java.math.BigInteger;
import java.math.BigDecimal;
import org.gephi.data.attributes.type.DynamicByte;
import org.gephi.data.attributes.type.DynamicShort;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicLong;
import org.gephi.data.attributes.type.DynamicFloat;
import org.gephi.data.attributes.type.DynamicDouble;
import org.gephi.data.attributes.type.DynamicBoolean;
import org.gephi.data.attributes.type.DynamicCharacter;
import org.gephi.data.attributes.type.DynamicString;
import org.gephi.data.attributes.type.DynamicBigInteger;
import org.gephi.data.attributes.type.DynamicBigDecimal;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.attributes.type.ByteList;
import org.gephi.data.attributes.type.ShortList;
import org.gephi.data.attributes.type.IntegerList;
import org.gephi.data.attributes.type.LongList;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.data.attributes.type.BooleanList;
import org.gephi.data.attributes.type.CharacterList;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.attributes.type.BigIntegerList;
import org.gephi.data.attributes.type.BigDecimalList;

/**
 * The different type an {@link AttributeColumn} can have.
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 * @author Cezary Bartosiak
 */
public enum AttributeType {

    BYTE(Byte.class),
    SHORT(Short.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    BOOLEAN(Boolean.class),
    CHAR(Character.class),
    STRING(String.class),
    BIGINTEGER(BigInteger.class),
    BIGDECIMAL(BigDecimal.class),
    DYNAMIC_BYTE(DynamicByte.class),
    DYNAMIC_SHORT(DynamicShort.class),
    DYNAMIC_INT(DynamicInteger.class),
    DYNAMIC_LONG(DynamicLong.class),
    DYNAMIC_FLOAT(DynamicFloat.class),
    DYNAMIC_DOUBLE(DynamicDouble.class),
    DYNAMIC_BOOLEAN(DynamicBoolean.class),
    DYNAMIC_CHAR(DynamicCharacter.class),
    DYNAMIC_STRING(DynamicString.class),
    DYNAMIC_BIGINTEGER(DynamicBigInteger.class),
    DYNAMIC_BIGDECIMAL(DynamicBigDecimal.class),
    TIME_INTERVAL(TimeInterval.class),
    LIST_BYTE(ByteList.class),
    LIST_SHORT(ShortList.class),
    LIST_INTEGER(IntegerList.class),
    LIST_LONG(LongList.class),
    LIST_FLOAT(FloatList.class),
    LIST_DOUBLE(DoubleList.class),
    LIST_BOOLEAN(BooleanList.class),
    LIST_CHARACTER(CharacterList.class),
    LIST_STRING(StringList.class),
    LIST_BIGINTEGER(BigIntegerList.class),
    LIST_BIGDECIMAL(BigDecimalList.class);
    private final Class type;

    AttributeType(Class type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.getSimpleName();
    }

    /**
     * The name of the enum constant.
     *
     * @return the name of the enum constant
     */
    public String getTypeString() {
        return super.toString();
    }

    /**
     * Returns the <code>Class</code> the type is associated with.
     *
     * @return      the <code>class</code> the type is associated with
     */
    public Class getType() {
        return type;
    }

    /**
     * Try to parse the given <code>str</code> snippet in an object of the type
     * associated to this <code>AttributeType</code>. For instance if the type
     * is <b>Boolean</b>, and <code>str</code> equals <code>true</code>, this
     * method will succeed to return a <code>Boolean</code> instance. May
     * throw <code>NumberFormatException</code>.
     *
     * <code>DYNAMIC</code> types and <code>TIME_INTERVAL</code> cannot be parsed with this method (see <code>canParseFromString</code> method) and a UnsupportedOperationException will be thrown if it is tried.
     * 
     * @param str   the string that is to be parsed
     * @return      an instance of the type of this  <code>AttributeType</code>.
     */
    public Object parse(String str) {
        switch (this) {
            case BYTE:
                return new Byte(str);
            case SHORT:
                return new Short(str);
            case INT:
                return new Integer(str);
            case LONG:
                return new Long(str);
            case FLOAT:
                return new Float(str);
            case DOUBLE:
                return new Double(str);
            case BOOLEAN:
                return new Boolean(str);
            case CHAR:
                return new Character(str.charAt(0));
            case BIGINTEGER:
                return new BigInteger(str);
            case BIGDECIMAL:
                return new BigDecimal(str);
            case DYNAMIC_BYTE:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_SHORT:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_INT:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_LONG:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_FLOAT:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_DOUBLE:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_BOOLEAN:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_CHAR:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_STRING:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_BIGINTEGER:
                throw new UnsupportedOperationException("Not supported.");
            case DYNAMIC_BIGDECIMAL:
                throw new UnsupportedOperationException("Not supported.");
            case TIME_INTERVAL:
                throw new UnsupportedOperationException("Not supported.");
            case LIST_BYTE:
                return new ByteList(str);
            case LIST_SHORT:
                return new ShortList(str);
            case LIST_INTEGER:
                return new IntegerList(str);
            case LIST_LONG:
                return new LongList(str);
            case LIST_FLOAT:
                return new FloatList(str);
            case LIST_DOUBLE:
                return new DoubleList(str);
            case LIST_BOOLEAN:
                return new BooleanList(str);
            case LIST_CHARACTER:
                return new CharacterList(str);
            case LIST_STRING:
                return new StringList(str);
            case LIST_BIGINTEGER:
                return new BigIntegerList(str);
            case LIST_BIGDECIMAL:
                return new BigDecimalList(str);
        }
        return str;
    }

    /**
     * Indicates if this AttributeType can be parsed from a String with <code>parse(String str)</code> method.
     * All types except <code>DYNAMIC</code> types and <code>TIME_INTERVAL</code> can be parsed.
     * @return
     */
    public boolean canParseFromString() {
        switch (this) {
            case DYNAMIC_BYTE:
            case DYNAMIC_SHORT:
            case DYNAMIC_INT:
            case DYNAMIC_LONG:
            case DYNAMIC_FLOAT:
            case DYNAMIC_DOUBLE:
            case DYNAMIC_BOOLEAN:
            case DYNAMIC_CHAR:
            case DYNAMIC_STRING:
            case DYNAMIC_BIGINTEGER:
            case DYNAMIC_BIGDECIMAL:
            case TIME_INTERVAL:
                return false;
            default:
                return true;
        }
    }

    /**
     * Build an <code>AttributeType</code> from the given <code>obj</code> type.
     * If the given <code>obj</code> class match with an
     * <code>AttributeType</code> type, returns this type. Returns <code>null</code>
     * otherwise.
     * <p>
     * For instance if
     * <b>obj instanceof Float</b> equals <b>true</b>, returns
     * <code>AttributeType.FLOAT</code>.
     *
     * @param obj   the object that is to be parsed
     * @return      the compatible <code>AttributeType</code>, or <code>null</code> if no type is found or the input object is null
     */
    public static AttributeType parse(Object obj) {
        if (obj == null) {
            return null;
        }
        Class<?> c = obj.getClass();

        for (AttributeType attributeType : AttributeType.values()) {
            if (c.equals(attributeType.getType())) {
                return attributeType;
            }
        }

        return null;
    }

    /**
     * Build an dynamic <code>AttributeType</code> from the given <code>obj</code> type.
     * If the given <code>obj</code> class match with an
     * <code>AttributeType</code> type, returns this type. Returns <code>null</code>
     * otherwise.
     * <p>
     * For instance if
     * <b>obj instanceof Float</b> equals <b>true</b>, returns
     * <code>AttributeType.DYNAMIC_FLOAT</code>.
     *
     * @param obj   the object that is to be parsed
     * @return      the compatible <code>AttributeType</code>, or <code>null</code>
     */
    public static AttributeType parseDynamic(Object obj) {

        if (obj == null) {
            return null;
        }
        Class<?> c = obj.getClass();

        if (c.equals(Byte.class)) {
            return DYNAMIC_BYTE;
        }
        if (c.equals(Short.class)) {
            return DYNAMIC_SHORT;
        }
        if (c.equals(Integer.class)) {
            return DYNAMIC_INT;
        }
        if (c.equals(Long.class)) {
            return DYNAMIC_LONG;
        }
        if (c.equals(Float.class)) {
            return DYNAMIC_FLOAT;
        }
        if (c.equals(Double.class)) {
            return DYNAMIC_DOUBLE;
        }
        if (c.equals(Boolean.class)) {
            return DYNAMIC_BOOLEAN;
        }
        if (c.equals(Character.class)) {
            return DYNAMIC_CHAR;
        }
        if (c.equals(String.class)) {
            return DYNAMIC_STRING;
        }
        if (c.equals(BigInteger.class)) {
            return DYNAMIC_BIGINTEGER;
        }
        if (c.equals(BigDecimal.class)) {
            return DYNAMIC_BIGDECIMAL;
        }

        return null;
    }
}
