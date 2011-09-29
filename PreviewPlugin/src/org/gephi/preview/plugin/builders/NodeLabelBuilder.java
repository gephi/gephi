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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.TextData;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class, position = 200)
public class NodeLabelBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {

        boolean useTextData = false;
        for (Node n : graph.getNodes()) {
            TextData textData = n.getNodeData().getTextData();
            if (textData != null && textData.getText() != null && !textData.getText().isEmpty()) {
                useTextData = true;
            }
        }

        //Build text
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel model = dynamicController != null ? dynamicController.getModel(graph.getGraphModel().getWorkspace()) : null;
        TimeInterval timeInterval = model != null ? model.getVisibleInterval() : null;
        Estimator estimator = model != null ? model.getEstimator() : null;
        Estimator numberEstimator = model != null ? model.getNumberEstimator() : null;
        VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
        AttributeColumn[] nodeColumns = vizController != null ? vizController.getNodeTextColumns() : null;

        List<Item> items = new ArrayList<Item>();
        for (Node n : graph.getNodes()) {
            NodeLabelItem labelItem = new NodeLabelItem(n.getNodeData().getRootNode());
            String label = getLabel(n, nodeColumns, timeInterval, estimator, numberEstimator);
            labelItem.setData(NodeLabelItem.LABEL, label);
            TextData textData = n.getNodeData().getTextData();
            if (textData != null && useTextData) {
                if (textData.getR() != -1) {
                    labelItem.setData(NodeLabelItem.COLOR, new Color((int) (textData.getR() * 255),
                            (int) (textData.getG() * 255),
                            (int) (textData.getB() * 255),
                            (int) (textData.getAlpha() * 255)));
                }
                labelItem.setData(NodeLabelItem.WIDTH, textData.getWidth());
                labelItem.setData(NodeLabelItem.HEIGHT, textData.getHeight());
                labelItem.setData(NodeLabelItem.SIZE, textData.getSize());
                labelItem.setData(NodeLabelItem.VISIBLE, textData.isVisible());
                labelItem.setData(NodeLabelItem.LABEL, textData.getText());
                if (textData.isVisible() && label != null && !label.isEmpty()) {
                    items.add(labelItem);
                }
            } else if (label != null && !label.isEmpty()) {
                items.add(labelItem);
            }
        }
        return items.toArray(new Item[0]);
    }

    private String getLabel(Node n, AttributeColumn[] cols, TimeInterval interval, Estimator estimator, Estimator numberEstimator) {
        NodeData nodeData = n.getNodeData();
        String str = "";
        if (cols != null) {
            int i = 0;
            for (AttributeColumn c : cols) {
                if (i++ > 0) {
                    str += " - ";
                }
                Object val = nodeData.getAttributes().getValue(c.getIndex());
                if (val instanceof DynamicType) {
                    DynamicType dynamicType = (DynamicType) val;
                    if (estimator == null) {
                        estimator = Estimator.FIRST;
                    }
                    if (Number.class.isAssignableFrom(dynamicType.getUnderlyingType())) {
                        estimator = numberEstimator;
                    }
                    if (interval != null) {
                        val = dynamicType.getValue(interval.getLow(), interval.getHigh(), estimator);
                    } else {
                        val = dynamicType.getValue(estimator);
                    }
                }
                str += val != null ? val : "";
            }
        }
        if(str.isEmpty()) {
            str = nodeData.getLabel();
        }
        return str;
    }

    public String getType() {
        return ItemBuilder.NODE_LABEL_BUILDER;
    }
}
