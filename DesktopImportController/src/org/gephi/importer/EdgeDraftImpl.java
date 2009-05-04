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
package org.gephi.importer;

import org.gephi.importer.NodeDraftImpl;
import java.awt.Color;
import org.gephi.graph.api.Edge;
import org.gephi.importer.api.EdgeDraft;
import org.gephi.importer.api.NodeDraft;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeDraftImpl implements EdgeDraft {

    public enum EdgeType {

        DIRECTED, UNDIRECTED, MUTUAL
    };
    private String id;
    private String label;

    //Topology
    private NodeDraftImpl nodeSource;
    private NodeDraftImpl nodeTarget;
    private float cardinal;
    private EdgeType edgeType;

    //Viz
    private Color color;
    private boolean labelVisible;
    private boolean visible;

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

    public void setColor(String r, String g, String b) {
        setColor(Integer.parseInt(r),Integer.parseInt(g),Integer.parseInt(b));
    }

    public void setColor(float r, float g, float b)
    {
        r = Math.max(Math.min(r, 1f),0f);
        g = Math.max(Math.min(g, 1f),0f);
        b = Math.max(Math.min(b, 1f),0f);
        this.color = new Color(r, g, b);
    }

    public void setColor(int r, int g, int b)
    {
        setColor(r/255f,g/255f,b/255f);
    }
    
    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setDirected(boolean directed) {
        if (directed) {
            this.edgeType = EdgeType.DIRECTED;
        } else {
            this.edgeType = EdgeType.UNDIRECTED;
        }
    }

    public boolean isDirected() {
        return edgeType == EdgeType.DIRECTED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeDraftImpl getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(NodeDraft nodeSource) {
        this.nodeSource = (NodeDraftImpl)nodeSource;
    }

    public NodeDraftImpl getNodeTarget() {
        return nodeTarget;
    }

    public void setNodeTarget(NodeDraft nodeTarget) {
        this.nodeTarget = (NodeDraftImpl)nodeTarget;
    }

    public void flushToEdge(Edge edge) {
        if (color != null) {
            edge.setR(color.getRed() / 255f);
            edge.setG(color.getGreen() / 255f);
            edge.setB(color.getBlue() / 255f);
        }

    }
}
