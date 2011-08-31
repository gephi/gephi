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
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.Item;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
public interface ItemBuilder {
    
    public static final String NODE_BUILDER = Item.NODE;
    public static final String NODE_LABEL_BUILDER = Item.NODE_LABEL;
    public static final String EDGE_BUILDER = Item.EDGE;
    
    public Item[] getItems(GraphModel graphModel, AttributeModel attributeModel);
    
    public String getType();
}
