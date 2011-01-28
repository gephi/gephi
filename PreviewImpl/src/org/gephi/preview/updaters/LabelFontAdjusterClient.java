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
package org.gephi.preview.updaters;

import java.awt.Font;

/**
 * Classes implementing this interface are able to have their label font
 * adjusted.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface LabelFontAdjusterClient {

    /**
     * Returns the label's base font.
     *
     * @return the base font
     */
    public Font getBaseFont();

    /**
     * Returns the label's size factor.
     *
     * @return the size factor
     */
    public float getSizeFactor();

    /**
     * Defines the label's font.
     *
     * @param font  the label's font to set
     */
    public void setFont(Font font);
}
