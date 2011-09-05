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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class, position = 100)
public class NodeBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {

        Item[] items = new NodeItem[graph.getNodeCount()];
        int i = 0;
        for (Node n : graph.getNodes()) {
            NodeItem nodeItem = new NodeItem(n.getNodeData().getRootNode());
            nodeItem.setData(NodeItem.X, n.getNodeData().x());
            nodeItem.setData(NodeItem.Y, -n.getNodeData().y());
            nodeItem.setData(NodeItem.Z, n.getNodeData().z());
            nodeItem.setData(NodeItem.SIZE, n.getNodeData().getSize() * 2f);
            nodeItem.setData(NodeItem.COLOR, new Color((int) (n.getNodeData().r() * 255),
                    (int) (n.getNodeData().g() * 255),
                    (int) (n.getNodeData().b() * 255),
                    (int) (n.getNodeData().alpha() * 255)));
            items[i++] = nodeItem;
        }
        return items;
    }

    public String getType() {
        return ItemBuilder.NODE_BUILDER;
    }
}
