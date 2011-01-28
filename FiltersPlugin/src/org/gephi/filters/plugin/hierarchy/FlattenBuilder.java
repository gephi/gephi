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
package org.gephi.filters.plugin.hierarchy;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class FlattenBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.HIERARCHY;
    }

    public String getName() {
        return NbBundle.getMessage(FlattenBuilder.class, "FlattenBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(FlattenBuilder.class, "FlattenBuilder.description");
    }

    public FlattenFilter getFilter() {
        return new FlattenFilter();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class FlattenFilter implements ComplexFilter {

        public boolean init(Graph graph) {
            return true;
        }

        public Graph filter(Graph graph) {
            HierarchicalGraph hierarchicalGraph = (HierarchicalGraph) graph;
            hierarchicalGraph.flatten();
            return hierarchicalGraph;
        }

        public void finish() {
        }

        public String getName() {
            return NbBundle.getMessage(FlattenBuilder.class, "FlattenBuilder.name");
        }

        public FilterProperty[] getProperties() {
            return new FilterProperty[0];
        }
    }
}
