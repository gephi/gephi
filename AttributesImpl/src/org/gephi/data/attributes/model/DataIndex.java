/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.data.attributes.model;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
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
import org.gephi.data.attributes.type.TimeInterval;

/**
 * The index where values of the current {@link IndexedAttributeManager} are. This index stores the objects
 * as {@link WeakReference}, so the {@link AttributeRow} may share the objects reference with this index.
 * Moreover when no more objects possess a reference to a value, the {@link WeakReference} system
 * (i.e. Garbage collector) will automatically clean the old references.
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 * @see AttributeType
 */
public class DataIndex {
    @SuppressWarnings("rawtypes")
    private static final Class[] SUPPORTED_TYPES = {
        Byte.class,       Short.class,          Integer.class,     Long.class,
        Float.class,      Double.class,         Boolean.class,     Character.class,
        String.class,     BigInteger.class,     BigDecimal.class,  TimeInterval.class,
        ByteList.class,   ShortList.class,      IntegerList.class, LongList.class,
        FloatList.class,  DoubleList.class,     BooleanList.class, CharacterList.class,
        StringList.class, BigIntegerList.class, BigDecimalList.class};

    @SuppressWarnings("rawtypes")
    private static Map<Class<?>, WeakHashMap> centralHashMap;

//    private WeakHashMap<Byte, WeakReference<Byte>> byteMap;
//    private WeakHashMap<Short, WeakReference<Short>> shortMap;
//    private WeakHashMap<Integer, WeakReference<Integer>> intMap;
//    private WeakHashMap<Long, WeakReference<Long>> longMap;
//    private WeakHashMap<Float, WeakReference<Float>> floatMap;
//    private WeakHashMap<Double, WeakReference<Double>> doubleMap;
//    private WeakHashMap<Boolean, WeakReference<Boolean>> booleanMap;
//    private WeakHashMap<Character, WeakReference<Character>> charMap;
//    private WeakHashMap<String, WeakReference<String>> stringMap;
//    private WeakHashMap<BigInteger, WeakReference<BigInteger>> bigIntegerMap;
//    private WeakHashMap<BigDecimal, WeakReference<BigDecimal>> bigDecimalMap;
//    private WeakHashMap<TimeInterval, WeakReference<TimeInterval>> timeIntervalMap;
//
//    private WeakHashMap<ByteList, WeakReference<ByteList>> byteListMap;
//    private WeakHashMap<ShortList, WeakReference<ShortList>> shortListMap;
//    private WeakHashMap<IntegerList, WeakReference<IntegerList>> integerListMap;
//    private WeakHashMap<LongList, WeakReference<LongList>> longListMap;
//    private WeakHashMap<FloatList, WeakReference<FloatList>> floatListMap;
//    private WeakHashMap<DoubleList, WeakReference<DoubleList>> doubleListMap;
//    private WeakHashMap<BooleanList, WeakReference<BooleanList>> booleanListMap;
//    private WeakHashMap<CharacterList, WeakReference<CharacterList>> characterListMap;
//    private WeakHashMap<StringList, WeakReference<StringList>> stringListMap;
//    private WeakHashMap<BigIntegerList, WeakReference<BigIntegerList>> bigIntegerListMap;
//    private WeakHashMap<BigDecimalList, WeakReference<BigDecimalList>> bigDecimalListMap;

