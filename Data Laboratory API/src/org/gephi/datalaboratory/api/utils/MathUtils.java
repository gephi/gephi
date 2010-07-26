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
package org.gephi.datalaboratory.api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class with some mathematic methods for calculating values such as the average, median, sum, max and min of a list of numbers.
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
        if (numbers == null || numbers.length == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);

        int numbersCount = 0;
        for (Number number : numbers) {
            if (number != null) {
                sum = sum.add(new BigDecimal(number.toString()));
                ++numbersCount;
            }
        }

        BigDecimal result;
        try {
            result = sum.divide(new BigDecimal(numbersCount));
        } catch (ArithmeticException ex) {
            result = sum.divide(new BigDecimal(numbersCount), 10, RoundingMode.HALF_EVEN);//Maximum of 10 decimal digits to avoid periodic number exception.
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
    public static BigDecimal median(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal[] bigDecimalNumbers = numbersArrayToSortedBigDecimalArray(numbers);
        return median(bigDecimalNumbers);
    }

    /**
     * Calculate median of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate median
     * @return Median as a BigDecimal
     */
    public static BigDecimal median(Collection<Number> numbers) {
        return median(numbers.toArray(new Number[0]));
    }

    /**
     * Calculate first quartile (Q1) of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate Q1
     * @return Q1 as a BigDecimal
     */
    public static BigDecimal quartile1(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal[] bigDecimalNumbers = numbersArrayToSortedBigDecimalArray(numbers);
        return quartile1(bigDecimalNumbers);
    }

    /**
     * Calculate first quartile (Q1) of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate Q1
     * @return Q1 as a BigDecimal
     */
    public static BigDecimal quartile1(Collection<Number> numbers) {
        return quartile1(numbers.toArray(new Number[0]));
    }

    /**
     * Calculate third quartile (Q3) of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate Q3
     * @return Q3 as a BigDecimal
     */
    public static BigDecimal quartile3(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal[] bigDecimalNumbers = numbersArrayToSortedBigDecimalArray(numbers);
        return quartile3(bigDecimalNumbers);
    }

    /**
     * Calculate third quartile (Q3) of various numbers as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Not null numbers to calculate Q3
     * @return Q3 as a BigDecimal
     */
    public static BigDecimal quartile3(Collection<Number> numbers) {
        return quartile3(numbers.toArray(new Number[0]));
    }

    /**
     * Get sum calculation of various numbers as a BigDecimal
     * Null values will not be counted.
     * @param numbers Numbers to calculate sum
     * @return Sum as a BigDecimal
     */
    public static BigDecimal sum(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal sum = new BigDecimal(0);
        for (Number number : numbers) {
            if (number != null) {
                sum = sum.add(new BigDecimal(number.toString()));
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
     * The elements don't need to be sorted.
     * @param numbers Numbers to get min
     * @return Minimum value as a BigDecimal
     */
    public static BigDecimal minValue(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal[] bigDecimalNumbers = numbersArrayToSortedBigDecimalArray(numbers);
        return bigDecimalNumbers[0];
    }

    /**
     * Get the minimum value of a collection of Number elements as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Numbers to get min
     * @return Minimum value as a BigDecimal
     */
    public static BigDecimal minValue(Collection<Number> numbers) {
        return minValue(numbers.toArray(new Number[0]));
    }

    /**
     * Get the maximum value of an array of Number elements as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Numbers to get max
     * @return Maximum value as a BigDecimal
     */
    public static BigDecimal maxValue(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal[] bigDecimalNumbers = numbersArrayToSortedBigDecimalArray(numbers);
        return bigDecimalNumbers[bigDecimalNumbers.length - 1];
    }

    /**
     * Get the maximum value of a collection of Number elements as a BigDecimal.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Numbers to get max
     * @return Maximum value as a BigDecimal
     */
    public static BigDecimal maxValue(Collection<Number> numbers) {
        return maxValue(numbers.toArray(new Number[0]));
    }

    /**
     * Calculates all statistics and returns them in a BigDecimal array.
     * Using this will be faster than calling all statistics separately.
     * Returns an array with length=8 of BigDecimal numbers with the results in the following order: average, first quartile (Q1), median, third quartile (Q3), interquartile range (IQR), sum, minimumValue and maximumValue.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Numbers to get all statistics
     * @return Array with all statisctis
     */
    public static BigDecimal[] getAllStatistics(Number[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        BigDecimal[] bigDecimalNumbers = numbersArrayToSortedBigDecimalArray(numbers);
        BigDecimal sum = sum(bigDecimalNumbers);

        BigDecimal[] statistics = new BigDecimal[8];
        statistics[0] = average(sum, new BigDecimal(bigDecimalNumbers.length));
        statistics[1] = quartile1(bigDecimalNumbers);
        statistics[2] = median(bigDecimalNumbers);
        statistics[3] = quartile3(bigDecimalNumbers);
        statistics[4] = statistics[3].subtract(statistics[1]);
        statistics[5] = sum;
        statistics[6] = minValue(bigDecimalNumbers);
        statistics[7] = maxValue(bigDecimalNumbers);
        return statistics;
    }

    /**
     * Calculates all statistics and returns them in a BigDecimal array.
     * <b>Using this will be faster than calling all statistics separately.</b>
     * Returns an array with length=8 of BigDecimal numbers with the results in the following order: average, first quartile (Q1), median, third quartile (Q3), interquartile range (IQR), sum, minimumValue and maximumValue.
     * The elements can't be null.
     * The elements don't need to be sorted.
     * @param numbers Numbers to get all statistics
     * @return Array with all statisctis
     */
    public static BigDecimal[] getAllStatistics(Collection<Number> numbers) {
        return getAllStatistics(numbers.toArray(new Number[0]));
    }

    /**
     * Takes an array of numbers of any type combination and returns
     * an array with their BigDecimal equivalent numbers.
     * @return BigDecimal array
     */
    public static BigDecimal[] numbersArrayToSortedBigDecimalArray(Number[] numbers) {
        if (numbers == null) {
            return null;
        }
        BigDecimal[] result = new BigDecimal[numbers.length];
        Number number;
        for (int i = 0; i < result.length; i++) {
            number = numbers[i];
            if (number != null) {
                result[i] = new BigDecimal(number.toString());
            }
        }
        Arrays.sort(result);
        return result;
    }

    /***********Private methods:***********/
    //Next methods need the number array already converted to BigDecimal and sorted.
    //Used for faster calculating of all statistics, not repeating the sorting and conversion to BigDecimal array.
    private static BigDecimal average(final BigDecimal sum, final BigDecimal numbersCount) {

        BigDecimal result;
        try {
            result = sum.divide(numbersCount);
        } catch (ArithmeticException ex) {
            result = sum.divide(numbersCount, 10, RoundingMode.HALF_EVEN);//Maximum of 10 decimal digits to avoid periodic number exception.
        }
        return result;
    }

    private static BigDecimal median(final BigDecimal[] bigDecimalNumbers) {
        return median(bigDecimalNumbers, 0, bigDecimalNumbers.length);
    }

    private static BigDecimal median(final BigDecimal[] bigDecimalNumbers, final int start, final int end) {
        final int size = end - start;

        if (size % 2 == 1) {
            return bigDecimalNumbers[start + (size + 1) / 2 - 1];
        } else {
            BigDecimal result = bigDecimalNumbers[start + (size) / 2 - 1];
            result = result.add(bigDecimalNumbers[start + (size) / 2]);
            return result.divide(BigDecimal.valueOf(2));
        }
    }

    private static BigDecimal quartile1(BigDecimal[] bigDecimalNumbers) {
        final int size = bigDecimalNumbers.length;
        if (size % 2 == 1) {
            if (size > 1) {
                return median(bigDecimalNumbers, 0, size / 2 + 1);
            } else {
                return median(bigDecimalNumbers, 0, 1);
            }
        } else {
            return median(bigDecimalNumbers, 0, size / 2);
        }
    }

    private static BigDecimal quartile3(BigDecimal[] bigDecimalNumbers) {
        final int size = bigDecimalNumbers.length;
        if (size % 2 == 1) {
            if (size > 1) {
                return median(bigDecimalNumbers, size / 2, size);
            } else {
                return median(bigDecimalNumbers, 0, 1);
            }
        } else {
            return median(bigDecimalNumbers, size / 2, size);
        }
    }

    private static BigDecimal sum(BigDecimal[] bigDecimalNumbers) {
        BigDecimal sum = new BigDecimal(0);
        for (BigDecimal number : bigDecimalNumbers) {
            if (number != null) {
                sum = sum.add(number);
            }
        }

        return sum;
    }

    private static BigDecimal minValue(BigDecimal[] bigDecimalNumbers) {
        return bigDecimalNumbers[0];
    }

    private static BigDecimal maxValue(BigDecimal[] bigDecimalNumbers) {
        return bigDecimalNumbers[bigDecimalNumbers.length - 1];
    }
}
