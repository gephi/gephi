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
package org.gephi.desktop.project.api;

import java.io.File;

/**
 *
 * @author Mathieu Bastian
 */
public interface ProjectControllerUI {

    public void saveProject();

    public void saveAsProject();

    public void openProject();

    public void openProject(File file);

    public void renameProject(final String name);

    public void projectProperties();

    public void openFile();

    public void newWorkspace();

    public void newProject();

    public void deleteWorkspace();

    public void closeProject();

    public void cleanWorkspace();

    public void renameWorkspace(final String name);

    public boolean canNewProject();

    public boolean canOpenProject();

    public boolean canCloseProject();

    public boolean canOpenFile();

    public boolean canSave();

    public boolean canSaveAs();

    public boolean canNewWorkspace();

    public boolean canDeleteWorkspace();

    public boolean canCleanWorkspace();

    public boolean canProjectProperties();
}
