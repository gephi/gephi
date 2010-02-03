/*
Copyright 2010 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
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
package org.gephi.timeline.api;

import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;

/**
 *
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
public interface TimelineController {

    /**
     *
     * @param workspace
     * @param from
     * @param to
     * @param node
     */
    public void pushSlice(Workspace workspace, String from, String to, Node node);

    /**
     *
     * @param workspace
     * @param from
     * @param to
     * @param edge
     */
    public void pushSlice(Workspace workspace, String from, String to, Edge edge);

    /**
     *
     * @param workspace
     * @return
     */
    public TimelineModel getModel(Workspace workspace);

    /**
     *
     * @param workspace
     * @return
     */
    public double getFrom(Workspace workspace);

    /**
     *
     * @param workspace
     * @return
     */
    public double getTo(Workspace workspace);

    /**
     *
     * @param workspace
     * @param min
     */
    public void setMin(Workspace workspace, String min);

    /**
     *
     * @param workspace
     * @param max
     */
    public void setMax(Workspace workspace, String max);

    /**
     *
     * @param workspace
     * @param min
     */
    public void setMin(Workspace workspace, double min);

    /**
     *
     * @param workspace
     * @param max
     */
    public void setMax(Workspace workspace, double max);

    /**
     *
     * @param workspace
     * @return
     */
    public TimeInterval getTimeInterval(Workspace workspace);

    /**
     * Get the current TimelineModel of the active Workspace's TimelineModel
     *
     * @return a TimelineModel
     */
    public TimelineModel getModel();

    /**
     * Get the current TimeInterval of the active Workspace's TimelineModel
     *
     * @return the current TimeInterval
     */
    public TimeInterval getTimeInterval();

    /**
     * Get the current lower selection cursor of the active Workspace's TimelineModel
     *
     * @return a double
     */
    public double getFrom();

    /**
     * Get the current upper selection cursor of the active Workspace's TimelineModel
     *
     * @return a double
     */
    public double getTo();

    /**
     * Set the minimum value of the active Workspace's TimelineModel, from a string
     *
     * @param min
     */
    public void setMin(String min);

    /**
     * Set the maximum value  of the active Workspace's TimelineModel, from a string
     *
     * @param max
     */
    public void setMax(String max);

    /**
     * Set the minimum value of the active Workspace's TimelineModel, from a double
     *
     * @param min
     */
    public void setMin(double min);

    /**
     * Set the maximum value of the active Workspace's TimelineModel, from a double
     *
     * @param max
     */
    public void setMax(double max);
}
