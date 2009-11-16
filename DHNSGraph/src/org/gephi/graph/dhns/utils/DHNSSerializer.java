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
package org.gephi.graph.dhns.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.core.GraphVersion;
import org.gephi.graph.dhns.core.IDGen;
import org.gephi.graph.dhns.core.SettingsManager;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MixedEdgeImpl;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.edge.SelfLoopImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.CloneNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSSerializer {

    private static final String ELEMENT_DHNS = "Dhns";
    private static final String ELEMENT_DHNS_STATUS = "Status";
    private static final String ELEMENT_EDGES = "Edges";
    private static final String ELEMENT_EDGES_PROPER = "ProperEdge";
    private static final String ELEMENT_EDGES_SELFLOOP = "SelfLoop";
    private static final String ELEMENT_EDGES_MIXED = "MixedEdge";
    private static final String ELEMENT_TREESTRUCTURE = "TreeStructure";
    private static final String ELEMENT_TREESTRUCTURE_TREE = "Tree";
    private static final String ELEMENT_TREESTRUCTURE_CLONENODE = "CloneNode";
    private static final String ELEMENT_TREESTRUCTURE_PRENODE = "PreNode";
    private static final String ELEMENT_GRAPHVERSION = "GraphVersion";
    private static final String ELEMENT_SETTINGS = "Settings";
    private static final String ELEMENT_SETTINGS_PROPERTY = "Property";
    private static final String ELEMENT_IDGEN = "IDGen";

    public Document createDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            return document;
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Element writeDhns(Document document, Dhns dhns) {
        Element dhnsE = document.createElement(ELEMENT_DHNS);

        Element dhnsStatusE = document.createElement(ELEMENT_DHNS_STATUS);
        dhnsStatusE.setAttribute("directed", String.valueOf(dhns.isDirected()));
        dhnsStatusE.setAttribute("undirected", String.valueOf(dhns.isUndirected()));
        dhnsStatusE.setAttribute("mixed", String.valueOf(dhns.isMixed()));
        dhnsStatusE.setAttribute("dynamic", String.valueOf(dhns.isDynamic()));
        dhnsStatusE.setAttribute("hierarchical", String.valueOf(dhns.isHierarchical()));
        dhnsE.appendChild(dhnsStatusE);

        Element idGenE = writeIDGen(document, dhns.getIdGen());
        dhnsE.appendChild(idGenE);
        Element graphVersionE = writeGraphVersion(document, dhns.getGraphVersion());
        dhnsE.appendChild(graphVersionE);
        Element settingsE = writeSettings(document, dhns.getSettingsManager());
        dhnsE.appendChild(settingsE);
        Element treeStructureE = writeTreeStructure(document, dhns.getGraphStructure().getStructure());
        dhnsE.appendChild(treeStructureE);
        Element edgesE = writeEdges(document, dhns.getGraphStructure().getStructure());
        dhnsE.appendChild(edgesE);

        return dhnsE;
    }

    public void readDhns(Element dhnsE, Dhns dhns) {
        NodeList dhnsListE = dhnsE.getChildNodes();
        for (int i = 0; i < dhnsListE.getLength(); i++) {
            if (dhnsListE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element itemE = (Element) dhnsListE.item(i);
                if (itemE.getTagName().equals(ELEMENT_DHNS_STATUS)) {
                    dhns.setDirected(Boolean.parseBoolean(itemE.getAttribute("directed")));
                    dhns.setUndirected(Boolean.parseBoolean(itemE.getAttribute("undirected")));
                    dhns.setMixed(Boolean.parseBoolean(itemE.getAttribute("mixed")));
                    dhns.getDynamicManager().setDynamic(Boolean.parseBoolean(itemE.getAttribute("dynamic")));
                } else if (itemE.getTagName().equals(ELEMENT_IDGEN)) {
                    readIDGen(itemE, dhns.getIdGen());
                } else if (itemE.getTagName().equals(ELEMENT_GRAPHVERSION)) {
                    readGraphVersion(itemE, dhns.getGraphVersion());
                } else if (itemE.getTagName().equals(ELEMENT_SETTINGS)) {
                    readSettings(itemE, dhns.getSettingsManager());
                } else if (itemE.getTagName().equals(ELEMENT_TREESTRUCTURE)) {
                    readTreeStructure(itemE, dhns.getGraphStructure());
                } else if (itemE.getTagName().equals(ELEMENT_EDGES)) {
                    readEdges(itemE, dhns.getGraphStructure());
                }
            }

        }
    }

    public Element writeEdges(Document document, TreeStructure treeStructure) {
        Element edgesE = document.createElement(ELEMENT_EDGES);

        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                AbstractEdge edge = edgeIterator.next();
                Element edgeE;
                if (edge.isSelfLoop()) {
                    edgeE = document.createElement(ELEMENT_EDGES_SELFLOOP);
                } else if (edge.isMixed()) {
                    edgeE = document.createElement(ELEMENT_EDGES_MIXED);
                    edgeE.setAttribute("directed", String.valueOf(edge.isDirected()));
                } else {
                    edgeE = document.createElement(ELEMENT_EDGES_PROPER);
                }
                edgeE.setAttribute("source", String.valueOf(edge.getSource().pre));
                edgeE.setAttribute("target", String.valueOf(edge.getTarget().pre));
                edgeE.setAttribute("weight", String.valueOf(edge.getWeight()));
                edgeE.setAttribute("id", String.valueOf(edge.getId()));
                edgesE.appendChild(edgeE);
            }
        }

        return edgesE;
    }

    public void readEdges(Element edgesE, GraphStructure graphStructure) {
        NodeList edgesListE = edgesE.getChildNodes();
        TreeStructure treeStructure = graphStructure.getStructure();
        for (int i = 0; i < edgesListE.getLength(); i++) {
            if (edgesListE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element edgeE = (Element) edgesListE.item(i);
                Integer id = Integer.parseInt(edgeE.getAttribute("id"));
                AbstractNode source = treeStructure.getNodeAt(Integer.parseInt(edgeE.getAttribute("source")));
                AbstractNode target = treeStructure.getNodeAt(Integer.parseInt(edgeE.getAttribute("target")));
                AbstractEdge edge;
                if (edgeE.getTagName().equals(ELEMENT_EDGES_PROPER)) {
                    edge = new ProperEdgeImpl(id, source, target);
                } else if (edgeE.getTagName().equals(ELEMENT_EDGES_MIXED)) {
                    edge = new MixedEdgeImpl(id, source, target, Boolean.parseBoolean(edgeE.getAttribute("directed")));
                } else {
                    edge = new SelfLoopImpl(id, source);
                }
                edge.setWeight(Float.parseFloat(edgeE.getAttribute("weight")));
                source.getEdgesOutTree().add(edge);
                target.getEdgesInTree().add(edge);
                graphStructure.getEdgeDictionnary().add(edge);
            }
        }
    }

    public Element writeTreeStructure(Document document, TreeStructure treeStructure) {
        Element treeStructureE = document.createElement(ELEMENT_TREESTRUCTURE);

        Element treeE = document.createElement(ELEMENT_TREESTRUCTURE_TREE);
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            Element nodeE;
            if (node.isClone()) {
                nodeE = document.createElement(ELEMENT_TREESTRUCTURE_CLONENODE);
                nodeE.setAttribute("prenode", String.valueOf(node.getOriginalNode().pre));
            } else {
                nodeE = document.createElement(ELEMENT_TREESTRUCTURE_PRENODE);
                nodeE.setAttribute("enabled", String.valueOf(node.isEnabled()));
                nodeE.setAttribute("id", String.valueOf(node.getId()));
            }
            nodeE.setAttribute("pre", String.valueOf(node.pre));
            nodeE.setAttribute("parent", String.valueOf(node.parent.pre));
            treeE.appendChild(nodeE);
        }
        treeStructureE.appendChild(treeE);
        return treeStructureE;
    }

    public void readTreeStructure(Element treeStructureE, GraphStructure graphStructure) {
        NodeList nodesE = treeStructureE.getChildNodes();
        TreeStructure treeStructure = graphStructure.getStructure();
        for (int i = 0; i < nodesE.getLength(); i++) {
            if (nodesE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (((Element) nodesE.item(i)).getTagName().equals(ELEMENT_TREESTRUCTURE_TREE)) {
                    nodesE = ((Element) nodesE.item(i)).getChildNodes();
                    break;
                }
            }
        }
        for (int i = 0; i < nodesE.getLength(); i++) {
            if (nodesE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element nodeE = (Element) nodesE.item(i);
                Boolean enabled = Boolean.parseBoolean(nodeE.getAttribute("enabled"));
                AbstractNode parentNode = treeStructure.getNodeAt(Integer.parseInt(nodeE.getAttribute("parent")));
                if (nodeE.getTagName().equals(ELEMENT_TREESTRUCTURE_CLONENODE)) {
                    AbstractNode preNode = treeStructure.getNodeAt(Integer.parseInt(nodeE.getAttribute("prenode")));
                    CloneNode cloneNode = new CloneNode(preNode);
                    cloneNode.parent = parentNode;
                    treeStructure.insertAsChild(cloneNode, parentNode);
                } else {
                    PreNode preNode = new PreNode(Integer.parseInt(nodeE.getAttribute("id")), 0, 0, 0, parentNode);
                    preNode.setEnabled(enabled);
                    treeStructure.insertAsChild(preNode, parentNode);
                    graphStructure.getNodeDictionnary().add(preNode);
                }
            }
        }
    }

    public Element writeGraphVersion(Document document, GraphVersion graphVersion) {
        Element graphVersionE = document.createElement(ELEMENT_GRAPHVERSION);
        graphVersionE.setAttribute("node", String.valueOf(graphVersion.getNodeVersion()));
        graphVersionE.setAttribute("edge", String.valueOf(graphVersion.getEdgeVersion()));
        return graphVersionE;
    }

    public void readGraphVersion(Element graphVersionE, GraphVersion graphVersion) {
        int nodeVersion = Integer.parseInt(graphVersionE.getAttribute("node"));
        int edgeVersion = Integer.parseInt(graphVersionE.getAttribute("edge"));
        graphVersion.setVersion(nodeVersion, edgeVersion);
    }

    public Element writeSettings(Document document, SettingsManager settingsManager) {
        Element settingsE = document.createElement(ELEMENT_SETTINGS);
        for (Entry<String, Object> entry : settingsManager.getClientProperties().entrySet()) {
            Element propertyE = document.createElement(ELEMENT_SETTINGS_PROPERTY);
            propertyE.setAttribute("key", entry.getKey());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XMLEncoder xmlEncoder = new XMLEncoder(stream);
            xmlEncoder.writeObject(entry.getValue());
            xmlEncoder.close();
            propertyE.setAttribute("value", stream.toString());
            settingsE.appendChild(propertyE);
        }
        return settingsE;
    }

    public void readSettings(Element settingsE, SettingsManager settingsManager) {
        NodeList propertiesE = settingsE.getChildNodes();
        for (int i = 0; i < propertiesE.getLength(); i++) {
            if (propertiesE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element propertyE = (Element) propertiesE.item(i);
                String key = propertyE.getAttribute("key");
                String valueXML = propertyE.getAttribute("value");
                XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(valueXML.getBytes()));
                Object value = xmlDecoder.readObject();
                settingsManager.putClientProperty(key, value);
            }
        }
    }

    public Element writeIDGen(Document document, IDGen idGen) {
        Element idGenE = document.createElement(ELEMENT_IDGEN);
        idGenE.setAttribute("node", String.valueOf(idGen.getNodeGen()));
        idGenE.setAttribute("edge", String.valueOf(idGen.getEdgeGen()));
        return idGenE;
    }

    public void readIDGen(Element idGenE, IDGen idGen) {
        int nodeGen = Integer.parseInt(idGenE.getAttribute("node"));
        int edgeGen = Integer.parseInt(idGenE.getAttribute("edge"));
        idGen.setNodeGen(nodeGen);
        idGen.setEdgeGen(edgeGen);
    }
}
