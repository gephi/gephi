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
package org.gephi.utils.progress;

/**
 *
 * @author Mathieu Bastian
 */
public interface ProgressTicket {

    /**
     * Finish the progress task.
     */
    public void finish();

    /**
     * Notify the user about a new completed unit. Equivalent to incrementing workunits by one.
     */
    public void progress();

    /**
     * Notify the user about completed workunits.
     * @param a cumulative number of workunits completed so far
     */
    public void progress(int workunit);

    /**
     * Notify the user about progress by showing message with details.
     * @param details about the status of the task
     */
    public void progress(String message);

    /**
     * Notify the user about completed workunits and show additional detailed message.
     * @param message details about the status of the task
     * @param workunit a cumulative number of workunits completed so far
     */
    public void progress(String message, int workunit);

    /**
     * Change the display name of the progress task. Use with care, please make sure the changed name is not completely different, or otherwise it might appear to the user as a different task.
     * @param newDisplayName the new display name
     */
    public void setDisplayName(String newDisplayName);

    /**
     * Start the progress indication for indeterminate task.
     */
    public void start();

    /**
     * Start the progress indication for a task with known number of steps.
     * @param workunits total number of workunits that will be processed
     */
    public void start(int workunits);

    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * @param workunits workunits total number of workunits that will be processed
     */
    public void switchToDeterminate(int workunits);

    /**
     * Currently determinate task can be switched to indeterminate mode.
     */
    public void switchToIndeterminate();
}
