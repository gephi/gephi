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
     * @param instance  the instance that is to be added to the lookup
     */
    public void add(Object instance);

    /**
     * Removes an abilities to this project.
     *
     * @param instance  the instance that is to be removed from the lookup
     */
    public void remove(Object instance);

    /**
     * Gets any optional abilities of this project.
     * <p>
     * May contains:
     * <ol><li>{@link ProjectInformation}</li>
     * <li>{@link ProjectMetaData}</li>
     * <li>{@link WorkspaceProvider}</li></ol>
     * @return the project's lookup
     */
    public Lookup getLookup();
}
