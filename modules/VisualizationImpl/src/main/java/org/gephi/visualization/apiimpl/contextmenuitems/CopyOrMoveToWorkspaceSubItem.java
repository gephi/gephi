/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import java.util.Arrays;
import javax.swing.Icon;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class CopyOrMoveToWorkspaceSubItem extends BasicItem implements NodesManipulator {

    private final Workspace workspace;
    private final boolean canExecute;
    private final int type;
    private final int position;
    private final boolean copy;

    @Override
    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    /**
     * Constructor with copy or move settings
     *
     * @param workspace Workspace to copy or move, or null to use new workspace
     * @param canExecute canExecute
     * @param type type
     * @param position position
     * @param copy True to copy, false to move
     */
    public CopyOrMoveToWorkspaceSubItem(Workspace workspace, boolean canExecute, int type, int position, boolean copy) {
        this.workspace = workspace;
        this.canExecute = canExecute;
        this.type = type;
        this.position = position;
        this.copy = copy;
    }

    @Override
    public void execute() {
        if (copy) {
            copyToWorkspace(workspace);
        } else {
            moveToWorkspace(workspace);
        }
    }

    @Override
    public String getName() {
        if (workspace != null) {
            return workspace.getLookup().lookup(WorkspaceInformation.class).getName();
        } else {
            return NbBundle.getMessage(CopyOrMoveToWorkspaceSubItem.class, copy ? "GraphContextMenu_CopyToWorkspace_NewWorkspace" : "GraphContextMenu_MoveToWorkspace_NewWorkspace");
        }
    }

    @Override
    public boolean canExecute() {
        return canExecute;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    public void copyToWorkspace(Workspace workspace) {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        
        GraphModel graphModel;
        if (workspace == null) {
            workspace = Lookup.getDefault().lookup(ProjectControllerUI.class).newWorkspace();
            graphModel = graphController.getGraphModel(workspace);
            
            GraphModel currentGraphModel = graphController.getGraphModel(currentWorkspace);
            graphModel.setConfiguration(currentGraphModel.getConfiguration());
            graphModel.setTimeFormat(currentGraphModel.getTimeFormat());
            graphModel.setTimeZone(currentGraphModel.getTimeZone());
        } else {
            graphModel = graphController.getGraphModel(workspace);
        }
        
        graphModel.bridge().copyNodes(nodes);
    }

    public void moveToWorkspace(Workspace workspace) {
        copyToWorkspace(workspace);
        delete();
    }

    public void delete() {
        Graph g = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        g.removeAllNodes(Arrays.asList(nodes));
    }
}
