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
package org.gephi.preview.plugin.items;

import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeItem extends AbstractItem {

    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";
    public static final String SIZE ="size";
    public static final String COLOR = "color";
    
    public NodeItem(Node source) {
        super(source, Item.NODE);
    }
}
