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
import javax.swing.JPanel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.spi.ProjectPropertiesUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public class ProjectProperties extends SystemAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(ProjectProperties.class, "CTL_ProjectProperties");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectController.class).canProjectProperties();
    }


    @Override
    protected String iconResource() {
        return "org/gephi/branding/desktop/actions/projectProperties.gif";
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.getCurrentProject();
        ProjectPropertiesUI ui = Lookup.getDefault().lookup(ProjectPropertiesUI.class);
        if (ui != null) {
            JPanel panel = ui.getPanel();
            ui.setup(project);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ProjectProperties.class, "ProjectProperties_dialog_title"));
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result == NotifyDescriptor.OK_OPTION) {
                ui.unsetup(project);
            }
        }
    }
}
