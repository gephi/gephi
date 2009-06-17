/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.io.importer.standard;

import java.awt.Color;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.Container;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.importer.FileType;
import org.gephi.io.importer.ImportException;
import org.gephi.io.importer.XMLImporter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterGEXF implements XMLImporter {

    public void importData(Document document, ContainerLoader container) throws ImportException {
        try {
            //XPath
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //Root
            Element root = document.getDocumentElement();

            XPathExpression exp = xpath.compile("./graph/nodes/node[@id and @label]");
            NodeList nodeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);

            exp = xpath.compile("./graph/edges/edge[@source and @target]");
            NodeList edgeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);

            //Nodes
            for (int i = 0; i < nodeListE.getLength(); i++) {
                Element nodeE = (Element) nodeListE.item(i);
                NodeDraft node = container.factory().newNodeDraft();

                //Id
                int nodeId = Integer.parseInt(nodeE.getAttribute("id"));
                node.setId(String.valueOf(nodeId));

                //Parent
                if (!nodeE.getAttribute("pid").isEmpty() && !nodeE.getAttribute("pid").equals("0")) {
                    int parentId = Integer.parseInt(nodeE.getAttribute("pid"));
                    node.setParent(container.getNode(String.valueOf(parentId)));
                    container.addNode(node);
                }
                else
                {
                    container.addNode(node);
                }
                


                //Node properties
                Element nodeColor = (Element) nodeE.getElementsByTagName("viz:color").item(0);
                if (nodeColor != null) {
                    int r = Integer.parseInt(nodeColor.getAttribute("r"));
                    int g = Integer.parseInt(nodeColor.getAttribute("g"));
                    int b = Integer.parseInt(nodeColor.getAttribute("b"));
                    node.setColor(new Color(r, g, b));
                }

                //Node label
                String label = nodeE.getAttribute("label");
                node.setLabel(label);

                //Node position
                Element nodePosition = (Element) nodeE.getElementsByTagName("viz:position").item(0);
                if (nodePosition != null) {
                    node.setX(Float.parseFloat(nodePosition.getAttribute("x")));
                    node.setY(Float.parseFloat(nodePosition.getAttribute("y")));
                    node.setZ(Float.parseFloat(nodePosition.getAttribute("z")));
                }

                //Node size
                Element nodeSize = (Element) nodeE.getElementsByTagName("viz:size").item(0);
                if (nodeSize != null) {
                    node.setSize(Float.parseFloat(nodeSize.getAttribute("value")));
                }
            }


            //Edges
            for (int i = 0; i < edgeListE.getLength(); i++) {
                Element edgeE = (Element) edgeListE.item(i);

                EdgeDraft edge = container.factory().newEdgeDraft();

                //Id
                if (!edgeE.getAttribute("id").isEmpty()) {
                    int edgeId = -1;
                    edgeId = Integer.parseInt(edgeE.getAttribute("id"));
                    edge.setId(String.valueOf(edgeId));
                }

                int edgeSource = Integer.parseInt(edgeE.getAttribute("source"));
                int edgeTarget = Integer.parseInt(edgeE.getAttribute("target"));

                NodeDraft nodeSource = container.getNode(String.valueOf(edgeSource));
                NodeDraft nodeTarget = container.getNode(String.valueOf(edgeTarget));
                if(nodeSource==null || nodeTarget==null)
                {
                    throw new NullPointerException(edgeSource+"  "+edgeTarget);
                }
                edge.setSource(nodeSource);
                edge.setTarget(nodeTarget);

                //Cardinal
                String cardinalStr = edgeE.getAttribute("cardinal");
                if (!cardinalStr.isEmpty()) {
                    float cardinal = Float.parseFloat(cardinalStr);
                    edge.setWeight(cardinal);
                }

                container.addEdge(edge);
            }

        } catch (Exception ex) {
            if (ex instanceof ImportException) {
                throw (ImportException) ex;
            } else {
                throw new ImportException(this, ex);
            }
        }
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gexf", NbBundle.getMessage(getClass(), "fileType_GEXF_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.hasExt("gexf");
    }
}
