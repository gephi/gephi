package org.gephi.filters.plugin.attribute;


import java.util.List;


class ComparableArrayConverter {
    private ComparableArrayConverter() {}

    @SuppressWarnings("rawtypes")
    public static Comparable[] convert(Object[] objectArray) {
        Comparable[] comparableArray = new Comparable [objectArray.length];
        for (int index = 0; index < comparableArray.length; index++)
            comparableArray[index] = (Comparable) objectArray[index];

        return comparableArray;
    }

    @SuppressWarnings("rawtypes")
    public static Comparable[] convert(List<? extends Object> objectList) {
        Comparable[] compatableArray = new Comparable [objectList.size()];
        for (int index = 0; index < compatableArray.length; index++)
            compatableArray[index] = (Comparable) objectList.get(index);

        return compatableArray;
    }
}
