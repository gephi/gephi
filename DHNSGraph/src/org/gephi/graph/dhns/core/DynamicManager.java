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
package org.gephi.graph.dhns.core;

import org.gephi.graph.api.DynamicData;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicManager {

    private Dhns dhns;
    private boolean dynamic;

    public DynamicManager(Dhns dhns) {
        this.dhns = dhns;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void pushNode(NodeData nodeData) {
        DynamicData dd = nodeData.getDynamicData();
        if (dd != null && dd.getRangeFrom() != -1 || dd.getRangeTo() != -1) {
            dynamic = true;
        }
    }

    public void pushEdge(EdgeData edgeData) {
        DynamicData dd = edgeData.getDynamicData();
        if (dd != null && dd.getRangeFrom() != -1 || dd.getRangeTo() != -1) {
            dynamic = true;
        }
    }
}
