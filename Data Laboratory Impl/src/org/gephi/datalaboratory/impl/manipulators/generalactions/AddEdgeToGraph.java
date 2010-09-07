/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.generalactions;

import javax.swing.Icon;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.impl.manipulators.generalactions.ui.AddEdgeToGraphUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.generalactions.GeneralActionsManipulator;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * GeneralActionsManipulator that adds a new edge to the graph, asking for source and target nodes and type of edge in UI.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = GeneralActionsManipulator.class)
public class AddEdgeToGraph implements GeneralActionsManipulator {

    private Node source=null, target=null;
    private boolean directed;
    private GraphModel graphModel=null;

    public void execute() {
        if (source != null && target != null) {
            Lookup.getDefault().lookup(GraphElementsController.class).createEdge(source, target, directed);
        }
    }

    public String getName() {
        return NbBundle.getMessage(AddNodeToGraph.class, "AddEdgeToGraph.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).getNodesCount()>1;//At least 2 nodes to link them
    }

    public ManipulatorUI getUI() {
        GraphModel currentGraphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        if(graphModel!=currentGraphModel){//If graph model has changed since last execution, change default mode for edges to create in UI, else keep this parameter across calls
            directed=currentGraphModel.isDirected()||currentGraphModel.isMixed();//Get graph directed state. Set to true if graph is directed or mixed
            graphModel=currentGraphModel;
        }
        return new AddEdgeToGraphUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 200;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/plus-white.png", true);
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }
}
