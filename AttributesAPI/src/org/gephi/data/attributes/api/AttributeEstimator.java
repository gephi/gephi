/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
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

package org.gephi.data.attributes.api;

/**
 * This enum is used to determine what should be done with "ties". For example
 * if in the given time interval some attribute has got 3 different values we
 * should know how to estimate its value.
 *
 * @author Cezary Bartosiak
 */
public enum AttributeEstimator {
    AVERAGE,
    SUM,
    MIN,
    MAX,
    FIRST_OCCURENCE,
    LAST_OCCURENCE,
    CONCAT
}
