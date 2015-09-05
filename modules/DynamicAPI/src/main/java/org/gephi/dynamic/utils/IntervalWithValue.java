package org.gephi.dynamic.utils;

import org.gephi.attribute.time.Interval;

/**
 * Represents an Interval with an associated value for it
 * @author Eduardo Ramos
 * @param <T> Type of the value
 */
public class IntervalWithValue<T> implements Comparable<IntervalWithValue<T>> {
    private final Interval interval;
    private final T value;
    
    public IntervalWithValue(double low, double high, boolean lopen, boolean ropen, T value) {
        this.interval = new Interval(low, high, lopen, ropen);
        this.value = value;
    }
    
    public IntervalWithValue(double low, double high, T value) {
        this.interval = new Interval(low, high);
        this.value = value;
    }

    public IntervalWithValue(Interval interval, T value) {
        this.interval = interval;
        this.value = value;
    }

    public Interval getInterval() {
        return interval;
    }
    
    public T getValue(){
        return value;
    }

    @Override
    public int compareTo(IntervalWithValue<T> other) {
        return this.interval.compareTo(other.interval);
    }

    public double getLow() {
        return interval.getLow();
    }

    public double getHigh() {
        return interval.getHigh();
    }

    public boolean isLowExcluded() {
        return interval.isLowExcluded();
    }

    public boolean isHighExcluded() {
        return interval.isHighExcluded();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.interval != null ? this.interval.hashCode() : 0);
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntervalWithValue<?> other = (IntervalWithValue<?>) obj;
        if (this.interval != other.interval && (this.interval == null || !this.interval.equals(other.interval))) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interval.isLowExcluded() ? '(' : '[');
        sb.append(interval.getLow());
        sb.append(", ");
        sb.append(interval.getHigh());
        
        if (value != null) {
            sb.append(", ");
            String stringValue = value.toString();
            if (containsSpecialCharacters(stringValue) || stringValue.trim().isEmpty()) {
                sb.append('"');
                sb.append(stringValue.replace("\\", "\\\\").replace("\"", "\\\""));
                sb.append('"');
            } else {
                sb.append(stringValue);
            }
        }

        sb.append(interval.isHighExcluded() ? ')' : ']');

        return sb.toString();
    }
    
    private static final char[] SPECIAL_CHARACTERS = ";,()[]\"'".toCharArray();
    /**
     * @param value String value
     * @return True if the string contains special characters for dynamic intervals syntax
     */
    public static boolean containsSpecialCharacters(String value) {
        for (char c : SPECIAL_CHARACTERS) {
            if (value.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }
}
