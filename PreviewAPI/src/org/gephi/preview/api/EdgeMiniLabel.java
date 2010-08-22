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

import java.awt.Font;
import org.gephi.preview.api.util.HAlign;

/**
 * Interface of a preview edge mini-label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface EdgeMiniLabel {

    /**
     * Returns the edge mini-label's color.
     *
     * @return the edge mini-label's color
     */
    public Color getColor();

    /**
     * Returns the edge mini-label's horizontal alignment.
     *
     * @return the edge mini-label's horizontal alignment
     */
    public HAlign getHAlign();

    /**
     * Returns the edge mini-label's position.
     *
     * @return the edge mini-label's position
     */
    public Point getPosition();

    /**
     * Returns the edge mini-label's angle.
     *
     * @return the edge mini-label's angl
     */
    public Float getAngle();

    /**
     * Returns the edge mini-label's current value.
     *
     * @return the edge mini-label's current value
     */
    public String getValue();

    /**
     * Returns the edge mini-label font.
     *
     * @return the edge mini-label font
     */
    public Font getFont();
}
