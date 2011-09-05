/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.plugin.builders;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class, position = 300)
public class EdgeBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {

        //If the edge weight is dynamic, we'll need the time interval
        TimeInterval timeInterval = null;
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        if (dynamicController != null) {
            timeInterval = DynamicUtilities.getVisibleInterval(dynamicController.getModel(graph.getGraphModel().getWorkspace()));
        }
        if (timeInterval == null) {
            timeInterval = new TimeInterval();
        }

        HierarchicalGraph hgraph = (HierarchicalGraph)graph;
        EdgeItem[] items = new EdgeItem[hgraph.getTotalEdgeCount()];
        int i = 0;
        for (Edge e : hgraph.getEdgesAndMetaEdges()) {
            EdgeItem item = new EdgeItem(e);
            item.setData(EdgeItem.WEIGHT, e.getWeight(timeInterval.getLow(), timeInterval.getHigh()));
            item.setData(EdgeItem.DIRECTED, e.isDirected());
            if (graph instanceof HierarchicalDirectedGraph) {
                item.setData(EdgeItem.MUTUAL, graph.getEdge(e.getTarget(), e.getSource()) != null);
            }
            item.setData(EdgeItem.SELF_LOOP, e.isSelfLoop());
            item.setData(EdgeItem.META_EDGE, e instanceof MetaEdge);
            item.setData(EdgeItem.COLOR, e.getEdgeData().r() == -1 ? null : new Color((int) (e.getEdgeData().r() * 255f),
                    (int) (e.getEdgeData().g() * 255f),
                    (int) (e.getEdgeData().b() * 255f),
                    (int) (e.getEdgeData().alpha() * 255f)));
            items[i++] = item;
        }
        return items;
    }

    public String getType() {
        return ItemBuilder.EDGE_BUILDER;
    }
}
