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
package org.gephi.graph.dhns.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Trace system to log node and edge modification. External modules can compare to their local integer
 * to know if they need to update the graph.
 *
 * @author Mathieu Bastian
 */
public class GraphVersion {

//    private int nodeVersion;
//    private int edgeVersion;
    private AtomicInteger nodeVersion = new AtomicInteger();
    private AtomicInteger edgeVersion = new AtomicInteger();

    public GraphVersion() {
//        nodeVersion = 0;
//        edgeVersion = 0;
    }

    public int getNodeVersion() {
//        return nodeVersion;
        return nodeVersion.get();
    }

    public int getEdgeVersion() {
//        return edgeVersion;
        return edgeVersion.get();
    }

    public void incNodeVersion() {
//        nodeVersion++;
        nodeVersion.incrementAndGet();
    }

    public void incEdgeVersion() {
//        edgeVersion++;
        edgeVersion.incrementAndGet();
    }

    public void incNodeAndEdgeVersion() {
//        nodeVersion++;
//        edgeVersion++;
        nodeVersion.incrementAndGet();
        edgeVersion.incrementAndGet();
    }

    public void setVersion(int nodeVersion, int edgeVersion) {
        this.nodeVersion.set(nodeVersion);
        this.edgeVersion.set(edgeVersion);
    }
}
