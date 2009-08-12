/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.force;

import org.gephi.graph.api.NodeData;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class ProportionalDisplacement implements Displacement {

    private float step;

    public ProportionalDisplacement(float step) {
        this.step = step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    private boolean assertValue(float value) {
        boolean ret = !Float.isInfinite(value) && !Float.isNaN(value);
        return ret;
    }

    public void moveNode(NodeData node, ForceVector forceData) {
        ForceVector displacement = new ForceVector(forceData);
        displacement.multiply(step);

        float x = node.x() + displacement.x();
        float y = node.y() + displacement.y();

        if (assertValue(x)) {
            node.setX(x);
        }
        if (assertValue(y)) {
            node.setY(y);
        }
    }
}
