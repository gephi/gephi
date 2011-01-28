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
import org.gephi.preview.api.NodeChildColorizerClient;
import org.gephi.preview.api.Point;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;

/**
 * Generic implementation of a preview node child.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class AbstractNodeChild implements NodeChildColorizerClient {

    protected final NodeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

    /**
     * Constructor.
     *
     * @param parent  the parent node
     */
    public AbstractNodeChild(NodeImpl parent) {
        this.parent = parent;
    }

    /**
     * Returns the node child's color.
     *
     * @return the node child's color
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * Returns the node child's position.
     *
     * @return the node child's position
     */
    public Point getPosition() {
        return parent.getPosition();
    }

    /**
     * Sets the color of the node child.
     *
     * @param color  the color to set to the node child
     */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    /**
     * Returns the color holder of the parent node.
     *
     * @return the color holder of the parent node
     */
    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }
}
