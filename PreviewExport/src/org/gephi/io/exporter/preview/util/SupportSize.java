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
 * Implementation of the size of an export support.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SupportSize {

    private final Integer width;
    private final Integer height;
    private final LengthUnit lengthUnit;

    /**
     * Constructor.
     *
     * @param width       the support's width
     * @param height      the support's height
     * @param lengthUnit  the lenght unit
     */
    public SupportSize(int width, int height, LengthUnit lengthUnit) {
        this.width = width;
        this.height = height;
        this.lengthUnit = lengthUnit;
    }

    public Integer getWidthInt() {
        return width;
    }

    public Integer getHeightInt() {
        return height;
    }

    /**
     * Returns the support's width.
     *
     * @return the support's width
     */
    public String getWidth() {
        return width.toString() + lengthUnit.toString();
    }

    /**
     * Returns the support's height.
     *
     * @return the support's height
     */
    public String getHeight() {
        return height.toString() + lengthUnit.toString();
    }
}
