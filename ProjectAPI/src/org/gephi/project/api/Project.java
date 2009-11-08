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

package org.gephi.project.api;

import org.gephi.workspace.api.Workspace;
import javax.swing.event.ChangeListener;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public interface Project {

    public boolean isOpen();

    public boolean isClosed();

    public boolean isInvalid();

    public String getName();

    public boolean hasFile();

    public String getFileName();

    public DataObject getDataObject();

    public Workspace[] getWorkspaces();

    public Workspace getCurrentWorkspace();

    public boolean hasCurrentWorkspace();

    public void addChangeListener(ChangeListener listener);

    public Lookup getLookup();

    public ProjectMetaData getMetaData();
}
