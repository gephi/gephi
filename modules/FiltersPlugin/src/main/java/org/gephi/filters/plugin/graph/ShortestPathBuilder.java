package org.gephi.filters.plugin.graph;

import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FilterBuilder.class)
public class ShortestPathBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ShortestPathBuilder.class, "ShortestPathBuilder.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(EgoBuilder.class, "ShortestPathBuilder.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new ShortestPathFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        ShortestPathUI ui = Lookup.getDefault().lookup(ShortestPathUI.class);
        if (ui != null) {
            return ui.getPanel((ShortestPathFilter) filter);
        }
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class ShortestPathFilter implements ComplexFilter {

        private String node1Pattern = "";
        private String node2Pattern = "";

        @Override
        public Graph filter(Graph graph) {
            String str1 = node1Pattern.toLowerCase();
            String str2 = node2Pattern.toLowerCase();

            Node n1 = null;
            Node n2 = null;

            for (Node n : graph.getNodes()) {
                if (n.getId().toString().toLowerCase().equals(str1)) {
                    n1 = n;
                } else if ((n.getLabel() != null) && n.getLabel().toLowerCase().equals(str1)) {
                    n1 = n;
                } else if (n.getId().toString().toLowerCase().equals(str2)) {
                    n2 = n;
                } else if ((n.getLabel() != null) && n.getLabel().toLowerCase().equals(str2)) {
                    n2 = n;
                }
            }

            if (n1 != null && n2 != null) {
                AbstractShortestPathAlgorithm algorithm;
                if (graph.isDirected()) {
                    algorithm = new BellmanFordShortestPathAlgorithm((DirectedGraph) graph, n1);
                } else {
                    algorithm = new DijkstraShortestPathAlgorithm(graph, n1);
                }

                algorithm.compute();

                Set<Edge> retainEdges = new HashSet<>();
                Set<Node> retainNodes = new HashSet<>();
                if (algorithm.getDistances().get(n2) != Double.POSITIVE_INFINITY) {
                    retainNodes.add(n2);
                    Edge predecessorEdge = algorithm.getPredecessorIncoming(n2);
                    Node predecessor = algorithm.getPredecessor(n2);
                    while (predecessorEdge != null && predecessor != n1) {
                        retainEdges.add(predecessorEdge);
                        retainNodes.add(predecessor);
                        predecessorEdge = algorithm.getPredecessorIncoming(predecessor);
                        predecessor = algorithm.getPredecessor(predecessor);
                    }
                    retainEdges.add(predecessorEdge);
                    retainNodes.add(n1);
                }

                for (Node node : graph.getNodes().toArray()) {
                    if (!retainNodes.contains(node)) {
                        graph.removeNode(node);
                    }
                }

                for (Edge edge : graph.getEdges().toArray()) {
                    if (!retainEdges.contains(edge)) {
                        graph.removeEdge(edge);
                    }
                }
            }

            return graph;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(ShortestPathBuilder.class, "ShortestPathBuilder.name");
        }

        @Override
        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[] {
                    FilterProperty.createProperty(this, String.class, "firstNodePattern"),
                    FilterProperty.createProperty(this, String.class, "secondNodePattern")};
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new FilterProperty[0];
        }

        public String getFirstNodePattern() {
            return node1Pattern;
        }

        public void setFirstNodePattern(String node1Pattern) {
            this.node1Pattern = node1Pattern;
        }

        public String getSecondNodePattern() {
            return node2Pattern;
        }

        public void setSecondNodePattern(String node2Pattern) {
            this.node2Pattern = node2Pattern;
        }
    }
}
