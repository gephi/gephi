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
