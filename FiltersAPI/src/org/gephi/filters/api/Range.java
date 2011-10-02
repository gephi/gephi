/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.filters.api;

/**
 *
 * @author Mathieu Bastian
 */
public final class Range {

    private final Class rangeType;
    private Number lowerNumber;
    private Number upperNumber;
    private Number min;
    private Number max;

    public Range(Double lowerBound, Double upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = Double.class;
    }

    public Range(Float lowerBound, Float upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = Float.class;
    }

    public Range(Integer lowerBound, Integer upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = Integer.class;
    }

    public Range(Long lowerBound, Long upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = Long.class;
    }

    public Range(Byte lowerBound, Byte upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = Byte.class;
    }

    public Range(Short lowerBound, Short upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = Short.class;
    }

    public Range(Double lowerBound, Double upperBound, Double min, Double max) {
        this(lowerBound, upperBound);
        this.min = min;
        this.max = max;
    }

    public Range(Float lowerBound, Float upperBound, Float min, Float max) {
        this(lowerBound, upperBound);
        this.min = min;
        this.max = max;
    }

    public Range(Integer lowerBound, Integer upperBound, Integer min, Integer max) {
        this(lowerBound, upperBound);
        this.min = min;
        this.max = max;
    }

    public Range(Long lowerBound, Long upperBound, Long min, Long max) {
        this(lowerBound, upperBound);
        this.min = min;
        this.max = max;
    }

    public Range(Byte lowerBound, Byte upperBound, Byte min, Byte max) {
        this(lowerBound, upperBound);
        this.min = min;
        this.max = max;
    }

    public Range(Short lowerBound, Short upperBound, Short min, Short max) {
        this(lowerBound, upperBound);
        this.min = min;
        this.max = max;
    }

    public Range(Number lowerBound, Number upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        rangeType = lowerBound.getClass();
        if (!lowerBound.getClass().equals(upperBound.getClass())) {
            throw new IllegalArgumentException("Lower and upper must be the same class");
        }
    }

    public Range(Number lowerBound, Number upperBound, Number min, Number max) {
        this(lowerBound, upperBound);
        if (!min.getClass().equals(lowerBound.getClass()) || !min.getClass().equals(max.getClass())) {
            throw new IllegalArgumentException("Lower and upper must be the same class");
        }
        this.min = min;
        this.max = max;
    }

