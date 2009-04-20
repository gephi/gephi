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
package org.gephi.dynamic.controller;

import org.gephi.data.network.api.DhnsController;
import org.gephi.data.network.api.Dictionary;
import org.gephi.data.network.api.EdgeFactory;
import org.gephi.data.network.api.FlatImporter;
import org.gephi.data.network.api.NodeFactory;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.importer.api.EdgeDraft;
import org.gephi.importer.api.ImportContainer;
import org.gephi.importer.api.NodeDraft;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicControllerImpl implements DynamicController {

    public void appendData(ImportContainer container) {
        DhnsController controller = Lookup.getDefault().lookup(DhnsController.class);
        Dictionary dico = controller.getDictionary();

        FlatImporter flatImporter = controller.getFlatImport();
        flatImporter.initImport();

        //Nodes
        for (NodeDraft nodeDraft : container.getNodes()) {
            Node existingNode = dico.getNode(nodeDraft.getLabel());
            if (existingNode == null) {
                existingNode = NodeFactory.createNode();
                nodeDraft.flushToNode(existingNode);

                flatImporter.addNode(existingNode);
            }
        }

        //Edges
        for (EdgeDraft edgeDraft : container.getEdges()) {
            Node nodeSource = edgeDraft.getNodeSource().getNode();
            Node nodeTarget = edgeDraft.getNodeTarget().getNode();

            Edge existingEdge = dico.getEdge(nodeSource, nodeTarget);
            if (existingEdge == null) {
                existingEdge = EdgeFactory.createEdge(nodeSource, nodeTarget);
                edgeDraft.flushToEdge(existingEdge);

                flatImporter.addEdge(existingEdge);
            } 
        }

        flatImporter.finishImport();
    }
}
