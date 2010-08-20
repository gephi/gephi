/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
