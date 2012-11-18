/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.utils.progress;

/**
 * Progress functionalities accessor.
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
     * Finish the progress task and display a wrap-up message
     * @param progressTicket the progress ticket of the task
     * @param finishMessage a message about the finished task
     */
    public static void finish(ProgressTicket progressTicket, String finishMessage) {
        if (progressTicket != null) {
            progressTicket.finish(finishMessage);
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
     * Returns the current task's display name, or an empty string.
     */
    public static String getDisplayName(ProgressTicket progressTicket) {
        if (progressTicket != null) {
            return progressTicket.getDisplayName();
        }
        return "";
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
