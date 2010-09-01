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
import org.gephi.preview.api.EdgeColorizer;

/**
 * Edge supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface EdgeSupervisor extends Supervisor {

    public Float getEdgeScale();

    public void setEdgeScale(Float scale);

    public Boolean getRescaleWeight();

    public void setRescaleWeight(Boolean rescaleWeight);

    /**
     * Returns true if the edges must be displayed in the preview.
     *
     * @return true if the edges must be displayed in the preview
     */
    public Boolean getShowFlag();

    /**
     * Returns true if the edges are curved.
     * 
     * @return true if the edges are curved
     */
    public Boolean getCurvedFlag();

    /**
     * Defines if the edges are curved.
     *
     * @param value  true for curved edges
     */
    public void setCurvedFlag(Boolean value);

    /**
     * Returns the edge colorizer.
     *
     * @return the edge colorizer
     */
    public EdgeColorizer getColorizer();

    /**
     * Defines the edge colorizer.
     *
     * @param value  the edge colorizer to set
     */
    public void setColorizer(EdgeColorizer value);

    /**
     * Returns true if the edge labels must be displayed in the preview.
     *
     * @return true if the edge labels must be displayed in the preview
     */
    public Boolean getShowLabelsFlag();

    /**
     * Defines if the edge labels must be displayed in the preview.
     *
     * @param value  true to display the edge labels in the preview
     */
    public void setShowLabelsFlag(Boolean value);

    /**
     * Returns the base edge label font.
     *
     * @return the base edge label font
     */
    public Font getBaseLabelFont();

    /**
     * Defines the base edge label font.
     *
     * @param value  the base edge label font to set
     */
    public void setBaseLabelFont(Font value);

    /**
     * Returns the edge label character limit.
     *
     * @return the edge label character limit
     */
    public Integer getLabelMaxChar();

    /**
     * Defines the edge label character limit.
     *
     * @param value  the edge label character limit to set
     */
    public void setLabelMaxChar(Integer value);

    /**
     * Returns whether the edge labels must be shortened.
     *
     * @return true to shorten the edge labels
     */
    public Boolean getShortenLabelsFlag();

    /**
     * Defines if the edge labels must be shortened.
     *
     * @param value  true to shorten the edge labels
     */
    public void setShortenLabelsFlag(Boolean value);

    /**
     * Returns the edge label colorizer.
     *
     * @return the edge label colorizer
     */
    public EdgeChildColorizer getLabelColorizer();

    /**
     * Defines the edge label colorizer.
     *
     * @param value  the edge label colorizer to set
     */
    public void setLabelColorizer(EdgeChildColorizer value);
}
