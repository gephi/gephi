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

package org.gephi.graph.api;

/**
 * Must be implemented by dynamic graphs
 *
 * @author Julian Bilcke
 */
public interface DynamicGraph {

    /**
     * set the position of the timeline controller. must be a comparable
     *
     * @param position a <code>double</code> representing the position as ratio of the first and last <code>comparables</code>
     * @throws IllegalArgumentException if <code>position</code> is not comprised in the dataset interval
     */
    public Graph getStepCount(double unit);

    /**
     * set the position of the timeline controller. must be a comparable
     *
     * @param position a <code>double</code> representing the upper position as ratio of the first and last <code>comparables</code>
     * @throws IllegalArgumentException if <code>position</code> is not comprised in the dataset interval
     */
    public Graph getSubgraphTo(double to);
    /**
     * set the position of the timeline controller. must be a comparable
     *
     * @param position a <code>double</code> representing the bottom position as ratio of the first and last <code>comparables</code>
     * @throws IllegalArgumentException if <code>position</code> is not comprised in the dataset interval
     */
    public Graph getSubgraphFrom(double from);
        /**
     * set the position of the timeline controller. must be a comparable
     *
     * @param position a <code>double</code> representing the position interval as ratio of the first and last <code>comparables</code>
     * @throws IllegalArgumentException if <code>position</code> is not comprised in the dataset interval
     */
    public Graph getSubgraphFromTo(double from, double to);
}