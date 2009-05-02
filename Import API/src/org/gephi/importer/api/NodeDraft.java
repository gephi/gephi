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
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDraft {

    private String id;
    private String label;
    private List<NodeDraft> children = new ArrayList();
    public boolean hasParent=false;

    //Viz attributes
    private Color color;
    private float size;
    private float x;
    private float y;
    private float z;

    //Result
    private Node node;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void addChild(NodeDraft child)
    {
        children.add(child);
    }

    public List<NodeDraft> getChildren()
    {
        return children;
    }

    public void flushToNode(Node node) {
        setNode(node);
        if (color != null) {
            node.setR(color.getRed() / 255f);
            node.setG(color.getGreen() / 255f);
            node.setB(color.getBlue() / 255f);
        }

        if(label!=null)
            node.setLabel(label);

        if (x != 0) {
            node.setX(x*13);
        }
        if (y != 0) {
            node.setY(y*13);
        }
        if (z != 0) {
            node.setZ(z*13);
        }

        if (size != 0) {
            node.setSize(size);
        }
    }
}
