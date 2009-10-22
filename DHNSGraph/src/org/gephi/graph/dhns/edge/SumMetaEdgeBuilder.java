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

/**
 *
 * @author Mathieu Bastian
 */
public class SumMetaEdgeBuilder implements MetaEdgeBuilder {

    private float weightMinimum = Float.NEGATIVE_INFINITY;
    private float weightLimit = Float.POSITIVE_INFINITY;
    //If the edge pushed to the metaEdge has a common adjacent node with the metaEdge, it is called
    //a non-deep edge and should be considered less important and thus less weighted
    private float nonDeepDivisor = 1f;

    public SumMetaEdgeBuilder() {
    }

    public SumMetaEdgeBuilder(float minimum, float limit, float divisor) {
        this.weightMinimum = minimum;
        this.weightLimit = limit;
        this.nonDeepDivisor = divisor;
    }

    public void pushEdge(AbstractEdge edge, MetaEdgeImpl metaEdge) {
        float edgeWeight = edge.weight;
        float metaWeight = metaEdge.getWeight();
        float div = 1f;
        if (edge.getSource() == metaEdge.getSource() ||
                edge.getSource() == metaEdge.getTarget() ||
                edge.getTarget() == metaEdge.getTarget() ||
                edge.getTarget() == metaEdge.getSource()) {
            div = nonDeepDivisor;
        }
        metaWeight += edgeWeight / div;
        if (metaWeight > weightLimit) {
            metaWeight = weightLimit;
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
            div = nonDeepDivisor;
        }
        metaWeight -= edgeWeight / div;
        if (metaWeight < weightMinimum) {
            metaWeight = weightMinimum;
        }
        metaEdge.setWeight(metaWeight);
    }
}
