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
package org.gephi.visualization;

import org.gephi.lib.gleem.linalg.Vec3f;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphLimits {

    private int minXviewport;
    private int maxXviewport;
    private int minYviewport;
    private int maxYviewport;
    private float minXoctree;
    private float maxXoctree;
    private float minYoctree;
    private float maxYoctree;
    private float minZoctree;
    private float maxZoctree;
    private Vec3f closestPoint = new Vec3f(0, 0, 0);
    private float maxWeight;
    private float minWeight;

    public synchronized float getMaxXoctree() {
        return maxXoctree;
    }

    public synchronized void setMaxXoctree(float maxXoctree) {
        this.maxXoctree = maxXoctree;
    }

    public synchronized int getMaxXviewport() {
        return maxXviewport;
    }

    public synchronized void setMaxXviewport(int maxXviewport) {
        this.maxXviewport = maxXviewport;
    }

    public synchronized float getMaxYoctree() {
        return maxYoctree;
    }

    public synchronized void setMaxYoctree(float maxYoctree) {
        this.maxYoctree = maxYoctree;
    }

    public synchronized int getMaxYviewport() {
        return maxYviewport;
    }

    public synchronized void setMaxYviewport(int maxYviewport) {
        this.maxYviewport = maxYviewport;
    }

    public synchronized float getMaxZoctree() {
        return maxZoctree;
    }

    public synchronized void setMaxZoctree(float maxZoctree) {
        this.maxZoctree = maxZoctree;
    }

    public synchronized float getMinXoctree() {
        return minXoctree;
    }

    public synchronized void setMinXoctree(float minXoctree) {
        this.minXoctree = minXoctree;
    }

    public synchronized int getMinXviewport() {
        return minXviewport;
    }

    public synchronized void setMinXviewport(int minXviewport) {
        this.minXviewport = minXviewport;
    }

    public synchronized float getMinYoctree() {
        return minYoctree;
    }

    public synchronized void setMinYoctree(float minYoctree) {
        this.minYoctree = minYoctree;
    }

    public synchronized int getMinYviewport() {
        return minYviewport;
    }

    public synchronized void setMinYviewport(int minYviewport) {
        this.minYviewport = minYviewport;
    }

    public synchronized float getMinZoctree() {
        return minZoctree;
    }

    public synchronized void setMinZoctree(float minZoctree) {
        this.minZoctree = minZoctree;
    }

    public synchronized float getDistanceFromPoint(float x, float y, float z) {

        float dis = (float) Math.sqrt((closestPoint.x() - x) * (closestPoint.x() - x) + (closestPoint.y() - y) * (closestPoint.y() - y) + (closestPoint.z() - z) * (closestPoint.z() - z));
        return dis;

    //Minimum distance with the 8 points of the cube, poor method
        /*double min = Double.POSITIVE_INFINITY;
    min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(minYoctree-y)*(minYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
    min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(minYoctree-y)*(minYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
    min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
    min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(maxZoctree-z)*(maxZoctree-z)));
    min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(minYoctree-y)*(minYoctree-y)+(minZoctree-z)*(minZoctree-z)));
    min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(minYoctree-y)*(minYoctree-y)+(minZoctree-z)*(minZoctree-z)));
    min = Math.min(min,Math.sqrt((minXoctree-x)*(minXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(minZoctree-z)*(minZoctree-z)));
    min = Math.min(min,Math.sqrt((maxXoctree-x)*(maxXoctree-x)+(maxYoctree-y)*(maxYoctree-y)+(minZoctree-z)*(minZoctree-z)));
    return (float)min;*/
    }

    public void setClosestPoint(Vec3f closestPoint) {
        this.closestPoint = closestPoint;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(float minWeight) {
        this.minWeight = minWeight;
    }
}
