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

import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.util.color.InheritedColor;

/**
 * This color mode colors its clients using the color of an edge's second
 * boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class EdgeB2ColorMode
        implements EdgeColorizer, EdgeChildColorizer {

    private static final String IDENTIFIER = "target";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeColorizerClient client) {
        client.setColor(new InheritedColor(client.getNode2().getColorHolder()));
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeChildColorizerClient client) {
        client.setColor(new InheritedColor(client.getParentEdge().getNode2().getColorHolder()));
    }

    /**
     * Formats the colorizer as a string.
     *
     * @return the colorizer formatted as a string
     */
    @Override
    public String toString() {
        return IDENTIFIER;
    }

    /**
     * Returns the colorizer's identifier.
     *
     * @return the colorizer's identifier
     */
    public static String getIdentifier() {
        return IDENTIFIER;
    }
}
