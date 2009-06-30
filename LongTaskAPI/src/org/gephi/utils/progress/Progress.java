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
public class Progress {

    /**
     * Finish the progress task.
     * @param progressTicket the progress ticket of the task
     */
    public static void finish(ProgressTicket progressTicket) {
        if (progressTicket != null) {
            progressTicket.finish();
        }
    }

    /**
     * Notify the user about a new completed unit. Equivalent to incrementing workunits by one.
     * @param progressTicket the progress ticket of the task
     */
    public static void progress(ProgressTicket progressTicket) {
        if (progressTicket != null) {
            progressTicket.progress();
        }
    }

    /**
     * Notify the user about completed workunits.
     * @param progressTicket the progress ticket of the task
     * @param workunit a cumulative number of workunits completed so far
     */
    public static void progress(ProgressTicket progressTicket, int workunit) {
        if (progressTicket != null) {
            progressTicket.progress(workunit);
        }
    }

    /**
     * Notify the user about progress by showing message with details.
     * @param progressTicket the progress ticket of the task
     * @param message details about the status of the task
     */
    public static void progress(ProgressTicket progressTicket, String message) {
        if (progressTicket != null) {
            progressTicket.progress(message);
        }
    }

    /**
     * Notify the user about completed workunits and show additional detailed message.
     * @param progressTicket the progress ticket of the task
     * @param message details about the status of the task
     * @param workunit a cumulative number of workunits completed so far
     */
    public static void progress(ProgressTicket progressTicket, String message, int workunit) {
        if (progressTicket != null) {
            progressTicket.progress(message, workunit);
        }
    }

    /**
     * Change the display name of the progress task. Use with care, please make sure the changed name is not completely different, or otherwise it might appear to the user as a different task.
     * @param progressTicket the progress ticket of the task
     * @param newDisplayName the new display name
     */
    public static void setDisplayName(ProgressTicket progressTicket, String newDisplayName) {
        if (progressTicket != null) {
            progressTicket.setDisplayName(newDisplayName);
        }
    }

    /**
     * Start the progress indication for indeterminate task.
     * @param progressTicket the progress ticket of the task
     */
    public static void start(ProgressTicket progressTicket) {
        if (progressTicket != null) {
            progressTicket.start();
        }
    }

    /**
     * Start the progress indication for a task with known number of steps.
     * @param progressTicket the progress ticket of the task
     * @param workunits total number of workunits that will be processed
     */
    public static void start(ProgressTicket progressTicket, int workunits) {
        if (progressTicket != null) {
            progressTicket.start(workunits);
        }
    }

    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * @param progressTicket the progress ticket of the task
     * @param workunits workunits total number of workunits that will be processed
     */
    public static void switchToDeterminate(ProgressTicket progressTicket, int workunits) {
        if (progressTicket != null) {
            progressTicket.switchToDeterminate(workunits);
        }
    }

    /**
     * Currently determinate task can be switched to indeterminate mode.
     * @param progressTicket the progress ticket of the task
     */
    public static void switchToIndeterminate(ProgressTicket progressTicket) {
        if (progressTicket != null) {
            progressTicket.switchToIndeterminate();
        }
    }
}
