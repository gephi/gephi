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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TextData;
import org.gephi.visualization.opengl.text.TextDataImpl;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

    public Element writeXML(Document document, Workspace workspace) {
        Element textDataE = document.createElement(ELEMENT_TEXTDATA);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();
        for (Node node : hierarchicalGraph.getNodesTree()) {
            TextData nodeTextData = node.getNodeData().getTextData();
            if (nodeTextData != null) {
                Element nodeE = document.createElement(ELEMENT_NODEDATA_TEXTDATA);
                nodeE.setAttribute("for", String.valueOf(node.getId()));
                if (nodeTextData.getR() > 0) {
                    Element colorE = document.createElement(ELEMENT_TEXTDATA_COLOR);
                    colorE.setAttribute("r", String.valueOf(nodeTextData.getR()));
                    colorE.setAttribute("g", String.valueOf(nodeTextData.getG()));
                    colorE.setAttribute("b", String.valueOf(nodeTextData.getB()));
                    colorE.setAttribute("a", String.valueOf(nodeTextData.getAlpha()));
                    nodeE.appendChild(colorE);
                }
                Element sizeE = document.createElement(ELEMENT_TEXTDATA_SIZE);
                sizeE.setAttribute("value", String.valueOf(nodeTextData.getSize()));
                nodeE.appendChild(sizeE);

                Element visibleE = document.createElement(ELEMENT_TEXTDATA_VISIBLE);
                visibleE.setAttribute("value", String.valueOf(nodeTextData.isVisible()));
                nodeE.appendChild(visibleE);

                textDataE.appendChild(nodeE);
            }
        }

        for (Node node : hierarchicalGraph.getNodesTree()) {
            for (Edge edge : hierarchicalGraph.getEdges(node)) {
                TextData edgeTextData = edge.getEdgeData().getTextData();
                if (edgeTextData != null) {
                    Element edgeE = document.createElement(ELEMENT_EDGEDATA_TEXTDATA);
                    edgeE.setAttribute("for", String.valueOf(edge.getId()));
                    if (edgeTextData.getR() > 0) {
                        Element colorE = document.createElement(ELEMENT_TEXTDATA_COLOR);
                        colorE.setAttribute("r", String.valueOf(edgeTextData.getR()));
                        colorE.setAttribute("g", String.valueOf(edgeTextData.getG()));
                        colorE.setAttribute("b", String.valueOf(edgeTextData.getB()));
                        colorE.setAttribute("a", String.valueOf(edgeTextData.getAlpha()));
                        edgeE.appendChild(colorE);
                    }
                    Element sizeE = document.createElement(ELEMENT_TEXTDATA_SIZE);
                    sizeE.setAttribute("value", String.valueOf(edgeTextData.getSize()));
                    edgeE.appendChild(sizeE);

                    Element visibleE = document.createElement(ELEMENT_TEXTDATA_VISIBLE);
                    visibleE.setAttribute("value", String.valueOf(edgeTextData.isVisible()));
                    edgeE.appendChild(visibleE);

                    textDataE.appendChild(edgeE);
                }
            }
        }
        return textDataE;
    }

    public void readXML(Element textDataE, Workspace workspace) {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        HierarchicalGraph hierarchicalGraph = graphModel.getHierarchicalGraph();

        NodeList textDataList = textDataE.getChildNodes();
        for (int i = 0; i < textDataList.getLength(); i++) {
            if (textDataList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element itemE = (Element) textDataList.item(i);
                if (itemE.getTagName().equals(ELEMENT_NODEDATA_TEXTDATA)) {
                    int id = Integer.parseInt(itemE.getAttribute("for"));
                    Node node = hierarchicalGraph.getNode(id);
                    TextDataImpl textDataImpl = (TextDataImpl) node.getNodeData().getTextData();
                    readTextData(itemE, textDataImpl);
                } else if (itemE.getTagName().equals(ELEMENT_EDGEDATA_TEXTDATA)) {
                    int id = Integer.parseInt(itemE.getAttribute("for"));
                    Edge edge = hierarchicalGraph.getEdge(id);
                    TextDataImpl textDataImpl = (TextDataImpl) edge.getEdgeData().getTextData();
                    readTextData(itemE, textDataImpl);
                }
            }
        }
    }

    private void readTextData(Element textDataE, TextData textData) {
        NodeList textDataList = textDataE.getChildNodes();
        for (int i = 0; i < textDataList.getLength(); i++) {
            if (textDataList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element itemE = (Element) textDataList.item(i);
                if (itemE.getTagName().equals(ELEMENT_TEXTDATA_COLOR)) {
                    float r = Float.parseFloat(itemE.getAttribute("r"));
                    float g = Float.parseFloat(itemE.getAttribute("g"));
                    float b = Float.parseFloat(itemE.getAttribute("b"));
                    float a = Float.parseFloat(itemE.getAttribute("a"));
                    textData.setColor(r, g, b, a);
                } else if (itemE.getTagName().equals(ELEMENT_TEXTDATA_SIZE)) {
                    textData.setSize(Float.parseFloat(itemE.getAttribute("value")));
                } else if (itemE.getTagName().equals(ELEMENT_TEXTDATA_VISIBLE)) {
                    textData.setVisible(Boolean.parseBoolean(itemE.getAttribute("value")));
                }
            }
        }
    }

    public String getIdentifier() {
        return ELEMENT_TEXTDATA;
    }
}
