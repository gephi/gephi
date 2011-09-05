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
public class OperationNodeRegionRepulse extends Operation {

    private final Node n;
    private final Region r;
    private final RepulsionForce f;
    private final double theta;

    public OperationNodeRegionRepulse(Node n, Region r, RepulsionForce f, double theta) {
        this.n = n;
        this.f = f;
        this.r = r;
        this.theta = theta;
    }

    @Override
    public void execute() {
        r.applyForce(n, f, theta);
    }
}
