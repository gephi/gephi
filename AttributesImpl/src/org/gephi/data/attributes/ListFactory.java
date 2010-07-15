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
