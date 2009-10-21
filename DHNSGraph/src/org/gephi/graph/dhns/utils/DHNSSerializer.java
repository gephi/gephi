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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.dhns.core.Dhns;
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
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSSerializer {

    private static final String ELEMENT_EDGES = "Edges";
    private static final String ELEMENT_EDGES_PROPER = "ProperEdge";
    private static final String ELEMENT_EDGES_SELFLOOP = "SelfLoop";
    private static final String ELEMENT_EDGES_MIXED = "MixedEdge";
    private static final String ELEMENT_TREESTRUCTURE = "TreeStructure";
    private static final String ELEMENT_TREESTRUCTURE_TREE = "Edges";
    private static final String ELEMENT_TREESTRUCTURE_CLONENODE = "CloneNode";
    private static final String ELEMENT_TREESTRUCTURE_PRENODE = "PreNode";

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

    public void readEdges(Element edgesE, TreeStructure treeStucture) {
        NodeList edgesListE = edgesE.getChildNodes();
        for (int i = 0; i < edgesListE.getLength(); i++) {
            Element edgeE = (Element) edgesListE.item(i);
            Integer id = Integer.parseInt(edgeE.getAttribute("id"));
            AbstractNode source = treeStucture.getNodeAt(Integer.parseInt(edgeE.getAttribute("source")));
            AbstractNode target = treeStucture.getNodeAt(Integer.parseInt(edgeE.getAttribute("target")));
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

    public TreeStructure readTreeStructure(Element treeStructureE, Dhns dhns) {
        TreeStructure treeStructure = new TreeStructure(dhns);
        NodeList nodesE = treeStructureE.getFirstChild().getChildNodes();
        for (int i = 0; i < nodesE.getLength(); i++) {
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
            }
        }
        return treeStructure;
    }
}
