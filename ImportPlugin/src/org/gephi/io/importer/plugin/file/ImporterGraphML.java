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
package org.gephi.io.importer.plugin.file;

import java.io.Reader;
import java.util.HashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterGraphML implements FileImporter, LongTask {

    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Settings
    private final boolean keepComplexAndEmptyAttributeTypes = true;
    private final boolean automaticProperties = true;
    //Attributes
    protected PropertiesAssociations properties = new PropertiesAssociations();
    private HashMap<String, NodeProperties> nodePropertiesAttributes = new HashMap<String, NodeProperties>();
    private HashMap<String, EdgeProperties> edgePropertiesAttributes = new HashMap<String, EdgeProperties>();

    public ImporterGraphML() {
        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "d3");  // Default node label used by yEd from yworks.com.
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.Z, "z");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "edgelabel");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "d7");  // Default edge label used by yEd from yworks.com.
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "edgeid");
    }

    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        Document document = ImportUtils.getXMLDocument(reader);
        try {
            importData(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return !cancel;
    }

    private void importData(Document document) throws Exception {
        Progress.start(progressTicket);        //Progress

        //Root
        Element root = document.getDocumentElement();
        if (!root.getTagName().equalsIgnoreCase("graphml")) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_syntax1"), Issue.Level.SEVERE));
            return;
        }

        //XPath
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        //Keys - NODE
        XPathExpression exp = xpath.compile("//key[@id and @for=\"node\"]");
        NodeList keyNodeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);

        //Keys - EDGE
        exp = xpath.compile("//key[@id and @for=\"edge\"]");
        NodeList keyEdgeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);

        //Nodes
        exp = xpath.compile("//node[@id]");
        NodeList nodeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
        if (cancel) {
            return;
        }
        //Edges
        exp = xpath.compile("//edge[@source and @target]");
        NodeList edgeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
        if (cancel) {
            return;
        }

        //Edge Default
        NodeList graphList = root.getChildNodes();
        for (int i = 0; i < graphList.getLength(); i++) {
            if (graphList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element itemE = (Element) graphList.item(i);
                if (itemE.getTagName().equalsIgnoreCase("graph")) {
                    if (itemE.hasAttribute("edgedefault")) {
                        String type = itemE.getAttribute("edgedefault");
                        if (type.equalsIgnoreCase("directed")) {
                            container.setEdgeDefault(EdgeDefault.DIRECTED);
                        } else if (type.equalsIgnoreCase("undirected")) {
                            container.setEdgeDefault(EdgeDefault.UNDIRECTED);
                        }
                    }
                }
            }
        }

        int taskMax = keyNodeListE.getLength() + keyEdgeListE.getLength() + nodeListE.getLength() + edgeListE.getLength();
        Progress.switchToDeterminate(progressTicket, taskMax);

        //Attributes keys
        getAttributesKeys(keyNodeListE);
        getAttributesKeys(keyEdgeListE);

        //Nodes
        for (int i = 0; i < nodeListE.getLength() && !cancel; i++) {
            Element nodeE = (Element) nodeListE.item(i);

            Progress.progress(progressTicket);

            String nodeId = nodeE.getAttribute("id");
            if (nodeId.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_nodeid"), Issue.Level.SEVERE));
                continue;
            }

            //Create node
            NodeDraft node = container.factory().newNodeDraft();
            node.setId(nodeId);

            //Label?
            String nodeLabel = nodeE.getAttribute("label");
            if (!nodeLabel.isEmpty()) {
                node.setLabel(nodeLabel);
            }

            //Parent
            if (nodeE.getParentNode().getNodeName().equalsIgnoreCase("graph")) {
                if (nodeE.getParentNode().getParentNode().getNodeName().equalsIgnoreCase("node")) {
                    //Nested
                    Element parentE = (Element) nodeE.getParentNode().getParentNode();
                    String parentId = parentE.getAttribute("id");
                    if (parentId.isEmpty()) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_nodeid"), Issue.Level.SEVERE));
                        continue;
                    }
                    NodeDraft parent = container.getNode(parentId);
                    if (parent != null) {
                        node.setParent(parent);
                    }
                }
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_syntax2", nodeId), Issue.Level.SEVERE));
                continue;
            }

            //Get Data child nodes, avoiding using descendants
            Node child = nodeE.getFirstChild();
            if (child != null) {
                do {
                    if (child.getNodeName().equalsIgnoreCase("data")) {
                        Element dataE = (Element) child;
                        setNodeData(dataE, node, nodeId);
                    }
                } while ((child = child.getNextSibling()) != null);
            }

            //Append node
            container.addNode(node);
        }

        //Edge
        for (int i = 0; i < edgeListE.getLength() && !cancel; i++) {
            Element edgeE = (Element) edgeListE.item(i);

            Progress.progress(progressTicket);

            //Create edge
            EdgeDraft edge = container.factory().newEdgeDraft();


            //Source & Target
            String sourceStr = edgeE.getAttribute("source");
            if (sourceStr.isEmpty()) {
                continue;
            }
            NodeDraft nodeSource = container.getNode(sourceStr);
            edge.setSource(nodeSource);
            String targetStr = edgeE.getAttribute("target");
            if (targetStr.isEmpty()) {
                continue;
            }
            NodeDraft nodeTarget = container.getNode(targetStr);
            edge.setTarget(nodeTarget);

            //Id
            String idStr = edgeE.getAttribute("id");
            if (!idStr.isEmpty()) {
                edge.setId(idStr);
            }

            //Label?
            String labelStr = edgeE.getAttribute("label");
            if (!labelStr.isEmpty()) {
                edge.setLabel(labelStr);
            }

            //Oriented
            if (edgeE.hasAttribute("directed")) {
                edge.setType(Boolean.parseBoolean(edgeE.getAttribute("directed")) ? EdgeDraft.EdgeType.DIRECTED : EdgeDraft.EdgeType.UNDIRECTED);
            } else {
                //Try to find default
                if (edgeE.getParentNode().getNodeName().equalsIgnoreCase("graph")) {
                    Element graphE = (Element) edgeE.getParentNode();
                    if (graphE.hasAttribute("edgedefault")) {
                        String type = graphE.getAttribute("edgedefault");
                        if (type.equalsIgnoreCase("directed")) {
                            edge.setType(EdgeDraft.EdgeType.DIRECTED);
                        } else if (type.equalsIgnoreCase("undirected")) {
                            edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
                        }
                    }
                }
            }

            //Get Data child nodes, avoiding using descendants
            Node child = edgeE.getFirstChild();
            if (child != null) {
                do {
                    if (child.getNodeName().equalsIgnoreCase("data")) {
                        Element dataE = (Element) child;
                        setEdgeData(dataE, edge, idStr);
                    }
                } while ((child = child.getNextSibling()) != null);
            }

            //Append
            container.addEdge(edge);
        }

        Progress.finish(progressTicket);
    }

    private void getAttributesKeys(NodeList keyListE) {

        //Keys
        for (int i = 0; i < keyListE.getLength() && !cancel; i++) {
            Element keyE = (Element) keyListE.item(i);

            Progress.progress(progressTicket);

            //Id & Name
            String keyId = keyE.getAttribute("id");
            String keyName = keyE.getAttribute("attr.name");
            if (keyName.isEmpty()) {
                keyName = keyId;
            }

            //For
            String keyFor = keyE.getAttribute("for");
            if (keyFor.isEmpty() || (!keyFor.equalsIgnoreCase("node") && !keyFor.equalsIgnoreCase("edge"))) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributefor", keyName), Issue.Level.SEVERE));
                continue;
            }

            //Try to see if the key is a node/edge property
            if (automaticProperties) {
                if (keyFor.equalsIgnoreCase("node")) {
                    NodeProperties prop = properties.getNodeProperty(keyName);
                    if (prop != null) {
                        nodePropertiesAttributes.put(keyId, prop);
                        report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_nodeproperty", keyName));
                        continue;
                    }
                } else if (keyFor.equalsIgnoreCase("edge")) {
                    EdgeProperties prop = properties.getEdgeProperty(keyName);
                    if (prop != null) {
                        edgePropertiesAttributes.put(keyId, prop);
                        report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_edgeproperty", keyName));
                        continue;
                    }
                }
            }

            //Type
            String keyType = keyE.getAttribute("attr.type");
            AttributeType attributeType = AttributeType.STRING;
            if (keyType.equalsIgnoreCase("boolean")) {
                attributeType = AttributeType.BOOLEAN;
            } else if (keyType.equalsIgnoreCase("int")) {
                attributeType = AttributeType.INT;
            } else if (keyType.equalsIgnoreCase("long")) {
                attributeType = AttributeType.LONG;
            } else if (keyType.equalsIgnoreCase("float")) {
                attributeType = AttributeType.FLOAT;
            } else if (keyType.equalsIgnoreCase("double")) {
                attributeType = AttributeType.DOUBLE;
            } else if (keyType.equalsIgnoreCase("string")) {
                attributeType = AttributeType.STRING;
            } else {
                if (keepComplexAndEmptyAttributeTypes) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributetype1", keyName), Issue.Level.WARNING));
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributetype2", keyName), Issue.Level.WARNING));
                    continue;
                }
            }

            //Default
            NodeList defaultList = keyE.getElementsByTagName("default");
            Object defaultValue = null;
            if (defaultList.getLength() > 0) {
                Element defaultE = (Element) defaultList.item(0);
                String defaultValueStr = defaultE.getTextContent();
                try {
                    defaultValue = attributeType.parse(defaultValueStr);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributedefault", keyName, attributeType.getTypeString()), Issue.Level.SEVERE));
                }
            }

            //Add as attribute
            if (keyFor.equalsIgnoreCase("node")) {
                AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
                nodeClass.addColumn(keyId, keyName, attributeType, AttributeOrigin.DATA, defaultValue);
                report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_nodeattribute", keyName, attributeType.getTypeString()));
            } else if (keyFor.equalsIgnoreCase("edge")) {
                AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
                edgeClass.addColumn(keyId, keyName, attributeType, AttributeOrigin.DATA, defaultValue);
                report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_edgeattribute", keyName, attributeType.getTypeString()));
            }
        }
    }

    private void setNodeData(Element dataE, NodeDraft nodeDraft, String nodeId) {
        //Key
        String dataKey = dataE.getAttribute("key");
        if (dataKey.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datakey", nodeDraft), Issue.Level.SEVERE));
            return;
        }

        String dataValue = dataE.getTextContent();
        if (!dataValue.isEmpty()) {
            //Look for a property datakey
            NodeProperties prop = nodePropertiesAttributes.get(dataKey);
            if (prop != null) {
                try {
                    switch (prop) {
                        case X:
                            nodeDraft.setX(parseFloat(dataValue));
                            break;
                        case Y:
                            nodeDraft.setY(parseFloat(dataValue));
                            break;
                        case Z:
                            nodeDraft.setZ(parseFloat(dataValue));
                            break;
                        case SIZE:
                            nodeDraft.setSize(parseFloat(dataValue));
                            break;
                        case LABEL:
                            nodeDraft.setLabel(dataValue);
                            break;
                    }
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue1", dataKey, nodeId, prop.toString()), Issue.Level.SEVERE));
                }
                return;
            }

            //Data attribute value
            AttributeColumn column = container.getAttributeModel().getNodeTable().getColumn(dataKey);
            if (column != null) {
                try {
                    Object value = column.getType().parse(dataValue);
                    nodeDraft.addAttributeValue(column, value);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue2", dataKey, nodeId, column.getTitle()), Issue.Level.SEVERE));
                }
            }
        }
    }

    private float parseFloat(String str) {
        str = str.replace(',', '.');
        return Float.parseFloat(str);
    }

    private void setEdgeData(Element dataE, EdgeDraft edgeDraft, String edgeId) {
        //Key
        String dataKey = dataE.getAttribute("key");
        if (dataKey.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datakey", edgeId), Issue.Level.SEVERE));
            return;
        }

        String dataValue = dataE.getTextContent();
        if (!dataValue.isEmpty()) {
            //Look for a property datakey
            EdgeProperties prop = edgePropertiesAttributes.get(dataKey);
            if (prop != null) {
                try {
                    switch (prop) {
                        case WEIGHT:
                            edgeDraft.setWeight(parseFloat(dataValue));
                            break;
                        case LABEL:
                            edgeDraft.setLabel(dataValue);
                            break;
                        case ID:
                            edgeDraft.setId(dataValue);
                            break;
                    }
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue1", dataKey, edgeId, prop.toString()), Issue.Level.SEVERE));
                }
                return;
            }

            //Data attribute value
            AttributeColumn column = container.getAttributeModel().getEdgeTable().getColumn(dataKey);
            if (column != null) {
                try {
                    Object value = column.getType().parse(dataValue);
                    edgeDraft.addAttributeValue(column, value);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue2", dataKey, edgeId, column.getTitle()), Issue.Level.SEVERE));
                }
            }
        }
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public ContainerLoader getContainer() {
        return container;
    }

    public Report getReport() {
        return report;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
