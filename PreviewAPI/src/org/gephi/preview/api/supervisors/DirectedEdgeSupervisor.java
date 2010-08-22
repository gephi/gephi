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
package org.gephi.preview.api.supervisors;

import java.awt.Font;
import org.gephi.preview.api.EdgeChildColorizer;

/**
 * Directed edge supervisor interface.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface DirectedEdgeSupervisor extends EdgeSupervisor {

    /**
     * Returns true if the edge mini-labels must be displayed in the preview.
     *
     * @return true if the edge mini-labels must be displayed in the preview.
     */
    public Boolean getShowMiniLabelsFlag();

    /**
     * Defines if the edge mini-labels must be displayed in the preview.
     *
     * @param value  true to display the edge mini-labels in the preview
     */
    public void setShowMiniLabelsFlag(Boolean value);

    /**
     * Returns the edge mini-label font.
     *
     * @return the edge mini-label font
     */
    public Font getMiniLabelFont();

    /**
     * Defines the edge mini-label font.
     *
     * @param value  the edge mini-label font to set
     */
    public void setMiniLabelFont(Font value);

    /**
     * Returns the edge mini-label character limit.
     *
     * @return the edge mini-label character limit
     */
    public Integer getMiniLabelMaxChar();

    /**
     * Defines the edge mini-label character limit.
     *
     * @param value  the edge mini-label character limit
     */
    public void setMiniLabelMaxChar(Integer value);

    /**
     * Returns whether the edge mini-labels must be shortened.
     *
     * @return true to shorten the edge mini-labels
     */
    public Boolean getShortenMiniLabelsFlag();

    /**
     * Defines if the edge mini-labels must be shortened.
     *
     * @param value  true to shorten the edge mini-labels
     */
    public void setShortenMiniLabelsFlag(Boolean value);

    /**
     * Returns the edge mini-label added radius.
     *
     * @return the edge mini-label added radius
     */
    public Float getMiniLabelAddedRadius();

    /**
     * Defines the edge mini-label added radius.
     *
     * @param value  the edge mini-label added radius to set
     */
    public void setMiniLabelAddedRadius(Float value);

    /**
     * Returns the edge mini-label colorizer.
     *
     * @return the edge mini-label colorizer
     */
    public EdgeChildColorizer getMiniLabelColorizer();

    /**
     * Defines the edge mini-label colorizer.
     *
     * @param value  the edge mini-label colorizer to set
     */
    public void setMiniLabelColorizer(EdgeChildColorizer value);

    /**
     * Returns true if the edge arrows must be displayed in the preview.
     *
     * @return true if the edge arrows must be displayed in the preview
     */
    public Boolean getShowArrowsFlag();

    /**
     * Defines if the edge arrows must be displayed in the preview.
     *
     * @param value  true to display the edge arrows in the preview
     */
    public void setShowArrowsFlag(Boolean value);

    /**
     * Returns the edge arrow added radius.
     *
     * @return the edge arrow added radius
     */
    public Float getArrowAddedRadius();

    /**
     * Defines the edge arrow added radius.
     *
     * @param value  the edge arrow added radius to set
     */
    public void setArrowAddedRadius(Float value);

    /**
     * Returns the edge arrow size.
     *
     * @return the edge arrow size
     */
    public Float getArrowSize();

    /**
     * Defines the edge arrow size.
     *
     * @param value  the edge arrow size to set
     */
    public void setArrowSize(Float value);

    /**
     * Returns the edge arrow colorizer.
     *
     * @return the edge arrow colorizer
     */
    public EdgeChildColorizer getArrowColorizer();

    /**
     * Defines the edge arrow colorizer.
     *
     * @param value  the edge arrow colorizer to set
     */
    public void setArrowColorizer(EdgeChildColorizer value);
}
