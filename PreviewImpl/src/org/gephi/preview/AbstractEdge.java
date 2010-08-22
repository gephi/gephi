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
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;

/**
 * Generic implementation of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class AbstractEdge implements EdgeColorizerClient {

    protected final GraphImpl parent;
    private final Float thickness;
    protected final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the edge
     * @param thickness  the edge's thickness
     */
    public AbstractEdge(GraphImpl parent, float thickness) {
        this.parent = parent;
        this.thickness = thickness;
    }

    /**
     * Returns the edge's color.
     *
     * @return the edge's color
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * Returns the edge's color holder.
     * 
     * @return the edge's color holder
     */
    public Holder<Color> getColorHolder() {
        return colorHolder;
    }

    /**
     * Returns the edge's thickness.
     *
     * @return the edge's thickness
     */
    public Float getThickness() {
        return thickness;
    }

    /**
     * Sets the edge's color.
     *
     * @return the color to set to the edge
     */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }
}
