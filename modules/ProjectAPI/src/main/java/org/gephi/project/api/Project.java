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
import java.util.Collection;
import org.openide.util.Lookup;

/**
 * Project interface that internally stores, through its Lookup, various
 * information and workspaces.
 * <p>
 * The lookup is a generic container for any instance, thus modules are free to
 * store and query anything they want to be stored within a project.
 *
 * @author Mathieu Bastian
 */
public interface Project extends Lookup.Provider {

    /**
     * Adds an abilities to this project.
     *
     * @param instance the instance that is to be added to the lookup
     */
    void add(Object instance);

    /**
     * Removes an abilities to this project.
     *
     * @param instance the instance that is to be removed from the lookup
     */
    void remove(Object instance);

    /**
     * Gets any optional abilities of this project.
     * <p>
     * May contains:
     * <ol><li>{@link ProjectInformation}</li>
     * <li>{@link ProjectMetaData}</li>
     * <li>{@link WorkspaceProvider}</li></ol>
     *
     * @return the project's lookup
     */
    @Override
    Lookup getLookup();

    /**
     * Returns the current workspace.
     *
     * @return current workspace or <code>null</code> if no workspace is set.
     */
    Workspace getCurrentWorkspace();

    /**
     * Returns true if the project has a current workspace.
     *
     * @return true if has a current workspace, false otherwise
     */
    boolean hasCurrentWorkspace();

    /**
     * Returns all the workspaces.
     * <p>
     * Returns an empty array if no workspaces.
     *
     * @return an array of all workspaces
     */
    Collection<Workspace> getWorkspaces();

    /**
     * Retrieve a workspace based on its unique identifier.
     *
     * @param id workspace's unique identifier
     * @return found workspace or null if not found
     */
    Workspace getWorkspace(int id);

    /**
     * Returns true if the project is open.
     *
     * @return true if open, false otherwise
     */
    boolean isOpen();

    /**
     * Returns true if the project is closed.
     *
     * @return true if closed, false otherwise
     */
    boolean isClosed();

    /**
     * Returns true if the project is invalid.
     *
     * @return true if invalid, false otherwise
     */
    boolean isInvalid();

    /**
     * Returns the name of the project.
     * <p>
     * The name can't be null and has a default value (e.g. Project 1).
     *
     * @return the project's name
     */
    String getName();

    /**
     * Returns true if the project is associated with a file.
     * <p>
     * A project is associated with a file if it has been saved/loaded to/from a
     * file.
     *
     * @return true if associated with a file, false otherwise
     */
    boolean hasFile();

    /**
     * Returns the filename associated with this project.
     * <p>
     * Returns an empty string if the project isn't associated with a file.
     *
     * @return file name
     * @see #hasFile()
     */
    String getFileName();

    /**
     * Returns the file associated with this project.
     * <p>
     * Returns null if the project isn't associated with a file.
     *
     * @return file or null if none
     * @see #hasFile()
     */
    File getFile();

    /**
     * Returns the project's metadata.
     *
     * @return project metadata
     */
    ProjectMetaData getProjectMetadata();
}
