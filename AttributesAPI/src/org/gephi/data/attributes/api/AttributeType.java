/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla, Cezary Bartosiak
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
package org.gephi.data.attributes.api;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.regex.Pattern;
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
import org.gephi.data.attributes.type.Interval;
import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte0;

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
     * <code>DYNAMIC</code> types and <code>TIME_INTERVAL</code> cannot be parsed with this method (see <code>isDynamicType</code> method) and a UnsupportedOperationException will be thrown if it is tried.
     * 
     * @param str   the string that is to be parsed
     * @return      an instance of the type of this  <code>AttributeType</code>.
     */
    public Object parse(String str) {
        switch (this) {
            case BYTE:
                return new Byte(removeDecimalDigitsFromString(str));
            case SHORT:
                return new Short(removeDecimalDigitsFromString(str));
            case INT:
                return new Integer(removeDecimalDigitsFromString(str));
            case LONG:
                return new Long(removeDecimalDigitsFromString(str));
            case FLOAT:
                return new Float(str);
            case DOUBLE:
                return new Double(str);
            case BOOLEAN:
                return new Boolean(str);
            case CHAR:
                return new Character(str.charAt(0));
            case BIGINTEGER:
                return new BigInteger(removeDecimalDigitsFromString(str));
            case BIGDECIMAL:
                return new BigDecimal(str);
            case DYNAMIC_BYTE:
                return new DynamicByte(new Interval<Byte>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Byte(str)));
            case DYNAMIC_SHORT:
                return new DynamicShort(new Interval<Short>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Short(str)));
            case DYNAMIC_INT:
                return new DynamicInteger(new Interval<Integer>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Integer(str)));
            case DYNAMIC_LONG:
                return new DynamicLong(new Interval<Long>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Long(str)));
            case DYNAMIC_FLOAT:
                return new DynamicFloat(new Interval<Float>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Float(str)));
            case DYNAMIC_DOUBLE:
                return new DynamicDouble(new Interval<Double>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Double(str)));
            case DYNAMIC_BOOLEAN:
                return new DynamicBoolean(new Interval<Boolean>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Boolean(str)));
            case DYNAMIC_CHAR:
                return new DynamicCharacter(new Interval<Character>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new Character(str.charAt(0))));
            case DYNAMIC_STRING:
                return new DynamicString(new Interval<String>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, str));
            case DYNAMIC_BIGINTEGER:
                return new DynamicBigInteger(new Interval<BigInteger>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new BigInteger(str)));
            case DYNAMIC_BIGDECIMAL:
                return new DynamicBigDecimal(new Interval<BigDecimal>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new BigDecimal(str)));
            case TIME_INTERVAL:
                throw new UnsupportedOperationException("Not supported.");
            case LIST_BYTE:
                return new ByteList(removeDecimalDigitsFromString(str));
            case LIST_SHORT:
                return new ShortList(removeDecimalDigitsFromString(str));
            case LIST_INTEGER:
                return new IntegerList(removeDecimalDigitsFromString(str));
            case LIST_LONG:
                return new LongList(removeDecimalDigitsFromString(str));
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
                return new BigIntegerList(removeDecimalDigitsFromString(str));
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

    /**
     * Indicates if this type is a {@code DynamicType}.
     *
     * @param type an {@code AttributeType} to check
     *
     * @return {@code true} if this is a {@code DynamicType},
     *         otherwise {@code false}.
     */
    public boolean isDynamicType() {
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
                return true;
            default:
                return false;
        }
    }

    /**
     * Removes the decimal digits and point of the numbers of string when necessary.
     * Used for trying to parse decimal numbers as not decimal.
     * For example BigDecimal to BigInteger.
     * @param s String to remove decimal digits
     * @return String without dot and decimal digits.
     */
    private String removeDecimalDigitsFromString(String s){
        return removeDecimalDigitsFromStringPattern.matcher(s).replaceAll("");
    }

    private static final Pattern removeDecimalDigitsFromStringPattern=Pattern.compile("\\.[0-9]*");
}
