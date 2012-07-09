/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class with some statistics methods for calculating values such as the average, median, sum, max and min of a list of numbers.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class StatisticsUtils {

    /**
     * <p>Get average calculation of various numbers as a BigDecimal</p>
     * <p>Null values will not be counted.</p>
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
     * <p>Get average calculation of various numbers as a BigDecimal</p>
     * <p>Null values will not be counted.</p>
     * @param numbers Numbers to calculate average
     * @return Average as a BigDecimal
     */
    public static BigDecimal average(Collection<Number> numbers) {
        return average(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Calculate median of various numbers as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
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
     * <p>Calculate median of various numbers as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
     * @param numbers Not null numbers to calculate median
     * @return Median as a BigDecimal
     */
    public static BigDecimal median(Collection<Number> numbers) {
        return median(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Calculate first quartile (Q1) of various numbers as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
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
     * <p>Calculate first quartile (Q1) of various numbers as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
     * @param numbers Not null numbers to calculate Q1
     * @return Q1 as a BigDecimal
     */
    public static BigDecimal quartile1(Collection<Number> numbers) {
        return quartile1(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Calculate third quartile (Q3) of various numbers as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
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
     * <p>Calculate third quartile (Q3) of various numbers as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
     * @param numbers Not null numbers to calculate Q3
     * @return Q3 as a BigDecimal
     */
    public static BigDecimal quartile3(Collection<Number> numbers) {
        return quartile3(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Get sum of various numbers as a BigDecimal</p>
     * <p>Null values will not be counted.</p>
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
     * <p>Get sum of various numbers as a BigDecimal</p>
     * <p>Null values will not be counted.</p>
     * @param numbers Numbers to calculate sum
     * @return Sum as a BigDecimal
     */
    public static BigDecimal sum(Collection<Number> numbers) {
        return sum(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Get the minimum value of an array of Number elements as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
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
     * <p>Get the minimum value of a collection of Number elements as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
     * @param numbers Numbers to get min
     * @return Minimum value as a BigDecimal
     */
    public static BigDecimal minValue(Collection<Number> numbers) {
        return minValue(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Get the maximum value of an array of Number elements as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
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
     * <p>Get the maximum value of a collection of Number elements as a BigDecimal.</p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
     * @param numbers Numbers to get max
     * @return Maximum value as a BigDecimal
     */
    public static BigDecimal maxValue(Collection<Number> numbers) {
        return maxValue(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Calculates all statistics and returns them in a <code>BigDecimal</code> numbers array.</p>
     * <p>Using this will be faster than calling all statistics separately.</p>
     * <p>Returns an array of <b>length=8</b> of <code>BigDecimal</code> numbers with the results in the following order:
     * <ol>
     * <li>average</li>
     * <li>first quartile (Q1)</li>
     * <li>median</li>
     * <li>third quartile (Q3)</li>
     * <li>interquartile range (IQR)</li>
     * <li>sum</li>
     * <li>minimumValue</li>
     * <li>maximumValue</li>
     * </ol>
     * </p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
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
     * <p>Calculates all statistics and returns them in a <code>BigDecimal</code> numbers array.</p>
     * <p>Using this will be faster than calling all statistics separately.</p>
     * <p>Returns an array of <b>length=8</b> of <code>BigDecimal</code> numbers with the results in the following order:
     * <ol>
     * <li>average</li>
     * <li>first quartile (Q1)</li>
     * <li>median</li>
     * <li>third quartile (Q3)</li>
     * <li>interquartile range (IQR)</li>
     * <li>sum</li>
     * <li>minimumValue</li>
     * <li>maximumValue</li>
     * </ol>
     * </p>
     * <p>The elements can't be null.</p>
     * <p>The elements don't need to be sorted.</p>
     * @param numbers Numbers to get all statistics
     * @return Array with all statisctis
     */
    public static BigDecimal[] getAllStatistics(Collection<Number> numbers) {
        return getAllStatistics(numbers.toArray(new Number[0]));
    }

    /**
     * <p>Takes an array of numbers of any type combination and returns
     * an array with their BigDecimal equivalent numbers.</p>
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
