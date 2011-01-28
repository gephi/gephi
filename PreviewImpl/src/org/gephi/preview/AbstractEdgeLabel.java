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

import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.Point;
import org.gephi.preview.updaters.LabelShortenerClient;
import org.gephi.preview.util.HolderImpl;
import org.gephi.preview.util.Vector;

/**
 * Generic implementation of an edge label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class AbstractEdgeLabel implements LabelShortenerClient, EdgeChildColorizerClient {

    protected final String originalValue;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
    protected String value;
    protected PointImpl position;

    /**
     * Constructor.
     *
     * @param value   the value of the edge label
     */
    public AbstractEdgeLabel(String value) {
        originalValue = value;
    }

    /**
     * Returns the edge label's position.
     *
     * @return the edge label's position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Returns the edge label's original value.
     *
     * @return the edge label's original value
     */
    public String getOriginalValue() {
        return originalValue;
    }

    /**
     * Returns the edge label's current value.
     *
     * @return the edge label's current value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the edge label's color.
     *
     * @return the edge label's color
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    public void revertOriginalValue() {
        setValue(originalValue);
    }

    /**
     * Sets the edge label's position above its parent edge's one.
     */
    protected void putPositionAboveEdge(Vector edgeDirection, float edgeThickness) {
        // normal vector for vertical align
        Vector n = new Vector(edgeDirection.y, -edgeDirection.x);

        // the mini-label mustn't be on the edge but over/under it
        n.mult(edgeThickness / 2);
        Vector positionVector = new Vector(position);
        positionVector.add(n);

        position = new PointImpl(positionVector);
    }
}
