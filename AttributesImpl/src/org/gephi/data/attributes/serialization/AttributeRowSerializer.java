/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
                        row.setValue(col, v);
                    }
                    value = "";
                    col = null;
                    break;
            }
        }
    }
}
