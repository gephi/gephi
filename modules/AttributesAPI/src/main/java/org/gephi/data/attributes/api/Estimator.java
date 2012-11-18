/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
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
 * <tr>
 *    <td><b>TimeInterval</b></td>
 *    <td>-</td>
 *    <td>the time interval separating the higher half from the lower half (if
 *        there is an even number of time intervals, the median is then defined
 *        to be the time interval which occured earlier than the second middle
 *        time interval)</td>
 *    <td>the time interval that occurs the most frequently</td>
 *    <td>-</td>
 *    <td>-</td>
 *    <td>-</td>
 *    <td>the time interval which occured firstly</td>
 *    <td>the time interval which occured lastly</td>
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
