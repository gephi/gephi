/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Sebastien Heymann
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.StringList;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.PropertiesAssociations;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileFormatImporter;
import org.gephi.io.importer.spi.XMLImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
@ServiceProvider(service = FileFormatImporter.class)
public class ImporterGEXF implements XMLImporter, LongTask {

    //Architecture
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    //Settings
    private boolean keepComplexAndEmptyAttributeTypes = true;
    private boolean isDynamicMode = false;
    private boolean isIdTypeInteger = false;
    private boolean isDateTypeInteger = false;
    //Attributes
    protected PropertiesAssociations properties = new PropertiesAssociations();
    private HashMap<String, NodeProperties> nodePropertiesAttributes;
    private HashMap<String, EdgeProperties> edgePropertiesAttributes;
    //Attributes options
    private HashMap<String, StringList> optionsAttributes;
    //Unknown parents nodes
    private ConcurrentHashMap<String, List<String>> unknownParents; // <parentId, ChildIdList>

    public ImporterGEXF() {
        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.Y, "z");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
    }

    public boolean importData(Document document, ContainerLoader container, Report report) throws Exception {
        this.container = container;
        this.report = report;
        this.nodePropertiesAttributes = new HashMap<String, NodeProperties>();
        this.edgePropertiesAttributes = new HashMap<String, EdgeProperties>();
        this.optionsAttributes = new HashMap<String, StringList>();

        try {
            importData(document);
        } catch (Exception e) {
            clean();
            throw e;
        }
        boolean result = !cancel;
        clean();
        return result;
    }

    private void clean() {
        //Clean
        this.container = null;
        this.progressTicket = null;
        this.report = null;
        this.nodePropertiesAttributes = null;
        this.edgePropertiesAttributes = null;
        this.optionsAttributes = null;
        this.cancel = false;
        this.isDynamicMode = false;
        this.isDateTypeInteger = false;
        this.unknownParents = null;
    }

    private void importData(Document document) throws Exception {
        Progress.start(progressTicket);        //Progress

        //Root
        Element root = document.getDocumentElement();

        //Version
        String version = root.getAttribute("version");
        if (version.isEmpty() || version.equals("1.0")) {
            ImporterGEXF10 importer = new ImporterGEXF10();
            importer.importData(document, container, report);
        } else {
            //XPath
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression exp = xpath.compile("./graph/attributes[@class]/attribute[@id and normalize-space(@title) and normalize-space(@type)]");
            NodeList columnListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (cancel) {
                return;
            }

            exp = xpath.compile("./graph/nodes/node[@id and normalize-space(@label)]");
            NodeList nodeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (cancel) {
                return;
            }
            exp = xpath.compile("./graph/edges/edge[@source and @target]");
            NodeList edgeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (cancel) {
                return;
            }

            // Progress steps
            int taskMax = nodeListE.getLength() + edgeListE.getLength();
            Progress.switchToDeterminate(progressTicket, taskMax);

            //Parsing mode
            exp = xpath.compile("./graph[@mode]");
            NodeList modeE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (modeE != null && modeE.getLength() > 0) {
                String mode = ((Element) modeE.item(0)).getAttribute("type");
                if (mode.equals("dynamic")) {
                    isDynamicMode = true;

                    //Date type
                    exp = xpath.compile("./graph[@datetype]");
                    NodeList datetypeE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
                    if (datetypeE != null && datetypeE.getLength() > 0) {
                        String datetype = ((Element) modeE.item(0)).getAttribute("datetype");
                        if (datetype.equals("integer")) {
                            isDateTypeInteger = true;
                        } else if (!datetype.isEmpty() && !datetype.equals("date")) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_parsingdatetype", datetype), Issue.Level.SEVERE));
                        }
                    }

                    //Graph date from
                    exp = xpath.compile("./graph[@datefrom]");
                    NodeList datefromE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
                    if (datefromE != null && datefromE.getLength() > 0) {
                        String datefrom = ((Element) modeE.item(0)).getAttribute("datefrom");
                        // TODO Graph date from
                    }

                    //Graph date to
                    exp = xpath.compile("./graph[@dateto]");
                    NodeList datetoE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
                    if (datetoE != null && datetoE.getLength() > 0) {
                        String dateto = ((Element) modeE.item(0)).getAttribute("dateto");
                        // TODO Graph date to
                    }
                } else if (!mode.isEmpty() && !mode.equals("static")) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_parsingmode", mode), Issue.Level.SEVERE));
                }
            }
            if (cancel) {
                return;
            }

            //Id type
            exp = xpath.compile("./graph[@idtype]");
            NodeList idtypeE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (idtypeE != null && idtypeE.getLength() > 0) {
                String idtype = ((Element) idtypeE.item(0)).getAttribute("idtype");
                if (idtype.equals("integer")) {
                    isIdTypeInteger = true;
                }
            }

            //Default edge type
            exp = xpath.compile("./graph[@defaultedgetype]");
            NodeList edgeTypeE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (edgeTypeE != null && edgeTypeE.getLength() > 0) {
                String defaultEdgeType = ((Element) edgeTypeE.item(0)).getAttribute("defaultedgetype");

                if (defaultEdgeType.equals("undirected")) {
                    container.setEdgeDefault(EdgeDefault.UNDIRECTED);
                } else if (defaultEdgeType.equals("directed")) {
                    container.setEdgeDefault(EdgeDefault.DIRECTED);
                } else if (defaultEdgeType.equals("double")) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgedouble"), Issue.Level.WARNING));
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_defaultedgetype", defaultEdgeType), Issue.Level.SEVERE));
                }
            }
            if (cancel) {
                return;
            }

            //Attributes columns
            setAttributesColumns(columnListE);

            //Nodes
            unknownParents = new ConcurrentHashMap<String, List<String>>();
            parseNodes(nodeListE, null);

            //Unknown parents
            setUnknownParents();

            //Edges
            parseEdges(edgeListE, null);
        }

        Progress.finish(progressTicket);
    }

    private void parseNodes(NodeList nodeListE, String parent) throws Exception {
        for (int i = 0; i < nodeListE.getLength(); i++) {
            Element nodeE = (Element) nodeListE.item(i);

            if (!nodeE.getNodeName().equals("node")) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_notnode", nodeE.getNodeName()), Issue.Level.WARNING));
                return;
            }

            //highest nodes are used to calculate the progress
            if (parent == null) {
                Progress.progress(progressTicket);
            }
            if (cancel) {
                return;
            }

            //Id
            String nodeId = nodeE.getAttribute("id");
            if (nodeId.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeid"), Issue.Level.SEVERE));
                continue;
            }

            //Create node
            NodeDraft node = container.factory().newNodeDraft();
            node.setId(nodeId);

            //Parent
            if (!nodeE.getAttribute("pid").isEmpty() && !nodeE.getAttribute("pid").equals("0")) {
                String pid = nodeE.getAttribute("pid");

                // parent node unknown, maybe declared after
                if (!container.nodeExists(pid)) {
                    if (unknownParents.containsKey(pid)) {
                        unknownParents.get(pid).add(nodeId);
                    } else {
                        List<String> childList = new ArrayList<String>();
                        childList.add(nodeId);
                        unknownParents.put(pid, childList);
                    }
                } // parent node known
                else {
                    node.setParent(container.getNode(pid));
                }
            } else if (parent != null) { //xml-element parent
                node.setParent(container.getNode(parent));
            }

            //Label
            String nodeLabel = nodeE.getAttribute("label");
            if (nodeLabel.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodelabel", nodeId), Issue.Level.SEVERE));
                continue;
            }
            node.setLabel(nodeLabel);

            //Get Attvalue child nodes, avoiding using descendants
            Node child = nodeE.getFirstChild();
            if (child != null) {
                do {
                    if (child.getNodeName().equals("attvalues")) {
                        Node childE = child.getFirstChild();

                        if (childE != null) {
                            do {
                                if (childE.getNodeName().equals("attvalue")) {
                                    Element dataE = (Element) childE;
                                    setNodeData(dataE, node, nodeId);
                                }
                            } while ((childE = childE.getNextSibling()) != null);
                        }

                    }
                } while ((child = child.getNextSibling()) != null);
            }

            //Node color
            Element nodeColor = (Element) nodeE.getElementsByTagName("viz:color").item(0);
            if (nodeColor != null) {
                String rStr = nodeColor.getAttribute("r");
                String gStr = nodeColor.getAttribute("g");
                String bStr = nodeColor.getAttribute("b");

                int r = (rStr.isEmpty()) ? 0 : Integer.parseInt(rStr);
                int g = (gStr.isEmpty()) ? 0 : Integer.parseInt(gStr);
                int b = (bStr.isEmpty()) ? 0 : Integer.parseInt(bStr);

                node.setColor(new Color(r, g, b));
            }

            //Node position
            Element nodePosition = (Element) nodeE.getElementsByTagName("viz:position").item(0);
            if (nodePosition != null) {
                try {
                    String xStr = nodePosition.getAttribute("x");
                    if (!xStr.isEmpty()) {
                        float x = Float.parseFloat(xStr);
                        node.setX(x);
                    }
                } catch (NumberFormatException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", nodeId, "X"), Issue.Level.WARNING));
                }
                try {
                    String yStr = nodePosition.getAttribute("y");
                    if (!yStr.isEmpty()) {
                        float y = Float.parseFloat(yStr);
                        node.setY(y);
                    }
                } catch (NumberFormatException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", nodeId, "Y"), Issue.Level.WARNING));
                }
                try {
                    String zStr = nodePosition.getAttribute("z");
                    if (!zStr.isEmpty()) {
                        float z = Float.parseFloat(zStr);
                        node.setZ(z);
                    }
                } catch (NumberFormatException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", nodeId, "Z"), Issue.Level.WARNING));
                }
            }

            //Node size
            Element nodeSize = (Element) nodeE.getElementsByTagName("viz:size").item(0);
            if (nodeSize != null) {
                try {
                    float size = Float.parseFloat(nodeSize.getAttribute("value"));
                    node.setSize(size);
                } catch (NumberFormatException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodesize", nodeId), Issue.Level.WARNING));
                }
            }

            if (isDynamicMode) {
                //Node date from
                if (!nodeE.getAttribute("datefrom").isEmpty()) {
                    String dateFrom = nodeE.getAttribute("datefrom");
                    float f = Float.valueOf(dateFrom).floatValue();
                    if (!Float.isNaN(f)) {
                        node.setDynamicFrom(f);
                    }
                    // FIXME probleme de conversions de dates
                }

                //Node date to
                if (!nodeE.getAttribute("dateto").isEmpty()) {
                    String dateTo = nodeE.getAttribute("dateto");
                    float f = Float.valueOf(dateTo).floatValue();
                    if (!Float.isNaN(f)) {
                        node.setDynamicTo(f);
                    }
                    // FIXME probleme de conversions de dates
                }
            }

            //Append node
            container.addNode(node);

            //Hierarchy nodes
            Node childNode = nodeE.getFirstChild();
            if (childNode != null) {
                do {
                    if (childNode.getNodeName().equals("nodes")) {
                        Node childE = childNode.getFirstChild();
                        NodeList childrenListE = childE.getChildNodes();
                        if (childrenListE != null && childrenListE.getLength() > 0) {
                            parseNodes(childrenListE, nodeId);
                        }
                    }
                } while ((childNode = childNode.getNextSibling()) != null);
            }

            //Hierarchy edges
            Node childEdge = nodeE.getFirstChild();
            if (childEdge != null) {
                do {
                    if (childEdge.getNodeName().equals("edges")) {
                        Node childE = childEdge.getFirstChild();
                        NodeList childrenListE = childE.getChildNodes();
                        if (childrenListE != null && childrenListE.getLength() > 0) {
                            parseEdges(childrenListE, nodeId);
                        }
                    }
                } while ((childEdge = childEdge.getNextSibling()) != null);
            }
        }
    }

    private void parseEdges(NodeList edgeListE, String parent) {
        for (int i = 0; i < edgeListE.getLength(); i++) {
            Element edgeE = (Element) edgeListE.item(i);

            EdgeDraft edge = container.factory().newEdgeDraft();

            //highest nodes are used to calculate the progress
            if (parent == null) {
                Progress.progress(progressTicket);
            }
            if (cancel) {
                return;
            }

            //Id
            String edgeId = edgeE.getAttribute("id");
            if (edgeId.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgeid"), Issue.Level.WARNING));
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

            //Type
            String edgeType = edgeE.getAttribute("type");
            if (!edgeType.isEmpty()) {
                if (edgeType.equals("undirected")) {
                    edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
                } else if (edgeType.equals("directed")) {
                    edge.setType(EdgeDraft.EdgeType.DIRECTED);
                } else if (edgeType.equals("double")) {
                    edge.setType(EdgeDraft.EdgeType.MUTUAL);
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgetype", edgeType, edgeId), Issue.Level.SEVERE));
                }
            }

            //Weight
            String weightStr = edgeE.getAttribute("weight");
            if (!weightStr.isEmpty()) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    edge.setWeight(weight);
                } catch (NumberFormatException e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgeweight", edgeId), Issue.Level.WARNING));
                }
            }
            // TODO dynamic weight: reserved title "weight" in attributes

            //Label
            String edgeLabel = edgeE.getAttribute("label");
            if (!edgeLabel.isEmpty()) {
                edge.setLabel(edgeLabel);
            }

            //Get Attvalue child nodes, avoiding using descendants
            Node child = edgeE.getFirstChild();
            if (child != null) {
                do {
                    if (child.getNodeName().equals("attvalues")) {
                        Node childE = child.getFirstChild();

                        if (childE != null) {
                            do {
                                if (childE.getNodeName().equals("attvalue")) {
                                    Element dataE = (Element) childE;
                                    setEdgeData(dataE, edge, edgeId);
                                }
                            } while ((childE = childE.getNextSibling()) != null);
                        }

                    }
                } while ((child = child.getNextSibling()) != null);
            }


            /*                if(isDynamicMode) {
            //Node date from
            if (!edgeE.getAttribute("datefrom").isEmpty()) {
            String dateFrom = edgeE.getAttribute("datefrom");
            float f = Float.valueOf(dateFrom).floatValue();
            edge.setDynamicFrom(f);
            // FIXME probleme de conversions de dates
            }

            //Node date to
            if (!edgeE.getAttribute("dateto").isEmpty()) {
            String dateTo = edgeE.getAttribute("dateto");
            float f = Float.valueOf(dateTo).floatValue();
            edge.setDynamicTo(f);
            // FIXME probleme de conversions de dates
            }
            }*/

            container.addEdge(edge);
        }
    }

    private void setAttributesColumns(NodeList columnListE) {
        //NodeColumn
        for (int i = 0; i < columnListE.getLength() && !cancel; i++) {
            Element columnE = (Element) columnListE.item(i);

            Progress.progress(progressTicket);

            //Id & Name
            String colId = columnE.getAttribute("id");
            String colTitle = columnE.getAttribute("title");
            if (colTitle.isEmpty()) {
                colTitle = colId;
            }

            //Class
            String colClass = ((Element) columnE.getParentNode()).getAttribute("class");
            if (colClass.isEmpty() || !(colClass.equals("node") || colClass.equals("edge"))) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributeclass", colTitle), Issue.Level.SEVERE));
                continue;
            }

            //Try to see if the column is a node/edge property
            if (colClass.equals("node")) {
                NodeProperties prop = properties.getNodeProperty(colTitle);
                if (prop != null) {
                    nodePropertiesAttributes.put(colId, prop);
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_nodeproperty", colTitle));
                    continue;
                }
            } else if (colClass.equals("edge")) {
                EdgeProperties prop = properties.getEdgeProperty(colTitle);
                if (prop != null) {
                    edgePropertiesAttributes.put(colId, prop);
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_edgeeproperty", colTitle));
                    continue;
                }
            }

            //Type
            String keyType = columnE.getAttribute("type");
            AttributeType attributeType = AttributeType.STRING;
            if (keyType.equals("boolean")) {
                attributeType = AttributeType.BOOLEAN;
            } else if (keyType.equals("integer")) {
                attributeType = AttributeType.INT;
            } else if (keyType.equals("long")) {
                attributeType = AttributeType.LONG;
            } else if (keyType.equals("float")) {
                attributeType = AttributeType.FLOAT;
            } else if (keyType.equals("double")) {
                attributeType = AttributeType.DOUBLE;
            } else if (keyType.equals("string")) {
                attributeType = AttributeType.STRING;
            } else if (keyType.equals("liststring")) {
                attributeType = AttributeType.LIST_STRING;
            } else if (keyType.equals("anyURI")) {
                attributeType = AttributeType.STRING; //TODO need to create a new type?
            } else {
                if (keepComplexAndEmptyAttributeTypes) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributetype1", colTitle), Issue.Level.WARNING));
                } else {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributetype2", colTitle), Issue.Level.SEVERE));
                    continue;
                }
            }

            //Default
            NodeList defaultList = columnE.getElementsByTagName("default");
            Object defaultValue = null;
            if (defaultList.getLength() > 0) {
                Element defaultE = (Element) defaultList.item(0);
                String defaultValueStr = defaultE.getTextContent();
                try {
                    defaultValue = attributeType.parse(defaultValueStr);
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_default", defaultValueStr, colTitle));
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributedefault", colTitle, attributeType.getTypeString()), Issue.Level.SEVERE));
                }
            }

            //Options
            NodeList optionsList = columnE.getElementsByTagName("options");
            if (optionsList.getLength() > 0) {
                Element optionE = (Element) optionsList.item(0);
                String optionsValueStr = optionE.getTextContent();
                try {
                    StringList optionValues = new StringList(optionsValueStr, "|");
                    optionsAttributes.put(colId, optionValues);
                    report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_options", optionsValueStr, colTitle));
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_attributeoptions", colTitle, attributeType.getTypeString()), Issue.Level.SEVERE));
                }
            }

            /* ???
            if(isDynamicMode) {
            //Node date from
            if (!columnE.getAttribute("datefrom").isEmpty()) {
            String dateFrom = columnE.getAttribute("datefrom");
            float f = Float.valueOf(dateFrom).floatValue();
            node.setDynamicFrom(f);
            // FIXME probleme de conversions de dates
            }

            //Node date to
            if (!columnE.getAttribute("dateto").isEmpty()) {
            String dateTo = columnE.getAttribute("dateto");
            float f = Float.valueOf(dateTo).floatValue();
            node.setDynamicTo(f);
            // FIXME probleme de conversions de dates
            }
            }*/

            //Add as attribute
            if (colClass.equals("node")) {
                AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
                nodeClass.addColumn(colId, colTitle, attributeType, AttributeOrigin.DATA, defaultValue);
                report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_nodeattribute", colTitle, attributeType.getTypeString()));
            } else if (colClass.equals("edge")) {
                AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
                edgeClass.addColumn(colId, colTitle, attributeType, AttributeOrigin.DATA, defaultValue);
                report.log(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_log_edgeattribute", colTitle, attributeType.getTypeString()));
            }
        }
    }

    private void setUnknownParents() {
        for (String pid : unknownParents.keySet()) {
            List<String> childList = unknownParents.get(pid);
            for (String childId : childList) {
                container.getNode(childId).setParent(container.getNode(pid));
            }
            unknownParents.remove(pid);
        }
    }

    private void setNodeData(Element dataE, NodeDraft nodeDraft, String nodeId) {
        //Key
        String dataKey = dataE.getAttribute("for");
        if (dataKey.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datakey", nodeDraft), Issue.Level.SEVERE));
            return;
        }
        String dataValue = dataE.getAttribute("value");
        if (!dataValue.isEmpty()) {

            //Data attribute value
            AttributeColumn column = container.getAttributeModel().getNodeTable().getColumn(dataKey);
            if (column != null) {
                try {
                    Object value = column.getType().parse(dataValue);

                    //Check value
                    if (column.getType() != AttributeType.LIST_STRING) { //otherwise this is a nonsense
                        StringList options = optionsAttributes.get(dataKey);
                        if (options != null && !options.contains(value.toString())) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_dataoptionsvalue", dataValue, nodeId, column.getTitle()), Issue.Level.SEVERE));
                            return;
                        }
                    }

                    nodeDraft.addAttributeValue(column, value);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datavalue", dataKey, nodeId, column.getTitle()), Issue.Level.SEVERE));
                }
            }
        }
    }

    private void setEdgeData(Element dataE, EdgeDraft edgeDraft, String edgeId) {
        //Key
        String dataKey = dataE.getAttribute("for");
        if (dataKey.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datakey", edgeDraft), Issue.Level.SEVERE));
            return;
        }
        String dataValue = dataE.getAttribute("value");
        if (!dataValue.isEmpty()) {

            //Data attribute value
            AttributeColumn column = container.getAttributeModel().getEdgeTable().getColumn(dataKey);
            if (column != null) {
                try {
                    Object value = column.getType().parse(dataValue);

                    //Check value
                    if (column.getType() != AttributeType.LIST_STRING) { //otherwise this is a nonsense
                        StringList options = optionsAttributes.get(dataKey);
                        if (options != null && !options.contains(value.toString())) {
                            report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_dataoptionsvalue", dataValue, edgeId, column.getTitle()), Issue.Level.SEVERE));
                            return;
                        }
                    }

                    edgeDraft.addAttributeValue(column, value);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datavalue", dataKey, edgeId, column.getTitle()), Issue.Level.SEVERE));
                }
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

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public final class ImporterGEXF10 {

        /**
         * GEXF 1.0 import
         */
        private void importData(Document document, ContainerLoader container, Report report) throws Exception {
            //Root
            Element root = document.getDocumentElement();

            //XPath
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression exp = xpath.compile("./graph/attributes[@class]/attribute[@id and normalize-space(@title) and normalize-space(@type)]");
            NodeList columnListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (cancel) {
                return;
            }

            exp = xpath.compile("./graph/nodes/node[@id and normalize-space(@label)]");
            NodeList nodeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (cancel) {
                return;
            }
            exp = xpath.compile("./graph/edges/edge[@source and @target]");
            NodeList edgeListE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (cancel) {
                return;
            }

            // Progress steps
            int taskMax = nodeListE.getLength() + edgeListE.getLength();
            Progress.switchToDeterminate(progressTicket, taskMax);

            // Default edge type
            exp = xpath.compile("./graph/edges[@defaultedgetype]");
            NodeList edgeTypeE = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            if (edgeTypeE != null && edgeTypeE.getLength() > 0) {
                String defaultEdgeType = ((Element) edgeTypeE.item(0)).getAttribute("defaultedgetype");

                if (!defaultEdgeType.isEmpty()) {
                    if (defaultEdgeType.equals("simple")) {
                        container.setEdgeDefault(EdgeDefault.UNDIRECTED);
                    } else if (defaultEdgeType.equals("directed")) {
                        container.setEdgeDefault(EdgeDefault.DIRECTED);
                    } else if (defaultEdgeType.equals("double")) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgedouble"), Issue.Level.WARNING));
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_defaultedgetype"), Issue.Level.SEVERE));
                    }
                }
            }

            //Attributes columns
            setAttributesColumns(columnListE);

            //Nodes
            for (int i = 0; i < nodeListE.getLength(); i++) {
                Element nodeE = (Element) nodeListE.item(i);

                //Id
                String nodeId = nodeE.getAttribute("id");
                if (nodeId.isEmpty()) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeid"), Issue.Level.SEVERE));
                    continue;
                }

                //Create node
                NodeDraft node = container.factory().newNodeDraft();
                node.setId(nodeId);

                //Parent
                if (!nodeE.getAttribute("pid").isEmpty() && !nodeE.getAttribute("pid").equals("0")) {
                    String parentId = nodeE.getAttribute("pid");
                    node.setParent(container.getNode(parentId));
                }

                //Label
                String nodeLabel = nodeE.getAttribute("label");
                if (nodeLabel.isEmpty()) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodelabel", nodeId), Issue.Level.SEVERE));
                    continue;
                }
                node.setLabel(nodeLabel);

                //Get Attvalue child nodes, avoiding using descendants
                Node child = nodeE.getFirstChild();
                if (child != null) {
                    do {
                        if (child.getNodeName().equals("attvalues")) {
                            Node childE = child.getFirstChild();

                            if (childE != null) {
                                do {
                                    if (childE.getNodeName().equals("attvalue")) {
                                        Element dataE = (Element) childE;
                                        setNodeData(dataE, node, nodeId);
                                    }
                                } while ((childE = childE.getNextSibling()) != null);
                            }

                        }
                    } while ((child = child.getNextSibling()) != null);
                }

                //Node color
                Element nodeColor = (Element) nodeE.getElementsByTagName("viz:color").item(0);
                if (nodeColor != null) {
                    String rStr = nodeColor.getAttribute("r");
                    String gStr = nodeColor.getAttribute("g");
                    String bStr = nodeColor.getAttribute("b");

                    int r = (rStr.isEmpty()) ? 0 : Integer.parseInt(rStr);
                    int g = (gStr.isEmpty()) ? 0 : Integer.parseInt(gStr);
                    int b = (bStr.isEmpty()) ? 0 : Integer.parseInt(bStr);

                    node.setColor(new Color(r, g, b));
                }

                //Node position
                Element nodePosition = (Element) nodeE.getElementsByTagName("viz:position").item(0);
                if (nodePosition != null) {
                    try {
                        String xStr = nodePosition.getAttribute("x");
                        if (!xStr.isEmpty()) {
                            float x = Float.parseFloat(xStr);
                            node.setX(x);
                        }
                    } catch (NumberFormatException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", nodeId, "X"), Issue.Level.WARNING));
                    }
                    try {
                        String yStr = nodePosition.getAttribute("y");
                        if (!yStr.isEmpty()) {
                            float y = Float.parseFloat(yStr);
                            node.setY(y);
                        }
                    } catch (NumberFormatException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", nodeId, "Y"), Issue.Level.WARNING));
                    }
                    try {
                        String zStr = nodePosition.getAttribute("z");
                        if (!zStr.isEmpty()) {
                            float z = Float.parseFloat(zStr);
                            node.setZ(z);
                        }
                    } catch (NumberFormatException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodeposition", nodeId, "Z"), Issue.Level.WARNING));
                    }
                }

                //Node size
                Element nodeSize = (Element) nodeE.getElementsByTagName("viz:size").item(0);
                if (nodeSize != null) {
                    try {
                        float size = Float.parseFloat(nodeSize.getAttribute("value"));
                        node.setSize(size);
                    } catch (NumberFormatException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_nodesize", nodeId), Issue.Level.WARNING));
                    }
                }

                //Append node
                container.addNode(node);
                Progress.progress(progressTicket);
            }


            //Edges
            for (int i = 0; i < edgeListE.getLength(); i++) {
                Element edgeE = (Element) edgeListE.item(i);

                EdgeDraft edge = container.factory().newEdgeDraft();

                //Id
                String edgeId = edgeE.getAttribute("id");
                if (edgeId.isEmpty()) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgeid"), Issue.Level.WARNING));
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

                // Type
                String edgeType = edgeE.getAttribute("type");
                if (!edgeType.isEmpty()) {
                    if (edgeType.equals("sim")) {
                        edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
                    } else if (edgeType.equals("dir")) {
                        edge.setType(EdgeDraft.EdgeType.DIRECTED);
                    } else if (edgeType.equals("dou")) {
                        edge.setType(EdgeDraft.EdgeType.MUTUAL);
                    } else {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgetype", edgeType), Issue.Level.SEVERE));
                    }
                }

                //Get Attvalue child nodes, avoiding using descendants
                Node child = edgeE.getFirstChild();
                if (child != null) {
                    do {
                        if (child.getNodeName().equals("attvalues")) {
                            Node childE = child.getFirstChild();

                            if (childE != null) {
                                do {
                                    if (childE.getNodeName().equals("attvalue")) {
                                        Element dataE = (Element) childE;
                                        setEdgeData(dataE, edge, edgeId);
                                    }
                                } while ((childE = childE.getNextSibling()) != null);
                            }

                        }
                    } while ((child = child.getNextSibling()) != null);
                }

                //Cardinal
                String cardinalStr = edgeE.getAttribute("cardinal");
                if (!cardinalStr.isEmpty()) {
                    try {
                        float weight = Float.parseFloat(cardinalStr);
                        edge.setWeight(weight);
                    } catch (NumberFormatException e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_edgeweight", edgeId), Issue.Level.WARNING));
                    }
                }

                container.addEdge(edge);
                Progress.progress(progressTicket);
            }
        }

        private void setNodeData(Element dataE, NodeDraft nodeDraft, String nodeId) {
            //Key
            String dataKey = dataE.getAttribute("id");
            if (dataKey.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datakey1", nodeDraft), Issue.Level.SEVERE));
                return;
            }
            String dataValue = dataE.getAttribute("value");
            if (!dataValue.isEmpty()) {

                //Data attribute value
                AttributeColumn column = container.getAttributeModel().getNodeTable().getColumn(dataKey);
                if (column != null) {
                    try {
                        Object value = column.getType().parse(dataValue);
                        nodeDraft.addAttributeValue(column, value);
                    } catch (Exception e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datavalue", dataKey, nodeId, column.getTitle()), Issue.Level.SEVERE));
                    }
                }
            }
        }

        private void setEdgeData(Element dataE, EdgeDraft edgeDraft, String edgeId) {
            //Key
            String dataKey = dataE.getAttribute("id");
            if (dataKey.isEmpty()) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datakey", edgeDraft), Issue.Level.SEVERE));
                return;
            }
            String dataValue = dataE.getAttribute("value");
            if (!dataValue.isEmpty()) {

                //Data attribute value
                AttributeColumn column = container.getAttributeModel().getEdgeTable().getColumn(dataKey);
                if (column != null) {
                    try {
                        Object value = column.getType().parse(dataValue);
                        edgeDraft.addAttributeValue(column, value);
                    } catch (Exception e) {
                        report.logIssue(new Issue(NbBundle.getMessage(ImporterGEXF.class, "importerGEXF_error_datavalue", dataKey, edgeId, column.getTitle()), Issue.Level.SEVERE));
                    }
                }
            }
        }
    }
}
