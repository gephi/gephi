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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.TextData;

/**
 *
 * @author Mathieu Bastian
 */
public class TextDataBuilder {

    private TextManager textManager;
    private AttributeColumn[] nodeColumns;
    private AttributeColumn[] edgeColumns;

    public void initBuilder(TextManager manager) {
        this.textManager = manager;
        nodeColumns = textManager.getModel().getNodeTextColumns();
        edgeColumns = textManager.getModel().getEdgeTextColumns();
    }

    public TextData buildTextNode(NodeData n) {
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
    }

    public AttributeColumn[] getNodeColumns() {
        return nodeColumns;
    }

    public AttributeColumn[] getEdgeColumns() {
        return edgeColumns;
    }
}
