/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>, Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.data.attributes.type;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.gephi.data.attributes.api.AttributeType;

/**
 * Class responsible for type manipulation and creation needed in Attributes API.
 *
 * @author Martin Škurla
 * @author Mathieu Bastian
 */
public final class TypeConvertor {

    private static final String CONVERSION_METHOD_NAME = "valueOf";

    private TypeConvertor() {
    }

    /**
     * Creates array of given type from single String value. String value is always parsed by given
     * separator into smaller chunks. Every chunk will represent independent object in final array.
     * The exact conversion process from String value into final type is done by
     * {@link #createInstanceFromString createInstanceFromString} method.
     *
     * @param <T>       type parameter representing final array type
     * @param input     input
     * @param separator separator which will be used in the process of tokenizing input
     * @param finalType type of final array
     * 
     * @return final array
     *
     * @throws NullPointerException     if any of given parameters is null
     * @throws IllegalArgumentException if array of given type cannot be created
     *
     * @see #createInstanceFromString createInstanceFromString
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArrayFromString(String input, String separator, Class<T> finalType) {
        if (input == null || separator == null || finalType == null) {
            throw new NullPointerException();
        }

        String[] stringValues = input.split(separator);
        T[] resultList = (T[]) Array.newInstance(finalType, stringValues.length);

        for (int i = 0; i < stringValues.length; i++) {
            String stringValue = stringValues[i].trim();
            T resultValue = null;

            if (finalType == String.class) {
                resultValue = (T) stringValue;
            } else {
                resultValue = TypeConvertor.<T>createInstanceFromString(stringValue, finalType);
            }

            resultList[i] = resultValue;
        }
        return resultList;
    }

    /**
     * Transforms String value to any kind of object with given type. The concrete conversion
     * must be done by the type itself. This assumes, that given type defines at least one of the
     * following:
     * <ul>
     * <li>public constructor with single parameter of type String
     * <li>factory method "valueOf" with single parameter of type String<br />
     * If given type does not definy any of these requirements, IllegalArgumentException will be
     * thrown.
     * 
     * @param <T>       type parameter representing final type
     * @param input     input
     * @param finalType type of final object
     *
     * @return final object
     *
     * @throws NullPointerException     if any of given parameters is null
     * @throws IllegalArgumentException if given type cannot be created
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstanceFromString(String input, Class<T> finalType) {
        if (input == null || finalType == null) {
            throw new NullPointerException();
        }

        T resultValue = null;

        try {
            Method conversionMethod = finalType.getMethod(CONVERSION_METHOD_NAME, String.class);

            resultValue = (T) conversionMethod.invoke(null, input);
        } catch (NoSuchMethodException e) {
            try {
                Constructor<T> constructor = finalType.getConstructor(String.class);
                resultValue = constructor.newInstance(input);
            } catch (NoSuchMethodException e1) {
                String errorMessage = String.format(
                        "Type '%s' does not have neither method 'T %s(String)' nor  constructor '<init>(String)'...",
                        finalType,
                        CONVERSION_METHOD_NAME);

                throw new IllegalArgumentException(errorMessage);
            } catch (Exception e2) {
            }
        } catch (Exception e) {
        }
        return resultValue;
    }

    /**
     * Converts given array of primitive type into array of wrapper type.
     *
     * @param <T>            type parameter representing final wrapper type
     * @param primitiveArray primitive array
     * 
     * @return wrapper array
     *
     * @throws NullPointerException     if given parameter is null
     * @throws IllegalArgumentException if given parameter is not array or given parameter is not
     *                                  array of primitive type
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] convertPrimitiveToWrapperArray(Object primitiveArray) {
        if (primitiveArray == null) {
            throw new NullPointerException();
        }

        if (!primitiveArray.getClass().isArray()) {
            throw new IllegalArgumentException("Given object is not of primitive array: " + primitiveArray.getClass());
        }

        Class<?> primitiveClass = primitiveArray.getClass().getComponentType();
        Class<T> wrapperClass = (Class<T>) getWrapperFromPrimitive(primitiveClass);
        int arrayLength = Array.getLength(primitiveArray);
        T[] wrapperArray = (T[]) Array.newInstance(wrapperClass, arrayLength);

        for (int i = 0; i < arrayLength; i++) {
            T arrayItem = (T) Array.get(primitiveArray, i);
            wrapperArray[i] = arrayItem;
        }

        return wrapperArray;
    }

    /**
     * Returns wrapper type from given primitive type.
     *
     * @param primitiveType primitive type
     * 
     * @return wrapper type
     *
     * @throws NullPointerException     if given parameter is null
     * @throws IllegalArgumentException if given parameter is not a primitive type
     */
    public static Class<?> getWrapperFromPrimitive(Class<?> primitiveType) {
        if (primitiveType == null) {
            throw new NullPointerException();
        }

        if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        }

        throw new IllegalArgumentException("Given type '" + primitiveType + "' is not primitive...");
    }

    /**
     * Returns the underlying static type from <code>dynamicType</code> For example
     * returns <code>FLOAT</code> if given type is <code>DYNAMIC_FLOAT</code>.
     * @param dynamicType a dynamic type
     * @return the underlying static type
     * @throws IllegalArgumentException if <code>dynamicType</code> is not dynamic
     */
    public static AttributeType getStaticType(AttributeType dynamicType) {
        if (!dynamicType.isDynamicType()) {
            throw new IllegalArgumentException("Given type '" + dynamicType + "' is not dynamic.");
        }
        switch (dynamicType) {
            case DYNAMIC_BIGDECIMAL:
                return AttributeType.BIGDECIMAL;
            case DYNAMIC_BIGINTEGER:
                return AttributeType.BIGINTEGER;
            case DYNAMIC_BOOLEAN:
                return AttributeType.BOOLEAN;
            case DYNAMIC_BYTE:
                return AttributeType.BYTE;
            case DYNAMIC_CHAR:
                return AttributeType.CHAR;
            case DYNAMIC_DOUBLE:
                return AttributeType.DOUBLE;
            case DYNAMIC_FLOAT:
                return AttributeType.FLOAT;
            case DYNAMIC_INT:
                return AttributeType.INT;
            case DYNAMIC_LONG:
                return AttributeType.LONG;
            case DYNAMIC_SHORT:
                return AttributeType.SHORT;
            case DYNAMIC_STRING:
                return AttributeType.STRING;
            default:
                return null;
        }
    }
}
