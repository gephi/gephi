/*
 Copyright 2008-2012 Gephi
 Authors : Martin Škurla <bujacik@gmail.com>, Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos<eduramiba@gmail.com>
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
package org.gephi.dynamic.utils;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Interval;

/**
 * <p>
 * Class for parsing dynamic types with several intervals.
 * </p>
 *
 * <p>
 * Examples of valid dynamic intervals are:
 * <ul>
 * <li>&lt;(1, 2, v1); [3, 5, v2]&gt;</li>
 * <li>[1,2]</li>
 * <li>[1,2] (5,6)</li>
 * <li>[1,2]; [1.15,2.21, 'literal value " \' ,[]()']</li>
 * <li>&lt;[1,2]; [1.15,2.21, "literal value \" ' ,[]()"]&gt;</li>
 * </ul>
 * </p>
 *
 * <p>
 * The most correct examples are those that include &lt; &gt; and proper commas and semicolons for separation, but the parser will be permissive when possible.</p>
 * <p>
 * Gephi will always format intervals in the correct way.</p>
 *
 * <p>
 * See https://gephi.org/users/supported-graph-formats/spreadsheet for more examples</p>
 *
 * @author Eduardo Ramos
 */
public final class DynamicIntervalsParser {

    private static final char LOPEN = '(';
    private static final char LCLOSE = '[';
    private static final char ROPEN = ')';
    private static final char RCLOSE = ']';
    private static final char COMMA = ',';

    /**
     * Parses a dynamic type with one or more intervals (without values, only start and end)
     *
     * @param input Input string to parse
     * @return List of parsed intervals, or null if the input equals '&lt;empty&gt;'
     * @throws IOException Thrown if error while reading input
     * @throws ParseException Thronw if the intervals could not be parsed
     */
    public static List<Interval> parseIntervals(String input) throws IOException, ParseException, IllegalArgumentException {
        List<IntervalWithValue<Object>> intervals = parseIntervals(null, input);
        
        List<Interval> result = new ArrayList<Interval>();
        for (IntervalWithValue<Object> interval : intervals) {
            result.add(interval.getInterval());
        }
        
        return result;
    }

    /**
     * Parses a dynamic type with one or more intervals, with associated values
     *
     * @param <T>
     * @param clazz Simple type for the result intervals values
     * @param input Input string to parse
     * @return List of parsed intervals, or null if the input equals '&lt;empty&gt;'
     * @throws IOException Thrown if error while reading input
     * @throws ParseException Thronw if the intervals could not be parsed
     */
    public static <T> List<IntervalWithValue<T>> parseIntervalsWithValues(Class<T> clazz, String input) throws IOException, ParseException, IllegalArgumentException {
        return parseIntervals(clazz, input);
    }
    
    /**
     * Parses intervals with values (of <code>type</code> Class) or without values (null <code>type</code> Class)
     * @param <T> Type of the interval value
     * @param type Class of the intervals' values or null to parse intervals without values
     * @param input Input to parse
     * @return List of Interval
     */
    public static <T> List<IntervalWithValue<T>> parseIntervals(Class<T> type, String input) throws IOException, ParseException, IllegalArgumentException {
        if (input.equalsIgnoreCase("<empty>")) {
            return null;
        }

        List<IntervalWithValue<T>> intervals = new ArrayList<IntervalWithValue<T>>();

        StringReader reader = new StringReader(input + ' ');//Add 1 space so reader.skip function always works when necessary (end of string not reached).

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case LCLOSE:
                case LOPEN:
                    intervals.add(parseInterval(type, reader, c == LOPEN));
                    break;
                default:
                //Ignore other chars outside of intervals
            }
        }

        if (intervals.isEmpty()) {
            throw new IllegalArgumentException("No dynamic intervals could be parsed");
        }

        return intervals;
    }

    private static <T> IntervalWithValue<T> parseInterval(Class<T> type, StringReader reader, boolean lopen) throws IOException, ParseException {
        ArrayList<String> values = new ArrayList<String>();
        boolean ropen = true;

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case RCLOSE:
                    ropen = false;
                case ROPEN:
                    return buildInterval(type, values, lopen, ropen);
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case COMMA:
                    //Ignore leading whitespace or similar until a value or literal starts:
                    break;
                case '"':
                case '\'':
                    values.add(parseLiteral(reader, c));
                    break;
                default:
                    reader.skip(-1);//Go backwards 1 position, for reading start of value
                    values.add(parseValue(reader));
            }
        }

        return buildInterval(type, values, lopen, ropen);
    }

    /**
     * Parse literal value until detecting the end of it (quote can be ' or ")
     *
     * @param reader Input reader
     * @param quote Quote mode that started this literal (' or ")
     * @return Parsed value
     * @throws IOException
     */
    private static String parseLiteral(StringReader reader, char quote) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean escapeEnabled = false;

        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            if (c == quote) {
                if (escapeEnabled) {
                    sb.append(quote);
                    escapeEnabled = false;
                } else {
                    return sb.toString();
                }
            } else {
                switch (c) {
                    case '\\':
                        if (escapeEnabled) {
                            sb.append('\\');

                            escapeEnabled = false;
                        } else {
                            escapeEnabled = true;
                        }
                        break;
                    default:
                        if (escapeEnabled) {
                            escapeEnabled = false;
                        }
                        sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Parses a value until end is detected either by a comma or an interval closing character.
     *
     * @param reader Input reader
     * @return Parsed value
     * @throws IOException
     */
    private static String parseValue(StringReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int r;
        char c;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            switch (c) {
                case ROPEN:
                case RCLOSE:
                    reader.skip(-1);//Go backwards 1 position, for detecting end of interval
                case COMMA:
                    return sb.toString().trim();
                default:
                    sb.append(c);
            }
        }

        return sb.toString().trim();
    }

    private static <T> IntervalWithValue<T> buildInterval(Class<T> type, ArrayList<String> values, boolean lopen, boolean ropen) throws ParseException {
        double low, high;

        low = DynamicUtilities.parseTime(values.get(0));
        high = DynamicUtilities.parseTime(values.get(1));

        if (type == null) {
            return new IntervalWithValue(low, high, lopen, ropen, null);
        } else {
            Object value = null;
            if (values.size() == 3) {
                //Interval with value:
                String valString = values.get(2);
                if (type.equals(Byte.class)
                        || type.equals(Short.class)
                        || type.equals(Integer.class)
                        || type.equals(Long.class)
                        || type.equals(BigInteger.class)
                        ) {
                    valString = DynamicUtilities.removeDecimalDigitsFromString(valString);
                } else if (type.equals(Float.class)
                        || type.equals(Double.class)
                        || type.equals(BigDecimal.class)
                        ) {
                    valString = DynamicUtilities.infinityIgnoreCase(valString);
                }
                
                value = AttributeUtils.parse(valString, type);
            }
            
            return new IntervalWithValue(low, high, lopen, ropen, value);
        }
    }
}
