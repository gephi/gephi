/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla, Cezary Bartosiak
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
package org.gephi.data.attributes.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.gephi.data.attributes.type.*;

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
     * Returns the
     * <code>Class</code> the type is associated with.
     *
     * @return the <code>class</code> the type is associated with
     */
    public Class getType() {
        return type;
    }

    /**
     * Try to parse the given
     * <code>str</code> snippet in an object of the type associated to this
     * <code>AttributeType</code>. For instance if the type is <b>Boolean</b>, and
     * <code>str</code> equals
     * <code>true</code>, this method will succeed to return a
     * <code>Boolean</code> instance. May throw
     * <code>NumberFormatException</code>.
     *
     * <code>DYNAMIC</code> types and
     * <code>TIME_INTERVAL</code> cannot be parsed with this method (see
     * <code>isDynamicType</code> method) and a UnsupportedOperationException will be thrown if it is tried.
     *
     * @param str the string that is to be parsed
     * @return an instance of the type of this or null if not able to parse given string as the type <code>AttributeType</code>.
     */
    public Object parse(String str) {
        try {
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
                    return Boolean.valueOf(str);
                case CHAR:
                    return new Character(str.charAt(0));
                case BIGINTEGER:
                    return new BigInteger(removeDecimalDigitsFromString(str));
                case BIGDECIMAL:
                    return new BigDecimal(str);
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
                    return parseDynamic(str);
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
        } catch (Exception e) {
            return null;//Any parse exception due to incorrect syntax
        }
    }

    private Object parseDynamic(String str) {
        List<Interval> intervals;
        try {
            intervals = DynamicParser.parseIntervals(this, str);
        } catch (Exception ex) {
            return null;//Unable to parse
        }

        return createDynamicObject(intervals);
    }

    private DynamicType createDynamicObject(List<Interval> in) {
        if (!this.isDynamicType()) {
            return null;
        }

        switch (this) {
            case DYNAMIC_BYTE: {
                ArrayList<Interval<Byte>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Byte>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Byte>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Byte) interval.getValue()));
                    }
                }
                return new DynamicByte(lin);
            }
            case DYNAMIC_SHORT: {
                ArrayList<Interval<Short>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Short>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Short>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Short) interval.getValue()));
                    }
                }
                return new DynamicShort(lin);
            }
            case DYNAMIC_INT: {
                ArrayList<Interval<Integer>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Integer>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Integer>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Integer) interval.getValue()));
                    }
                }
                return new DynamicInteger(lin);
            }
            case DYNAMIC_LONG: {
                ArrayList<Interval<Long>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Long>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Long>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Long) interval.getValue()));
                    }
                }
                return new DynamicLong(lin);
            }
            case DYNAMIC_FLOAT: {
                ArrayList<Interval<Float>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Float>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Float>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Float) interval.getValue()));
                    }
                }
                return new DynamicFloat(lin);
            }
            case DYNAMIC_DOUBLE: {
                ArrayList<Interval<Double>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Double>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Double>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Double) interval.getValue()));
                    }
                }
                return new DynamicDouble(lin);
            }
            case DYNAMIC_BOOLEAN: {
                ArrayList<Interval<Boolean>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Boolean>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Boolean>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Boolean) interval.getValue()));
                    }
                }
                return new DynamicBoolean(lin);
            }
            case DYNAMIC_CHAR: {
                ArrayList<Interval<Character>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<Character>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<Character>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (Character) interval.getValue()));
                    }
                }
                return new DynamicCharacter(lin);
            }
            case DYNAMIC_STRING: {
                ArrayList<Interval<String>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<String>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<String>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (String) interval.getValue()));
                    }
                }
                return new DynamicString(lin);
            }
            case DYNAMIC_BIGINTEGER: {
                ArrayList<Interval<BigInteger>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<BigInteger>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<BigInteger>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (BigInteger) interval.getValue()));
                    }
                }
                return new DynamicBigInteger(lin);
            }
            case DYNAMIC_BIGDECIMAL: {
                ArrayList<Interval<BigDecimal>> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval<BigDecimal>>();
                    for (Interval interval : in) {
                        lin.add(new Interval<BigDecimal>(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded(), (BigDecimal) interval.getValue()));
                    }
                }
                return new DynamicBigDecimal(lin);
            }
            case TIME_INTERVAL: {
                ArrayList<Interval> lin = null;
                if (in != null) {
                    lin = new ArrayList<Interval>();
                    for (Interval interval : in) {
                        lin.add(new Interval(interval.getLow(), interval.getHigh(),
                                interval.isLowExcluded(), interval.isHighExcluded()));
                    }
                }
                return new TimeInterval(lin);
            }
            default:
                return null;
        }
    }

    /**
     * Build an
     * <code>AttributeType</code> from the given
     * <code>obj</code> type. If the given
     * <code>obj</code> class match with an
     * <code>AttributeType</code> type, returns this type. Returns
     * <code>null</code> otherwise. <p> For instance if <b>obj instanceof Float</b> equals <b>true</b>, returns
     * <code>AttributeType.FLOAT</code>.
     *
     * @param obj the object that is to be parsed
     * @return the compatible <code>AttributeType</code>, or <code>null</code> if no type is found or the input object is null
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
     * Build an dynamic
     * <code>AttributeType</code> from the given
     * <code>obj</code> type. If the given
     * <code>obj</code> class match with an
     * <code>AttributeType</code> type, returns this type. Returns
     * <code>null</code> otherwise. <p> For instance if <b>obj instanceof Float</b> equals <b>true</b>, returns
     * <code>AttributeType.DYNAMIC_FLOAT</code>.
     *
     * @param obj the object that is to be parsed
     * @return the compatible <code>AttributeType</code>, or <code>null</code>
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
     * @return {@code true} if this is a {@code DynamicType}, {@code false} otherwise
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

    public boolean isListType() {
        if (this.equals(LIST_BIGDECIMAL)
                || this.equals(LIST_BIGINTEGER)
                || this.equals(LIST_BOOLEAN)
                || this.equals(LIST_BYTE)
                || this.equals(LIST_CHARACTER)
                || this.equals(LIST_DOUBLE)
                || this.equals(LIST_FLOAT)
                || this.equals(LIST_INTEGER)
                || this.equals(LIST_LONG)
                || this.equals(LIST_SHORT)
                || this.equals(LIST_STRING)) {
            return true;
        }
        return false;
    }

    /**
     * Removes the decimal digits and point of the numbers of string when necessary. Used for trying to parse decimal numbers as not decimal. For example BigDecimal to BigInteger.
     *
     * @param s String to remove decimal digits
     * @return String without dot and decimal digits.
     */
    public static String removeDecimalDigitsFromString(String s) {
        return removeDecimalDigitsFromStringPattern.matcher(s).replaceAll("");
    }
    private static final Pattern removeDecimalDigitsFromStringPattern = Pattern.compile("\\.[0-9]*");
}
