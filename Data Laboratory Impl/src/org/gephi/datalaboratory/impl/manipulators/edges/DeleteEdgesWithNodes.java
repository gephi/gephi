/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.edges;

import javax.swing.Icon;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.impl.manipulators.edges.ui.DeleteEdgesWithNodesUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class DeleteEdgesWithNodes implements EdgesManipulator {

    private Edge[] edges;
    private boolean deleteSource,deleteTarget;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.edges = edges;
    }

    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        gec.deleteEdgesWithNodes(edges,deleteSource,deleteTarget);
    }

    public String getName() {
        if (edges.length > 1) {
            return NbBundle.getMessage(DeleteEdgesWithNodes.class, "DeleteEdgesWithNodes.name.multiple");
        } else {
            return NbBundle.getMessage(DeleteEdgesWithNodes.class, "DeleteEdgesWithNodes.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).areEdgesInGraph(edges);
    }

    public ManipulatorUI getUI() {
        return new DeleteEdgesWithNodesUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 300;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/cross.png", true);
    }

    public void setDeleteSource(boolean deleteSource) {
        this.deleteSource = deleteSource;
    }

    public void setDeleteTarget(boolean deleteTarget) {
        this.deleteTarget = deleteTarget;
    }
}
