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
import javax.swing.event.ChangeEvent;
import org.gephi.project.explorer.actions.AddWorkspace;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.Project;
import org.gephi.project.explorer.actions.CloseProject;
import org.gephi.project.explorer.actions.OpenProject;
import org.gephi.project.explorer.actions.ProjectProperties;
import org.gephi.project.explorer.actions.RemoveProject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Mathieu Bastian
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
        if (project.isOpen() && getChildren() == Children.LEAF) {
            setChildren(new ProjectChildren(project));
        } else if (!project.isOpen()) {
            setChildren(Children.LEAF);
        }

        //Properties
        fireDisplayNameChange("a", "b");
        fireIconChange();
    }

    @Override
    public String getHtmlDisplayName() {
        if (project.isOpen()) {
            String fileName = project.hasFile() ? project.getFileName() : "";
            return "<font color='#000000'>" + project.getName() + "</font> " +
                    "<font color='#999999'><i>" + fileName + "</i></font>";
        } else {
            String fileName = project.hasFile() ? project.getFileName() : "";
            return "<font color='#888888'>" + project.getName() + "</font> " +
                    "<font color='#BBBBBB'><i>" + fileName + "</i></font>";
        }
    }

    @Override
    public Image getIcon(int type) {
        if (project.isOpen()) {
            return ImageUtilities.loadImage("org/gephi/project/explorer/project_open.png");
        } else {
            return ImageUtilities.loadImage("org/gephi/project/explorer/project.png");
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        if (project.isOpen()) {
            return ImageUtilities.loadImage("org/gephi/project/explorer/project_open.png");
        } else {
            return ImageUtilities.loadImage("org/gephi/project/explorer/project.png");
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{new OpenProject(project), new CloseProject(project), new RemoveProject(project), new AddWorkspace(project), new ProjectProperties(project)};
    }
}
