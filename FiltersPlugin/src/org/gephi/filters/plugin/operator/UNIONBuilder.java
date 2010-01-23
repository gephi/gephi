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
package org.gephi.filters.plugin.operator;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class UNIONBuilder implements FilterBuilder {

    public Category getCategory() {
        return new Category("Operator");
    }

    public String getName() {
        return "UNION";
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Filter getFilter() {
        return new UnionOperator();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    private static class UnionOperator implements Operator {

        public int getInputCount() {
            return Integer.MAX_VALUE;
        }

        public String getName() {
            return "Union";
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public Graph filter(Graph[] graphs) {
            Graph maxGraph = graphs[0];
            int maxElements = 0;
            for (int i = 0; i < graphs.length; i++) {
                int count = graphs[i].getNodeCount();
                if (count > maxElements) {
                    maxGraph = graphs[i];
                    maxElements = count;
                }
            }
            for (int i = 0; i < graphs.length; i++) {
                if (graphs[i] != maxGraph) {
                    //Merge
                    for (Node n : graphs[i].getNodes().toArray()) {
                        maxGraph.addNode(n);
                    }
                    for (Edge e : graphs[i].getEdges().toArray()) {
                        maxGraph.addEdge(e);
                    }
                }
            }
            return maxGraph;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            List<NodeFilter> nodeFilters = new ArrayList<NodeFilter>();
            List<EdgeFilter> edgeFilters = new ArrayList<EdgeFilter>();
            for (Filter f : filters) {
                if (f instanceof NodeFilter) {
                    nodeFilters.add((NodeFilter) f);
                } else if (f instanceof EdgeFilter) {
                    edgeFilters.add((EdgeFilter) f);
                }
            }
            if (nodeFilters.size() > 0) {
                for (NodeFilter nf : nodeFilters) {
                    nf.init(graph);
                }
                List<Node> nodesToRemove = new ArrayList<Node>();
                for (Node n : graph.getNodes()) {
                    boolean remove = true;
                    for (NodeFilter nf : nodeFilters) {
                        if (nf.evaluate(graph, n)) {
                            remove = false;
                        }
                    }
                    if (remove) {
                        nodesToRemove.add(n);
                    }
                }

                for (Node n : nodesToRemove) {
                    graph.removeNode(n);
                }
                for (NodeFilter nf : nodeFilters) {
                    nf.finish();
                }
            }
            if (edgeFilters.size() > 0) {
                for (EdgeFilter ef : edgeFilters) {
                    ef.init(graph);
                }
                List<Edge> edgesToRemove = new ArrayList<Edge>();
                for (Edge e : graph.getEdges()) {
                    boolean remove = true;
                    for (EdgeFilter ef : edgeFilters) {
                        if (ef.evaluate(graph, e)) {
                            remove = false;
                        }
                    }
                    if (remove) {
                        edgesToRemove.add(e);
                    }
                }

                for (Edge e : edgesToRemove) {
                    graph.removeEdge(e);
                }
                for (EdgeFilter ef : edgeFilters) {
                    ef.finish();
                }
            }
            return graph;
        }
    }
}
