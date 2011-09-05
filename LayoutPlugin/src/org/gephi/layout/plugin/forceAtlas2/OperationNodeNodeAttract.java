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
import org.gephi.layout.plugin.forceAtlas2.ForceFactory.AttractionForce;

/**
 *
 * @author Mathieu Jacomy
 */
public class OperationNodeNodeAttract extends Operation {

    private final Node n1;
    private final Node n2;
    private final AttractionForce f;
    private final double coefficient;

    public OperationNodeNodeAttract(Node n1, Node n2, AttractionForce f, double coefficient) {
        this.n1 = n1;
        this.n2 = n2;
        this.f = f;
        this.coefficient = coefficient;
    }

    @Override
    public void execute() {
        f.apply(n1, n2, coefficient);
    }
}
