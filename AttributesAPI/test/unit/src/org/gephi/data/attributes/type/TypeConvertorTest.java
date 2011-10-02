/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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
