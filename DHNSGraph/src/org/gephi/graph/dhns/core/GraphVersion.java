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

/**
 * Trace system to log node and edge modification. External modules can compare to their local integer
 * to know if they need to update the graph.
 *
 * @author Mathieu Bastian
 */
public class GraphVersion {

    private int nodeVersion;
    private int edgeVersion;

    public GraphVersion() {
        nodeVersion = 0;
        edgeVersion = 0;
    }

    public int getNodeVersion() {
        return nodeVersion;
    }

    public int getEdgeVersion() {
        return edgeVersion;
    }

    public void incNodeVersion() {
        nodeVersion++;
    }

    public void incEdgeVersion() {
        edgeVersion++;
    }

    public void incNodeAndEdgeVersion() {
        nodeVersion++;
        edgeVersion++;
    }
}
