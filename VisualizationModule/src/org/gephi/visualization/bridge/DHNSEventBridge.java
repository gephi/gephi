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
