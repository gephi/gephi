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

package org.gephi.project.impl;

import java.util.ArrayList;
import java.util.List;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;

/**
 * @author Mathieu Bastian
 */
public class WorkspaceProviderImpl implements WorkspaceProvider {

    private final ProjectImpl project;
    private final List<Workspace> workspaces;
    private WorkspaceImpl currentWorkspace;

    public WorkspaceProviderImpl(ProjectImpl project) {
        this.project = project;
        workspaces = new ArrayList<>();
    }

    protected WorkspaceImpl newWorkspace() {
        return newWorkspace(project.nextWorkspaceId());
    }

    protected WorkspaceImpl newWorkspace(int id) {
        synchronized (workspaces) {
            WorkspaceImpl workspace = new WorkspaceImpl(project, id);
            workspaces.add(workspace);
            return workspace;
        }
    }

    protected void removeWorkspace(Workspace workspace) {
        synchronized (workspaces) {
            workspaces.remove(workspace);
        }
    }

    protected Workspace getPrecedingWorkspace(Workspace workspace) {
        Workspace[] ws = getWorkspaces();
        int index = -1;
        for (int i = 0; i < ws.length; i++) {
            if (ws[i] == workspace) {
                index = i;
            }
        }
        if (index >= 1) {
            //Get preceding
            return ws[index - 1];
        } else if (index == 0 && ws.length > 1) {
            //Get following
            return ws[1];
        }
        return null;
    }

    @Override
    public WorkspaceImpl getCurrentWorkspace() {
        synchronized (workspaces) {
            return currentWorkspace;
        }
    }

    protected void setCurrentWorkspace(Workspace currentWorkspace) {
        synchronized (workspaces) {
            if (this.currentWorkspace != null) {
                this.currentWorkspace.close();
            }
            this.currentWorkspace = (WorkspaceImpl) currentWorkspace;
            this.currentWorkspace.open();
        }
    }

    @Override
    public Workspace[] getWorkspaces() {
        synchronized (workspaces) {
            return workspaces.toArray(new Workspace[0]);
        }
    }

    @Override
    public Workspace getWorkspace(int id) {
        synchronized (workspaces) {
            for (Workspace w : workspaces) {
                if (w.getId() == id) {
                    return w;
                }
            }
            return null;
        }
    }

    @Override
    public boolean hasCurrentWorkspace() {
        synchronized (workspaces) {
            return currentWorkspace != null;
        }
    }
}
