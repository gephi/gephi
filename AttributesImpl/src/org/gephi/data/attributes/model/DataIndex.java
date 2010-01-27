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
import java.util.WeakHashMap;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.attributes.type.TimeInterval;

/**
 * The index where values of the current {@link IndexedAttributeManager} are. This index stores the objects
 * as {@link WeakReference}, so the {@link AttributeRow} may share the objects reference with this index.
 * Moreover when no more objects possess a reference to a value, the {@link WeakReference} system
 * (i.e. Garbage collector) will automatically clean the old references.
 * 
 * @author Mathieu Bastian
 * @see AttributeType
 */
public class DataIndex {

    private WeakHashMap<Float, WeakReference<Float>> floatMap;
    private WeakHashMap<Integer, WeakReference<Integer>> intMap;
    private WeakHashMap<String, WeakReference<String>> stringMap;
    private WeakHashMap<Boolean, WeakReference<Boolean>> booleanMap;
    private WeakHashMap<StringList, WeakReference<StringList>> stringListMap;
    private WeakHashMap<Long, WeakReference<Long>> longMap;
    private WeakHashMap<Double, WeakReference<Double>> doubleMap;
    private WeakHashMap<TimeInterval, WeakReference<TimeInterval>> timeIntervalMap;

    public DataIndex() {
        floatMap = new WeakHashMap<Float, WeakReference<Float>>();
        intMap = new WeakHashMap<Integer, WeakReference<Integer>>();
        stringMap = new WeakHashMap<String, WeakReference<String>>();
        booleanMap = new WeakHashMap<Boolean, WeakReference<Boolean>>();
        stringListMap = new WeakHashMap<StringList, WeakReference<StringList>>();
        longMap = new WeakHashMap<Long, WeakReference<Long>>();
        doubleMap = new WeakHashMap<Double, WeakReference<Double>>();
        timeIntervalMap = new WeakHashMap<TimeInterval, WeakReference<TimeInterval>>();
    }

    public int countEntries() {
        int entries = 0;
        entries += floatMap.size();
        entries += intMap.size();
        entries += stringMap.size();
        entries += booleanMap.size();
        entries += stringListMap.size();
        entries += longMap.size();
        entries += doubleMap.size();
        entries += timeIntervalMap.size();

        return entries;
    }

    Float pushData(Float data) {
        WeakReference<Float> value = floatMap.get(data);
        if (value == null) {
            WeakReference<Float> weakRef = new WeakReference<Float>(data);
            floatMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    Integer pushData(Integer data) {
        WeakReference<Integer> value = intMap.get(data);
        if (value == null) {
            WeakReference<Integer> weakRef = new WeakReference<Integer>(data);
            intMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    Boolean pushData(Boolean data) {
        WeakReference<Boolean> value = booleanMap.get(data);
        if (value == null) {
            WeakReference<Boolean> weakRef = new WeakReference<Boolean>(data);
            booleanMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    String pushData(String data) {
        WeakReference<String> value = stringMap.get(data);
        if (value == null) {
            WeakReference<String> weakRef = new WeakReference<String>(data);
            stringMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    Double pushData(Double data) {
        WeakReference<Double> value = doubleMap.get(data);
        if (value == null) {
            WeakReference<Double> weakRef = new WeakReference<Double>(data);
            doubleMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    Long pushData(Long data) {
        WeakReference<Long> value = longMap.get(data);
        if (value == null) {
            WeakReference<Long> weakRef = new WeakReference<Long>(data);
            longMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    StringList pushData(StringList data) {
        WeakReference<StringList> value = stringListMap.get(data);
        if (value == null) {
            WeakReference<StringList> weakRef = new WeakReference<StringList>(data);
            stringListMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    TimeInterval pushData(TimeInterval data) {
        WeakReference<TimeInterval> value = timeIntervalMap.get(data);
        if (value == null) {
            WeakReference<TimeInterval> weakRef = new WeakReference<TimeInterval>(data);
            timeIntervalMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    public void clear() {
        stringListMap.clear();
        stringMap.clear();
        floatMap.clear();
        booleanMap.clear();
        intMap.clear();
        longMap.clear();
        doubleMap.clear();
        timeIntervalMap.clear();
    }
}
