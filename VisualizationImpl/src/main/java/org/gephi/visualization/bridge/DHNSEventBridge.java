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
package org.gephi.visualization.bridge;

import java.util.ArrayList;
import java.util.List;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSEventBridge implements EventBridge, VizArchitecture {

    //Architecture
    private AbstractEngine engine;
    private HierarchicalGraph graph;
    private GraphIO graphIO;
    private GraphController graphController;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.graphIO = VizController.getInstance().getGraphIO();
        this.graphController = Lookup.getDefault().lookup(GraphController.class);
        initEvents();
    }

    @Override
    public void initEvents() {
    }

    public HierarchicalGraph getGraph(){
        return graph;
    }

    public Node[] getSelectedNodes(){
        GraphModel graphModel = graphController.getModel();
        if (graphModel == null) {
            return new Node[0];
        }
        this.graph = graphModel.getHierarchicalGraphVisible();
        ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        ArrayList<Node> nodes=new ArrayList<Node>();
        for (ModelImpl metaModelImpl : selectedNodeModels) {
            NodeData nodeData = (NodeData) metaModelImpl.getObj();
            Node node = nodeData.getNode(graph.getView().getViewId());
            if (node != null) {
                nodes.add(node);
            }
        }
        return nodes.toArray(new Node[0]);
    }
    
    public void mouseClick(ModelClass objectClass, Model[] clickedObjects) {
    }
}
