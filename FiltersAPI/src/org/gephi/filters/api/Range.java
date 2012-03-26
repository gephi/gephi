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

    private final Number lowerNumber;
    private final Number upperNumber;
    private Number min;
    private Number max;
    private boolean leftInclusive = true;
    private boolean rightInclusive = true;
    private Number[] values;

    public Range(Number lowerBound, Number upperBound) {
        lowerNumber = lowerBound;
        upperNumber = upperBound;
        if (!lowerBound.getClass().equals(upperBound.getClass())) {
            throw new IllegalArgumentException("Lower and upper must be the same class");
        }
    }

    public Range(Number lowerBound, Number upperBound, boolean leftInclusive, boolean rightInclusive) {
        this(lowerBound, upperBound);
        this.leftInclusive = leftInclusive;
        this.rightInclusive = rightInclusive;
    }

    public Range(Number lowerBound, Number upperBound, Number min, Number max) {
        this(lowerBound, upperBound);
        if (!min.getClass().equals(lowerBound.getClass()) || !min.getClass().equals(max.getClass())) {
            throw new IllegalArgumentException("Lower and upper must be the same class");
        }
        this.min = min;
        this.max = max;
    }

    public Range(Number lowerBound, Number upperBound, Number min, Number max, Number[] values) {
        this(lowerBound, upperBound, min, max);
        this.values = values;
    }

    public Range(Number lowerBound, Number upperBound, Number min, Number max, boolean leftInclusive, boolean rightInclusive, Number[] values) {
        this(lowerBound, upperBound, min, max, values);
        this.leftInclusive = leftInclusive;
        this.rightInclusive = rightInclusive;
    }

    public boolean isInRange(Number value) {
        return ((Comparable) lowerNumber).compareTo(value) <= (leftInclusive ? 0 : -1)
                && ((Comparable) upperNumber).compareTo(value) >= (rightInclusive ? 0 : 1);
    }

    public Double getLowerDouble() {
        return lowerNumber.doubleValue();
    }

    public Float getLowerFloat() {
        return lowerNumber.floatValue();
    }

    public Integer getLowerInteger() {
        return lowerNumber.intValue();
    }

    public Long getLowerLong() {
        return lowerNumber.longValue();
    }

    public Byte getLowerByte() {
        return lowerNumber.byteValue();
    }

    public Short getLowerShort() {
        return lowerNumber.shortValue();
    }

    public Double getUpperDouble() {
        return upperNumber.doubleValue();
    }

    public Float getUpperFloat() {
        return upperNumber.floatValue();
    }

    public Integer getUpperInteger() {
        return upperNumber.intValue();
    }

    public Long getUpperLong() {
        return upperNumber.longValue();
    }

    public Short getUpperShort() {
        return upperNumber.shortValue();
    }

    public Byte getUpperByte() {
        return upperNumber.byteValue();
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
        return lowerNumber.getClass();
    }

    public Number[] getValues() {
        return values;
    }

    public boolean isLeftInclusive() {
        return leftInclusive;
    }

    public boolean isRightInclusive() {
        return rightInclusive;
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
        if (this.leftInclusive != other.leftInclusive) {
            return false;
        }
        if (this.rightInclusive != other.rightInclusive) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.lowerNumber != null ? this.lowerNumber.hashCode() : 0);
        hash = 13 * hash + (this.upperNumber != null ? this.upperNumber.hashCode() : 0);
        hash = 13 * hash + (this.min != null ? this.min.hashCode() : 0);
        hash = 13 * hash + (this.max != null ? this.max.hashCode() : 0);
        hash = 13 * hash + (this.leftInclusive ? 1 : 0);
        hash = 13 * hash + (this.rightInclusive ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return lowerNumber + " - " + upperNumber;
    }
}
