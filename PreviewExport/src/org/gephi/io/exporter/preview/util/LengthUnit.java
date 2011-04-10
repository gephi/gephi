/*
Copyright 2008-2010 Gephi
Authors : Jérémy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gephi.io.exporter.preview.util;

/**
 * Enum representing a set of lenght units.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public enum LengthUnit {

    CENTIMETER,
    MILLIMETER,
    INCH,
    PIXELS,
    PERCENTAGE;

    @Override
    public String toString() {
        switch (this) {
            case CENTIMETER:
                return "cm";
            case MILLIMETER:
                return "mm";
            case INCH:
                return "in";
            case PIXELS:
                return "px";
            default:
            case PERCENTAGE:
                return "%";
        }
    }
}
