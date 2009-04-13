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
package org.gephi.data.network.potato;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.node.PreNode;

/**
 *
 * @author Mathieu Bastian
 */
public class Potato {

    PreNode master;
    private List<PreNode> content;

    //Display
    List<Triangle> triangles = new ArrayList<Triangle>();
    List<TriangleFan> trianglesFan = new ArrayList<TriangleFan>();
    List<Square> squares = new ArrayList<Square>();
    List<Circle> circles = new ArrayList<Circle>();

    public Potato() {
        content = new ArrayList<PreNode>();
    }

    public void setMaster(PreNode master) {
        this.master = master;
    }

    public void addContent(PreNode content) {
        this.content.add(content);
    }

    public PreNode getMaster()
    {
        return master;
    }

    public List<PreNode> getContent()
    {
        return content;
    }

    public void print() {
        System.out.println("Father: " + master.pre);
        for (int i = 0; i < content.size(); i++) {
            System.out.println("\t" + content.get(i).pre);
        }
    }

    public static class Triangle
    {
        public float[] array = new float[6];

         public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {

            array[0] = x1;
            array[1] = y1;
            array[2] = x2;
            array[3] = y2;
            array[4] = x3;
            array[5] = y3;
        }
    }

    public static class TriangleFan
    {
        public float[] array;
        private int pointer=0;
        public TriangleFan(int num)
        {
            array = new float[3*num];
        }

        public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
            array[pointer++] = x1;
            array[pointer++] = y1;
            array[pointer++] = x2;
            array[pointer++] = y2;
            array[pointer++] = x3;
            array[pointer++] = y3;
        }
    }

    public static class Square
    {
        public float[] array = new float[8];

        public void square(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
             array[0] = x1;
            array[1] = y1;
            array[2] = x2;
            array[3] = y2;
            array[4] = x3;
            array[5] = y3;
            array[6] = x4;
            array[7] = y4;
        }
    }

    public static class Circle
    {
        float x;
        float y;
        float rayon;

        public void circle(float x, float y, float rayon)
        {
            this.x = x;
            this.y = y;
            this.rayon = rayon;
        }
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public void setSquares(List<Square> squares) {
        this.squares = squares;
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public void setTriangles(List<Triangle> triangles) {
        this.triangles = triangles;
    }

    public List<TriangleFan> getTrianglesFan() {
        return trianglesFan;
    }

    public void setTrianglesFan(List<TriangleFan> trianglesFan) {
        this.trianglesFan = trianglesFan;
    }

    
}
