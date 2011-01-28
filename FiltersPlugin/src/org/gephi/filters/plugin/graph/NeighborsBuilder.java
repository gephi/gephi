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
package org.gephi.filters.plugin.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Sebastien Heymann
 */
@ServiceProvider(service = FilterBuilder.class)
public class NeighborsBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(NeighborsBuilder.class, "NeighborsBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(NeighborsBuilder.class, "NeighborsBuilder.description");
    }

    public Filter getFilter() {
        return new NeighborsFilter();
    }

    public JPanel getPanel(Filter filter) {
        NeighborsUI ui = Lookup.getDefault().lookup(NeighborsUI.class);
        if (ui != null) {
            return ui.getPanel((NeighborsFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class NeighborsFilter implements ComplexFilter {

        private boolean self = true;
        private int depth = 1;

        public Graph filter(Graph graph) {

            GraphView graphView = graph.getView();
            HierarchicalGraph mainGraph = graphView.getGraphModel().getHierarchicalGraph();

            List<Node> nodes = new ArrayList<Node>();
            for (Node n : graph.getNodes()) {
                nodes.add(n.getNodeData().getNode(mainGraph.getView().getViewId()));
            }

            Set<Node> result = new HashSet<Node>();

            Set<Node> neighbours = new HashSet<Node>();
            neighbours.addAll(nodes);

            //Put all neighbors into result
            for (int i = 0; i < depth; i++) {
                Node[] nei = neighbours.toArray(new Node[0]);
                neighbours.clear();
                for (Node n : nei) {
                    //Extract all neighbors of n
                    for (Node neighbor : mainGraph.getNeighbors(n)) {
                        if (!result.contains(neighbor)) {
                            neighbours.add(neighbor);
                            result.add(neighbor);
                        }
                    }
                }
                if (neighbours.isEmpty()) {
                    break;
                }
            }

            if (self) {
                result.addAll(nodes);
            } else {
                result.removeAll(nodes);
            }

            //Update nodes
            for (Node node : mainGraph.getNodes().toArray()) {
                if (result.contains(node)) {
                    graph.addNode(node);
                } else if(graph.contains(node)) {
                    graph.removeNode(node);
                }
            }

            //Update edges
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

        public String getName() {
            return NbBundle.getMessage(NeighborsBuilder.class, "NeighborsBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, Integer.class, "depth"),
                            FilterProperty.createProperty(this, Boolean.class, "self")};
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public Integer getDepth() {
            return depth;
        }

        public void setDepth(Integer depth) {
            this.depth = depth;
        }

        public boolean isSelf() {
            return self;
        }

        public void setSelf(boolean self) {
            this.self = self;
        }
    }
}
