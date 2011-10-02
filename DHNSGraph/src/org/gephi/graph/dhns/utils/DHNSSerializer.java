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
package org.gephi.graph.dhns.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphFactoryImpl;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.core.GraphVersion;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.core.IDGen;
import org.gephi.graph.dhns.core.SettingsManager;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MixedEdgeImpl;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.edge.SelfLoopImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

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
    private static final String ELEMENT_VIEW = "View";
    private static final String ELEMENT_VIEW_NODE = "ViewNode";
    private static final String ELEMENT_VIEW_EDGE = "ViewEdge";
    private static final String ELEMENT_TREESTRUCTURE = "TreeStructure";
    private static final String ELEMENT_TREESTRUCTURE_TREE = "Tree";
    private static final String ELEMENT_TREESTRUCTURE_NODE = "Node";
    private static final String ELEMENT_GRAPHVERSION = "GraphVersion";
    private static final String ELEMENT_SETTINGS = "Settings";
    private static final String ELEMENT_SETTINGS_PROPERTY = "Property";
    private static final String ELEMENT_IDGEN = "IDGen";

    public void writeDhns(XMLStreamWriter writer, Dhns dhns) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_DHNS);

        writer.writeStartElement(ELEMENT_DHNS_STATUS);
        writer.writeAttribute("directed", String.valueOf(dhns.isDirected()));
        writer.writeAttribute("undirected", String.valueOf(dhns.isUndirected()));
        writer.writeAttribute("mixed", String.valueOf(dhns.isMixed()));
        writer.writeAttribute("hierarchical", String.valueOf(dhns.isHierarchical()));
        writer.writeEndElement();

        writeIDGen(writer, dhns.getIdGen());
        writeSettings(writer, dhns.getSettingsManager());
        writeGraphVersion(writer, dhns.getGraphVersion());
        writeTreeStructure(writer, dhns.getGraphStructure().getMainView());
        writeEdges(writer, dhns.getGraphStructure().getMainView().getStructure());

        for (GraphViewImpl view : dhns.getGraphStructure().getViews()) {
            if (view != dhns.getGraphStructure().getMainView()) {
                writeGraphView(writer, view);
            }
        }

        writer.writeEndElement();
    }

    public void readDhns(XMLStreamReader reader, Dhns dhns) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (ELEMENT_DHNS_STATUS.equalsIgnoreCase(name)) {
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String attName = reader.getAttributeName(i).getLocalPart();
                        if ("directed".equalsIgnoreCase(attName)) {
                            dhns.setDirected(Boolean.parseBoolean(reader.getAttributeValue(i)));
                        } else if ("undirected".equalsIgnoreCase(attName)) {
                            dhns.setUndirected(Boolean.parseBoolean(reader.getAttributeValue(i)));
                        } else if ("mixed".equalsIgnoreCase(attName)) {
                            dhns.setMixed(Boolean.parseBoolean(reader.getAttributeValue(i)));
                        }
                    }
                } else if (ELEMENT_IDGEN.equalsIgnoreCase(name)) {
                    readIDGen(reader, dhns.getIdGen());
                } else if (ELEMENT_SETTINGS.equalsIgnoreCase(name)) {
                    readSettings(reader, dhns.getSettingsManager());
                } else if (ELEMENT_GRAPHVERSION.equalsIgnoreCase(name)) {
                    readGraphVersion(reader, dhns.getGraphVersion());
                } else if (ELEMENT_TREESTRUCTURE.equalsIgnoreCase(name)) {
                    readTreeStructure(reader, dhns.getGraphStructure(), dhns.factory());
                } else if (ELEMENT_EDGES.equalsIgnoreCase(name)) {
                    readEdges(reader, dhns.getGraphStructure(), dhns.factory());
                } else if (ELEMENT_VIEW.equalsIgnoreCase(name)) {
                    readGraphView(reader, dhns.getGraphStructure());
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (ELEMENT_DHNS.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    public void writeEdges(XMLStreamWriter writer, TreeStructure treeStructure) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_EDGES);

        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                AbstractEdge edge = edgeIterator.next();
                if (edge.isSelfLoop()) {
                    writer.writeStartElement(ELEMENT_EDGES_SELFLOOP);
                } else if (edge.isMixed()) {
                    writer.writeStartElement(ELEMENT_EDGES_MIXED);
                    writer.writeAttribute("directed", String.valueOf(edge.isDirected()));
                } else {
                    writer.writeStartElement(ELEMENT_EDGES_PROPER);
                }
                writer.writeAttribute("source", String.valueOf(edge.getSource().pre));
                writer.writeAttribute("target", String.valueOf(edge.getTarget().pre));
                writer.writeAttribute("weight", String.valueOf(edge.getWeight()));
                writer.writeAttribute("id", String.valueOf(edge.getId()));
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
    }

    public void readEdges(XMLStreamReader reader, GraphStructure graphStructure, GraphFactoryImpl factory) throws XMLStreamException {
        TreeStructure treeStructure = graphStructure.getMainView().getStructure();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();

                    Integer source = 0;
                    Integer target = 0;
                    Integer id = 0;
                    Boolean directed = false;
                    Float weight = 0f;

                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String attName = reader.getAttributeName(i).getLocalPart();
                        if ("id".equalsIgnoreCase(attName)) {
                            id = Integer.parseInt(reader.getAttributeValue(i));
                        } else if ("source".equalsIgnoreCase(attName)) {
                            source = Integer.parseInt(reader.getAttributeValue(i));
                        } else if ("target".equalsIgnoreCase(attName)) {
                            target = Integer.parseInt(reader.getAttributeValue(i));
                        } else if ("directed".equalsIgnoreCase(attName)) {
                            directed = Boolean.parseBoolean(reader.getAttributeValue(i));
                        } else if ("weight".equalsIgnoreCase(attName)) {
                            weight = Float.parseFloat(reader.getAttributeValue(i));
                        }
                    }
                    AbstractNode srcNode = treeStructure.getNodeAt(source);
                    AbstractNode destNode = treeStructure.getNodeAt(target);
                    AbstractEdge edge;
                    if (ELEMENT_EDGES_PROPER.equalsIgnoreCase(name)) {
                        edge = new ProperEdgeImpl(id, srcNode, destNode);
                    } else if (ELEMENT_EDGES_MIXED.equalsIgnoreCase(name)) {
                        edge = new MixedEdgeImpl(id, srcNode, destNode, directed);
                    } else {
                        edge = new SelfLoopImpl(id, srcNode);
                    }
                    edge.setWeight(weight);
                    edge.getEdgeData().setAttributes(factory.newEdgeAttributes(edge.getEdgeData()));
                    edge.getEdgeData().setId(String.valueOf(edge.getId()));//Set edge Id attribute the same as the int id at first because Id attribute is not serialized in DataSerializer if they are equal
                    edge.getEdgeData().setTextData(factory.newTextData());
                    srcNode.getEdgesOutTree().add(edge);
                    destNode.getEdgesInTree().add(edge);
                    graphStructure.addToDictionnary(edge);
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_EDGES.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
        graphStructure.getMainView().getStructureModifier().getEdgeProcessor().computeMetaEdges();
    }

    public void writeTreeStructure(XMLStreamWriter writer, GraphViewImpl view) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_TREESTRUCTURE);
        writer.writeAttribute("edgesenabled", String.valueOf(view.getEdgesCountEnabled()));
        writer.writeAttribute("edgestotal", String.valueOf(view.getEdgesCountTotal()));
        writer.writeAttribute("mutualedgesenabled", String.valueOf(view.getMutualEdgesEnabled()));
        writer.writeAttribute("mutualedgestotal", String.valueOf(view.getMutualEdgesTotal()));
        writer.writeAttribute("nodesenabled", String.valueOf(view.getNodesEnabled()));

        writer.writeStartElement(ELEMENT_TREESTRUCTURE_TREE);
        for (TreeListIterator itr = new TreeListIterator(view.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            writer.writeStartElement(ELEMENT_TREESTRUCTURE_NODE);
            writer.writeAttribute("id", String.valueOf(node.getId()));
            writer.writeAttribute("enabled", String.valueOf(node.isEnabled()));
            writer.writeAttribute("pre", String.valueOf(node.pre));
            writer.writeAttribute("parent", String.valueOf(node.parent.pre));
            writer.writeAttribute("enabledindegree", String.valueOf(node.getEnabledInDegree()));
            writer.writeAttribute("enabledoutdegree", String.valueOf(node.getEnabledOutDegree()));
            writer.writeAttribute("enabledmutualdegree", String.valueOf(node.getEnabledMutualDegree()));
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeEndElement();
    }

    public void readTreeStructure(XMLStreamReader reader, GraphStructure graphStructure, GraphFactoryImpl factory) throws XMLStreamException {
        graphStructure.getMainView().setEdgesCountEnabled(Integer.parseInt(reader.getAttributeValue(null, "edgesenabled")));
        graphStructure.getMainView().setEdgesCountTotal(Integer.parseInt(reader.getAttributeValue(null, "edgestotal")));
        graphStructure.getMainView().setMutualEdgesEnabled(Integer.parseInt(reader.getAttributeValue(null, "mutualedgesenabled")));
        graphStructure.getMainView().setMutualEdgesTotal(Integer.parseInt(reader.getAttributeValue(null, "mutualedgestotal")));
        graphStructure.getMainView().setNodesEnabled(Integer.parseInt(reader.getAttributeValue(null, "nodesenabled")));

        TreeStructure treeStructure = graphStructure.getMainView().getStructure();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_TREESTRUCTURE_NODE.equalsIgnoreCase(name)) {
                        Boolean enabled = Boolean.parseBoolean(reader.getAttributeValue(null, "enabled"));
                        AbstractNode parentNode = treeStructure.getNodeAt(Integer.parseInt(reader.getAttributeValue(null, "parent")));
                        AbstractNode absNode = new AbstractNode(Integer.parseInt(reader.getAttributeValue(null, "id")), 0, 0, 0, 0, parentNode);
                        absNode.setEnabled(enabled);
                        Integer inDegree = Integer.parseInt(reader.getAttributeValue(null, "enabledindegree"));
                        Integer outDegree = Integer.parseInt(reader.getAttributeValue(null, "enabledoutdegree"));
                        Integer mutualDegree = Integer.parseInt(reader.getAttributeValue(null, "enabledmutualdegree"));
                        absNode.setEnabledInDegree(inDegree);
                        absNode.setEnabledOutDegree(outDegree);
                        absNode.setEnabledMutualDegree(mutualDegree);
                        absNode.getNodeData().setAttributes(factory.newNodeAttributes(absNode.getNodeData()));
                        absNode.getNodeData().setId(String.valueOf(absNode.getId()));//Set node Id attribute the same as the int id at first because Id attribute is not serialized in DataSerializer if they are equal
                        absNode.getNodeData().setTextData(factory.newTextData());
                        treeStructure.insertAsChild(absNode, parentNode);
                        graphStructure.addToDictionnary(absNode);
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

    public void writeGraphView(XMLStreamWriter writer, GraphViewImpl graphView) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_VIEW);
        writer.writeAttribute("id", String.valueOf(graphView.getViewId()));
        writer.writeAttribute("edgesenabled", String.valueOf(graphView.getEdgesCountEnabled()));
        writer.writeAttribute("edgestotal", String.valueOf(graphView.getEdgesCountTotal()));
        writer.writeAttribute("mutualedgesenabled", String.valueOf(graphView.getMutualEdgesEnabled()));
        writer.writeAttribute("mutualedgestotal", String.valueOf(graphView.getMutualEdgesTotal()));
        writer.writeAttribute("nodesenabled", String.valueOf(graphView.getNodesEnabled()));

        //Nodes
        for (TreeListIterator itr = new TreeListIterator(graphView.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            writer.writeStartElement(ELEMENT_VIEW_NODE);
            writer.writeAttribute("mainpre", String.valueOf(node.getInView(0).pre));
            writer.writeAttribute("enabled", String.valueOf(node.isEnabled()));
            writer.writeAttribute("pre", String.valueOf(node.pre));
            writer.writeAttribute("parent", String.valueOf(node.parent.pre));
            writer.writeAttribute("enabledindegree", String.valueOf(node.getEnabledInDegree()));
            writer.writeAttribute("enabledoutdegree", String.valueOf(node.getEnabledOutDegree()));
            writer.writeAttribute("enabledmutualdegree", String.valueOf(node.getEnabledMutualDegree()));
            writer.writeEndElement();
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(graphView.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                AbstractEdge edge = edgeIterator.next();
                writer.writeStartElement(ELEMENT_VIEW_EDGE);
                writer.writeAttribute("id", String.valueOf(edge.getId()));
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
    }

    public void readGraphView(XMLStreamReader reader, GraphStructure graphStructure) throws XMLStreamException {
        GraphViewImpl graphView = graphStructure.createView(Integer.parseInt(reader.getAttributeValue(null, "id")));
        graphView.setEdgesCountEnabled(Integer.parseInt(reader.getAttributeValue(null, "edgesenabled")));
        graphView.setEdgesCountTotal(Integer.parseInt(reader.getAttributeValue(null, "edgestotal")));
        graphView.setMutualEdgesEnabled(Integer.parseInt(reader.getAttributeValue(null, "mutualedgesenabled")));
        graphView.setMutualEdgesTotal(Integer.parseInt(reader.getAttributeValue(null, "mutualedgestotal")));
        graphView.setNodesEnabled(Integer.parseInt(reader.getAttributeValue(null, "nodesenabled")));

        TreeStructure mainStructure = graphStructure.getMainView().getStructure();
        TreeStructure treeStructure = graphView.getStructure();

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_VIEW_NODE.equalsIgnoreCase(name)) {
                        Boolean enabled = Boolean.parseBoolean(reader.getAttributeValue(null, "enabled"));
                        AbstractNode mainNode = mainStructure.getNodeAt(Integer.parseInt(reader.getAttributeValue(null, "mainpre")));
                        AbstractNode parentNode = treeStructure.getNodeAt(Integer.parseInt(reader.getAttributeValue(null, "parent")));
                        AbstractNode node = new AbstractNode(mainNode.getNodeData(), graphView.getViewId(), 0, 0, 0, parentNode);
                        Integer inDegree = Integer.parseInt(reader.getAttributeValue(null, "enabledindegree"));
                        Integer outDegree = Integer.parseInt(reader.getAttributeValue(null, "enabledoutdegree"));
                        Integer mutualDegree = Integer.parseInt(reader.getAttributeValue(null, "enabledmutualdegree"));
                        node.setEnabledInDegree(inDegree);
                        node.setEnabledOutDegree(outDegree);
                        node.setEnabledMutualDegree(mutualDegree);
                        node.setEnabled(enabled);
                        treeStructure.insertAsChild(node, parentNode);
                    } else if (ELEMENT_VIEW_EDGE.equalsIgnoreCase(name)) {
                        AbstractEdge edge = graphStructure.getEdgeFromDictionnary(Integer.parseInt(reader.getAttributeValue(null, "id")));
                        AbstractNode source = edge.getSource(graphView.getViewId());
                        AbstractNode target = edge.getTarget(graphView.getViewId());
                        source.getEdgesOutTree().add(edge);
                        target.getEdgesInTree().add(edge);
                    }
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_VIEW.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        graphView.getStructureModifier().getEdgeProcessor().computeMetaEdges();
    }

    public void writeGraphVersion(XMLStreamWriter writer, GraphVersion graphVersion) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_GRAPHVERSION);
        writer.writeAttribute("node", String.valueOf(graphVersion.getNodeVersion()));
        writer.writeAttribute("edge", String.valueOf(graphVersion.getEdgeVersion()));
        writer.writeEndElement();
    }

    public void readGraphVersion(XMLStreamReader reader, GraphVersion graphVersion) {
        int nodeVersion = Integer.parseInt(reader.getAttributeValue(null, "node"));
        int edgeVersion = Integer.parseInt(reader.getAttributeValue(null, "edge"));
        graphVersion.setVersion(nodeVersion, edgeVersion);
    }

    public void writeSettings(XMLStreamWriter writer, SettingsManager settingsManager) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_SETTINGS);
        for (Entry<String, Object> entry : settingsManager.getClientProperties().entrySet()) {
            writer.writeStartElement(ELEMENT_SETTINGS_PROPERTY);
            writer.writeAttribute("key", entry.getKey());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XMLEncoder xmlEncoder = new XMLEncoder(stream);
            xmlEncoder.writeObject(entry.getValue());
            xmlEncoder.close();
            writer.writeAttribute("value", stream.toString());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    public void readSettings(XMLStreamReader reader, SettingsManager settingsManager) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if (ELEMENT_SETTINGS_PROPERTY.equalsIgnoreCase(name)) {
                        String key = reader.getAttributeValue(null, "key");
                        String valueXML = reader.getAttributeValue(null, "value");
                        XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(valueXML.getBytes()));
                        Object value = xmlDecoder.readObject();
                        settingsManager.putClientProperty(key, value);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ELEMENT_SETTINGS.equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeIDGen(XMLStreamWriter writer, IDGen idGen) throws XMLStreamException {
        writer.writeStartElement(ELEMENT_IDGEN);
        writer.writeAttribute("node", String.valueOf(idGen.getNodeGen()));
        writer.writeAttribute("edge", String.valueOf(idGen.getEdgeGen()));
        writer.writeEndElement();
    }

    public void readIDGen(XMLStreamReader reader, IDGen idGen) {
        int nodeGen = Integer.parseInt(reader.getAttributeValue(null, "node"));
        int edgeGen = Integer.parseInt(reader.getAttributeValue(null, "edge"));
        idGen.setNodeGen(nodeGen);
        idGen.setEdgeGen(edgeGen);
    }
}
