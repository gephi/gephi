/*
Copyright 2008-2011 Gephi
Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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
package org.gephi.layout.plugin.forceAtlas2;

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory.RepulsionForce;

/**
 *
 * @author Mathieu Jacomy
 */
public class NodesThread implements Runnable {

    private Node[] nodes;
    private int from;
    private int to;
    private Region rootRegion;
    private boolean barnesHutOptimize;
    private RepulsionForce Repulsion;
    private double barnesHutTheta;
    private double gravity;
    private RepulsionForce GravityForce;
    private double scaling;

    public NodesThread(Node[] nodes, int from, int to, boolean barnesHutOptimize, double barnesHutTheta, double gravity, RepulsionForce GravityForce, double scaling, Region rootRegion, RepulsionForce Repulsion) {
        this.nodes = nodes;
        this.from = from;
        this.to = to;
        this.rootRegion = rootRegion;
        this.barnesHutOptimize = barnesHutOptimize;
        this.Repulsion = Repulsion;
        this.barnesHutTheta = barnesHutTheta;
        this.gravity = gravity;
        this.GravityForce = GravityForce;
        this.scaling = scaling;
    }

    @Override
    public void run() {
        // Repulsion
        if (barnesHutOptimize) {
            for (int nIndex = from; nIndex < to; nIndex++) {
                Node n = nodes[nIndex];
                rootRegion.applyForce(n, Repulsion, barnesHutTheta);
            }
        } else {
            for (int n1Index = from; n1Index < to; n1Index++) {
                Node n1 = nodes[n1Index];
                for (int n2Index = 0; n2Index < n1Index; n2Index++) {
                    Node n2 = nodes[n2Index];
                    Repulsion.apply(n1, n2);
                }
            }
        }

        // Gravity
        for (int nIndex = from; nIndex < to; nIndex++) {
            Node n = nodes[nIndex];
            GravityForce.apply(n, gravity / scaling);
        }
    }
}
