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
package org.gephi.project.api;

import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Hosts various information about a project.
 * <p>
 * Clients can subscribe to changes by using the 
 * {@link #addChangeListener(java.beans.PropertyChangeListener) } method. It
 * triggers the following events:
 * <ul>
 * <li><b>EVENT_OPEN:</b> Project opened
 * <li><b>EVENT_CLOSE:</b> Project closed
 * <li><b>EVENT_RENAME:</b> Project renamed
 * <li><b>EVENT_SET_FILE:</b> Project file set
 * </ul>
 *
 * @author Mathieu Bastian
 * @see Project
 */
public interface ProjectInformation {

    public static final String EVENT_OPEN = "open";
    public static final String EVENT_CLOSE = "close";
    public static final String EVENT_RENAME = "rename";
    public static final String EVENT_SET_FILE = "setFile";

    /**
     * Returns true if the project is open.
     *
     * @return true if open, false otherwise
     */
    public boolean isOpen();

    /**
     * Returns true if the project is closed.
     *
     * @return true if closed, false otherwise
     */
    public boolean isClosed();

    /**
     * Returns true if the project is invalid.
     *
     * @return true if invalid, false otherwise
     */
    public boolean isInvalid();

    /**
     * Returns the name of the project.
     * <p>
     * The name can't be null and has a default value (e.g. Project 1).
     *
     * @return the project's name
     */
    public String getName();

    /**
     * Returns true if the project is associated with a file.
     * <p>
     * A project is associated with a file if it has been saved/loaded to/from a
     * file.
     *
     * @return true if associated with a file, false otherwise
     */
    public boolean hasFile();

    /**
     * Returns the filename associated with this project.
     * <p>
     * Returns an empty string if the project isn't associated with a file.
     *
     * @see #hasFile()
     * @return file name
     */
    public String getFileName();

    /**
     * Returns the file associated with this project.
     * <p>
     * Returns null if the project isn't associated with a file.
     *
     * @see #hasFile()
     * @return file or null if none
     */
    public File getFile();

    /**
     * Returns the project this information class belongs to.
     *
     * @return project reference
     */
    public Project getProject();

    /**
     * Add change listener.
     *
     * @param listener change listener
     */
    public void addChangeListener(PropertyChangeListener listener);

    /**
     * Remove change listener.
     *
     * @param listener change listener
     */
    public void removeChangeListener(PropertyChangeListener listener);
}
