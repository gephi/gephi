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
package org.gephi.io.processor.plugin;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processor 'Append graph' that tries to find in the current workspace nodes
 * and edges in the container to only append new elements. It uses elements' id
 * and label to do the matching.
 * <p>
 * The attibutes are not merged and values are from the latest element imported.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class)
public class AppendProcessor extends AbstractProcessor implements Processor {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AppendProcessor.class, "AppendProcessor.displayName");
    }

    @Override
    public void process() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        //Workspace
        if (workspace == null) {
            workspace = pc.getCurrentWorkspace();
            if (workspace == null) {
                //Append mode but no workspace
                workspace = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            }
        }
        if (container.getSource() != null) {
            pc.setSource(workspace, container.getSource());
        }

        //Architecture
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        Graph graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        //Attributes - Creates columns for properties
        attributeModel = graphController.getAttributeModel();
        flushColumns();

        //Dynamic
//        if (container.getTimeFormat() != null) {
//            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
//            dynamicController.setTimeFormat(container.getTimeFormat());
//        }

        int nodeCount = 0;
        //Create all nodes
        for (NodeDraft draftNode : container.getNodes()) {
            String id = draftNode.getId();
            Node node = graph.getNode(id);
            if (node == null) {
                node = factory.newNode(id);
                graph.addNode(node);
                nodeCount++;
            }
            flushToNode(draftNode, node);
        }

        //Create all edges and push to data structure
        int edgeCount = 0;
        for (EdgeDraft draftEdge : container.getEdges()) {
            String id = draftEdge.getId();
            String sourceId = draftEdge.getSource().getId();
            String targetId = draftEdge.getTarget().getId();
            Node source = graph.getNode(sourceId);
            Node target = graph.getNode(targetId);
            Object type = draftEdge.getType();
            int edgeType = graphModel.addEdgeType(type);

            Edge edge = graph.getEdge(source, target, edgeType);
            if (edge == null) {
                switch (container.getEdgeDefault()) {
                    case DIRECTED:
                        edge = factory.newEdge(id, source, target, edgeType, draftEdge.getWeight(), true);
                        break;
                    case UNDIRECTED:
                        edge = factory.newEdge(id, source, target, edgeType, draftEdge.getWeight(), true);
                        break;
                    case MIXED:
                        boolean directed = draftEdge.getDirection().equals(EdgeDirection.UNDIRECTED) ? false : true;
                        edge = factory.newEdge(id, source, target, edgeType, draftEdge.getWeight(), directed);
                }
                edgeCount++;
                graph.addEdge(edge);
            }
            flushToEdge(draftEdge, edge);
        }

        System.out.println("# New Nodes appended: " + nodeCount + "\n# New Edges appended: " + edgeCount);
        workspace = null;
    }
}
