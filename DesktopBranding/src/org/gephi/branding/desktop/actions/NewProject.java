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
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import org.gephi.project.api.ProjectController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Mathieu Bastian
 */
public final class NewProject extends SystemAction {

    public void actionPerformed(ActionEvent e) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SaveProject.class, "CTL_NewProject");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
