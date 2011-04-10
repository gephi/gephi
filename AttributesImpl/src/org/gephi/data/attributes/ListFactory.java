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

package org.gephi.data.attributes;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.gephi.data.attributes.type.AbstractList;
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

/**
 *
 * @author Martin Škurla
 */
public class ListFactory {
    private ListFactory() {}

    public static AbstractList<?> fromArray(Object array) {
        Class<?> componentType = array.getClass().getComponentType();

        if (componentType == byte.class)
            return new ByteList((byte[]) array);
        else if (componentType == Byte.class)
            return new ByteList((Byte[]) array);

        else if (componentType == short.class)
            return new ShortList((short[]) array);
        else if (componentType == Short.class)
            return new ShortList((Short[]) array);

        else if (componentType == int.class)
            return new IntegerList((int[]) array);
        else if (componentType == Integer.class)
            return new IntegerList((Integer[]) array);

        else if (componentType == long.class)
            return new LongList((long[]) array);
        else if (componentType == Long.class)
            return new LongList((Long[]) array);

        else if (componentType == float.class)
            return new FloatList((float[]) array);
        else if (componentType == Float.class)
            return new FloatList((Float[]) array);

        else if (componentType == double.class)
            return new DoubleList((double[]) array);
        else if (componentType == Double.class)
            return new DoubleList((Double[]) array);

        else if (componentType == boolean.class)
            return new BooleanList((boolean[]) array);
        else if (componentType == Boolean.class)
            return new BooleanList((Boolean[]) array);

        else if (componentType == char.class)
            return new CharacterList((char[]) array);
        else if (componentType == Character.class)
            return new CharacterList((Character[]) array);

        else if (componentType == String.class)
            return new StringList((String[]) array);

        else if (componentType == BigInteger.class)
            return new BigIntegerList((BigInteger[]) array);

        else if (componentType == BigDecimal.class)
            return new BigDecimalList((BigDecimal[]) array);

        throw new AssertionError();
    }
}