    @SuppressWarnings("rawtypes")
    public DataIndex() {
        centralHashMap = new HashMap<Class<?>, WeakHashMap>();

        for (Class<?> supportedType : SUPPORTED_TYPES)
            putInCentralMap(supportedType);

//        byteMap = new WeakHashMap<Byte, WeakReference<Byte>>();
//        shortMap = new WeakHashMap<Short, WeakReference<Short>>();
//        intMap = new WeakHashMap<Integer, WeakReference<Integer>>();
//        longMap = new WeakHashMap<Long, WeakReference<Long>>();
//        floatMap = new WeakHashMap<Float, WeakReference<Float>>();
//        doubleMap = new WeakHashMap<Double, WeakReference<Double>>();
//        booleanMap = new WeakHashMap<Boolean, WeakReference<Boolean>>();
//        charMap = new WeakHashMap<Character, WeakReference<Character>>();
//        stringMap = new WeakHashMap<String, WeakReference<String>>();
//        bigIntegerMap = new WeakHashMap<BigInteger, WeakReference<BigInteger>>();
//        bigDecimalMap = new WeakHashMap<BigDecimal, WeakReference<BigDecimal>>();
//        timeIntervalMap = new WeakHashMap<TimeInterval, WeakReference<TimeInterval>>();
//
//        byteListMap = new WeakHashMap<ByteList, WeakReference<ByteList>>();
//        shortListMap = new WeakHashMap<ShortList, WeakReference<ShortList>>();
//        integerListMap = new WeakHashMap<IntegerList, WeakReference<IntegerList>>();
//        longListMap = new WeakHashMap<LongList, WeakReference<LongList>>();
//        floatListMap = new WeakHashMap<FloatList, WeakReference<FloatList>>();
//        doubleListMap = new WeakHashMap<DoubleList, WeakReference<DoubleList>>();
//        booleanListMap = new WeakHashMap<BooleanList, WeakReference<BooleanList>>();
//        characterListMap = new WeakHashMap<CharacterList, WeakReference<CharacterList>>();
//        stringListMap = new WeakHashMap<StringList, WeakReference<StringList>>();
//        bigIntegerListMap = new WeakHashMap<BigIntegerList, WeakReference<BigIntegerList>>();
//        bigDecimalListMap = new WeakHashMap<BigDecimalList, WeakReference<BigDecimalList>>();
    }

    private static <T> void putInCentralMap(Class<T> supportedType) {
        centralHashMap.put(supportedType, new WeakHashMap<T, WeakReference<T>>());
    }

