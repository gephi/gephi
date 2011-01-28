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
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
//@ServiceProvider(service = FilterBuilder.class)
public class LevelBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.HIERARCHY;
    }

    public String getName() {
        return NbBundle.getMessage(LevelBuilder.class, "LevelBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public LevelFilter getFilter() {
        return new LevelFilter();
    }

    public JPanel getPanel(Filter filter) {
        LevelUI ui = Lookup.getDefault().lookup(LevelUI.class);
        if (ui != null) {
            return ui.getPanel((LevelFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class LevelFilter implements NodeFilter {

        private Integer level = 0;
        private int height;

        public boolean init(Graph graph) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            height = hg.getHeight();
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            return hg.getLevel(node) == level.intValue();
        }

        public void finish() {
        }

        public int getHeight() {
            return height;
        }

        public String getName() {
            return NbBundle.getMessage(LevelBuilder.class, "LevelBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, Integer.class, "level")
                        };
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }
}
