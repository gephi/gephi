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
package org.gephi.filters.plugin.operator;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class NOTBuilderNode implements FilterBuilder {

    public Category getCategory() {
        return new Category("Operator");
    }

    public String getName() {
        return "NOT (Nodes)";
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Filter getFilter() {
        return new NOTOperatorNode();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public static class NOTOperatorNode implements Operator {

        public int getInputCount() {
            return 1;
        }

        public String getName() {
            return "NOT (Nodes)";
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public Graph filter(Graph[] graphs) {
            if (graphs.length > 1) {
                throw new IllegalArgumentException("Not Filter accepts a single graph in parameter");
            }
            Graph graph = graphs[0];
            GraphView graphView = graph.getView();
            Graph mainGraph = graph.getView().getGraphModel().getGraph();
            for (Node n : mainGraph.getNodes().toArray()) {
                if (n.getNodeData().getNode(graphView.getViewId()) == null) {
                    //The node n is not in graph
                    graph.addNode(n);
                } else {
                    //The node n is in graph
                    graph.removeNode(n);
                }
            }

            for (Node n : graph.getNodes().toArray()) {
                Node mainNode = n.getNodeData().getNode(mainGraph.getView().getViewId());
                Edge[] edges = mainGraph.getEdges(mainNode).toArray();
                for (Edge e : edges) {
                    if (e.getSource().getNodeData().getNode(graphView.getViewId()) != null
                            && e.getTarget().getNodeData().getNode(graphView.getViewId()) != null) {
                        graph.addEdge(e);
                    }
                }
            }

            return graph;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            if (filters.length > 1) {
                throw new IllegalArgumentException("Not Filter accepts a single filter in parameter");
            }
            Filter filter = filters[0];
            if (filter instanceof NodeFilter && ((NodeFilter) filter).init(graph)) {
                NodeFilter nodeFilter = (NodeFilter) filter;
                for (Node n : graph.getNodes().toArray()) {
                    if (nodeFilter.evaluate(graph, n)) {
                        graph.removeNode(n);
                    }
                }
                nodeFilter.finish();
            }

            return graph;
        }
    }
}
