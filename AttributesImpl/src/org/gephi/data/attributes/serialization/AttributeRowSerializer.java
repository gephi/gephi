/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.data.attributes.serialization;

import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.AttributeColumnImpl;
import org.gephi.data.attributes.AttributeRowImpl;
import org.gephi.data.attributes.AttributeTableImpl;
import org.gephi.data.attributes.AttributeValueImpl;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeRowSerializer {

    private static final String ELEMENT_ROWS = "attributerows";
    private static final String ELEMENT_NODE_ROW = "noderow";
    private static final String ELEMENT_EDGE_ROW = "edgerow";
    private static final String ELEMENT_VALUE = "attvalue";

    public Element writeRows(Document document, GraphModel graphModel) {
        Element rowsE = document.createElement(ELEMENT_ROWS);

        HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();
        for (Node node : hierarchicalGraph.getNodesTree()) {
            if (node.getNodeData().getAttributes() != null && node.getNodeData().getAttributes() instanceof AttributeRowImpl) {
                AttributeRowImpl row = (AttributeRowImpl) node.getNodeData().getAttributes();
                Element rowE = document.createElement(ELEMENT_NODE_ROW);
                rowE.setAttribute("for", String.valueOf(node.getId()));
                if (writeRow(document, rowE, row)) {
                    rowsE.appendChild(rowE);
                }
            }
        }

        for (Node node : hierarchicalGraph.getNodesTree()) {
            for (Edge edge : hierarchicalGraph.getEdges(node)) {
                if (edge.getEdgeData().getAttributes() != null && edge.getEdgeData().getAttributes() instanceof AttributeRowImpl) {
                    AttributeRowImpl row = (AttributeRowImpl) edge.getEdgeData().getAttributes();
                    Element rowE = document.createElement(ELEMENT_EDGE_ROW);
                    rowE.setAttribute("for", String.valueOf(edge.getId()));
                    if (writeRow(document, rowE, row)) {
                        rowsE.appendChild(rowE);
                    }
                }
            }
        }

        return rowsE;
    }

    public void readRows(Element rowsE, GraphModel graphModel, AbstractAttributeModel attributeModel) {
        HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();

        NodeList rowList = rowsE.getChildNodes();
        for (int i = 0; i < rowList.getLength(); i++) {
            if (rowList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element itemE = (Element) rowList.item(i);
                if (itemE.getTagName().equals(ELEMENT_NODE_ROW)) {
                    int id = Integer.parseInt(itemE.getAttribute("for"));
                    Node node = hierarchicalGraph.getNode(id);
                    if (node.getNodeData().getAttributes() != null && node.getNodeData().getAttributes() instanceof AttributeRowImpl) {
                        AttributeRowImpl row = (AttributeRowImpl) node.getNodeData().getAttributes();
                        readRow(itemE, attributeModel, attributeModel.getNodeTable(), row);
                    }
                } else if (itemE.getTagName().equals(ELEMENT_EDGE_ROW)) {
                    int id = Integer.parseInt(itemE.getAttribute("for"));
                    Edge edge = hierarchicalGraph.getEdge(id);
                    if (edge.getEdgeData().getAttributes() != null && edge.getEdgeData().getAttributes() instanceof AttributeRowImpl) {
                        AttributeRowImpl row = (AttributeRowImpl) edge.getEdgeData().getAttributes();
                        readRow(itemE, attributeModel, attributeModel.getEdgeTable(), row);
                    }
                }
            }
        }
    }

    public boolean writeRow(Document document, Element rowE, AttributeRowImpl row) {
        rowE.setAttribute("version", String.valueOf(row.getRowVersion()));
        int writtenRows = 0;
        for (AttributeValue value : row.getValues()) {
            int index = value.getColumn().getIndex();
            Object obj = value.getValue();
            if (obj != null) {
                writtenRows++;
                Element valueE = document.createElement(ELEMENT_VALUE);
                valueE.setAttribute("index", String.valueOf(index));
                valueE.setTextContent(obj.toString());
                rowE.appendChild(valueE);
            }
        }
        return writtenRows > 0;
    }

    public void readRow(Element rowE, AbstractAttributeModel model, AttributeTableImpl table, AttributeRowImpl row) {
        NodeList rowList = rowE.getChildNodes();
        for (int i = 0; i < rowList.getLength(); i++) {
            if (rowList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element itemE = (Element) rowList.item(i);
                if (itemE.getTagName().equals(ELEMENT_VALUE)) {
                    AttributeColumnImpl col = (AttributeColumnImpl) table.getColumn(Integer.parseInt(itemE.getAttribute("index")));
                    AttributeType type = col.getType();
                    Object value = type.parse(itemE.getTextContent());
                    value = model.getManagedValue(value, type);
                    row.setValue(col, value);
                }
            }
        }
        row.setRowVersion(Integer.parseInt(rowE.getAttribute("version")));
    }
}
