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
package org.gephi.data.attributes.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
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

/**
 * The different type an {@link AttributeColumn} can have.
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
@SuppressWarnings("rawtypes")
public enum AttributeType {

    BYTE      (Byte.class),
    SHORT     (Short.class),
    INT       (Integer.class),
    LONG      (Long.class),
    FLOAT     (Float.class),
    DOUBLE    (Double.class),
    BIGINTEGER(BigInteger.class),
    BIGDECIMAL(BigDecimal.class),

    BOOLEAN      (Boolean.class),
    CHAR         (Character.class),
    STRING       (String.class),
    TIME_INTERVAL(TimeInterval.class),

    LIST_BYTE      (ByteList.class,       new Class[] {byte.class, Byte.class}),
    LIST_SHORT     (ShortList.class,      new Class[] {short.class, Short.class}),
    LIST_INTEGER   (IntegerList.class,    new Class[] {int.class, Integer.class}),
    LIST_LONG      (LongList.class,       new Class[] {long.class, Long.class}),
    LIST_FLOAT     (FloatList.class,      new Class[] {float.class, Float.class}),
    LIST_DOUBLE    (DoubleList.class,     new Class[] {double.class, Double.class}),
    LIST_BIGINTEGER(BigIntegerList.class, new Class[] {BigInteger.class}),
    LIST_BIGDECIMAL(BigDecimalList.class, new Class[] {BigDecimal.class}),

    LIST_BOOLEAN  (BooleanList.class,     new Class[] {boolean.class, Boolean.class}),
    LIST_CHARACTER(CharacterList.class,   new Class[] {char.class, Character.class}),
    LIST_STRING   (StringList.class,      new Class[] {String.class});

    private final Class<?> type;
    private final Class[] componentTypes;

    AttributeType(Class<?> type) {
        this(type, null);
    }

    private AttributeType(Class<?> type, Class[] componentTypes) {
        this.type = type;
        this.componentTypes = componentTypes;
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
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns component types. These types can be used for parsing an array into
     * List type. Every type defined as first constructor parameter must have constructor
     * with one parameter of type array from every of component types.
     * <p>
     * For example we have constant <code>LIST_BYTE(ByteList.class,new Class[] {byte.class,
     * Byte.class})</code>. This means type ByteList must have at least two constructors
     * with one parameter of type byte[] and Byte[].
     *
     * @return component types
     */
    public Class[] getComponentTypes() {
        return componentTypes;
    }

    /**
     * Try to parse the given <code>str</code> snippet in an object of the type
     * associated to this <code>AttributeType</code>. For instance if the type
     * is <b>Boolean</b>, and <code>str</code> equals <code>true</code>, this
     * method will succeed to return a <code>Boolean</code> instance. May
     * throw <code>NumberFormatException</code>.
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
            case STRING:
                return str;
            case BIGINTEGER:
                return new BigInteger(str);
            case BIGDECIMAL:
                return new BigDecimal(str);
            case TIME_INTERVAL:
                return new TimeInterval(str);

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
     * @return      the compatible <code>AttributeType</code>, or <code>null</code>
     */
    public static AttributeType parse(Object obj) {
        Class<?> type = obj.getClass();

        for (AttributeType attributeType : values()) {
            if (type.equals(attributeType.getType()))
                return attributeType;
        }

        return null;
    }

    /**
     * Build an <code>AttributeType</code> from the given <code>array</code> type.
     * If the given <code>array</code> type match with component types within any
     * AttributeType, appropriate List type will be returned. Returns <code>null</code>
     * otherwise.
     * 
     * @param array the array that is to be parsed
     * @return      the compatible List <code>AttributeType</code>, or <code>null</code>
     */
    public static AttributeType parseFromArray(Object array) {
        Class<?> arrayComponentType = array.getClass().getComponentType();

        if (arrayComponentType != null) {
            for (AttributeType attributeType : values()) {

                if (attributeType.getComponentTypes() != null) {
                    if (Arrays.asList(attributeType.getComponentTypes()).contains(arrayComponentType))
                        return attributeType;
                }
            }
        }

        return null;
    }
}
