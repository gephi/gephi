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

package org.gephi.project.explorer;

import java.util.Collection;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.Project;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Mathieu
 */
class ProjectChildren extends Children.Keys<Workspace> implements LookupListener {

    private Project project;
    private Lookup.Result<Workspace> result;

    public ProjectChildren(Project project) {
        this.project = project;

        //Init lookup
        result = project.getLookup().lookupResult(Workspace.class);
        result.addLookupListener(this);
    }


    @Override
    protected Node[] createNodes(Workspace workspace) {
        return new Node[] { new WorkspaceNode(workspace)};
    }

    @Override
    protected void addNotify() {
        if(project.getWorkspaces()!=null)
            setKeys(project.getWorkspaces());
    }

    public void resultChanged(LookupEvent lookupEvent) {
        Lookup.Result<Workspace> r = (Lookup.Result<Workspace>) lookupEvent.getSource();
        Collection<? extends Workspace> c = r.allInstances();
        setKeys(c);
    }

}
