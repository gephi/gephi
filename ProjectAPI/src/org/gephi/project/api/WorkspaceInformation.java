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

import javax.swing.event.ChangeListener;
import org.gephi.project.api.Project;

/**
 * Hosts various information about a workspace the module is maintaining.
 *
 * @author Mathieu Bastian
 * @see Workspace
 */
public interface WorkspaceInformation {

    public boolean isOpen();

    public boolean isClosed();

    public boolean isInvalid();

    public boolean hasSource();

    public String getSource();

    public String getName();

    public Project getProject();

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);
}
