/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla <bujacik@gmail.com>
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

    @SuppressWarnings("rawtypes")
    public DataIndex() {
        centralHashMap = new HashMap<Class<?>, WeakHashMap>();

        for (Class<?> supportedType : SUPPORTED_TYPES)
            putInCentralMap(supportedType);
    }

    private static <T> void putInCentralMap(Class<T> supportedType) {
        centralHashMap.put(supportedType, new WeakHashMap<T, WeakReference<T>>());
    }

    public int countEntries() {
        int entries = 0;

        for (WeakHashMap<?,?> weakHashMap : centralHashMap.values())
            entries += weakHashMap.size();

        return entries;
    }

    @SuppressWarnings("unchecked")
    <T> T pushData(T data) {
        Class<?> classObjectKey = data.getClass();
        WeakHashMap<T, WeakReference<T>> weakHashMap = centralHashMap.get(classObjectKey);

        if (weakHashMap == null)
            return data;

        WeakReference<T> value = weakHashMap.get(data);
        if (value == null) {
            WeakReference<T> weakRef = new WeakReference<T>(data);
            weakHashMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    public void clear() {
        for (WeakHashMap<?,?> weakHashMap : centralHashMap.values())
            weakHashMap.clear();
    }
}
