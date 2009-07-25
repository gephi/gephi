/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.dhns.edge;

import org.gephi.graph.dhns.core.EdgeProcessor.MetaEdgeBuilder;

/**
 *
 * @author Mathieu Bastian
 */
public class DefaultMetaEdgeBuilder implements MetaEdgeBuilder {

    private static final float WEIGHT_MINIMUM = 0.1f;
    private static final float WEIGHT_LIMIT = 10f;
    //If the edge pushed to the metaEdge has a common adjacent node with the metaEdge, it is called
    //a non-deep edge and should be considered less important and thus less weighted
    private static final float NON_DEEP_DIVISOR = 10f;

    public void pushEdge(AbstractEdge edge, MetaEdgeImpl metaEdge) {
        float edgeWeight = edge.weight;
        float metaWeight = metaEdge.getWeight();
        float div = 1f;
        if (edge.getSource() == metaEdge.getSource() ||
                edge.getSource() == metaEdge.getTarget() ||
                edge.getTarget() == metaEdge.getTarget() ||
                edge.getTarget() == metaEdge.getSource()) {
            div = NON_DEEP_DIVISOR;
        }
        metaWeight += edgeWeight / div;
        if (metaWeight > WEIGHT_LIMIT) {
            metaWeight = WEIGHT_LIMIT;
        }
        metaEdge.setWeight(metaWeight);
    }

    public void pullEdge(AbstractEdge edge, MetaEdgeImpl metaEdge) {
        float edgeWeight = edge.weight;
        float metaWeight = metaEdge.getWeight();
        float div = 1f;
        if (edge.getSource() == metaEdge.getSource() ||
                edge.getSource() == metaEdge.getTarget() ||
                edge.getTarget() == metaEdge.getTarget() ||
                edge.getTarget() == metaEdge.getSource()) {
            div = NON_DEEP_DIVISOR;
        }
        metaWeight -= edgeWeight / div;
        if (metaWeight < WEIGHT_MINIMUM) {
            metaWeight = WEIGHT_MINIMUM;
        }
        metaEdge.setWeight(metaWeight);
    }
}
