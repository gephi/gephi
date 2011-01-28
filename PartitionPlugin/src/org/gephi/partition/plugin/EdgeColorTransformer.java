/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.partition.plugin;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;
import org.gephi.partition.spi.Transformer;
import org.gephi.utils.PaletteUtils;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeColorTransformer implements Transformer {

    private static final Color DEFAULT_COLOR = Color.BLACK;
    private Map<Object, Color> map;

    public EdgeColorTransformer() {
        map = new HashMap<Object, Color>();
    }

    public Map<Object, Color> getMap() {
        return map;
    }

    public void randomizeColors(Partition partition) {
        List<Color> colors = PaletteUtils.getSequenceColors(partition.getPartsCount());
        int i = 0;
        for (Part p : partition.getParts()) {
            getMap().put(p.getValue(), colors.get(i));
            i++;
        }
    }

    public void transform(Partition partition) {
        EdgePartition edgePartition = (EdgePartition) partition;
        for (Part<Edge> part : edgePartition.getParts()) {
            Color color = map.get(part.getValue());
            if (color == null) {
                color = DEFAULT_COLOR;
            }
            part.setColor(color);
            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;

            for (Edge edge : part.getObjects()) {
                edge.getEdgeData().setColor(r, g, b);
            }
        }
    }
}
