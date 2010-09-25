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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class GiantComponentBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.description");
    }

    public Filter getFilter() {
        return new GiantComponentFilter();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class GiantComponentFilter implements NodeFilter {

        private AttributeModel attributeModel;
        private int componentId;
        private AttributeColumn column;

        public GiantComponentFilter() {
            attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        }

        public boolean init(Graph graph) {
            ConnectedComponents cc = new ConnectedComponents();
            UndirectedGraph undirectedGraph = null;
            if (cc instanceof UndirectedGraph) {
                undirectedGraph = (UndirectedGraph) graph;
            } else {
                undirectedGraph = graph.getView().getGraphModel().getUndirectedGraph(graph.getView());
            }

            cc.weaklyConnected(undirectedGraph, attributeModel);
            componentId = cc.getGiantComponent();
            column = attributeModel.getNodeTable().getColumn(ConnectedComponents.WEAKLY);

            return column != null && componentId != -1;
        }

        public boolean evaluate(Graph graph, Node node) {
            Integer component = (Integer) node.getNodeData().getAttributes().getValue(column.getIndex());
            if (component != null) {
                return component.equals(componentId);
            }
            return false;
        }

        public void finish() {
        }

        public String getName() {
            return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.name");
        }

        public FilterProperty[] getProperties() {
            return new FilterProperty[0];
        }
    }
}
