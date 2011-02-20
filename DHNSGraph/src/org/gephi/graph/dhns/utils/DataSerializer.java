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
package org.gephi.graph.dhns.utils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.EdgeDataImpl;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.NodeDataImpl;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.predicate.Tautology;

/**
 *
 * @author Mathieu Bastian
 */
public class DataSerializer {

    private static final String ELEMENT_DATA = "Data";
    private static final String ELEMENT_NODEDATA = "nodedata";
    private static final String ELEMENT_NODEDATA_POSITION = "position";
    private static final String ELEMENT_NODEDATA_COLOR = "color";
    private static final String ELEMENT_NODEDATA_SIZE = "size";
    private static final String ELEMENT_EDGEDATA = "edgedata";
    private static final String ELEMENT_EDGEDATA_COLOR = "color";

    public void writeData(XMLStreamWriter writer, Dhns dhns) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_DATA);

        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1);
        for (; itr.hasNext();) {
            AbstractNode absNode = itr.next();
            NodeDataImpl nodeData = absNode.getNodeData();
            if (nodeData != null) {
                writeNodeData(writer, nodeData);
            }
        }

        EdgeIterator edgeIterator = new EdgeIterator(treeStructure, new TreeListIterator(treeStructure.getTree(), 1), false, Tautology.instance, Tautology.instance);
        for (; edgeIterator.hasNext();) {
            EdgeDataImpl edgeData = edgeIterator.next().getEdgeData();
            if (edgeData != null) {
                writeEdgeData(writer, edgeData);
            }
        }

        writer.writeEndElement();
    }

    public void readData(XMLStreamReader reader, Dhns dhns) throws XMLStreamException {

        GraphStructure structure = dhns.getGraphStructure();
        TreeStructure treeStructure = structure.getMainView().getStructure();

        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_NODEDATA.equalsIgnoreCase(name)) {
                    AbstractNode node = treeStructure.getNodeAt(Integer.parseInt(reader.getAttributeValue(null, "nodepre")));
                    NodeDataImpl nodeDataImpl = (NodeDataImpl) node.getNodeData();
                    readNodeData(reader, nodeDataImpl, structure);
                } else if (ELEMENT_EDGEDATA.equalsIgnoreCase(name)) {
                    AbstractNode source = treeStructure.getNodeAt(Integer.parseInt(reader.getAttributeValue(null, "sourcepre")));
                    AbstractNode target = treeStructure.getNodeAt(Integer.parseInt(reader.getAttributeValue(null, "targetpre")));
                    AbstractEdge edge = source.getEdgesOutTree().getItem(target.getId());
                    EdgeDataImpl edgeDataImpl = (EdgeDataImpl) edge.getEdgeData();
                    readEdgeData(reader, edgeDataImpl, structure);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_DATA.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    public void writeNodeData(XMLStreamWriter writer, NodeDataImpl nodeData) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_NODEDATA);
        AbstractNode node = nodeData.getRootNode();

        writer.writeAttribute("nodepre", String.valueOf(node.getPre()));

        if (nodeData.getId() != null && !nodeData.getId().equals("" + node.getId())) {
            writer.writeAttribute("id", nodeData.getId());
        }

        writer.writeStartElement(ELEMENT_NODEDATA_POSITION);
        writer.writeAttribute("x", String.valueOf(nodeData.x()));
        writer.writeAttribute("y", String.valueOf(nodeData.y()));
        writer.writeAttribute("z", String.valueOf(nodeData.z()));
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_NODEDATA_COLOR);
        writer.writeAttribute("r", String.valueOf(nodeData.r()));
        writer.writeAttribute("g", String.valueOf(nodeData.g()));
        writer.writeAttribute("b", String.valueOf(nodeData.b()));
        writer.writeAttribute("a", String.valueOf(nodeData.alpha()));
        writer.writeEndElement();

        writer.writeStartElement(ELEMENT_NODEDATA_SIZE);
        writer.writeAttribute("value", String.valueOf(nodeData.getSize()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    public void readNodeData(XMLStreamReader reader, NodeData nodeData, GraphStructure structure) throws XMLStreamException {
        if (reader.getAttributeValue(null, "id") != null) {
            structure.setNodeId((NodeDataImpl) nodeData, reader.getAttributeValue(null, "id"));
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_NODEDATA_POSITION.equalsIgnoreCase(name)) {
                    nodeData.setX(Float.parseFloat(reader.getAttributeValue(null, "x")));
                    nodeData.setY(Float.parseFloat(reader.getAttributeValue(null, "y")));
                    nodeData.setZ(Float.parseFloat(reader.getAttributeValue(null, "z")));
                } else if (ELEMENT_NODEDATA_COLOR.equalsIgnoreCase(name)) {
                    nodeData.setR(Float.parseFloat(reader.getAttributeValue(null, "r")));
                    nodeData.setG(Float.parseFloat(reader.getAttributeValue(null, "g")));
                    nodeData.setB(Float.parseFloat(reader.getAttributeValue(null, "b")));
                    nodeData.setAlpha(Float.parseFloat(reader.getAttributeValue(null, "a")));
                } else if (ELEMENT_NODEDATA_SIZE.equalsIgnoreCase(name)) {
                    nodeData.setSize(Float.parseFloat(reader.getAttributeValue(0)));
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_NODEDATA.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    public void writeEdgeData(XMLStreamWriter writer, EdgeDataImpl edgeData) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_EDGEDATA);
        AbstractEdge edge = edgeData.getEdge();

        writer.writeAttribute("sourcepre", String.valueOf(edge.getSource().getPre()));
        writer.writeAttribute("targetpre", String.valueOf(edge.getTarget().getPre()));

        if (edgeData.getId() != null && !edgeData.getId().equals("" + edge.getId())) {
            writer.writeAttribute("id", edgeData.getId());
        }

        writer.writeStartElement(ELEMENT_EDGEDATA_COLOR);
        writer.writeAttribute("r", String.valueOf(edgeData.r()));
        writer.writeAttribute("g", String.valueOf(edgeData.g()));
        writer.writeAttribute("b", String.valueOf(edgeData.b()));
        writer.writeAttribute("a", String.valueOf(edgeData.alpha()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    public void readEdgeData(XMLStreamReader reader, EdgeData edgeData, GraphStructure structure) throws XMLStreamException {
        if (reader.getAttributeValue(null, "id") != null) {
            structure.setEdgeId((AbstractEdge) edgeData.getEdge(), reader.getAttributeValue(null, "id"));
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_EDGEDATA_COLOR.equalsIgnoreCase(name)) {
                    edgeData.setR(Float.parseFloat(reader.getAttributeValue(null, "r")));
                    edgeData.setG(Float.parseFloat(reader.getAttributeValue(null, "g")));
                    edgeData.setB(Float.parseFloat(reader.getAttributeValue(null, "b")));
                    edgeData.setAlpha(Float.parseFloat(reader.getAttributeValue(null, "a")));
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_EDGEDATA.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }
}
