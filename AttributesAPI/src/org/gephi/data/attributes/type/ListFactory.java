package org.gephi.data.attributes.type;

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
        else if (componentType == short.class)
            return new ShortList((short[]) array);
        else if (componentType == int.class)
            return new IntegerList((int[]) array);
        else if (componentType == long.class)
            return new LongList((long[]) array);
        else if (componentType == float.class)
            return new FloatList((float[]) array);
        else if (componentType == double.class)
            return new DoubleList((double[]) array);
        else if (componentType == boolean.class)
            return new BooleanList((boolean[]) array);
        else if (componentType == char.class)
            return new CharacterList((char[]) array);
        else if (componentType == String.class)
            return new StringList((String[]) array);
        //TODO add support for wrapper classes and bidinteger/bigdecimal
        // more intelligent approach???

        throw new AssertionError();
    }
}
