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
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.NodeChildColorizerClient;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.color.InheritedColor;

/**
 * This color mode colors its clients using their parent's color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class ParentColorMode implements NodeChildColorizer, EdgeChildColorizer {

    public static final String IDENTIFIER = "parent";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
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
