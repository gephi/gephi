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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.Progress;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processor 'Add full graph' that unloads the complete container into the
 * workspace.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class, position = 10)
public class DefaultProcessor extends AbstractProcessor implements Processor {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultProcessor.class, "DefaultProcessor.displayName");
    }

    @Override
    public void process() {
        if (containers.length > 1) {
            throw new RuntimeException("This processor can only handle single containers");
        }
        ContainerUnloader container = containers[0];

        //Workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (workspace == null) {
            workspace = pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        processConfiguration(container, workspace);

        if (container.getSource() != null) {
            pc.setSource(workspace, container.getSource());
        }

        process(container, workspace);

        //Clean
        workspace = null;
        graphModel = null;
        containers = null;
        progressTicket = null;
    }

    protected void processConfiguration(ContainerUnloader container, Workspace workspace) {
        //Configuration
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Configuration configuration = new Configuration();
        configuration.setTimeRepresentation(container.getTimeRepresentation());
        if (container.getEdgeTypeLabelClass() != null) {
//            configuration.setEdgeLabelType(container.getEdgeTypeLabelClass());
        }
        graphController.getGraphModel(workspace).setConfiguration(configuration);
    }

    protected void process(ContainerUnloader container, Workspace workspace) {
        //Architecture
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getGraphModel(workspace);

        //Get graph
        Graph graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        //Time Format & Time zone
        graphModel.setTimeFormat(container.getTimeFormat());
        graphModel.setTimeZone(container.getTimeZone());

        //Progress
        Progress.start(progressTicket, container.getNodeCount() + container.getEdgeCount());

        //Attributes - Creates columns for properties
        flushColumns(container);

        //Counters
        int addedNodes = 0, addedEdges = 0;

        //Create all nodes
        for (NodeDraft draftNode : container.getNodes()) {
            String id = draftNode.getId();
            Node node = graph.getNode(id);
            if (node == null) {
                node = factory.newNode(id);
                addedNodes++;
            }
            flushToNode(draftNode, node);

            graph.addNode(node);

            Progress.progress(progressTicket);
        }

        //Create all edges and push to data structure
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
                        edge = factory.newEdge(id, source, target, edgeType, draftEdge.getWeight(), false);
                        break;
                    case MIXED:
                        boolean directed = draftEdge.getDirection() == null || !draftEdge.getDirection().equals(EdgeDirection.UNDIRECTED);
                        edge = factory.newEdge(id, source, target, edgeType, draftEdge.getWeight(), directed);
                        break;
                }
                addedEdges++;
            }
            flushToEdge(draftEdge, edge);

            graph.addEdge(edge);

            Progress.progress(progressTicket);
        }

        //Report
        int touchedNodes = container.getNodeCount();
        int touchedEdges = container.getEdgeCount();
        if (touchedNodes != addedNodes || touchedEdges != addedEdges) {
            Logger.getLogger(getClass().toString()).log(Level.INFO, "# Nodes loaded: {0} ({1} added)", new Object[]{touchedNodes, addedNodes});
            Logger.getLogger(getClass().toString()).log(Level.INFO, "# Edges loaded: {0} ({1} added)", new Object[]{touchedEdges, addedEdges});
        } else {
            Logger.getLogger(getClass().toString()).log(Level.INFO, "# Nodes loaded: {0}", new Object[]{touchedNodes});
            Logger.getLogger(getClass().toString()).log(Level.INFO, "# Edges loaded: {0}", new Object[]{touchedEdges});
        }

        Progress.finish(progressTicket);
    }
}