    public boolean isInRange(Double value) {
        if (rangeType != Double.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return ((Double) lowerNumber).compareTo(value) <= 0 && ((Double) upperNumber).compareTo(value) >= 0;
    }

    public boolean isInRange(Float value) {
        if (rangeType != Float.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return ((Float) lowerNumber).compareTo(value) <= 0 && ((Float) upperNumber).compareTo(value) >= 0;
    }

    public boolean isInRange(Integer value) {
        if (rangeType != Integer.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return ((Integer) lowerNumber).compareTo(value) <= 0 && ((Integer) upperNumber).compareTo(value) >= 0;
    }

    public boolean isInRange(Long value) {
        if (rangeType != Long.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return ((Long) lowerNumber).compareTo(value) <= 0 && ((Long) upperNumber).compareTo(value) >= 0;
    }

    public boolean isInRange(Byte value) {
        if (rangeType != Byte.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return ((Byte) lowerNumber).compareTo(value) <= 0 && ((Byte) upperNumber).compareTo(value) >= 0;
    }

    public boolean isInRange(Short value) {
        if (rangeType != Short.class) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        return ((Short) lowerNumber).compareTo(value) <= 0 && ((Short) upperNumber).compareTo(value) >= 0;
    }

    public boolean isInRange(Number value) {
        if (rangeType == Double.class) {
            return isInRange((Double) value);
        } else if (rangeType == Float.class) {
            return isInRange((Float) value);
        } else if (rangeType == Integer.class) {
            return isInRange((Integer) value);
        } else if (rangeType == Long.class) {
            return isInRange((Long) value);
        } else if (rangeType == Byte.class) {
            return isInRange((Byte) value);
        } else if (rangeType == Short.class) {
            return isInRange((Short) value);
        }
        return false;
    }

    public void setMinMax(Number min, Number max) {
        if (rangeType != min.getClass() || rangeType != max.getClass()) {
            throw new IllegalArgumentException("value must be " + rangeType.getName());
        }
        if (rangeType == Double.class) {
            Double minD = (Double) min;
            Double maxD = (Double) max;
            if (minD > (Double) lowerNumber || maxD < (Double) lowerNumber || lowerNumber.equals(upperNumber)) {
                lowerNumber = minD;
            } else if (lowerNumber.equals(this.min)) {
                lowerNumber = minD;
            }
            if (minD > (Double) upperNumber || maxD < (Double) upperNumber || lowerNumber.equals(upperNumber)) {
                upperNumber = maxD;
            } else if (upperNumber.equals(this.max)) {
                upperNumber = maxD;
            }
        } else if (rangeType == Float.class) {
            Float minF = (Float) min;
            Float maxF = (Float) max;
            if (minF > (Float) lowerNumber || maxF < (Float) lowerNumber || lowerNumber.equals(upperNumber)) {
                lowerNumber = minF;
            } else if (lowerNumber.equals(this.min)) {
                lowerNumber = minF;
            }
            if (minF > (Float) upperNumber || maxF < (Float) upperNumber || lowerNumber.equals(upperNumber)) {
                upperNumber = maxF;
            } else if (upperNumber.equals(this.max)) {
                upperNumber = maxF;
            }
        } else if (rangeType == Integer.class) {
            Integer minI = (Integer) min;
            Integer maxI = (Integer) max;
            if (minI > (Integer) lowerNumber || maxI < (Integer) lowerNumber || lowerNumber.equals(upperNumber)) {
                lowerNumber = minI;
            } else if (lowerNumber.equals(this.min)) {
                lowerNumber = minI;
            }
            if (minI > (Integer) upperNumber || maxI < (Integer) upperNumber || lowerNumber.equals(upperNumber)) {
                upperNumber = maxI;
            } else if (upperNumber.equals(this.max)) {
                upperNumber = maxI;
            }
        } else if (rangeType == Long.class) {
            Long minL = (Long) min;
            Long maxL = (Long) max;
            if (minL > (Long) lowerNumber || maxL < (Long) lowerNumber || lowerNumber.equals(upperNumber)) {
                lowerNumber = minL;
            } else if (lowerNumber.equals(this.min)) {
                lowerNumber = minL;
            }
            if (minL > (Long) upperNumber || maxL < (Long) upperNumber || lowerNumber.equals(upperNumber)) {
                upperNumber = maxL;
            } else if (upperNumber.equals(this.max)) {
                upperNumber = maxL;
            }
        } else if (rangeType == Short.class) {
            Short minS = (Short) min;
            Short maxS = (Short) max;
            if (minS > (Short) lowerNumber || maxS < (Short) lowerNumber || lowerNumber.equals(upperNumber)) {
                lowerNumber = minS;
            } else if (lowerNumber.equals(this.min)) {
                lowerNumber = minS;
            }
            if (minS > (Short) upperNumber || maxS < (Short) upperNumber || lowerNumber.equals(upperNumber)) {
                upperNumber = maxS;
            } else if (upperNumber.equals(this.max)) {
                upperNumber = maxS;
            }
        } else if (rangeType == Byte.class) {
            Byte minB = (Byte) min;
            Byte maxB = (Byte) max;
            if (minB > (Byte) lowerNumber || maxB < (Byte) lowerNumber || lowerNumber.equals(upperNumber)) {
                lowerNumber = minB;
            } else if (lowerNumber.equals(this.min)) {
                lowerNumber = minB;
            }
            if (minB > (Byte) upperNumber || maxB < (Byte) upperNumber || lowerNumber.equals(upperNumber)) {
                upperNumber = maxB;
            } else if (upperNumber.equals(this.max)) {
                upperNumber = maxB;
            }
        }
        this.min = min;
        this.max = max;
    }

    public Double getLowerDouble() {
        return (Double) lowerNumber;
    }

    public Float getLowerFloat() {
        return (Float) lowerNumber;
    }

    public Integer getLowerInteger() {
        return (Integer) lowerNumber;
    }

    public Long getLowerLong() {
        return (Long) lowerNumber;
    }

    public Byte getLowerByte() {
        return (Byte) lowerNumber;
    }

    public Short getLowerShort() {
        return (Short) lowerNumber;
    }

    public Double getUpperDouble() {
        return (Double) upperNumber;
    }

    public Float getUpperFloat() {
        return (Float) upperNumber;
    }

    public Integer getUpperInteger() {
        return (Integer) upperNumber;
    }

    public Long getUpperLong() {
        return (Long) upperNumber;
    }

    public Short getUpperShort() {
        return (Short) upperNumber;
    }

    public Byte getUpperByte() {
        return (Byte) upperNumber;
    }

    public Number getLowerBound() {
        return lowerNumber;
    }

    public Number getUpperBound() {
        return upperNumber;
    }

    public Number getMinimum() {
        return min;
    }

    public Number getMaximum() {
        return max;
    }

    public Class getRangeType() {
        return rangeType;
    }

    public static Number trimToBounds(Number min, Number max, Number value) {
        if (min != null && max != null && value != null) {
            if (min.getClass().equals(max.getClass()) && max.getClass().equals(value.getClass())) {
                if (min instanceof Long || min instanceof Integer || min instanceof Short || min instanceof Byte) {
                    if (value.longValue() < min.longValue()) {
                        value = min;
                    } else if (value.longValue() > max.longValue()) {
                        value = max;
                    }
                } else if (min instanceof Float || min instanceof Double) {
                    if (value.doubleValue() < min.doubleValue()) {
                        value = min;
                    } else if (value.doubleValue() > max.doubleValue()) {
                        value = max;
                    }
                }
            } else {
                throw new IllegalArgumentException("min, max and value must be the same class");
            }
        }
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Range other = (Range) obj;
        if (this.rangeType != other.rangeType && (this.rangeType == null || !this.rangeType.equals(other.rangeType))) {
            return false;
        }
        if (this.lowerNumber != other.lowerNumber && (this.lowerNumber == null || !this.lowerNumber.equals(other.lowerNumber))) {
            return false;
        }
        if (this.upperNumber != other.upperNumber && (this.upperNumber == null || !this.upperNumber.equals(other.upperNumber))) {
            return false;
        }
        if (this.min != other.min && (this.min == null || !this.min.equals(other.min))) {
            return false;
        }
        if (this.max != other.max && (this.max == null || !this.max.equals(other.max))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.rangeType != null ? this.rangeType.hashCode() : 0);
        hash = 19 * hash + (this.lowerNumber != null ? this.lowerNumber.hashCode() : 0);
        hash = 19 * hash + (this.upperNumber != null ? this.upperNumber.hashCode() : 0);
        hash = 19 * hash + (this.min != null ? this.min.hashCode() : 0);
        hash = 19 * hash + (this.max != null ? this.max.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return lowerNumber + " - " + upperNumber;
    }
}
