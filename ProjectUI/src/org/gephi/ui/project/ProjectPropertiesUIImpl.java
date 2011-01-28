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
package org.gephi.ui.project;

import javax.swing.JPanel;
import org.gephi.project.api.Project;
import org.gephi.project.spi.ProjectPropertiesUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectPropertiesUI.class)
public class ProjectPropertiesUIImpl implements ProjectPropertiesUI {

    private ProjectPropertiesEditor panel;

    public JPanel getPanel() {
        panel = new ProjectPropertiesEditor();
        return panel;
    }

    public void setup(Project project) {
        panel.load(project);
    }

    public void unsetup(Project project) {
        panel.save(project);
        panel = null;
    }
}
