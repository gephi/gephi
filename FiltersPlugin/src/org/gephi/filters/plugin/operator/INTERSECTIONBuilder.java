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

import java.util.ArrayList;
import java.util.Iterator;
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
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class INTERSECTIONBuilder implements FilterBuilder {

    public Category getCategory() {
        return new Category("Operator");
    }

    public String getName() {
        return NbBundle.getMessage(INTERSECTIONBuilder.class, "INTERSECTIONBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(INTERSECTIONBuilder.class, "INTERSECTIONBuilder.description");
    }

    public Filter getFilter() {
        return new IntersectionOperator();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class IntersectionOperator implements Operator {

        public int getInputCount() {
            return Integer.MAX_VALUE;
        }

        public String getName() {
            return NbBundle.getMessage(INTERSECTIONBuilder.class, "INTERSECTIONBuilder.name");
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public Graph filter(Graph[] graphs) {
            HierarchicalGraph minHGraph = (HierarchicalGraph) graphs[0];
            int minElements = Integer.MAX_VALUE;
            for (int i = 0; i < graphs.length; i++) {
                int count = ((HierarchicalGraph)graphs[i]).getNodeCount();
                if (count < minElements) {
                    minHGraph = (HierarchicalGraph) graphs[i];
                    minElements = count;
                }
            }
            for (Node n : minHGraph.getNodes().toArray()) {
                for (int i = 0; i < graphs.length; i++) {
                    if ((HierarchicalGraph)graphs[i] != minHGraph) {
                        if (!((HierarchicalGraph)graphs[i]).contains(n)) {
                            minHGraph.removeNode(n);
                            break;
                        }
                    }
                }
            }
            for (Edge e : minHGraph.getEdges().toArray()) {
                for (int i = 0; i < graphs.length; i++) {
                    if ((HierarchicalGraph)graphs[i] != minHGraph) {
                        if (!((HierarchicalGraph)graphs[i]).contains(e)) {
                            minHGraph.removeEdge(e);
                            break;
                        }
                    }
                }
            }
            return minHGraph;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
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
                for (Iterator<NodeFilter> itr = nodeFilters.iterator(); itr.hasNext();) {
                    NodeFilter nf = itr.next();
                    if (!nf.init(hgraph)) {
                        itr.remove();
                    }
                }
                List<Node> nodesToRemove = new ArrayList<Node>();
                for (Node n : hgraph.getNodes()) {
                    for (NodeFilter nf : nodeFilters) {
                        if (!nf.evaluate(hgraph, n)) {
                            nodesToRemove.add(n);
                            break;
                        }
                    }
                }

                for (Node n : nodesToRemove) {
                    hgraph.removeNode(n);
                }

                for (NodeFilter nf : nodeFilters) {
                    nf.finish();
                }
            }
            if (edgeFilters.size() > 0) {
                for (Iterator<EdgeFilter> itr = edgeFilters.iterator(); itr.hasNext();) {
                    EdgeFilter ef = itr.next();
                    if (!ef.init(hgraph)) {
                        itr.remove();
                    }
                }
                List<Edge> edgesToRemove = new ArrayList<Edge>();
                for (Edge e : hgraph.getEdges()) {
                    for (EdgeFilter ef : edgeFilters) {
                        if (!ef.evaluate(hgraph, e)) {
                            edgesToRemove.add(e);
                            break;
                        }
                    }
                }

                for (Edge e : edgesToRemove) {
                    hgraph.removeEdge(e);
                }

                for (EdgeFilter ef : edgeFilters) {
                    ef.finish();
                }
            }
            return hgraph;
        }
    }
}
