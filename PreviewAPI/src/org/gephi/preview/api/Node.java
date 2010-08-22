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

import org.gephi.preview.api.util.Holder;

/**
 * Interface of a preview node.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Node {

    /**
     * Returns whether or not the node has a label.
     *
     * @return true if the node has a label
     */
    public boolean hasLabel();

    /**
     * Returns the node's top left position.
     *
     * @return the node's top left position
     */
    public Point getTopLeftPosition();

    /**
     * Returns the node's bottom right position.
     *
     * @return the node's bottom right position
     */
    public Point getBottomRightPosition();

    /**
     * Returns the node's current color.
     *
     * @return the node's current color
     */
    public Color getColor();

    /**
     * Returns the node's position.
     *
     * @return the node's position
     */
    public Point getPosition();

    /**
     * Returns the node's radius.
     *
     * @return the node's radius
     */
    public Float getRadius();

    /**
     * Returns the node's diameter.
     *
     * @return the node's diameter
     */
    public Float getDiameter();

    /**
     * Returns the node's label border.
     *
     * @return the node's label border
     */
    public NodeLabelBorder getLabelBorder();

    /**
     * Returns the node's label.
     *
     * @return the node's label
     */
    public NodeLabel getLabel();

    /**
     * Returns the node's border color.
     *
     * @return the node's border color
     */
    public Color getBorderColor();

    /**
     * Returns the node's border width.
     *
     * @return the node's border width
     */
    public Float getBorderWidth();

    /**
     * Returns whether or not the node's label must be displayed.
     *
     * @return true to display the node's label
     */
    public Boolean showLabel();

    /**
     * Returns whether or not the node's label borders must be displayed.
     *
     * @return true to display the node's label borders
     */
    public Boolean showLabelBorders();

    /**
     * Returns the node's color holder.
     *
     * @return the node's color holder
     */
    public Holder<Color> getColorHolder();
}
