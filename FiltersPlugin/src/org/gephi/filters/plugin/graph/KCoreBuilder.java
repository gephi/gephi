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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class KCoreBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(KCoreBuilder.class, "KCoreBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(KCoreBuilder.class, "KCoreBuilder.description");
    }

    public Filter getFilter() {
        return new KCoreFilter();
    }

    public JPanel getPanel(Filter filter) {
        KCoreUI ui = Lookup.getDefault().lookup(KCoreUI.class);
        if (ui != null) {
            return ui.getPanel((KCoreFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class KCoreFilter implements ComplexFilter {

        private FilterProperty[] filterProperties;
        private Integer k = 1;

        public Graph filter(Graph graph) {
            int removed = 0;
            do {
                removed = 0;
                for (Node n : graph.getNodes().toArray()) {
                    if (graph.getDegree(n) < k) {
                        graph.removeNode(n);
                        removed++;
                    }
                }
            } while (removed > 0);
            return graph;
        }

        public String getName() {
            return NbBundle.getMessage(KCoreBuilder.class, "KCoreBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                        FilterProperty.createProperty(this, Integer.class, "k"),};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public Integer getK() {
            return k;
        }

        public void setK(Integer k) {
            this.k = k;
        }
    }
}
