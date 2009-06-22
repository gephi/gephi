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

package org.gephi.utils.longtask;

import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Mathieu Bastian
 */
public interface LongTask {

    /**
     * Cancel the task. Returns <code>true</code> if the task has been sucessfully cancelled, <code>false</code> otherwise.
     * @return  <code>true</code> if the task has been sucessfully cancelled, <code>false</code> otherwise
     */
    public boolean cancel();

    /**
     * Set the progress ticket for the long task. Can't be null.
     * @param progressTicket the progress ticket for this task
     */
    public void setProgressTicket(ProgressTicket progressTicket);
}
