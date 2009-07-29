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

package org.gephi.project.explorer.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.project.explorer.WorkspaceNode;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu
 */
public class RenameWorkspace extends AbstractAction {

    private Workspace workspace;

    public RenameWorkspace(Workspace workspace) {
        this.workspace = workspace;
        putValue(Action.NAME, NbBundle.getMessage(WorkspaceNode.class, "WorkspaceNode_renameWorkspace"));
    }

    public void actionPerformed(ActionEvent e) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);

        String s = (String) JOptionPane.showInputDialog(
                null,
                "",
                NbBundle.getMessage(WorkspaceNode.class, "WorkspaceNode_renameWorkspace_dialogTitle"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                workspace.getName());

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            pc.renameWorkspace(workspace, s);
        }

    }
}
