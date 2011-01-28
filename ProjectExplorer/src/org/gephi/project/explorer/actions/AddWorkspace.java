/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.project.explorer.actions;

import org.gephi.project.explorer.*;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu
 */
public class AddWorkspace extends AbstractAction {

    private Project project;

    public AddWorkspace(Project project) {
        putValue(Action.NAME, NbBundle.getMessage(ProjectNode.class, "ProjectNode_addWorkspace"));
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newWorkspace(project);
    }

    @Override
    public boolean isEnabled() {
        return project.isOpen();
    }

}
