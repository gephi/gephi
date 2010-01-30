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

import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.Lookup;

/**
 * Workspace interface that internally stores, through its <b>Lookup</b>, various
 * information and instances.
 * <p>
 * Workpace is a top concept in Gephi because all models that modules
 * possesses are usually divided by workspace, for instance one <code>GraphModel</code>
 * per workspace. Therefore this class has a Lookup mechanism to let modules store
 * their model in the workspace's lookup and query it when needed.
 * <p>
 * To know how you can manage loading and saving data in Gephi project files, see
 * {@link WorkspacePersistenceProvider}.
 * <h3>How to associate new data model to the workspace</h3>
 * In your new module, listen to {@link WorkspaceListener} and call <code>add()</code>
 * method when initialize:
 * <pre>public void initialize(Workspace workspace) {
 *      workspace.add(new MyDataModel())
 *}
 * </pre>
 * When a workspace is selected, retrieve the workspace's data model:
 * <pre>public void select(Workspace workspace) {
 *      MyDataModel model = workspace.getLookup().lookup(MyDataModel.class);
 *}
 * </pre>
 * @author Mathieu Bastian
 */
public interface Workspace extends Lookup.Provider {

    /**
     * Adds an instance to this workspaces lookup.
     * @param instance  the instance that is to be pushed to the lookup
     */
    public void add(Object instance);

    /**
     * Removes an instance from this workspaces lookup.
     * @param instance  the instance that is to be removed from the lookup
     */
    public void remove(Object instance);

    /**
     * Get any instance in the current lookup. All important API in Gephi are
     * storing models in this lookup.
     * <p>
     * May contains:
     * <ol><li><code>GraphModel</code></li>
     * <li><code>AttributeModel</code></li>
     * <li><code>LayoutModel</code></li>
     * <li><code>StatisticsModel</code></li>
     * <li><code>FiltersModel</code></li>
     * <li><code>PreviewModel</code></li>
     * <li><code>VizModel</code></li>
     * <li>...</li>
     * </ol>
     * @return the workspace's lookup
     */
    public Lookup getLookup();
}
