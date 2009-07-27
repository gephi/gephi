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
import javax.swing.JPanel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectPropertiesUI;
import org.gephi.project.explorer.ProjectNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ProjectProperties extends AbstractAction {

    private Project project;

    public ProjectProperties(Project project) {
        this.project = project;
        putValue(Action.NAME, NbBundle.getMessage(ProjectNode.class, "ProjectNode_projectProperties"));
    }

    public void actionPerformed(ActionEvent e) {
        ProjectPropertiesUI ui = Lookup.getDefault().lookup(ProjectPropertiesUI.class);
        if (ui != null) {
            JPanel panel = ui.getPanel();
            ui.load(project);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ProjectNode.class, "ProjectNode_projectProperties_dialogTitle"));
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result == NotifyDescriptor.OK_OPTION) {
                ui.save(project);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return project.isOpen();
    }
}
