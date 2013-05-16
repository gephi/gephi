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
package org.gephi.filters.plugin.operator;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.AttributableFilter;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class NOTBuilderNode implements FilterBuilder {

    public Category getCategory() {
        return new Category(NbBundle.getMessage(NOTBuilderNode.class, "Operator.category"));
    }

    public String getName() {
        return NbBundle.getMessage(NOTBuilderNode.class, "NOTBuilderNode.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(NOTBuilderNode.class, "NOTBuilderNode.description");
    }

    public Filter getFilter() {
        return new NOTOperatorNode();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class NOTOperatorNode implements Operator {

        public int getInputCount() {
            return 1;
        }

        public String getName() {
            return NbBundle.getMessage(NOTBuilderNode.class, "NOTBuilderNode.name");
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public Graph filter(Graph[] graphs) {
            if (graphs.length > 1) {
                throw new IllegalArgumentException("Not Filter accepts a single graph in parameter");
            }
            HierarchicalGraph hgraph = (HierarchicalGraph) graphs[0];
            GraphView hgraphView = hgraph.getView();
            HierarchicalGraph mainHGraph = hgraph.getView().getGraphModel().getHierarchicalGraph();
            for (Node n : mainHGraph.getNodes().toArray()) {
                if (n.getNodeData().getNode(hgraphView.getViewId()) == null) {
                    //The node n is not in graph
                    hgraph.addNode(n);
                } else {
                    //The node n is in graph
                    hgraph.removeNode(n);
                }
            }

            for (Node n : hgraph.getNodes().toArray()) {
                Node mainNode = n.getNodeData().getNode(mainHGraph.getView().getViewId());
                Edge[] edges = mainHGraph.getEdgesAndMetaEdges(mainNode).toArray();
                for (Edge e : edges) {
                    if (e.getSource().getNodeData().getNode(hgraphView.getViewId()) != null
                            && e.getTarget().getNodeData().getNode(hgraphView.getViewId()) != null) {
                        hgraph.addEdge(e);
                    }
                }
            }

            return hgraph;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            if (filters.length > 1) {
                throw new IllegalArgumentException("Not Filter accepts a single filter in parameter");
            }
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
            Filter filter = filters[0];
            if (filter instanceof NodeFilter && ((NodeFilter) filter).init(hgraph)) {
                List<Node> nodeToRemove = new ArrayList<Node>();
                NodeFilter nodeFilter = (NodeFilter) filter;
                for (Node n : hgraph.getNodes().toArray()) {
                    if (nodeFilter.evaluate(hgraph, n)) {
                        nodeToRemove.add(n);
                    }
                }
                for (Node n : nodeToRemove) {
                    hgraph.removeNode(n);
                }
                nodeFilter.finish();
            }
            
            if (filter instanceof AttributableFilter && ((AttributableFilter) filter).getType()==AttributableFilter.Type.NODE && ((AttributableFilter) filter).init(hgraph)) {
                List<Node> nodeToRemove = new ArrayList<Node>();
                AttributableFilter attributableFilter = (AttributableFilter) filter;
                for (Node n : hgraph.getNodes().toArray()) {
                    if (attributableFilter.evaluate(hgraph, n)) {
                        nodeToRemove.add(n);
                    }
                }
                for (Node n : nodeToRemove) {
                    hgraph.removeNode(n);
                }
                attributableFilter.finish();
            }

            return hgraph;
        }
    }
}