    public int countEntries() {
        int entries = 0;

        for (WeakHashMap<?,?> weakHashMap : centralHashMap.values()) {
            entries += weakHashMap.size();
        }
//        entries += byteMap.size();
//        entries += shortMap.size();
//        entries += intMap.size();
//        entries += longMap.size();
//        entries += floatMap.size();
//        entries += doubleMap.size();
//        entries += booleanMap.size();
//        entries += charMap.size();
//        entries += stringMap.size();
//        entries += bigIntegerMap.size();
//        entries += bigDecimalMap.size();
//        entries += timeIntervalMap.size();
//
//        entries += byteListMap.size();
//        entries += shortListMap.size();
//        entries += integerListMap.size();
//        entries += longListMap.size();
//        entries += floatListMap.size();
//        entries += doubleListMap.size();
//        entries += booleanListMap.size();
//        entries += characterListMap.size();
//        entries += stringListMap.size();
//        entries += bigIntegerListMap.size();
//        entries += bigDecimalListMap.size();

        return entries;
    }

//    Float pushData(Float data) {
//        WeakReference<Float> value = floatMap.get(data);
//        if (value == null) {
//            WeakReference<Float> weakRef = new WeakReference<Float>(data);
//            floatMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    Integer pushData(Integer data) {
//        WeakReference<Integer> value = intMap.get(data);
//        if (value == null) {
//            WeakReference<Integer> weakRef = new WeakReference<Integer>(data);
//            intMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    Boolean pushData(Boolean data) {
//        WeakReference<Boolean> value = booleanMap.get(data);
//        if (value == null) {
//            WeakReference<Boolean> weakRef = new WeakReference<Boolean>(data);
//            booleanMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    String pushData(String data) {
//        WeakReference<String> value = stringMap.get(data);
//        if (value == null) {
//            WeakReference<String> weakRef = new WeakReference<String>(data);
//            stringMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    Double pushData(Double data) {
//        WeakReference<Double> value = doubleMap.get(data);
//        if (value == null) {
//            WeakReference<Double> weakRef = new WeakReference<Double>(data);
//            doubleMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    Long pushData(Long data) {
//        WeakReference<Long> value = longMap.get(data);
//        if (value == null) {
//            WeakReference<Long> weakRef = new WeakReference<Long>(data);
//            longMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    StringList pushData(StringList data) {
//        WeakReference<StringList> value = stringListMap.get(data);
//        if (value == null) {
//            WeakReference<StringList> weakRef = new WeakReference<StringList>(data);
//            stringListMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    TimeInterval pushData(TimeInterval data) {
//        WeakReference<TimeInterval> value = timeIntervalMap.get(data);
//        if (value == null) {
//            WeakReference<TimeInterval> weakRef = new WeakReference<TimeInterval>(data);
//            timeIntervalMap.put(data, weakRef);
//            return data;
//        }
//
//        return value.get();
//    }
//
//    IntegerList pushData(IntegerList data) {
//        WeakReference<IntegerList> value = integerListMap.get(data);
//        if (value == null) {
//            WeakReference<IntegerList> weakRef = new WeakReference<IntegerList>(data);
//            integerListMap.put(data, weakRef);
//            return data;
//        }
//        return value.get();
//    }
//
//    FloatList pushData(FloatList data) {
//        WeakReference<FloatList> value = floatListMap.get(data);
//        if (value == null) {
//            WeakReference<FloatList> weakRef = new WeakReference<FloatList>(data);
//            floatListMap.put(data, weakRef);
//            return data;
//        }
//        return value.get();
//    }
//
//    DoubleList pushData(DoubleList data) {
//        WeakReference<DoubleList> value = doubleListMap.get(data);
//        if (value == null) {
//            WeakReference<DoubleList> weakRef = new WeakReference<DoubleList>(data);
//            doubleListMap.put(data, weakRef);
//            return data;
//        }
//        return value.get();
//    }
//
//    BooleanList pushData(BooleanList data) {
//        WeakReference<BooleanList> value = booleanListMap.get(data);
//        if (value == null) {
//            WeakReference<BooleanList> weakRef = new WeakReference<BooleanList>(data);
//            booleanListMap.put(data, weakRef);
//            return data;
//        }
//        return value.get();
//    }
//
//    LongList pushData(LongList data) {
//        WeakReference<LongList> value = longListMap.get(data);
//        if (value == null) {
//            WeakReference<LongList> weakRef = new WeakReference<LongList>(data);
//            longListMap.put(data, weakRef);
//            return data;
//        }
//        return value.get();
//    }
//
//    BigIntegerList pushData(BigIntegerList data) {
//        WeakReference<BigIntegerList> value = bigIntegerListMap.get(data);
//        if (value == null) {
//            WeakReference<BigIntegerList> weakRef = new WeakReference<BigIntegerList>(data);
//            bigIntegerListMap.put(data, weakRef);
//            return data;
//        }
//        return value.get();
//    }

    @SuppressWarnings("unchecked")
    <T> T pushData(T data) {
        Class<?> classObjectKey = data.getClass();
        WeakHashMap<T, WeakReference<T>> weakHashMap = centralHashMap.get(classObjectKey);

        if (weakHashMap == null)
            throw new IllegalArgumentException();

        WeakReference<T> value = weakHashMap.get(data);
        if (value == null) {
            WeakReference<T> weakRef = new WeakReference<T>(data);
            weakHashMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    public void clear() {
        for (WeakHashMap<?,?> weakHashMap : centralHashMap.values()) {
            weakHashMap.clear();
        }
//        byteMap.clear();
//        shortMap.clear();
//        intMap.clear();
//        longMap.clear();
//        floatMap.clear();
//        doubleMap.clear();
//        booleanMap.clear();
//        charMap.clear();
//        stringMap.clear();
//        bigIntegerMap.clear();
//        bigDecimalMap.clear();
//        timeIntervalMap.clear();
//
//        stringListMap.clear();
//        integerListMap.clear();
//        floatListMap.clear();
//        doubleListMap.clear();
//        longListMap.clear();
//        booleanListMap.clear();
//        bigIntegerListMap.clear();
    }
}
