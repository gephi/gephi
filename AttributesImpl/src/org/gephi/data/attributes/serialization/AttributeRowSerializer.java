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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.AttributeColumnImpl;
import org.gephi.data.attributes.AttributeRowImpl;
import org.gephi.data.attributes.AttributeTableImpl;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeRowSerializer {

    private static final String ELEMENT_ROWS = "attributerows";
    private static final String ELEMENT_NODE_ROW = "noderow";
    private static final String ELEMENT_EDGE_ROW = "edgerow";
    private static final String ELEMENT_VALUE = "attvalue";

    public void writeRows(XMLStreamWriter writer, GraphModel graphModel) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_ROWS);

        HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();
        for (Node node : hierarchicalGraph.getNodesTree()) {
            if (node.getNodeData().getAttributes() != null && node.getNodeData().getAttributes() instanceof AttributeRowImpl) {
                AttributeRowImpl row = (AttributeRowImpl) node.getNodeData().getAttributes();
                writer.writeStartElement(ELEMENT_NODE_ROW);
                writer.writeAttribute("for", String.valueOf(node.getId()));
                if (writeRow(writer, row)) {
                    writer.writeEndElement();
                }
            }
        }

        for (Node node : hierarchicalGraph.getNodesTree()) {
            for (Edge edge : hierarchicalGraph.getEdges(node)) {
                if (edge.getEdgeData().getAttributes() != null && edge.getEdgeData().getAttributes() instanceof AttributeRowImpl) {
                    AttributeRowImpl row = (AttributeRowImpl) edge.getEdgeData().getAttributes();
                    writer.writeStartElement(ELEMENT_EDGE_ROW);
                    writer.writeAttribute("for", String.valueOf(edge.getId()));
                    if (writeRow(writer, row)) {
                        writer.writeEndElement();
                    }
                }
            }
        }

        writer.writeEndElement();
    }

    public void readRows(XMLStreamReader reader, GraphModel graphModel, AbstractAttributeModel attributeModel) throws XMLStreamException {
        HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_NODE_ROW.equalsIgnoreCase(name)) {
                        int id = Integer.parseInt(reader.getAttributeValue(null, "for"));
                        Node node = hierarchicalGraph.getNode(id);
                        if (node.getNodeData().getAttributes() != null && node.getNodeData().getAttributes() instanceof AttributeRowImpl) {
                            AttributeRowImpl row = (AttributeRowImpl) node.getNodeData().getAttributes();
                            readRow(reader, attributeModel, attributeModel.getNodeTable(), row);
                        }
                    } else if (ELEMENT_EDGE_ROW.equalsIgnoreCase(name)) {
                        int id = Integer.parseInt(reader.getAttributeValue(null, "for"));
                        Edge edge = hierarchicalGraph.getEdge(id);
                        if (edge.getEdgeData().getAttributes() != null && edge.getEdgeData().getAttributes() instanceof AttributeRowImpl) {
                            AttributeRowImpl row = (AttributeRowImpl) edge.getEdgeData().getAttributes();
                            readRow(reader, attributeModel, attributeModel.getEdgeTable(), row);
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_ROWS.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public boolean writeRow(XMLStreamWriter writer, AttributeRowImpl row) throws XMLStreamException {
        writer.writeAttribute("version", String.valueOf(row.getRowVersion()));
        int writtenRows = 0;
        for (AttributeValue value : row.getValues()) {
            int index = value.getColumn().getIndex();
            Object obj = value.getValue();
            if (obj != null) {
                writtenRows++;
                writer.writeStartElement(ELEMENT_VALUE);
                writer.writeAttribute("index", String.valueOf(index));
                writer.writeCharacters(obj.toString());
                writer.writeEndElement();
            }
        }
        return writtenRows > 0;
    }

    public void readRow(XMLStreamReader reader, AbstractAttributeModel model, AttributeTableImpl table, AttributeRowImpl row) throws XMLStreamException {
        row.setRowVersion(Integer.parseInt(reader.getAttributeValue(null, "version")));
        AttributeColumnImpl col = null;
        String value = "";

        boolean end = false;
        while (reader.hasNext() && !end) {
            int t = reader.next();

            switch (t) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_VALUE.equalsIgnoreCase(name)) {
                        col = (AttributeColumnImpl) table.getColumn(Integer.parseInt(reader.getAttributeValue(null, "index")));
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace() && col != null) {
                        value += reader.getText();
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_NODE_ROW.equalsIgnoreCase(reader.getLocalName()) || ELEMENT_EDGE_ROW.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    if (!value.isEmpty() && col != null) {
                        AttributeType type = col.getType();
                        Object v = type.parse(value);
                        v = model.getManagedValue(v, type);
                        row.setValue(col, value);
                    }
                    value = "";
                    col = null;
                    break;
            }
        }
    }
}
