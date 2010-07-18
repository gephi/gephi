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
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class with some mathematic methods for calculating values such as the average, median and min of a list of numbers.
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
        if(numbers==null||numbers.length==0){
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        
        int numbersCount = 0;
        for (Number number : numbers) {
            if (number != null) {
                sum=sum.add(new BigDecimal(number.toString()));
                ++numbersCount;
            }
        }

        BigDecimal result;
        try{
            result=sum.divide(new BigDecimal(numbersCount));
        }catch(ArithmeticException ex){
            result=sum.divide(new BigDecimal(numbersCount),10,RoundingMode.HALF_EVEN);//Maximum of 10 decimal digits to avoid periodic number exception.
        }
        return result;
    }

    /**
     * Get average calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate average
     * @return Average as a BigDecimal
     */
    public static BigDecimal average(Collection<Number> numbers) {
        return average(numbers.toArray(new Number[0]));
    }

    /**
     * Calculate median of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate median
     * @return Median as a BigDecimal
     */
    public static BigDecimal median(Number[] numbers){
        if(numbers==null||numbers.length==0){
            return null;
        }

        BigDecimal[] bigDecimalNumbers=numbersArrayToBigDecimalArray(numbers);

        Arrays.sort(bigDecimalNumbers);
        if(bigDecimalNumbers.length%2==1){
            return bigDecimalNumbers[(bigDecimalNumbers.length+1)/2-1];
        }else{
            BigDecimal result=bigDecimalNumbers[(bigDecimalNumbers.length)/2-1];
            result=result.add(bigDecimalNumbers[(bigDecimalNumbers.length)/2]);
            return result.divide(BigDecimal.valueOf(2));
        }
    }

    /**
     * Calculate median of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate median
     * @return Median as a BigDecimal
     */
    public static BigDecimal median(Collection<Number> numbers){
        return median(numbers.toArray(new Number[0]));
    }

    /**
     * Get sum calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate sum
     * @return Sum as a BigDecimal
     */
    public static BigDecimal sum(Number[] numbers) {
        if(numbers==null||numbers.length==0){
            return null;
        }
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
    public static BigDecimal sum(Collection<Number> numbers) {
        return sum(numbers.toArray(new Number[0]));
    }

    /**
     * Get the minimum value of an array of Number elements as a BigDecimal.
     * The elements can't be null.
     * @param numbers Numbers to get min
     * @return Minimum value as a BigDecimal
     */
    public static BigDecimal minValue(Number[] numbers){
        if(numbers==null||numbers.length==0){
            return null;
        }

        BigDecimal[] bigDecimalNumbers=numbersArrayToBigDecimalArray(numbers);

        Arrays.sort(bigDecimalNumbers);
        return bigDecimalNumbers[0];
    }

    /**
     * Get the minimum value of a collection of Number elements as a BigDecimal.
     * The elements can't be null.
     * @param numbers Numbers to get min
     * @return Minimum value as a BigDecimal
     */
    public static BigDecimal minValue(Collection<Number> numbers){
        return minValue(numbers.toArray(new BigDecimal[0]));
    }

     /**
     * Get the maximum value of an array of Number elements as a BigDecimal.
     * The elements can't be null.
     * @param numbers Numbers to get max
     * @return Maximum value as a BigDecimal
     */
    public static BigDecimal maxValue(Number[] numbers){
        if(numbers==null||numbers.length==0){
            return null;
        }

        BigDecimal[] bigDecimalNumbers=numbersArrayToBigDecimalArray(numbers);

        Arrays.sort(bigDecimalNumbers);
        return bigDecimalNumbers[bigDecimalNumbers.length-1];
    }

    /**
     * Get the maximum value of a collection of Number elements as a BigDecimal.
     * The elements can't be null.
     * @param numbers Numbers to get max
     * @return Maximum value as a BigDecimal
     */
    public static BigDecimal maxValue(Collection<Number> numbers){
        return maxValue(numbers.toArray(new Number[0]));
    }

    /**
     * Takes an array of numbers of any type combination and returns
     * an array with their BigDecimal equivalent numbers.
     * @return BigDecimal array
     */
    private static BigDecimal[] numbersArrayToBigDecimalArray(Number[] numbers){
        if(numbers==null){
            return null;
        }
        BigDecimal[] result=new BigDecimal[numbers.length];
        Number number;
        for (int i = 0; i < result.length; i++) {
            number=numbers[i];
            if(number!=null){
                result[i]=new BigDecimal(number.toString());
            }
        }
        return result;
    }
}
