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
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.TextData;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.EdgeLabelItem;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class)
public class EdgeLabelBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {
        HierarchicalGraph hgraph = (HierarchicalGraph) graph;

        boolean useTextData = false;
        for (Edge e : hgraph.getEdgesAndMetaEdges()) {
            TextData textData = e.getEdgeData().getTextData();
            if (textData != null && textData.getText() != null && !textData.getText().isEmpty()) {
                useTextData = true;
            }
        }

        List<Item> items = new ArrayList<Item>();
        for (Edge e : hgraph.getEdgesAndMetaEdges()) {
            EdgeLabelItem labelItem = new EdgeLabelItem(e);
            String label = e.getEdgeData().getLabel();
            labelItem.setData(EdgeLabelItem.LABEL, label);
            TextData textData = e.getEdgeData().getTextData();
            if (textData != null && useTextData) {
                if (textData.getR() != -1) {
                    labelItem.setData(NodeLabelItem.COLOR, new Color((int) (textData.getR() * 255),
                            (int) (textData.getG() * 255),
                            (int) (textData.getB() * 255),
                            (int) (textData.getAlpha() * 255)));
                }
                labelItem.setData(EdgeLabelItem.WIDTH, textData.getWidth());
                labelItem.setData(EdgeLabelItem.HEIGHT, textData.getHeight());
                labelItem.setData(EdgeLabelItem.SIZE, textData.getSize());
                labelItem.setData(EdgeLabelItem.VISIBLE, textData.isVisible());
                labelItem.setData(EdgeLabelItem.LABEL, textData.getText());
                if (textData.isVisible() && textData.getText() != null && !textData.getText().isEmpty()) {
                    items.add(labelItem);
                }
            } else if (label != null && !label.isEmpty()) {
                items.add(labelItem);
            }
        }
        return items.toArray(new Item[0]);
    }

    public String getType() {
        return ItemBuilder.EDGE_LABEL_BUILDER;
    }
}
