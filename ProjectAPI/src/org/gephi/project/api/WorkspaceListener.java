/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.project.api;

/**
 * Workspace event listener.
 *
 * @author Mathieu Bastian
 */
public interface WorkspaceListener {

    /**
     * Notify a workspace has been created.
     * @param workspace the workspace that was created
     */
    public void initialize(Workspace workspace);

    /**
     * Notify a workspace has become the selected workspace.
     * @param workspace the workspace that was made current workspace
     */
    public void select(Workspace workspace);

    /**
     * Notify another workspace will be selected. The <code>select()</code> always
     * follows.
     * @param workspace the workspace that is currently the selected workspace
     */
    public void unselect(Workspace workspace);

    /**
     * Notify a workspace will be closed, all data must be destroyed.
     * @param workspace the workspace that is to be closed
     */
    public void close(Workspace workspace);

    /**
     * Notify no more workspace is currently selected, the project is empty.
     */
    public void disable();
}
