/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.operator;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
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
public class NOTBuilderEdge implements FilterBuilder {

    public Category getCategory() {
        return new Category("Operator");
    }

    public String getName() {
        return "NOT (Edges)";
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Filter getFilter() {
        return new NotOperatorEdge();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public static class NotOperatorEdge implements Operator {

        public int getInputCount() {
            return 1;
        }

        public String getName() {
            return "NOT (Edges)";
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
            for (Edge e : mainGraph.getEdges().toArray()) {
                Node source = e.getSource().getNodeData().getNode(graphView.getViewId());
                Node target = e.getTarget().getNodeData().getNode(graphView.getViewId());
                if (source != null && target != null) {
                    Edge edgeInGraph = graph.getEdge(source, target);
                    if (edgeInGraph == null) {
                        //The edge is not in graph
                        graph.addEdge(e);
                    } else {
                        //The edge is in the graph
                        graph.removeEdge(edgeInGraph);
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
            if (filter instanceof EdgeFilter && ((EdgeFilter) filter).init(graph)) {
                EdgeFilter edgeFilter = (EdgeFilter) filter;
                for (Edge e : graph.getEdges().toArray()) {
                    if (edgeFilter.evaluate(graph, e)) {
                        graph.removeEdge(e);
                    }
                }
                edgeFilter.finish();
            }

            return graph;
        }
    }
}
