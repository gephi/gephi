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

import java.io.File;
import java.util.Collection;

/**
 * Project controller, manage projects and workspaces states.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);</pre>
 * <p>
 * Only a single project can be opened at a time. It can be retrieved from {@link #getCurrentProject()}.}
 * <p>
 * At startup, no project is opened. To open a project, use {@link #openProject(java.io.File)} or create a new one with {@link #newProject()}.
 * <p>
 * A project contains one or more workspaces. A project can have only one workspace selected at a time. By default, a project starts with one workspace.
 *
 * @author Mathieu Bastian
 * @see Project
 * @see Workspace
 */
public interface ProjectController {

    /**
     * Creates and open a new project.
     * <p>
     * If a project is currently opened, it will be closed first.
     *
     * @return newly created project
     */
    Project newProject();

    /**
     * Opens a project from a <code>.gephi</code> file.
     * <p>
     * If a project is currently opened, it will be closed first.
     *
     * @param file project file
     * @return opened project
     */
    Project openProject(File file);

    /**
     * Opens a project from the list of active projects.
     * <p>
     * If a project is currently opened, it will be closed first.
     *
     * @param project project to open
     * @throws IllegalArgumentException if the project doesn't belong to the list of active projects
     */
    void openProject(Project project);

    /**
     * Saves the current project to its <code>.gephi</code> file.
     *
     * @param project project to save
     * @throws IllegalStateException is the project hasn't a file configured
     */
    void saveProject(Project project);

    /**
     * Saves the current project to a new <code>.gephi</code> file.
     * <p>
     * The project file is updated with the new file.
     *
     * @param project project to save
     * @param file    file to be written
     */
    void saveProject(Project project, File file);

    /**
     * Closes the current project.
     */
    void closeCurrentProject();

    /**
     * Removes the project from the active project list.
     * <p>
     * It won't delete any <code>.gephi</code> files.
     *
     * @param project project to remove
     */
    void removeProject(Project project);

    /**
     * Gets the set of active projects.
     *
     * @return projects
     * @deprecated Directly use this class instead as all the methods have been ported.
     */
    Projects getProjects();

    /**
     * Returns true if a project is selected.
     *
     * @return true if current project, false otherwise
     */
    boolean hasCurrentProject();

    /**
     * Returns the current opened project.
     *
     * @return current open project or <code>null</code> if missing
     */
    Project getCurrentProject();

    /**
     * Gets all active projects
     *
     * @return project array
     */
    Collection<Project> getAllProjects();

    /**
     * Creates and adds a new workspace to the given project.
     * <p>
     * The new workspace is not selected. Call {@link #openWorkspace(Workspace)} (org.gephi.project.api.Workspace)} to select it.
     *
     * @param project project to add the workspace to
     * @return workspace
     */
    Workspace newWorkspace(Project project);

    /**
     * Creates and adds a new workspace to the given project and adds objects to the workspace lookup.
     * <p>
     * The new workspace is not selected. Call {@link #openWorkspace(Workspace)} (org.gephi.project.api.Workspace)} to select it.
     *
     * @param project project to add the workspace to
     * @param objectsForLookup objects to add to the workspace lookup
     * @return workspace
     */
    Workspace newWorkspace(Project project, Object... objectsForLookup);

    /**
     * Deletes the given workspace from its project.
     * <p>
     * If the workspace is currently selected, it's preceding workspace will be selected.
     * <p>
     * If this workspace is the unique workspace in the project, the project will be closed.
     *
     * @param workspace workspace to delete
     */
    void deleteWorkspace(Workspace workspace);

    /**
     * Renames the given workspace with the provided string.
     *
     * @param workspace workspace to rename
     * @param name      new name
     */
    void renameWorkspace(Workspace workspace, String name);

    /**
     * Renames the given project with the provided string.
     *
     * @param project project to rename
     * @param name    new name
     */
    void renameProject(Project project, String name);

    /**
     * Returns the selected workspace of the current project.
     *
     * @return selected workspace or <code>null</code> if no current project
     */
    Workspace getCurrentWorkspace();

    /**
     * Selects the given workspace as the current workspace of the project.
     * <p>
     * This method calls {@link #closeCurrentWorkspace()} beforehand.
     *
     * @param workspace workspace to select
     */
    void openWorkspace(Workspace workspace);

    /**
     * Creates and open a new workspace in the current project. If not project is opened, a new one is created.
     */
    Workspace openNewWorkspace();

    /**
     * Creates and open a new workspace in the current project and adds objects to the workspace lookup.
     * <p>
     *
     * If not project is opened, a new one is created.
     *
     * @param objectsForLookup objects to add to the workspace lookup
     */
    Workspace openNewWorkspace(Object... objectsForLookup);

    /**
     * Unselects the current workspace.
     */
    void closeCurrentWorkspace();

    /**
     * Duplicates the given workspace and adds it to the project.
     * <p>
     * The new workspace is automatically selected.
     *
     * @param workspace workspace to duplicate
     * @return duplicated workspace
     */
    Workspace duplicateWorkspace(Workspace workspace);

    void setSource(Workspace workspace, String source);

    void addWorkspaceListener(WorkspaceListener workspaceListener);

    void removeWorkspaceListener(WorkspaceListener workspaceListener);
}
