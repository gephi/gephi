/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl;

import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalaboratory.api.AttributesController;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see AttributesController
 */
@ServiceProvider(service = AttributesController.class)
public class AttributesControllerImpl implements AttributesController {

    public void clearNodeData(Node node) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        if (gec.isNodeInGraph(node)) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            AttributeValue[] values = row.getValues();
            for (int i = 0; i < values.length; i++) {
                //Clear all except id and computed attributes:
                if (values[i].getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex() && values[i].getColumn().getOrigin() != AttributeOrigin.COMPUTED) {
                    row.setValue(i, null);
                }
            }
        }
    }

    public void clearNodesData(Node[] nodes) {
        for (Node n : nodes) {
            clearNodeData(n);
        }
    }

    public void clearEdgeData(Edge edge) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        if (gec.isEdgeInGraph(edge)) {
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            AttributeValue[] values = row.getValues();
            for (int i = 0; i < values.length; i++) {
                //Clear all except id and computed attributes:
                if (values[i].getColumn().getIndex() != PropertiesColumn.EDGE_ID.getIndex() && values[i].getColumn().getOrigin() != AttributeOrigin.COMPUTED) {
                    row.setValue(i, null);
                }
            }
        }
    }

    public void clearEdgesData(Edge[] edges) {
        for (Edge e : edges) {
            clearEdgeData(e);
        }
    }
}
