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
package org.gephi.graph.dhns;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.IDGen;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceDuplicateProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Singleton which manages the graph access.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = GraphController.class)
public class DhnsGraphController implements GraphController {

    protected IDGen iDGen;

    public DhnsGraphController() {
        iDGen = new IDGen();
    }

    public Dhns newDhns(Workspace workspace) {
        Dhns dhns = new Dhns(this, workspace);
        workspace.add(dhns);
        return dhns;
    }

    public IDGen getIDGen() {
        return iDGen;
    }

    private synchronized Dhns getCurrentDhns() {
        Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        if (currentWorkspace == null) {
            return null;
        }
        Dhns dhns = currentWorkspace.getLookup().lookup(Dhns.class);
        if (dhns == null) {
            dhns = newDhns(currentWorkspace);
        }
        return dhns;
    }

    public GraphModel getModel(Workspace workspace) {
        Dhns dhns = workspace.getLookup().lookup(Dhns.class);
        if (dhns == null) {
            dhns = newDhns(workspace);
        }
        return dhns;
    }

    public GraphModel getModel() {
        return getCurrentDhns();
    }
}
