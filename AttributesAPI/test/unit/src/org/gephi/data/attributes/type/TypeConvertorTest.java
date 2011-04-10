/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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

import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Škurla
 */
public class TypeConvertorTest {

    @Test
    public void testCreatingInstanceFromStringUsingConversionMethod() {
        int result = TypeConvertor.createInstanceFromString("123", Integer.class);
        assertEquals(result, 123);
    }

    @Test
    public void testCreatingInstanceFromStringUsingConstructor() {
        BigInteger result = TypeConvertor.createInstanceFromString("123", BigInteger.class);
        assertEquals(result, new BigInteger("123"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreatingInstanceFromStringThroughInappropriateObject() {
        TypeConvertor.createInstanceFromString("some text", Object.class);
    }

    @Test
    public void testCreatingArrayFromString() {
        Integer[] result = TypeConvertor.createArrayFromString("1;2;3;4;5", ";", Integer.class);
        assertArrayEquals(result, new Integer[] {1, 2, 3, 4, 5});
    }

    @Test
    public void testGettingWrapperFromPrimitive() {
        Class<?>[] primitiveTypes = {byte.class,  short.class,  int.class,     long.class,
                                     float.class, double.class, boolean.class, char.class};
        Class<?>[] wrapperTypes = {Byte.class,  Short.class,  Integer.class, Long.class,
                                   Float.class, Double.class, Boolean.class, Character.class};

        for (int index = 0; index < primitiveTypes.length; index++) {
            Class<?> primitiveType = primitiveTypes[index];
            Class<?> wrapperType = TypeConvertor.getWrapperFromPrimitive(primitiveType);

            assertEquals(wrapperType, wrapperTypes[index]);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGettingWrapperFromPrimitiveIllegalArgument() {
        TypeConvertor.getWrapperFromPrimitive(String.class);
    }

    @Test
    public void testConvertingPrimitiveToWrapperArray() {
        Integer[] result = TypeConvertor.<Integer>convertPrimitiveToWrapperArray(new int[] {1, 2, 3, 4, 5, 6});

        assertArrayEquals(result, new Integer[] {1, 2, 3, 4, 5, 6});
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertingPrimitiveToWrapperArrayArgumentNotArray() {
        TypeConvertor.convertPrimitiveToWrapperArray(new Object());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertingPrimitiveToWrapperArrayArgumentNotPrimitiveArray() {
        TypeConvertor.convertPrimitiveToWrapperArray(new Object[0]);
    }
}
