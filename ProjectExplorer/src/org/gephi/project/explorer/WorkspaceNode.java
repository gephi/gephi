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
import org.gephi.project.explorer.actions.CloseWorkspace;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.project.explorer.actions.OpenWorkspace;
import org.gephi.project.explorer.actions.RenameWorkspace;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceNode extends AbstractNode implements ChangeListener {

    private Workspace workspace;

    public WorkspaceNode(Workspace workspace) {
        super(Children.LEAF);
        this.workspace = workspace;

        //Add Workspace Listener
        workspace.addChangeListener(WeakListeners.change(this, workspace));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentWorkspace() == workspace) {
            //Current
        } else {
            //Not current
        }

        //Properties
        fireDisplayNameChange("a", "b");
        fireIconChange();
    }

    @Override
    public String getHtmlDisplayName() {
        if (workspace.isOpen()) {
            String fileName = workspace.hasSource() ? workspace.getSource() : "";
            return "<font color='#000000'>" + workspace.getName() + "</font>" +
                    "<font color='#999999'><i>" + fileName + "</i></font>";
        } else {
            String fileName = workspace.hasSource() ? workspace.getSource() : "";
            return "<font color='#888888'>" + workspace.getName() + "</font>" +
                    "<font color='#BBBBBB'><i>" + fileName + "</i></font>";
        }
    }

    @Override
    public Image getIcon(int type) {
        if (workspace.isOpen()) {
            return ImageUtilities.loadImage("org/gephi/project/explorer/workspace_open.png");
        } else {
            return ImageUtilities.loadImage("org/gephi/project/explorer/workspace.png");
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{new OpenWorkspace(workspace), new CloseWorkspace(workspace), new RenameWorkspace(workspace)};
    }
}
