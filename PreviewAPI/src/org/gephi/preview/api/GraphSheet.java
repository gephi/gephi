/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview.api;

/**
 * Interface of a preview graph sheet.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface GraphSheet {

    /**
     * Returns the preview graph.
     *
     * @return the preview graph
     */
    public Graph getGraph();

    /**
     * Returns the top left position of the graph sheet.
     *
     * @return the top left position of the graph sheet
     */
    public Point getTopLeftPosition();

    /**
     * Returns the bottom right position of the graph sheet.
     *
     * @return the bottom right position of the graph sheet
     */
    public Point getBottomRightPosition();

    /**
     * Returns the sheet's width.
     *
     * @return the sheet's width
     */
    public Float getWidth();

    /**
     * Returns the sheet's height.
     *
     * @return the sheet's height
     */
    public Float getHeight();

    /**
     * Defines the sheet's margin.
     *
     * @param value  the value of the sheet's margin
     */
    public void setMargin(float value);
}
