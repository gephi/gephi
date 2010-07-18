/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class MathUtils {

    /**
     * Get average calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate average
     * @return Average as a BigDecimal
     */
    public static BigDecimal average(Number[] numbers) {
        BigDecimal sum = new BigDecimal(0);
        if(numbers.length==0){
            return sum;
        }
        int numbersCount = 0;
        for (Number number : numbers) {
            if (number != null) {
                sum=sum.add(new BigDecimal(number.toString()));
                ++numbersCount;
            }
        }

        return sum.divide(new BigDecimal(numbersCount));
    }

    /**
     * Get average calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate average
     * @return Average as a BigDecimal
     */
    public static BigDecimal average(Collection<? extends Number> numbers) {
        return average(numbers.toArray(new Number[0]));
    }

    /**
     * Get sum calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate sum
     * @return Sum as a BigDecimal
     */
    public static BigDecimal sum(Number[] numbers) {
        BigDecimal sum = new BigDecimal(0);
        for (Number number : numbers) {
            if (number != null) {
                sum=sum.add(new BigDecimal(number.toString()));
            }
        }

        return sum;
    }

    /**
     * Get sum calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate sum
     * @return Sum as a BigDecimal
     */
    public static BigDecimal sum(Collection<? extends Number> numbers) {
        return sum(numbers.toArray(new Number[0]));
    }

    /**
     * Get the minimum value of an array of Number elements.
     * All elements need to be the same type of number.
     * Cannot handle null values.
     * @param numbers Numbers to get min
     * @return Minimum value
     */
    public static <T extends Number> T minValue(T[] numbers){
        Arrays.sort(numbers);
        return numbers[0];
    }

    /**
     * Get the minimum value of a collection of Number elements.
     * All elements need to be the same type of number.
     * Cannot handle null values.
     * @param numbers Numbers to get min
     * @return Minimum value
     */
    public static <T extends Number> T minValue(Collection<T> numbers){
        return (T) minValue((T[])numbers.toArray());
    }

     /**
     * Get the maximum value of an array of Number elements.
     * All elements need to be the same type of number.
     * Cannot handle null values.
     * @param numbers Numbers to get max
     * @return Maximum value
     */
    public static <T extends Number> T maxValue(T[] numbers){
        Arrays.sort(numbers);
        return numbers[numbers.length-1];
    }

    /**
     * Get the maximum value of a collection of Number elements.
     * All elements need to be the same type of number.
     * Cannot handle null values.
     * @param numbers Numbers to get max
     * @return Maximum value
     */
    public static <T extends Number> T maxValue(Collection<T> numbers){
        return (T) maxValue((T[])numbers.toArray());
    }
}
