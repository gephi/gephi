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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.api.ElementIdType;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.Progress;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processor 'Add full graph' that unloads the complete container into the workspace.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class, position = 10)
public class DefaultProcessor extends AbstractProcessor {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultProcessor.class, "DefaultProcessor.displayName");
    }

    @Override
    public void process() {
        try {
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
        } finally {
            clean();
        }
    }

    protected void processConfiguration(ContainerUnloader container, Workspace workspace) {
        //Configuration
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Configuration configuration = new Configuration();
        configuration.setTimeRepresentation(container.getTimeRepresentation());
        if (container.getEdgeTypeLabelClass() != null) {
            configuration.setEdgeLabelType(container.getEdgeTypeLabelClass());
        }
        configuration.setNodeIdType(container.getElementIdType().getTypeClass());
        configuration.setEdgeIdType(container.getElementIdType().getTypeClass());

        ColumnDraft weightColumn = container.getEdgeColumn("weight");
        if (weightColumn != null && weightColumn.isDynamic()) {
            if (container.getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
                configuration.setEdgeWeightType(IntervalDoubleMap.class);
            } else {
                configuration.setEdgeWeightType(TimestampDoubleMap.class);
            }
        }

        GraphConfigurationWrapper originalConfig = new GraphConfigurationWrapper(graphController.getGraphModel(workspace).getConfiguration());
        if (container.getEdgeCount() == 0) {
            //Fix different config problems that are not actually problems since no edges are present:
            //A case user-friendly specially for spreadsheet import

            //Make weight types match:
            if (!originalConfig.edgeWeightType.equals(configuration.getEdgeWeightType())) {
                configuration.setEdgeWeightType(originalConfig.edgeWeightType);
            }
        }

        GraphConfigurationWrapper newConfig = new GraphConfigurationWrapper(configuration);

        if (!originalConfig.equals(newConfig)) {
            try {
                graphController.getGraphModel(workspace).setConfiguration(configuration);
            } catch (Exception e) {
                String message = NbBundle.getMessage(
                        DefaultProcessor.class, "DefaultProcessor.error.configurationChangeForbidden",
                        new GraphConfigurationWrapper(graphController.getGraphModel(workspace).getConfiguration()).toString(),
                        new GraphConfigurationWrapper(configuration).toString()
                );
                report.logIssue(new Issue(message, Issue.Level.SEVERE));
            }
        }
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
        ElementIdType elementIdType = container.getElementIdType();
        for (NodeDraft draftNode : container.getNodes()) {
            String idString = draftNode.getId();
            Object id = toElementId(elementIdType, idString);
            Node node = graph.getNode(id);

            boolean newNode = false;
            if (node == null) {
                node = factory.newNode(id);
                addedNodes++;
                newNode = true;
            }
            flushToNode(container, draftNode, node);

            if (newNode) {
                graph.addNode(node);
            }

            Progress.progress(progressTicket);
        }

        final EdgeMergeStrategy edgesMergeStrategy = containers[0].getEdgesMergeStrategy();

        //Create all edges and push to data structure
        for (EdgeDraft draftEdge : container.getEdges()) {
            String idString = draftEdge.getId();
            Object id = toElementId(elementIdType, idString);
            String sourceId = draftEdge.getSource().getId();
            String targetId = draftEdge.getTarget().getId();
            Node source = graph.getNode(toElementId(elementIdType, sourceId));
            Node target = graph.getNode(toElementId(elementIdType, targetId));
            Object type = draftEdge.getType();
            int edgeType = graphModel.addEdgeType(type);

            boolean createDirected = true;
            switch (container.getEdgeDefault()) {
                case DIRECTED:
                    createDirected = true;
                    break;
                case UNDIRECTED:
                    createDirected = false;
                    break;
                case MIXED:
                    createDirected = draftEdge.getDirection() != EdgeDirection.UNDIRECTED;
                    draftEdge.setDirection(createDirected ? EdgeDirection.DIRECTED : EdgeDirection.UNDIRECTED);
                    break;
            }

            Edge edge = graph.getEdge(source, target, edgeType);

            if (edge != null && edgesMergeStrategy == EdgeMergeStrategy.NO_MERGE) {
                //Undirected and directed edges are incompatible, check for them or we could get an exception:
                final Edge incompatibleEdge = findIncompatibleEdge(graph, source, target, createDirected, edgeType);
                if (incompatibleEdge == null) {
                    //Force create, no merge
                    edge = null;
                } else {
                    String message = NbBundle.getMessage(
                            DefaultProcessor.class, "DefaultProcessor.error.incompatibleEdges",
                            String.format(
                                    "[%s -> %s; %s, type %s]",
                                    sourceId, targetId, createDirected ? "Directed" : "Undirected", type
                            ),
                            String.format(
                                    "[%s -> %s; %s; type: %s; id: %s]",
                                    incompatibleEdge.getSource().getId(), incompatibleEdge.getTarget().getId(),
                                    incompatibleEdge.isDirected() ? "Directed" : "Undirected",
                                    incompatibleEdge.getTypeLabel(),
                                    incompatibleEdge.getId()
                            )
                    );
                    report.logIssue(new Issue(message, Issue.Level.WARNING));

                    Progress.progress(progressTicket);
                    continue;
                }
            }

            boolean newEdge = edge == null;
            if (newEdge) {
                if (!graph.hasEdge(id)) {
                    edge = factory.newEdge(id, source, target, edgeType, draftEdge.getWeight(), createDirected);
                } else {
                    //The id is already in use by a different edge, generate a new id:
                    edge = factory.newEdge(source, target, edgeType, draftEdge.getWeight(), createDirected);
                }

                addedEdges++;
            }

            flushToEdge(container, draftEdge, edge, newEdge);

            if (newEdge) {
                graph.addEdge(edge);
            }

            Progress.progress(progressTicket);
        }

        //Report
        int touchedNodes = container.getNodeCount();
        int touchedEdges = container.getEdgeCount();
        if (touchedNodes != addedNodes || touchedEdges != addedEdges) {
            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "# Nodes loaded: {0} ({1} added)", new Object[]{touchedNodes, addedNodes});
            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "# Edges loaded: {0} ({1} added)", new Object[]{touchedEdges, addedEdges});
        } else {
            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "# Nodes loaded: {0}", new Object[]{touchedNodes});
            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "# Edges loaded: {0}", new Object[]{touchedEdges});
        }

        Progress.finish(progressTicket);
    }

    private Edge findIncompatibleEdge(Graph graph, Node source, Node target, boolean directed, int edgeType) {
        Edge edge = graph.getEdge(source, target, edgeType);

        if (edge == null) {
            if (directed) {
                //The edge may exist with opposite source-target but undirected. In that case we can't create a directed one:
                edge = graph.getEdge(target, source, edgeType);

                if (edge != null && edge.isDirected()) {
                    //Actually it's directed so we can create the opposite directed edge, not incompatible
                    edge = null;
                }
            }
        } else {
            if (edge.isDirected() == directed) {
                //Same directedness, not incompatible
                edge = null;
            }
        }

        return edge;
    }

    private Object toElementId(ElementIdType elementIdType, String idString) {
        Object id;
        switch (elementIdType) {
            case INTEGER:
                id = Integer.parseInt(idString);
                break;
            case LONG:
                id = Long.parseLong(idString);
                break;
            default:
                id = idString;
                break;
        }
        return id;
    }

    private class GraphConfigurationWrapper {

        private final Class nodeIdType;
        private final Class edgeIdType;
        private final Class edgeLabelType;
        private final Class edgeWeightType;
        private final Boolean edgeWeightColumn;
        private final TimeRepresentation timeRepresentation;

        public GraphConfigurationWrapper(Configuration configuration) {
            nodeIdType = configuration.getNodeIdType();
            edgeIdType = configuration.getEdgeIdType();
            edgeLabelType = configuration.getEdgeLabelType();
            edgeWeightType = configuration.getEdgeWeightType();
            edgeWeightColumn = configuration.getEdgeWeightColumn();
            timeRepresentation = configuration.getTimeRepresentation();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.nodeIdType);
            hash = 19 * hash + Objects.hashCode(this.edgeIdType);
            hash = 19 * hash + Objects.hashCode(this.edgeLabelType);
            hash = 19 * hash + Objects.hashCode(this.edgeWeightType);
            hash = 19 * hash + Objects.hashCode(this.edgeWeightColumn);
            hash = 19 * hash + Objects.hashCode(this.timeRepresentation);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GraphConfigurationWrapper other = (GraphConfigurationWrapper) obj;
            if (!Objects.equals(this.nodeIdType, other.nodeIdType)) {
                return false;
            }
            if (!Objects.equals(this.edgeIdType, other.edgeIdType)) {
                return false;
            }
            if (!Objects.equals(this.edgeLabelType, other.edgeLabelType)) {
                return false;
            }
            if (!Objects.equals(this.edgeWeightType, other.edgeWeightType)) {
                return false;
            }
            if (!Objects.equals(this.edgeWeightColumn, other.edgeWeightColumn)) {
                return false;
            }
            if (this.timeRepresentation != other.timeRepresentation) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "GraphConfigurationWrapper{" + "nodeIdType=" + nodeIdType + ", edgeIdType=" + edgeIdType + ", edgeLabelType=" + edgeLabelType + ", edgeWeightType=" + edgeWeightType + ", edgeWeightColumn=" + edgeWeightColumn + ", timeRepresentation=" + timeRepresentation + '}';
        }
    }
}
