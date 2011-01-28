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

import org.gephi.preview.api.Color;
import org.gephi.preview.api.NodeColorizer;
import org.gephi.preview.api.NodeColorizerClient;

/**
 * This color mode colors its node clients using their original color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeOriginalColorMode implements NodeColorizer {

    private static final String IDENTIFIER = "original";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeColorizerClient client) {
        Color color = client.getOriginalColor();
        client.setColor(color);
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
