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
package org.gephi.preview.supervisors;

import java.awt.Font;
import org.gephi.preview.updaters.EdgeB1ColorMode;
import org.gephi.preview.updaters.ParentColorMode;

/**
 * Unidirectional edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class UnidirectionalEdgeSupervisorImpl extends DirectedEdgeSupervisorImpl {

    /**
     * Constructor.
     *
     * Initializes default values.
     */
    public UnidirectionalEdgeSupervisorImpl() {
        defaultValues();
    }

    public void defaultValues() {
        curvedFlag = true;
        colorizer = new EdgeB1ColorMode();
        showLabelsFlag = false;
        shortenLabelsFlag = false;
        labelMaxChar = 10;
        baseLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        labelColorizer = new ParentColorMode();
        showMiniLabelsFlag = false;
        shortenMiniLabelsFlag = false;
        miniLabelMaxChar = 10;
        miniLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
        miniLabelAddedRadius = 15f;
        miniLabelColorizer = new ParentColorMode();
        showArrowsFlag = true;
        arrowAddedRadius = 65f;
        arrowSize = 20f;
        arrowColorizer = new ParentColorMode();
        edgeScale = new Float(1f);
        rescaleWeight = Boolean.FALSE;
    }
}
