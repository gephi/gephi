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
package gephi.data.network;

import gephi.visualization.Renderable;
import gephi.visualization.opengl.Object3d;

/**
 *
 * @author Mathieu
 */
public class Edge implements Renderable {

    private Node source;
    private Node target;
    public float r = 0f;
    public float g = 0f;
    public float b = 0f;
    public float a = 0.5f;
    private float cardinal = 1f;
    private Object3d object3d;

    public Edge(Node source, Node target)
    {
        this.source = source;
        this.target = target;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public Object3d getObject3d() {
        return object3d;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }

    public float getRadius() {
        return cardinal;
    }

    public float getSize() {
        return cardinal;
    }

    public float getX() {
        return Math.abs(source.getX() - target.getX());
    }

    public float getY() {
        return Math.abs(source.getY() - target.getY());
    }

    public float getZ() {
        return Math.abs(source.getZ() - target.getZ());
    }

    public void setObject3d(Object3d obj) {
        this.object3d = obj;
    }

    public void setX(float x) {
    }

    public void setY(float y) {
    }

    public void setZ(float z) {
    }

    public void setCardinal(float cardinal)
    {
        this.cardinal = cardinal;
    }
}
