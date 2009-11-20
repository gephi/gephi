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
package org.gephi.visualization.opengl.text;

import org.gephi.graph.api.TextData;
import org.gephi.graph.api.TextDataFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service=TextDataFactory.class)
public class TextDataBuilderImpl implements TextDataFactory {

    public TextData newTextData() {
        return new TextDataImpl();
    }

    /* public TextData buildTextNode(NodeData n) {
    TextDataImpl t = new TextDataImpl();
    if (nodeColumns != null) {
    String str = "";
    for (AttributeColumn c : nodeColumns) {
    str += n.getAttributes().getValue(c.getIndex());
    }
    t.setLine(str);
    } else {
    t.setLine(n.getLabel());
    }
    return t;
    }

    public TextData buildTextEdge(EdgeData e) {
    TextDataImpl t = new TextDataImpl();
    if (edgeColumns != null) {
    String str = "";
    for (AttributeColumn c : edgeColumns) {
    str += e.getAttributes().getValue(c.getIndex());
    }
    t.setLine(str);
    } else {
    t.setLine(e.getLabel());
    }
    return t;
    }*/
}
