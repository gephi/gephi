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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class EgoBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.description");
    }

    public Filter getFilter() {
        return new EgoFilter();
    }

    public JPanel getPanel(Filter filter) {
        EgoUI ui = Lookup.getDefault().lookup(EgoUI.class);
        if (ui != null) {
            return ui.getPanel((EgoFilter) filter);
        }
        return null;
    }

    public static class EgoFilter implements ComplexFilter {

        private String pattern = "";
        private int depth = 1;

        public Graph filter(Graph graph) {

            String str = pattern.toLowerCase();

            List<Node> nodes = new ArrayList<Node>();
            for (Node n : graph.getNodes()) {
                if (n.getNodeData().getId().toLowerCase().contains(str)) {
                    nodes.add(n);
                } else if (n.getNodeData().getLabel().toLowerCase().contains(str)) {
                    nodes.add(n);
                }
            }

            Set<Node> result = new HashSet<Node>();

            Set<Node> neighbours = new HashSet<Node>();
            neighbours.addAll(nodes);

            for (int i = 0; i < depth; i++) {
                Node[] nei = neighbours.toArray(new Node[0]);
                neighbours.clear();
                for (Node n : nei) {
                    for (Node neighbor : graph.getNeighbors(n)) {
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

            for (Node node : graph.getNodes().toArray()) {
                if (!result.contains(node)) {
                    graph.removeNode(node);
                }
            }

            return graph;
        }

        public String getName() {
            return NbBundle.getMessage(DegreeRangeBuilder.class, "EgoBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, String.class, "pattern"),
                            FilterProperty.createProperty(this, Integer.class, "depth")};
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Integer getDepth() {
            return depth;
        }

        public void setDepth(Integer depth) {
            this.depth = depth;
        }
    }
}
