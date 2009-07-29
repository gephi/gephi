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
package org.gephi.branding.desktop;

import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.WorkspaceDataController;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        initGephi();
    }

    private void initGephi() {
        WorkspaceDataController wdc = Lookup.getDefault().lookup(WorkspaceDataController.class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
    }
}
