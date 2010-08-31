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
package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.EdgeLabel;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.updaters.LabelFontAdjusterClient;
import org.gephi.preview.util.Vector;

/**
 * Implementation of an edge label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class EdgeLabelImpl extends AbstractEdgeLabel
        implements EdgeLabel, LabelFontAdjusterClient {

    private final EdgeImpl parent;
    private final float labelSizeFactor;
    private Font font;

    /**
     * Constructor.
     *
     * @param parent     the parent edge of the edge label
     * @param value      the value of the edge label
     * @param labelSize  the size of the edge label
     */
    public EdgeLabelImpl(EdgeImpl parent, String value, float labelSize) {
        super(value);
        this.parent = parent;
        labelSizeFactor = labelSize;
        genPosition();
    }

    /**
     * Generates the edge label's position.
     */
    public void genPosition() {
        // relative position from the first boundary
        Vector positionVector = new Vector(parent.getNode1().getPosition());
        
        // move it to the middle of the edge
        Vector semiLength = new Vector(parent.getDirection());
        semiLength.mult(parent.getLength() / 2);
        positionVector.add(semiLength);

        position = new PointImpl(positionVector);

        // set label position above the parent edge
        putPositionAboveEdge(parent.getDirection(), parent.getThickness());
    }

    public Font getFont() {
        return font;
    }

    public Font getBaseFont() {
        return parent.getBaseLabelFont();
    }

    public float getSizeFactor() {
        return labelSizeFactor;
    }

    public Float getAngle() {
        return parent.getAngle();
    }

    public EdgeColorizerClient getParentEdge() {
        return parent;
    }

    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }

    public void setFont(Font font) {
        this.font = font;
    }
}
