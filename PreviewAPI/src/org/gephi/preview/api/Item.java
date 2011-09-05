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

import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;

/**
 * An item is a visual element built by an {@link ItemBuilder} and later used
 * by a {@link Renderer} to be displayed. 
 * <p>
 * An item simply stores the reference to the original object (e.g. node, edge) and
 * all the information useful for the <code>Renderer</code> like the color, size or
 * position.
 * <p>
 * All items can be retrieved from the {@link PreviewModel}.
 * 
 * @author Yudi Xue, Mathieu Bastian
 */
public interface Item {

    public static final String NODE = "node";
    public static final String EDGE = "edge";
    public static final String NODE_LABEL = "node_label";
    public static final String EDGE_LABEL = "edge_label";

    /**
     * Returns the source of the item. The source is usually a graph object like
     * a <code>Node</code> or <code>Edge</code>.
     * @return the item's source object
     */
    public Object getSource();

    /**
     * Returns the type of the item. Default types are <code>Item.NODE</code>, 
     * <code>Item.EDGE</code>, <code>Item.NODE_LABEL</code> and <code>Item.EDGE_LABEL</code>.
     * @return the item's type
     */
    public String getType();

    /**
     * Returns data associated to this item.
     * @param <D> the type of the data
     * @param key the key
     * @return the value associated to <code>key</code>, or <code>null</code> if
     * not exist
     */
    public <D> D getData(String key);

    /**
     * Sets data to this item.
     * @param key the key
     * @param value the value to be associated with <code>key</code>
     */
    public void setData(String key, Object value);

    /**
     * Returns all the keys. That allows to enumerate all data associated with
     * this item.
     * @return all keys
     */
    public String[] getKeys();
}
