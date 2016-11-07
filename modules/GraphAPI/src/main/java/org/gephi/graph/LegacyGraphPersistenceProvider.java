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
import javax.xml.stream.events.XMLEvent;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 110)
public class LegacyGraphPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    private static final String ELEMENT_DHNS = "Dhns";
    private static final String ELEMENT_EDGES = "Edges";
    private static final String ELEMENT_TREESTRUCTURE = "TreeStructure";
    private static final String ELEMENT_TREESTRUCTURE_NODE = "Node";

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        GraphModel model = LegacyMapHelper.getGraphModel(workspace);
        LegacyMapHelper mapHelper = LegacyMapHelper.get(workspace);
        try {
            readDhns(reader, model, mapHelper);
            workspace.add(model);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "Dhns";
    }

    private void readDhns(XMLStreamReader reader, GraphModel graphModel, LegacyMapHelper mapHelper) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_TREESTRUCTURE.equalsIgnoreCase(name)) {
                    readTreeStructure(reader, graphModel, graphModel.factory(), mapHelper);
                } else if (ELEMENT_EDGES.equalsIgnoreCase(name)) {
                    readEdges(reader, graphModel, graphModel.factory(), mapHelper);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_DHNS.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    public void readTreeStructure(XMLStreamReader reader, GraphModel graphModel, GraphFactory factory, LegacyMapHelper mapHelper) throws XMLStreamException {
        Graph graph = graphModel.getGraph();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_TREESTRUCTURE_NODE.equalsIgnoreCase(name)) {
                        String id = reader.getAttributeValue(null, "id");
                        String pre = reader.getAttributeValue(null, "pre");
                        mapHelper.preToIdMap.put(pre, id);

                        Node node = factory.newNode(id);
                        graph.addNode(node);
                    }
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_TREESTRUCTURE.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void readEdges(XMLStreamReader reader, GraphModel graphModel, GraphFactory factory, LegacyMapHelper mapHelper) throws XMLStreamException {
        Graph graph = graphModel.getGraph();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String source = null;
                    String target = null;
                    String id = null;
                    Boolean directed = false;
                    Float weight = 0f;

                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String attName = reader.getAttributeName(i).getLocalPart();
                        if ("id".equalsIgnoreCase(attName)) {
                            id = reader.getAttributeValue(i);
                        } else if ("source".equalsIgnoreCase(attName)) {
                            source = reader.getAttributeValue(i);
                        } else if ("target".equalsIgnoreCase(attName)) {
                            target = reader.getAttributeValue(i);
                        } else if ("directed".equalsIgnoreCase(attName)) {
                            directed = Boolean.parseBoolean(reader.getAttributeValue(i));
                        } else if ("weight".equalsIgnoreCase(attName)) {
                            weight = Float.parseFloat(reader.getAttributeValue(i));
                        }
                    }

                    if (source == null || target == null || id == null) {
                        throw new IllegalArgumentException("source, target or id cannot be null");
                    }

                    Node srcNode = graph.getNode(mapHelper.preToIdMap.get(source));
                    Node destNode = graph.getNode(mapHelper.preToIdMap.get(target));

                    Edge edge = factory.newEdge(id, srcNode, destNode, 0, weight.doubleValue(), directed);
                    graph.addEdge(edge);

                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_EDGES.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }
}
