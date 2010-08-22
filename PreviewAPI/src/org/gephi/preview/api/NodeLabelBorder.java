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
 * Implementation of a node label border.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface NodeLabelBorder {

    /**
     * Returns the node label border's color.
     *
     * @return the node label border's color
     */
    public Color getColor();

    /**
     * Returns the node label border's position.
     *
     * @return the node label border's position
     */
    public Point getPosition();

    /**
     * Returns the node label border's related label.
     *
     * @return the node label border's related label
     */
    public NodeLabel getLabel();
}
