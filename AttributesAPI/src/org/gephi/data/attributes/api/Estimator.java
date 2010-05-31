/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.attributes.api;

/**
 * This enum is used to determine what should be done with "ties". For example
 * if in the given time interval some attribute has got 3 different values we
 * should know how to estimate its value.
 * 
 * <p>The table below shows how the estimation is done for different types
 * ({@code -} means {@code not specified}).
 * <table>
 * <tr>
 *    <td></td>
 *    <td><b>AVERAGE</b></td>
 *    <td><b>MEDIAN</b></td>
 *    <td><b>MODE</b></td>
 *    <td><b>SUM</b></td>
 *    <td><b>MIN</b></td>
 *    <td><b>MAX</b></td>
 *    <td><b>FIRST</b></td>
 *    <td><b>LAST</b></td>
 * </tr>
 * <tr>
 *    <td><b>Real numbers</b></td>
 *    <td>arithmetic mean</td>
 *    <td>the value separating the higher half from the lower half (if there is
 *        an even number of values, the median is then defined to be the mean of
 *        the two middle values)</td>
 *    <td>the value that occurs the most frequently</td>
 *    <td>the result of addition of all values</td>
 *    <td>the lowest value</td>
 *    <td>the highest value</td>
 *    <td>the value which occured firstly</td>
 *    <td>the value which occured lastly</td>
 * </tr>
 * <tr>
 *    <td><b>Integers</b></td>
 *    <td>arithmetic mean - using integer division</td>
 *    <td>the value separating the higher half from the lower half (if there is
 *        an even number of values, the median is then defined to be the mean of
 *        the two middle values - using integer division)</td>
 *    <td>the value that occurs the most frequently</td>
 *    <td>the result of addition of all values</td>
 *    <td>the lowest value</td>
 *    <td>the highest value</td>
 *    <td>the value which occured firstly</td>
 *    <td>the value which occured lastly</td>
 * </tr>
 * <tr>
 *    <td><b>Boolean</b></td>
 *    <td>-</td>
 *    <td>the bool separating the higher half from the lower half (if there is
 *        an even number of bools, the median is then defined to be the bool
 *        which occured earlier than the second middle bool)</td>
 *    <td>the bool that occurs the most frequently</td>
 *    <td>-</td>
 *    <td>false if exists in a given set of bools, otherwise true</td>
 *    <td>true if exists in a given set of bools, otherwise false</td>
 *    <td>the bool which occured firstly</td>
 *    <td>the bool which occured lastly</td>
 * </tr>
 * <tr>
 *    <td><b>Character</b></td>
 *    <td>-</td>
 *    <td>the character separating the higher half from the lower half (if there
 *        is an even number of characters, the median is then defined to be the
 *        character which occured earlier than the second middle character)</td>
 *    <td>the character that occurs the most frequently</td>
 *    <td>-</td>
 *    <td>the lowest character (with the lowest {@code int} value)</td>
 *    <td>the highest character (with the highest {@code int} value)</td>
 *    <td>the character which occured firstly</td>
 *    <td>the character which occured lastly</td>
 * </tr>
 * <tr>
 *    <td><b>String</b></td>
 *    <td>-</td>
 *    <td>the string separating the higher half from the lower half (if there
 *        is an even number of strings, the median is then defined to be the
 *        string which occured earlier than the second middle string)</td>
 *    <td>the string that occurs the most frequently</td>
 *    <td>-</td>
 *    <td>the lowest string (using {@code compareTo} method)</td>
 *    <td>the highest string (using {@code compareTo} method)</td>
 *    <td>the string which occured firstly</td>
 *    <td>the string which occured lastly</td>
 * </tr>
 * </table>
 * 
 * @author Cezary Bartosiak
 */
public enum Estimator {
	AVERAGE,
	MEDIAN,
	MODE,
	SUM,
	MIN,
	MAX,
	FIRST,
	LAST
}
