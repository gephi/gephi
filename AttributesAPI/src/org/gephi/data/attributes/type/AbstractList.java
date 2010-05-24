/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @author Martin Škurla
 * @param <T>
 */
public abstract class AbstractList<T> {

    public static final String DEFAULT_SEPARATOR = ",|;";
    protected final T[] list;
    private volatile int hashCode = 0;

    public AbstractList(String value, Class<T> finalType) {
        this(value, DEFAULT_SEPARATOR, finalType);
    }

    public AbstractList(String value, String separator, Class<T> finalType) {
        this(AbstractList.<T>parse(value, separator, finalType));
    }

    public AbstractList(T[] list) {
        this.list = Arrays.copyOf(list, list.length);
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] parse(String value, String separator, Class<T> finalType) {
        if (value == null || separator == null || finalType == null) {
            throw new NullPointerException();
        }

        String[] stringValueList = value.split(separator);
        T[] resultList = (T[]) Array.newInstance(finalType, stringValueList.length);

        for (int i = 0; i < stringValueList.length; i++) {
            String stringValue = stringValueList[i].trim();
            T resultValue = null;

            if (finalType == String.class) {
                resultValue = (T) stringValue;
            } else {
                resultValue = AbstractList.<T>createInstance(stringValue, finalType);
            }

            resultList[i] = resultValue;
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(String value, Class<T> finalType) {
        T resultValue = null;

        try {
            Method method = finalType.getMethod("valueOf", String.class);
            resultValue = (T) method.invoke(null, value);
        } catch (NoSuchMethodException e) {
            try {
                Constructor<T> constructor = finalType.getConstructor(String.class);
                resultValue = constructor.newInstance(value);
            } catch (NoSuchMethodException e1) {
                throw new IllegalArgumentException("Type '" + finalType + "' does not have either method valueOf(String) or  constructor <init>  (String)...");
            } catch (Exception e2) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultValue;
    }

    public int size() {
        return list.length;
    }

    public T getItem(int index) {
        if (index >= list.length) {
            return null;
        }

        return list[index];
    }

    public boolean contains(T value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.length; i++) {
            builder.append(list[i]);
            builder.append(',');
        }

        if (list.length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractList<?>)) {
            return false;
        }

        AbstractList<?> s = (AbstractList<?>) obj;

        if (s.size() != this.size()) {
            return false;
        }

        for (int i = 0; i < list.length; i++) {
            if (this.getItem(i) != s.getItem(i)) {
                if (!this.getItem(i).equals(s.getItem(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int hash = 7;

            for (int i = 0; i < list.length; i++) {
                hash = 53 * hash + (this.list[i] != null ? this.list[i].hashCode() : 0);
            }
            hashCode = hash;
        }
        return hashCode;
    }
}

