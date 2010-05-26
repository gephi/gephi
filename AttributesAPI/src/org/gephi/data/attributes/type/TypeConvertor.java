/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>
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
package org.gephi.data.attributes.type;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Class responsible for manipulation of 
 *
 * @author Martin Škurla
 */
public final class TypeConvertor {
    private static final String CONVERSION_METHOD_NAME = "valueOf";

    private TypeConvertor() {}

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

    @SuppressWarnings("unchecked")
    public static <T> T createInstanceFromString(String input, Class<T> finalType) {
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
                e2.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] convertPrimitiveToWrapperArray(Object primitiveArray) {
        if (primitiveArray == null) {
            throw new NullPointerException();
        }

        Class<?> primitiveClass = primitiveArray.getClass().getComponentType();
        Class<T> wrapperClass = (Class<T>) getWrapperFromPrimitive(primitiveClass);
        int arrayLength = Array.getLength(primitiveArray);
        T[] wrapperArray = (T[]) Array.newInstance(wrapperClass, arrayLength);

        if (primitiveArray.getClass().isArray()) {
            for (int i = 0; i < arrayLength; i++) {
                T arrayItem = (T) Array.get(primitiveArray, i);
                wrapperArray[i] = arrayItem;
            }
        } else {
            throw new IllegalArgumentException("Given object is not of primitive array: " + primitiveArray.getClass());
        }

        return wrapperArray;
    }

    public static Class<?> getWrapperFromPrimitive(Class<?> primitiveType) {
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
}
