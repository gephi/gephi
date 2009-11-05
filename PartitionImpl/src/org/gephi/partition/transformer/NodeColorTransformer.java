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
package org.gephi.partition.transformer;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.gephi.partition.api.Transformer;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeColorTransformer implements Transformer {

    private Map<Object, Color> map;

    public NodeColorTransformer() {
        map = new HashMap<Object, Color>();
    }

    public Map<Object, Color> getMap() {
        return map;
    }
}
