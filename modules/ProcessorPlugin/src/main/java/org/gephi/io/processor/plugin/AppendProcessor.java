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

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.EdgeDraft.EdgeType;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processor 'Append graph' that tries to find in the current workspace nodes and
 * edges in the container to only append new elements. It uses elements' id and
 * label to do the matching.
 * <p>
 * The attibutes are not merged and values are from the latest element imported.
 * 
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class)
public class AppendProcessor extends AbstractProcessor implements Processor {

    public String getDisplayName() {
        return NbBundle.getMessage(AppendProcessor.class, "AppendProcessor.displayName");
    }

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
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

        HierarchicalGraph graph = null;
        switch (container.getEdgeDefault()) {
            case DIRECTED:
                graph = graphModel.getHierarchicalDirectedGraph();
                break;
            case UNDIRECTED:
                graph = graphModel.getHierarchicalUndirectedGraph();
                break;
            case MIXED:
                graph = graphModel.getHierarchicalMixedGraph();
                break;
            default:
                graph = graphModel.getHierarchicalMixedGraph();
                break;
        }
        GraphFactory factory = graphModel.factory();

        //Attributes - Creates columns for properties
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        attributeModel.mergeModel(container.getAttributeModel());

        //Dynamic
        if (container.getTimeFormat() != null) {
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            dynamicController.setTimeFormat(container.getTimeFormat());
        }

        //Index existing graph
        Map<String, Node> map = new HashMap<String, Node>();
        for (Node n : graph.getNodes()) {
            String id = n.getNodeData().getId();
            if (id != null && !id.equalsIgnoreCase(String.valueOf(n.getId()))) {
                map.put(id, n);
            }
            if (n.getNodeData().getLabel() != null && !n.getNodeData().getLabel().isEmpty()) {
                map.put(n.getNodeData().getLabel(), n);
            }
        }

        int nodeCount = 0;
        //Create all nodes
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node n;
            String id = draftNode.getId();
            String label = draftNode.getLabel();
            if (!draftNode.isAutoId() && id != null && map.get(id) != null) {
                n = map.get(id);
            } else if (label != null && map.get(label) != null) {
                n = map.get(label);
            } else {
                n = factory.newNode(draftNode.isAutoId()?null:id);
                nodeCount++;
            }
            flushToNode(draftNode, n);
            draftNode.setNode(n);
        }

        //Push nodes in data structure
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node n = draftNode.getNode();
            NodeDraftGetter[] parents = draftNode.getParents();
            if (parents != null) {
                for (int i = 0; i < parents.length; i++) {
                    Node parent = parents[i].getNode();
                    graph.addNode(n, parent);
                }
            } else {
                graph.addNode(n);
            }
        }

        //Create all edges and push to data structure
        int edgeCount = 0;
        for (EdgeDraftGetter edge : container.getEdges()) {
            Node source = edge.getSource().getNode();
            Node target = edge.getTarget().getNode();
            if (graph.getEdge(source, target) == null) {
                Edge e = null;
                switch (container.getEdgeDefault()) {
                    case DIRECTED:
                        e = factory.newEdge(edge.isAutoId()?null:edge.getId(), source, target, edge.getWeight(), true);
                        break;
                    case UNDIRECTED:
                        e = factory.newEdge(edge.isAutoId()?null:edge.getId(), source, target, edge.getWeight(), false);
                        break;
                    case MIXED:
                        e = factory.newEdge(edge.isAutoId()?null:edge.getId(), source, target, edge.getWeight(), edge.getType().equals(EdgeType.UNDIRECTED) ? false : true);
                        break;
                }

                flushToEdge(edge, e);
                edgeCount++;
                graph.addEdge(e);
            }
        }

        System.out.println("# New Nodes appended: " + nodeCount + "\n# New Edges appended: " + edgeCount);
        workspace = null;
    }
}
