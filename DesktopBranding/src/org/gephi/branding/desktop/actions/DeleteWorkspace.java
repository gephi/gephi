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
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public class DeleteWorkspace extends SystemAction {

    public void actionPerformed(ActionEvent e) {
        Lookup.getDefault().lookup(ProjectControllerUI.class).deleteWorkspace();
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUI.class).canDeleteWorkspace();
    }

    @Override
    protected String iconResource() {
        return "org/gephi/branding/desktop/actions/resources/deleteWorkspace.png";
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteWorkspace.class, "CTL_DeleteWorkspace");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
