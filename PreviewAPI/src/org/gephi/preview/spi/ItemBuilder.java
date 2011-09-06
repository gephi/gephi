/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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
package org.gephi.preview.spi;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;

/**
 * Builds and returns new {@link Item} instances.
 * <p>
 * Items are the visual elements representing the graph and are built by item
 * builders from the graph object. 
 * <p>
 * An item builder should build only a single type of items. Items are defined 
 * by a type {@link Item#getType()}, which needs to be the same as the value
 * returned by {@link #getType()}. In other words if this builder is building
 * <code>Node.Item</code> items, it should return <code>Node.Item</code> as a type.
 * <p>
 * Item builders are singleton services and implementations need to add the
 * following annotation to be recognized by the system:
 * <p>
 * <code>@ServiceProvider(service=ItemBuilder.class)</code>
 * 
 * @author Yudi Xue, Mathieu Bastian
 */
public interface ItemBuilder {

    public static final String NODE_BUILDER = Item.NODE;
    public static final String NODE_LABEL_BUILDER = Item.NODE_LABEL;
    public static final String EDGE_BUILDER = Item.EDGE;
    public static final String EDGE_LABEL_BUILDER = Item.EDGE_LABEL;

    /**
     * Build items from the <code>graph</code> and <code>attributeModel</code>.
     * @param graph the graph to build items from
     * @param attributeModel the attribute model associated to the graph
     * @return an array of new items, from the same type returned by {@link #getType()}
     */
    public Item[] getItems(Graph graph, AttributeModel attributeModel);

    /**
     * Returns the type of this builder. 
     * <p>
     * The type should <b>always</b> match
     * the type of <code>Item</code> the builder is building. For instance if the
     * builder is building <code>Item.Node</code> type, this method should return
     * <code>Item.Node</code>.
     * @return the builder item type.
     */
    public String getType();
}
