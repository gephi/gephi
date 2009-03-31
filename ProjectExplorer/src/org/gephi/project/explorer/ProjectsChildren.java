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
import org.gephi.project.api.Project;
import org.gephi.project.api.Projects;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Mathieu
 */
 class ProjectsChildren extends Children.Keys<Project> implements LookupListener {

     private Projects projects;
     private Lookup.Result<Project> result;

    ProjectsChildren(Projects projects) {
        this.projects = projects;

        //Init lookup
        result = projects.getLookup().lookupResult(Project.class);
        result.addLookupListener(this);
    }

    @Override
    protected Node[] createNodes(Project project) {
        return new Node[] {new ProjectNode(project)};
    }

    @Override
    public void resultChanged(LookupEvent lookupEvent) {
         Lookup.Result<Project> r = (Lookup.Result<Project>) lookupEvent.getSource();
         Collection<? extends Project> c = r.allInstances();
         setKeys(c);
    }

    @Override
    protected void addNotify() {
        
    }



}
