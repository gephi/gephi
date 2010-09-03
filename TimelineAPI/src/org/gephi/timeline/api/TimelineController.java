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

import org.gephi.project.api.Workspace;

/**
 *
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
public interface TimelineController {

    public TimelineModel getModel(Workspace workspace);

    /**
     * Get the current TimelineModel of the active Workspace's TimelineModel
     *
     * @return a TimelineModel
     */
    public TimelineModel getModel();

    /**
     *
     * @param listener
     */
    public void addListener(TimelineModelListener listener);

    /**
     *
     * @param listener
     */
    public void removeListener(TimelineModelListener listener);
}
