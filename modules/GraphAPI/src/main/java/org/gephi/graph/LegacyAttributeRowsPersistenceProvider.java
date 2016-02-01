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
package org.gephi.graph;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 140)
public class LegacyAttributeRowsPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    private static final String ELEMENT_ROWS = "attributerows";
    private static final String ELEMENT_NODE_ROW = "noderow";
    private static final String ELEMENT_EDGE_ROW = "edgerow";
    private static final String ELEMENT_VALUE = "attvalue";

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        GraphModel model = workspace.getLookup().lookup(GraphModel.class);
        if (model == null) {
            throw new IllegalStateException("The graphModel is null");
        }
        LegacyMapHelper helper = workspace.getLookup().lookup(LegacyMapHelper.class);
        if (helper != null) {
            try {
                readRows(reader, model, helper);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            } finally {
                workspace.remove(helper);
            }
        }
    }

    @Override
    public String getIdentifier() {
        return "attributerows";
    }

    public void readRows(XMLStreamReader reader, GraphModel graphModel, LegacyMapHelper mapHelper) throws XMLStreamException {
        Graph graph = graphModel.getGraph();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_NODE_ROW.equalsIgnoreCase(name)) {
                        int id = Integer.parseInt(reader.getAttributeValue(null, "for"));
                        Node node = graph.getNode(id);

                        readRow(reader, node, graphModel.getNodeTable(), mapHelper);
                    } else if (ELEMENT_EDGE_ROW.equalsIgnoreCase(name)) {
                        int id = Integer.parseInt(reader.getAttributeValue(null, "for"));
                        Edge edge = graph.getEdge(id);

                        readRow(reader, edge, graphModel.getEdgeTable(), mapHelper);
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

    public void readRow(XMLStreamReader reader, Element element, Table table, LegacyMapHelper mapHelper) throws XMLStreamException {
        Integer index = null;
        String value = "";

        boolean end = false;
        while (reader.hasNext() && !end) {
            int t = reader.next();

            switch (t) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_VALUE.equalsIgnoreCase(name)) {
                        index = Integer.parseInt(reader.getAttributeValue(null, "index"));
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace() && index != null) {
                        value += reader.getText();
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_NODE_ROW.equalsIgnoreCase(reader.getLocalName()) || ELEMENT_EDGE_ROW.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    if (!value.isEmpty() && index != null) {
                        String id = table.getElementClass().equals(Node.class) ? mapHelper.nodeIndexToIds.get(index) : mapHelper.edgeIndexToIds.get(index);
                        if (id != null) {
                            Column col = table.getColumn(id);

                            if (col != null && !col.isReadOnly()) {
                                try {
                                    Object val = AttributeUtils.parse(value, col.getTypeClass());
                                    element.setAttribute(col, val);
                                } catch (Exception e) {
                                    //Ignore
                                }
                            }
                        }

                    }
                    value = "";
                    index = null;
                    break;
            }
        }
    }

}
