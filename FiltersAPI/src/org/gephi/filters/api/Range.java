/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.filters.api;

/**
 *
 * @author Mathieu Bastian
 */
public final class Range {

    private final Class rangeType;
    private Double lowerDouble;
    private Double upperDouble;
    private Float lowerFloat;
    private Float upperFloat;
    private Integer lowerInteger;
    private Integer upperInteger;
    private Long lowerLong;
    private Long upperLong;

    public Range(Double lowerBound, Double upperBound) {
        lowerDouble = lowerBound;
        upperDouble = upperBound;
        rangeType = Double.class;
    }

    public Range(Float lowerBound, Float upperBound) {
        lowerFloat = lowerBound;
        upperFloat = upperBound;
        rangeType = Float.class;
    }

    public Range(Integer lowerBound, Integer upperBound) {
        lowerInteger = lowerBound;
        upperInteger = upperBound;
        rangeType = Integer.class;
    }

    public Range(Long lowerBound, Long upperBound) {
        lowerLong = lowerBound;
        upperLong = upperBound;
        rangeType = Long.class;
    }

    public Range(Object lowerBound, Object upperBound) {
        if (lowerBound instanceof Double) {
            lowerDouble = (Double) lowerBound;
            upperDouble = (Double) upperBound;
            rangeType = Double.class;
        } else if (lowerBound instanceof Float) {
            lowerFloat = (Float) lowerBound;
            upperFloat = (Float) upperBound;
            rangeType = Float.class;
        } else if (lowerBound instanceof Integer) {
            lowerInteger = (Integer) lowerBound;
            upperInteger = (Integer) upperBound;
            rangeType = Integer.class;
        } else if (lowerBound instanceof Long) {
            lowerLong = (Long) lowerBound;
            upperLong = (Long) upperBound;
            rangeType = Long.class;
        } else {
            throw new IllegalArgumentException("must be Double, Float, Integer or Long");
        }
    }

    public boolean isInRange(Double value) {
        if (rangeType != Double.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return lowerDouble.compareTo(value) <= 0 && upperDouble.compareTo(value) >= 0;
    }

    public boolean isInRange(Float value) {
        if (rangeType != Float.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return lowerFloat.compareTo(value) <= 0 && upperFloat.compareTo(value) >= 0;
    }

    public boolean isInRange(Integer value) {
        if (rangeType != Integer.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return lowerInteger.compareTo(value) <= 0 && upperInteger.compareTo(value) >= 0;
    }

    public boolean isInRange(Long value) {
        if (rangeType != Long.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return lowerLong.compareTo(value) <= 0 && upperLong.compareTo(value) >= 0;
    }

    public boolean isInRange(Object value) {
        if (rangeType == Double.class) {
            return isInRange((Double) value);
        } else if (rangeType == Float.class) {
            return isInRange((Float) value);
        } else if (rangeType == Integer.class) {
            return isInRange((Integer) value);
        } else if (rangeType == Long.class) {
            return isInRange((Long) value);
        }
        return false;
    }

    public void trimBounds(Object min, Object max) {
        if (rangeType == Double.class) {
            Double minD = (Double) min;
            Double maxD = (Double) max;
            if (minD > lowerDouble || maxD < lowerDouble || lowerDouble.equals(upperDouble)) {
                lowerDouble = minD;
            }
            if (minD > upperDouble || maxD < upperDouble || lowerDouble.equals(upperDouble)) {
                upperDouble = maxD;
            }
        } else if (rangeType == Float.class) {
            Float minF = (Float) min;
            Float maxF = (Float) max;
            if (minF > lowerFloat || maxF < lowerFloat || lowerFloat.equals(upperFloat)) {
                lowerFloat = minF;
            }
            if (minF > upperFloat || maxF < upperFloat || lowerFloat.equals(upperFloat)) {
                upperFloat = maxF;
            }
        } else if (rangeType == Integer.class) {
            Integer minI = (Integer) min;
            Integer maxI = (Integer) max;
            if (minI > lowerInteger || maxI < lowerInteger || lowerInteger.equals(upperInteger)) {
                lowerInteger = minI;
            }
            if (minI > upperInteger || maxI < upperInteger || lowerInteger.equals(upperInteger)) {
                upperInteger = maxI;
            }
        } else if (rangeType == Long.class) {
            Long minL = (Long) min;
            Long maxL = (Long) max;
            if (minL > lowerLong || maxL < lowerLong || lowerLong.equals(upperLong)) {
                lowerLong = minL;
            }
            if (minL > upperLong || maxL < upperLong || lowerLong.equals(upperLong)) {
                upperLong = maxL;
            }
        }
    }

    public Double getLowerDouble() {
        return lowerDouble;
    }

    public Float getLowerFloat() {
        return lowerFloat;
    }

    public Integer getLowerInteger() {
        return lowerInteger;
    }

    public Long getLowerLong() {
        return lowerLong;
    }

    public Double getUpperDouble() {
        return upperDouble;
    }

    public Float getUpperFloat() {
        return upperFloat;
    }

    public Integer getUpperInteger() {
        return upperInteger;
    }

    public Long getUpperLong() {
        return upperLong;
    }

    public Object getLowerBound() {
        if (rangeType == Double.class) {
            return lowerDouble;
        } else if (rangeType == Float.class) {
            return lowerFloat;
        } else if (rangeType == Integer.class) {
            return lowerInteger;
        } else if (rangeType == Long.class) {
            return lowerLong;
        }
        return null;
    }

    public Object getUpperBound() {
        if (rangeType == Double.class) {
            return upperDouble;
        } else if (rangeType == Float.class) {
            return upperFloat;
        } else if (rangeType == Integer.class) {
            return upperInteger;
        } else if (rangeType == Long.class) {
            return upperLong;
        }
        return null;
    }

    public Class getRangeType() {
        return rangeType;
    }
    //TODO equals

    @Override
    public String toString() {
        if (rangeType == Double.class) {
            return lowerDouble.toString() + " - " + upperDouble.toString();
        } else if (rangeType == Float.class) {
            return lowerFloat.toString() + " - " + upperFloat.toString();
        } else if (rangeType == Integer.class) {
            return lowerInteger.toString() + " - " + upperInteger.toString();
        } else if (rangeType == Long.class) {
            return lowerLong.toString() + " - " + upperLong.toString();
        }
        return "null";
    }
}
