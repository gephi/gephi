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

import java.awt.Image;
import java.util.Collection;
import javax.swing.event.ChangeEvent;
import org.gephi.project.explorer.actions.AddWorkspace;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;
import org.gephi.project.explorer.actions.CloseProject;
import org.gephi.project.explorer.actions.OpenProject;
import org.gephi.project.explorer.actions.RemoveProject;
import org.gephi.project.explorer.actions.RenameProject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Mathieu
 */
public class ProjectNode extends AbstractNode implements ChangeListener {

    private Project project;

    public ProjectNode(Project project) {
        super(Children.LEAF);
        if (project.isOpen()) {
            setChildren(new ProjectChildren(project));
        }
        this.project = project;

        //Add Project Listener
        project.addChangeListener(WeakListeners.change(this, project));
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        //Project open
        if (project.isOpen() && getChildren()==Children.LEAF) {
            setChildren(new ProjectChildren(project));
        }
        else if(!project.isOpen())
        {
            setChildren(Children.LEAF);
        }

        //Project name
        setDisplayName(project.getName());
    }

    @Override
    public String getDisplayName() {
        return project.getName();
    }


    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{new OpenProject(project), new CloseProject(project), new RemoveProject(project), new AddWorkspace(project), new RenameProject(project)};
    }
}
