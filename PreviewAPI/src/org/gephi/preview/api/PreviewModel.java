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
package org.gephi.preview.api;

import java.awt.Dimension;
import java.awt.Point;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;

/**
 * The Preview Model contains all items and all preview properties.
 * <p>
 * Items are the visual elements built from the {@link Graph} by {@link ItemBuilder} 
 * implementations and can be retrieved from this class. Each item has a type and
 * default types are {@link Item#NODE}, {@link Item#EDGE}, {@link Item#NODE_LABEL}
 * and {@link Item#EDGE_LABEL}.
 * <p>
 * A preview model is attached to it's workspace and can be retrieved from the
 * {@link PreviewController}.
 * 
 * @author Yudi Xue, Mathieu Bastian
 * @see Item
 * @see Renderer
 */
public interface PreviewModel {

    /**
     * Returns the preview properties attached to this model.
     * @return the preview properties
     */
    public PreviewProperties getProperties();

    /**
     * Returns all items with <code>type</code> as type.
     * <p>
     * Default types are {@link Item#NODE}, {@link Item#EDGE}, {@link Item#NODE_LABEL}
     * and {@link Item#EDGE_LABEL}.
     * @param type the item's type
     * @return all items from this type
     */
    public Item[] getItems(String type);

    /**
     * Returns all items attached to <code>source</code>.
     * <p>
     * The source is the graph object behind the item (e.g.
     * {@link Node} or {@link Edge}). Multiple items can be created from the same
     * source object. For instance both <code>Item.NODE</code> and
     * <code>Item.NODE_LABEL</code> have the node object as source.
     * @param source the item's source
     * @return all items with <code>source</code> as source
     */
    public Item[] getItems(Object source);

    /**
     * Returns the item attached to <code>source</code> and with the type
     * <code>type</code>. 
     * <p>
     * The source is the graph object behind the item (e.g.
     * {@link Node} or {@link Edge}) and the type a default or a custom type.
     * <p>
     * Default types are {@link Item#NODE}, {@link Item#EDGE}, {@link Item#NODE_LABEL}
     * and {@link Item#EDGE_LABEL}.
     * @param type the item's type
     * @param source the item's source object
     * @return the item or <code>null</code> if not found
     */
    public Item getItem(String type, Object source);

    /**
     * Returns the width and height of the graph in the graph coordinates.
     * @return the graph dimensions
     */
    public Dimension getDimensions();

    /**
     * Returns the top left position in the graph coordinate (i.e. not the preview
     * coordinates).
     * @return the top left position point
     */
    public Point getTopLeftPosition();
}
