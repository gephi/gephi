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
package org.gephi.importer.api;

import java.awt.Color;
import org.gephi.graph.api.Edge;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeDraft {

    public enum EdgeType {

        DIRECTED, UNDIRECTED, MUTUAL
    };
    private String id;

    //Topology
    private NodeDraft nodeSource;
    private NodeDraft nodeTarget;
    private float cardinal;
    private EdgeType edgeType;

    //Viz
    private Color color;

    public float getCardinal() {
        return cardinal;
    }

    public void setCardinal(float cardinal) {
        this.cardinal = cardinal;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(EdgeType edgeType) {
        this.edgeType = edgeType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeDraft getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(NodeDraft nodeSource) {
        this.nodeSource = nodeSource;
    }

    public NodeDraft getNodeTarget() {
        return nodeTarget;
    }

    public void setNodeTarget(NodeDraft nodeTarget) {
        this.nodeTarget = nodeTarget;
    }

    public void flushToEdge(Edge edge) {
        if (color != null) {
            edge.setR(color.getRed() / 255f);
            edge.setG(color.getGreen() / 255f);
            edge.setB(color.getBlue() / 255f);
        }
        
    }
}
