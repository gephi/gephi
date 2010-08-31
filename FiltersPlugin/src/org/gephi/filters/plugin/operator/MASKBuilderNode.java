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
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
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
public class MASKBuilderNode implements FilterBuilder {

    public Category getCategory() {
        return new Category("Operator");
    }

    public String getName() {
        return NbBundle.getMessage(MASKBuilderNode.class, "MASKBuilderNode.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(MASKBuilderNode.class, "MASKBuilderNode.description");
    }

    public Filter getFilter() {
        return new MaskNodeOperator();
    }

    public JPanel getPanel(Filter filter) {
        MASKNodeUI ui = Lookup.getDefault().lookup(MASKNodeUI.class);
        if (ui != null) {
            return ui.getPanel((MaskNodeOperator) filter);
        }
        return null;
    }

    public static class MaskNodeOperator implements Operator {

        public enum NodesOptions {

            AT_LEAST_ONE, NONE, ALL
        };
        private NodesOptions option = NodesOptions.AT_LEAST_ONE;
        private FilterProperty[] filterProperties;

        public int getInputCount() {
            return 1;
        }

        public String getName() {
            return NbBundle.getMessage(MASKBuilderNode.class, "MASKBuilderNode.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, String.class, "option")
                            };
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public Graph filter(Graph[] graphs) {
            if (graphs.length > 1) {
                throw new IllegalArgumentException("Filter accepts a single graph in parameter");
            }

           

            return null;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            if (filters.length > 1) {
                throw new IllegalArgumentException("Filter accepts a single filter in parameter");
            }
           
            return graph;
        }

        public String getOption() {
            return option.toString();
        }

        public void setOption(String option) {
            this.option = NodesOptions.valueOf(option);
        }
    }
}
