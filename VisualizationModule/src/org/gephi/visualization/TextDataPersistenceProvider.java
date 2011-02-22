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
package org.gephi.visualization;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TextData;
import org.gephi.visualization.impl.TextDataImpl;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class TextDataPersistenceProvider implements WorkspacePersistenceProvider {

    private static final String ELEMENT_TEXTDATA = "textdata";
    private static final String ELEMENT_NODEDATA_TEXTDATA = "textdatanode";
    private static final String ELEMENT_EDGEDATA_TEXTDATA = "textdataedge";
    private static final String ELEMENT_TEXTDATA_COLOR = "color";
    private static final String ELEMENT_TEXTDATA_SIZE = "size";
    private static final String ELEMENT_TEXTDATA_VISIBLE = "visible";

    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        try {
            writer.writeStartElement(ELEMENT_TEXTDATA);
            GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
            HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();
            for (Node node : hierarchicalGraph.getNodesTree()) {
                TextData nodeTextData = node.getNodeData().getTextData();
                if (nodeTextData != null) {
                    writer.writeStartElement(ELEMENT_NODEDATA_TEXTDATA);
                    writer.writeAttribute("for", String.valueOf(node.getId()));
                    if (nodeTextData.getR() > 0) {
                        writer.writeStartElement(ELEMENT_TEXTDATA_COLOR);
                        writer.writeAttribute("r", String.valueOf(nodeTextData.getR()));
                        writer.writeAttribute("g", String.valueOf(nodeTextData.getG()));
                        writer.writeAttribute("b", String.valueOf(nodeTextData.getB()));
                        writer.writeAttribute("a", String.valueOf(nodeTextData.getAlpha()));
                        writer.writeEndElement();
                    }
                    writer.writeStartElement(ELEMENT_TEXTDATA_SIZE);
                    writer.writeAttribute("value", String.valueOf(nodeTextData.getSize()));
                    writer.writeEndElement();
                    writer.writeStartElement(ELEMENT_TEXTDATA_VISIBLE);
                    writer.writeAttribute("value", String.valueOf(nodeTextData.isVisible()));
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
            }
            for (Node node : hierarchicalGraph.getNodesTree()) {
                for (Edge edge : hierarchicalGraph.getEdges(node)) {
                    TextData edgeTextData = edge.getEdgeData().getTextData();
                    if (edgeTextData != null) {
                        writer.writeStartElement(ELEMENT_EDGEDATA_TEXTDATA);
                        writer.writeAttribute("for", String.valueOf(edge.getId()));
                        if (edgeTextData.getR() > 0) {
                            writer.writeStartElement(ELEMENT_TEXTDATA_COLOR);
                            writer.writeAttribute("r", String.valueOf(edgeTextData.getR()));
                            writer.writeAttribute("g", String.valueOf(edgeTextData.getG()));
                            writer.writeAttribute("b", String.valueOf(edgeTextData.getB()));
                            writer.writeAttribute("a", String.valueOf(edgeTextData.getAlpha()));
                            writer.writeEndElement();
                        }
                        writer.writeStartElement(ELEMENT_TEXTDATA_SIZE);
                        writer.writeAttribute("value", String.valueOf(edgeTextData.getSize()));
                        writer.writeEndElement();
                        writer.writeStartElement(ELEMENT_TEXTDATA_VISIBLE);
                        writer.writeAttribute("value", String.valueOf(edgeTextData.isVisible()));
                        writer.writeEndElement();
                        writer.writeEndElement();
                    }
                }
            }
            writer.writeEndElement();
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) {
        try {
            GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
            HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();
            boolean end = false;
            while (reader.hasNext() && !end) {
                int type = reader.next();
                switch (type) {
                    case XMLStreamReader.START_ELEMENT:
                        String name = reader.getLocalName();
                        if (ELEMENT_NODEDATA_TEXTDATA.equalsIgnoreCase(name)) {
                            int id = Integer.parseInt(reader.getAttributeValue(null, "for"));
                            Node node = hierarchicalGraph.getNode(id);
                            TextDataImpl textDataImpl = (TextDataImpl) node.getNodeData().getTextData();
                            readTextData(reader, textDataImpl);
                        } else if (ELEMENT_EDGEDATA_TEXTDATA.equalsIgnoreCase(name)) {
                            int id = Integer.parseInt(reader.getAttributeValue(null, "for"));
                            Edge edge = hierarchicalGraph.getEdge(id);
                            TextDataImpl textDataImpl = (TextDataImpl) edge.getEdgeData().getTextData();
                            readTextData(reader, textDataImpl);
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        if (ELEMENT_TEXTDATA.equalsIgnoreCase(reader.getLocalName())) {
                            end = true;
                        }
                        break;
                }
            }
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void readTextData(XMLStreamReader reader, TextData textData) throws XMLStreamException {

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_TEXTDATA_COLOR.equalsIgnoreCase(name)) {
                        float r = Float.parseFloat(reader.getAttributeValue(null, "r"));
                        float g = Float.parseFloat(reader.getAttributeValue(null, "g"));
                        float b = Float.parseFloat(reader.getAttributeValue(null, "b"));
                        float a = Float.parseFloat(reader.getAttributeValue(null, "a"));
                        textData.setColor(r, g, b, a);
                    } else if (ELEMENT_TEXTDATA_SIZE.equalsIgnoreCase(name)) {
                        textData.setSize(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if (ELEMENT_TEXTDATA_VISIBLE.equalsIgnoreCase(name)) {
                        textData.setVisible(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_EDGEDATA_TEXTDATA.equalsIgnoreCase(reader.getLocalName())||ELEMENT_NODEDATA_TEXTDATA.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public String getIdentifier() {
        return ELEMENT_TEXTDATA;
    }
}
