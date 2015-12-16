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
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 120)
public class LegacyDataPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    private static final String ELEMENT_DATA = "Data";
    private static final String ELEMENT_NODEDATA = "nodedata";
    private static final String ELEMENT_NODEDATA_POSITION = "position";
    private static final String ELEMENT_NODEDATA_COLOR = "color";
    private static final String ELEMENT_NODEDATA_SIZE = "size";
    private static final String ELEMENT_EDGEDATA = "edgedata";
    private static final String ELEMENT_EDGEDATA_COLOR = "color";

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
                readData(reader, model, helper);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public String getIdentifier() {
        return "Data";
    }

    public void readData(XMLStreamReader reader, GraphModel graphModel, LegacyMapHelper helper) throws XMLStreamException {
        Graph graph = graphModel.getGraph();

        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_NODEDATA.equalsIgnoreCase(name)) {
                    Node node = graph.getNode(helper.preToIdMap.get(Integer.parseInt(reader.getAttributeValue(null, "nodepre"))));
                    readNodeData(reader, node);
                } else if (ELEMENT_EDGEDATA.equalsIgnoreCase(name)) {
                    Node source = graph.getNode(helper.preToIdMap.get(Integer.parseInt(reader.getAttributeValue(null, "sourcepre"))));
                    Node target = graph.getNode(helper.preToIdMap.get(Integer.parseInt(reader.getAttributeValue(null, "targetpre"))));
                    Edge edge = graph.getEdge(source, target, 0);
                    readEdgeData(reader, edge);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_DATA.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    public void readNodeData(XMLStreamReader reader, Node node) throws XMLStreamException {

        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_NODEDATA_POSITION.equalsIgnoreCase(name)) {
                    node.setX(Float.parseFloat(reader.getAttributeValue(null, "x")));
                    node.setY(Float.parseFloat(reader.getAttributeValue(null, "y")));
                    node.setZ(Float.parseFloat(reader.getAttributeValue(null, "z")));
                } else if (ELEMENT_NODEDATA_COLOR.equalsIgnoreCase(name)) {
                    node.setR(Float.parseFloat(reader.getAttributeValue(null, "r")));
                    node.setG(Float.parseFloat(reader.getAttributeValue(null, "g")));
                    node.setB(Float.parseFloat(reader.getAttributeValue(null, "b")));
                    node.setAlpha(Float.parseFloat(reader.getAttributeValue(null, "a")));
                } else if (ELEMENT_NODEDATA_SIZE.equalsIgnoreCase(name)) {
                    node.setSize(Float.parseFloat(reader.getAttributeValue(0)));
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_NODEDATA.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    public void readEdgeData(XMLStreamReader reader, Edge edge) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_EDGEDATA_COLOR.equalsIgnoreCase(name)) {
                    float r = Float.parseFloat(reader.getAttributeValue(null, "r"));
                    float g = Float.parseFloat(reader.getAttributeValue(null, "g"));
                    float b = Float.parseFloat(reader.getAttributeValue(null, "b"));
                    float alpha = Float.parseFloat(reader.getAttributeValue(null, "a"));
                    
                    //Old gephi versions stored r = -1 to indicate that the edge had no color (use the node color)
                    //Since Gephi 0.9, we do that with alpha = 0
                    if(r < 0 || g < 0 || b < 0){
                        r = g = b = 0;
                        alpha = 0;
                    }
                    
                    edge.setR(r);
                    edge.setG(g);
                    edge.setB(b);
                    edge.setAlpha(alpha);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_EDGEDATA.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }
}
