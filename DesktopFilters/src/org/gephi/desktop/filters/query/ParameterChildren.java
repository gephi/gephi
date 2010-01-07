/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.filters.query;

import org.gephi.filters.api.Query;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class ParameterChildren extends Children.Keys<Integer> {

    private Query function;

    public ParameterChildren(Query function) {
        this.function = function;
        Integer[] indexes = new Integer[function.getParametersCount()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        setKeys(indexes);
    }

    @Override
    protected Node[] createNodes(Integer key) {
        return new Node[]{new ParameterNode(function.getParameterName(key), function.getParameterValue(key))};
    }
}
