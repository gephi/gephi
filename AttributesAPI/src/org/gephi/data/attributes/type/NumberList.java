/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

import java.lang.reflect.Array;

/**
 *
 * @author Martin Škurla
 */
public abstract class NumberList<T extends Number> extends AbstractList<T> {

    public NumberList(T[] wrapperArray) {
        super(wrapperArray);
    }

    public NumberList(Object primitiveArray, int arrayLength) {
        super(NumberList.<T>parse(primitiveArray, arrayLength));
    }

    public NumberList(String value, Class<T> finalType) {
        this(value, AbstractList.DEFAULT_SEPARATOR, finalType);
    }

    public NumberList(String value, String separator, Class<T> finalType) {
        super(value, separator, finalType);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> T[] parse(Object primitiveArray, int arrayLength) {
        if (primitiveArray == null) {
            throw new NullPointerException();
        }

        Class<T> wrapperClass = (Class<T>) getWrapperClass(primitiveArray);
        T[] wrapperArray = (T[]) Array.newInstance(wrapperClass, arrayLength);

        if (primitiveArray.getClass().isArray()) {
            for (int i = 0; i < arrayLength; i++) {
                T arrayItem = (T) Array.get(primitiveArray, i);
                wrapperArray[i] = arrayItem;
            }
        } else {
            throw new IllegalArgumentException("Given object is not of primitive array primitiveArray.getClass()");
        }

        return wrapperArray;
    }

    private static Class<?> getWrapperClass(Object primitiveArray) {
        Class<?> primitiveArrayType = primitiveArray.getClass().getComponentType();

        if (primitiveArrayType == byte.class) {
            return Byte.class;
        } else if (primitiveArrayType == short.class) {
            return Short.class;
        } else if (primitiveArrayType == int.class) {
            return Integer.class;
        } else if (primitiveArrayType == long.class) {
            return Long.class;
        } else if (primitiveArrayType == float.class) {
            return Float.class;
        } else if (primitiveArrayType == double.class) {
            return Double.class;
        } else if (primitiveArrayType == boolean.class) {
            return Boolean.class;
        } else if (primitiveArrayType == char.class) {
            return Character.class;
        }

        throw new IllegalArgumentException("Given parameter '" + primitiveArray.getClass() + "' is not array of primitive type...");
    }
}

