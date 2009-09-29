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
import java.util.HashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gephi.data.properties.EdgeProperties;
import org.gephi.data.properties.NodeProperties;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.importer.FileType;
import org.gephi.io.importer.PropertiesAssociations;
import org.gephi.io.importer.PropertyAssociation;
import org.gephi.io.importer.XMLImporter;
import org.gephi.io.logging.Report;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterGEXF implements XMLImporter, LongTask {

    //Architecture
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    //Settings
    private boolean keepComplexAndEmptyAttributeTypes = true;
    private boolean automaticProperties = true;

    //Attributes
    protected PropertiesAssociations properties = new PropertiesAssociations();
    private HashMap<String, NodeProperties> nodePropertiesAttributes;
    private HashMap<String, EdgeProperties> edgePropertiesAttributes;

    public ImporterGEXF() {
        //Default node associations
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.LABEL, "label"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.X, "x"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.Y, "y"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.Y, "z"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.SIZE, "size"));

        //Default edge associations
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.LABEL, "label"));
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.WEIGHT, "weight"));
    }

    public void importData(Document document, ContainerLoader container, Report report) throws Exception {
        this.container = container;
        this.report = report;
        this.nodePropertiesAttributes = new HashMap<String, NodeProperties>();
        this.edgePropertiesAttributes = new HashMap<String, EdgeProperties>();

        Progress.start(progressTicket);        //Progress

        //Root
        Element root = document.getDocumentElement();

        //XPath
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        XPathExpression exp = xpath.compile("./graph/nodes/node[@id and @label]");
        NodeList nodeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
        if (cancel) {
            return;
        }
        exp = xpath.compile("./graph/edges/edge[@source and @target]");
        NodeList edgeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
        if (cancel) {
            return;
        }
        int taskMax = nodeListE.getLength() + edgeListE.getLength();
        Progress.switchToDeterminate(progressTicket, taskMax);

        //Nodes
        for (int i = 0; i < nodeListE.getLength(); i++) {
            Element nodeE = (Element) nodeListE.item(i);
            NodeDraft node = container.factory().newNodeDraft();

            //Id
            String nodeId = nodeE.getAttribute("id");
            node.setId(nodeId);

            //Parent
            if (!nodeE.getAttribute("pid").isEmpty() && !nodeE.getAttribute("pid").equals("0")) {
                String parentId = nodeE.getAttribute("pid");
                node.setParent(container.getNode(parentId));
                container.addNode(node);
            } else {
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
                String edgeId = "-1";
                edgeId = edgeE.getAttribute("id");
                edge.setId(edgeId);
            }

            String edgeSource = edgeE.getAttribute("source");
            String edgeTarget = edgeE.getAttribute("target");

            NodeDraft nodeSource = container.getNode(edgeSource);
            NodeDraft nodeTarget = container.getNode(edgeTarget);
            if (nodeSource == null || nodeTarget == null) {
                throw new NullPointerException(edgeSource + "  " + edgeTarget);
            }
            edge.setSource(nodeSource);
            edge.setTarget(nodeTarget);

            //Cardinal
            String weightStr = edgeE.getAttribute("weight");
            if (!weightStr.isEmpty()) {
                float cardinal = Float.parseFloat(weightStr);
                edge.setWeight(cardinal);
            }

            container.addEdge(edge);
        }

        Progress.finish(progressTicket);

        //Clean
        this.container = null;
        this.progressTicket = null;
        this.report = null;
        this.nodePropertiesAttributes = null;
        this.edgePropertiesAttributes = null;
    }

    private void setNodeData(Element dataE, NodeDraft nodeDraft, String nodeId) {
        
    }

    private void setEdgeData(Element dataE, EdgeDraft edgeDraft, String edgeId) {
        
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gexf", NbBundle.getMessage(getClass(), "fileType_GEXF_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.hasExt("gexf");
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
