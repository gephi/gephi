/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
