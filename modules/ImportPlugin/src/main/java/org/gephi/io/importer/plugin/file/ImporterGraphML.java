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
package org.gephi.io.importer.plugin.file;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
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

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterGraphML implements FileImporter, LongTask {

    private static final String GRAPHML = "graphml";
    private static final String GRAPH = "graph";
    private static final String GRAPH_DEFAULT_EDGETYPE = "edgedefault";
    private static final String GRAPH_ID = "id";
    private static final String NODE = "node";
    private static final String NODE_ID = "id";
    private static final String EDGE = "edge";
    private static final String EDGE_ID = "id";
    private static final String EDGE_SOURCE = "source";
    private static final String EDGE_TARGET = "target";
    private static final String EDGE_DIRECTED = "directed";
    private static final String ATTRIBUTE = "key";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TITLE = "attr.name";
    private static final String ATTRIBUTE_TYPE = "attr.type";
    private static final String ATTRIBUTE_DEFAULT = "default";
    private static final String ATTRIBUTE_FOR = "for";
    private static final String ATTVALUE = "data";
    private static final String ATTVALUE_FOR = "key";
    //Architecture
    private Reader reader;
    private ContainerLoader container;
    private EdgeDirection edgeDefault;
    private boolean cancel;
    private Report report;
    private ProgressTicket progress;
    private XMLStreamReader xmlReader;
    private final PropertiesAssociations properties = new PropertiesAssociations();
    private final HashMap<String, NodeProperties> nodePropertiesAttributes = new HashMap<>();
    private final HashMap<String, EdgeProperties> edgePropertiesAttributes = new HashMap<>();

    public ImporterGraphML() {
        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "d3");  // Default node label used by yEd from yworks.com.
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.X, "xpos");
        properties.addNodePropertyAssociation(NodeProperties.Y, "ypos");
        properties.addNodePropertyAssociation(NodeProperties.Z, "z");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");
        properties.addNodePropertyAssociation(NodeProperties.R, "r");
        properties.addNodePropertyAssociation(NodeProperties.G, "g");
        properties.addNodePropertyAssociation(NodeProperties.B, "b");
        properties.addNodePropertyAssociation(NodeProperties.COLOR, "color");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "edgelabel");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "d7");  // Default edge label used by yEd from yworks.com.
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "Edge Weight");
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "edgeid");
        properties.addEdgePropertyAssociation(EdgeProperties.R, "r");
        properties.addEdgePropertyAssociation(EdgeProperties.G, "g");
        properties.addEdgePropertyAssociation(EdgeProperties.B, "b");
        properties.addEdgePropertyAssociation(EdgeProperties.COLOR, "color");
    }

    @Override
    public boolean execute(ContainerLoader container) {
        this.container = container;
        this.report = new Report();
        Progress.start(progress);
        try {

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            inputFactory.setXMLReporter(new XMLReporter() {
                @Override
                public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                }
            });
            xmlReader = inputFactory.createXMLStreamReader(reader);

            while (xmlReader.hasNext()) {

                Integer eventType = xmlReader.next();
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String name = xmlReader.getLocalName();
                    if (GRAPHML.equalsIgnoreCase(name)) {
                    } else if (GRAPH.equalsIgnoreCase(name)) {
                        readGraph(xmlReader);
                    } else if (NODE.equalsIgnoreCase(name)) {
                        readNode(xmlReader, null);
                    } else if (EDGE.equalsIgnoreCase(name)) {
                        readEdge(xmlReader);
                    } else if (ATTRIBUTE.equalsIgnoreCase(name)) {
                        readAttribute(xmlReader);
                    }
                } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                    String name = xmlReader.getLocalName();
                    if (NODE.equalsIgnoreCase(name)) {
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } finally {
            try {
                xmlReader.close();
            } catch (XMLStreamException e) {
            }
        }
        Progress.finish(progress);
        return !cancel;
    }

    private void readGraph(XMLStreamReader reader) throws Exception {
        String id = "";
        String defaultEdgeType = "";

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (GRAPH_DEFAULT_EDGETYPE.equalsIgnoreCase(attName)) {
                defaultEdgeType = reader.getAttributeValue(i);
            } else if (GRAPH_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            }
        }

        //Edge Type
        // Container edge type should NOT be set to default edge type, as this is 
        // not what it really means. Mixed is the appropriate type, as GraphML supports
        // mixed edge types. 
        container.setEdgeDefault(EdgeDirectionDefault.MIXED);
        edgeDefault = EdgeDirection.DIRECTED;

        if (!defaultEdgeType.isEmpty()) {
            if (defaultEdgeType.equalsIgnoreCase("undirected")) {
                edgeDefault = EdgeDirection.UNDIRECTED;
            } else if (defaultEdgeType.equalsIgnoreCase("directed")) {
                edgeDefault = EdgeDirection.DIRECTED;
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_defaultedgetype", defaultEdgeType), Issue.Level.SEVERE));
            }
        }
    }

    private void readNode(XMLStreamReader reader, NodeDraft parent) throws Exception {
        String id = "";

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (NODE_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            }
        }

        if (id.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_nodeid"), Issue.Level.SEVERE));
            return;
        }

        NodeDraft node = null;
        if (container.nodeExists(id)) {
            node = container.getNode(id);
        } else {
            node = container.factory().newNodeDraft(id);
        }

        //TODO - PARENT REL
        if (!container.nodeExists(id)) {
            container.addNode(node);
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = xmlReader.getLocalName();
                    if (ATTVALUE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readNodeAttValue(reader, node);
                    } else if (NODE.equalsIgnoreCase(name)) {
                        readNode(reader, node);
                    }
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (NODE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    private void readNodeAttValue(XMLStreamReader reader, NodeDraft node) throws Exception {
        String fore = "";
        String value = "";

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (ATTVALUE_FOR.equalsIgnoreCase(attName)) {
                fore = reader.getAttributeValue(i);
            }
        }

        if (fore.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datakey", node), Issue.Level.SEVERE));
            return;
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            int xmltype = reader.next();

            switch (xmltype) {
                case XMLStreamReader.CHARACTERS:
                    if (!xmlReader.isWhiteSpace()) {
                        value += xmlReader.getText();
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ATTVALUE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        if (!value.isEmpty()) {
            //Property
            NodeProperties prop = nodePropertiesAttributes.get(fore);
            if (prop != null) {
                try {
                    switch (prop) {
                        case X:
                            node.setX(parseFloat(value));
                            break;
                        case Y:
                            node.setY(parseFloat(value));
                            break;
                        case Z:
                            node.setZ(parseFloat(value));
                            break;
                        case SIZE:
                            node.setSize(parseFloat(value));
                            break;
                        case LABEL:
                            node.setLabel(value);
                            break;
                        case COLOR:
                            node.setColor(value);
                            break;
                        case R:
                            if (node.getColor() == null) {
                                node.setColor(Integer.parseInt(value), 0, 0);
                            } else {
                                node.setColor(Integer.parseInt(value), node.getColor().getGreen(), node.getColor().getBlue());
                            }
                            break;
                        case G:
                            if (node.getColor() == null) {
                                node.setColor(0, Integer.parseInt(value), 0);
                            } else {
                                node.setColor(node.getColor().getRed(), Integer.parseInt(value), node.getColor().getBlue());
                            }
                            break;
                        case B:
                            if (node.getColor() == null) {
                                node.setColor(0, 0, Integer.parseInt(value));
                            } else {
                                node.setColor(node.getColor().getRed(), node.getColor().getGreen(), Integer.parseInt(value));
                            }
                            break;
                    }
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue", fore, node, prop.toString()), Issue.Level.SEVERE));
                }
                return;
            }

            //Data attribute value
            ColumnDraft column = container.getNodeColumn(fore);
            if (column != null) {
                try {
                    node.parseAndSetValue(column.getId(), value);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue", fore, node, column.getTitle()), Issue.Level.SEVERE));
                }
            }
        }
    }

    private void readEdge(XMLStreamReader reader) throws Exception {
        String id = "";
        String source = "";
        String target = "";
        String directed = "";

        //Attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (EDGE_SOURCE.equalsIgnoreCase(attName)) {
                source = reader.getAttributeValue(i);
            } else if (EDGE_TARGET.equalsIgnoreCase(attName)) {
                target = reader.getAttributeValue(i);
            } else if (EDGE_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            } else if (EDGE_DIRECTED.equalsIgnoreCase(attName)) {
                directed = reader.getAttributeValue(i);
            }
        }

        //Edge Id
        EdgeDraft edge;
        if (!id.isEmpty()) {
            edge = container.factory().newEdgeDraft(id);
        } else {
            edge = container.factory().newEdgeDraft();
        }

        NodeDraft nodeSource = container.getNode(source);
        NodeDraft nodeTarget = container.getNode(target);
        edge.setSource(nodeSource);
        edge.setTarget(nodeTarget);

        //Type
        if (!directed.isEmpty()) {
            if (directed.equalsIgnoreCase("true")) {
                edge.setDirection(EdgeDirection.DIRECTED);
            } else if (directed.equalsIgnoreCase("false")) {
                edge.setDirection(EdgeDirection.UNDIRECTED);
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_edgetype", directed, edge), Issue.Level.SEVERE));
                edge.setDirection(edgeDefault);
            }
        } else {
            edge.setDirection(edgeDefault);
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    if (ATTVALUE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        readEdgeAttValue(reader, edge);
                    }
                    break;

                case XMLStreamReader.END_ELEMENT:
                    if (EDGE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
        container.addEdge(edge);
    }

    private void readEdgeAttValue(XMLStreamReader reader, EdgeDraft edge) throws Exception {
        String fore = "";
        String value = "";

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (ATTVALUE_FOR.equalsIgnoreCase(attName)) {
                fore = reader.getAttributeValue(i);
            }
        }

        if (fore.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datakey", edge), Issue.Level.SEVERE));
            return;
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            int xmltype = reader.next();

            switch (xmltype) {
                case XMLStreamReader.CHARACTERS:
                    if (!xmlReader.isWhiteSpace()) {
                        value += xmlReader.getText();
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (ATTVALUE.equalsIgnoreCase(xmlReader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        if (!value.isEmpty()) {
            EdgeProperties prop = edgePropertiesAttributes.get(fore);
            if (prop != null) {
                try {
                    switch (prop) {
                        case WEIGHT:
                            edge.setWeight(parseFloat(value));
                            break;
                        case LABEL:
                            edge.setLabel(value);
                            break;
                        case COLOR:
                            edge.setColor(value);
                            break;
                        case R:
                            if (edge.getColor() == null) {
                                edge.setColor(Integer.parseInt(value), 0, 0);
                            } else {
                                edge.setColor(Integer.parseInt(value), edge.getColor().getGreen(), edge.getColor().getBlue());
                            }
                            break;
                        case G:
                            if (edge.getColor() == null) {
                                edge.setColor(0, Integer.parseInt(value), 0);
                            } else {
                                edge.setColor(edge.getColor().getRed(), Integer.parseInt(value), edge.getColor().getBlue());
                            }
                            break;
                        case B:
                            if (edge.getColor() == null) {
                                edge.setColor(0, 0, Integer.parseInt(value));
                            } else {
                                edge.setColor(edge.getColor().getRed(), edge.getColor().getGreen(), Integer.parseInt(value));
                            }
                            break;
                    }
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue", fore, edge, prop.toString()), Issue.Level.SEVERE));
                }
                return;
            }

            //Data attribute value
            ColumnDraft column = container.getEdgeColumn(fore);
            if (column != null) {
                try {
                    edge.parseAndSetValue(column.getId(), value);
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_datavalue", fore, edge, column.getTitle()), Issue.Level.SEVERE));
                }
            }
        }
    }

    private void readAttribute(XMLStreamReader reader) throws Exception {
        String id = "";
        String type = "";
        String title = "";
        String defaultStr = "";
        String forStr = "all";
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attName = reader.getAttributeName(i).getLocalPart();
            if (ATTRIBUTE_ID.equalsIgnoreCase(attName)) {
                id = reader.getAttributeValue(i);
            } else if (ATTRIBUTE_TYPE.equalsIgnoreCase(attName)) {
                type = reader.getAttributeValue(i);
            } else if (ATTRIBUTE_TITLE.equalsIgnoreCase(attName)) {
                title = reader.getAttributeValue(i);
            } else if (ATTRIBUTE_FOR.equalsIgnoreCase(attName)) {
                forStr = reader.getAttributeValue(i);
            }
        }

        if (title.isEmpty()) {
            title = id;
        }

        boolean property = false;
        if (!id.isEmpty()) {
            //Properties
            if (forStr.equalsIgnoreCase("node")) {
                NodeProperties prop = properties.getNodeProperty(id) == null ? properties.getNodeProperty(title) : properties.getNodeProperty(id);
                if (prop != null) {
                    nodePropertiesAttributes.put(id, prop);
                    report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_nodeproperty", title));
                    property = true;
                }
            } else if (forStr.equalsIgnoreCase("edge")) {
                EdgeProperties prop = properties.getEdgeProperty(id) == null ? properties.getEdgeProperty(title) : properties.getEdgeProperty(id);
                if (prop != null) {
                    edgePropertiesAttributes.put(id, prop);
                    report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_edgeproperty", title));
                    property = true;
                }
            }
            if (property) {
                return;
            }
        } else {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributeempty", title), Issue.Level.SEVERE));
            return;
        }

        if (!property && type.isEmpty()) {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributetype1", title), Issue.Level.SEVERE));
            type = "string";
        }

        if (!property) {
            //Class type
            if (forStr.isEmpty() || !(forStr.equalsIgnoreCase("node") || forStr.equalsIgnoreCase("edge") || forStr.equalsIgnoreCase("all"))) {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributeclass", title), Issue.Level.SEVERE));
                return;
            }

            //Default?
            boolean end = false;
            boolean defaultFlag = false;
            while (reader.hasNext() && !end) {
                int xmltype = reader.next();

                switch (xmltype) {
                    case XMLStreamReader.START_ELEMENT:
                        if (ATTRIBUTE_DEFAULT.equalsIgnoreCase(xmlReader.getLocalName())) {
                            defaultFlag = true;
                        }
                        break;
                    case XMLStreamReader.CHARACTERS:
                        if (defaultFlag && !xmlReader.isWhiteSpace()) {
                            defaultStr = xmlReader.getText();
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        if (ATTRIBUTE.equalsIgnoreCase(xmlReader.getLocalName())) {
                            end = true;
                        }
                        break;
                }
            }

            //Type
            Class attributeType = String.class;
            if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("bool")) {
                attributeType = boolean.class;
            } else if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("int")) {
                attributeType = int.class;
            } else if (type.equalsIgnoreCase("long")) {
                attributeType = Long.class;
            } else if (type.equalsIgnoreCase("float")) {
                attributeType = Float.class;
            } else if (type.equalsIgnoreCase("double")) {
                attributeType = Double.class;
            } else if (type.equalsIgnoreCase("string")) {
                attributeType = String.class;
            } else if (type.equalsIgnoreCase("bigdecimal")) {
                attributeType = BigDecimal.class;
            } else if (type.equalsIgnoreCase("biginteger")) {
                attributeType = BigInteger.class;
            } else if (type.equalsIgnoreCase("byte")) {
                attributeType = Byte.class;
            } else if (type.equalsIgnoreCase("char")) {
                attributeType = Character.class;
            } else if (type.equalsIgnoreCase("short")) {
                attributeType = Short.class;
            } else if (type.equalsIgnoreCase("listboolean")) {
                attributeType = boolean[].class;
            } else if (type.equalsIgnoreCase("listint")) {
                attributeType = int[].class;
            } else if (type.equalsIgnoreCase("listlong")) {
                attributeType = long[].class;
            } else if (type.equalsIgnoreCase("listfloat")) {
                attributeType = float[].class;
            } else if (type.equalsIgnoreCase("listdouble")) {
                attributeType = double[].class;
            } else if (type.equalsIgnoreCase("liststring")) {
                attributeType = String[].class;
            } else if (type.equalsIgnoreCase("listbigdecimal")) {
                attributeType = BigDecimal[].class;
            } else if (type.equalsIgnoreCase("listbiginteger")) {
                attributeType = BigInteger[].class;
            } else if (type.equalsIgnoreCase("listbyte")) {
                attributeType = byte[].class;
            } else if (type.equalsIgnoreCase("listchar")) {
                attributeType = char[].class;
            } else if (type.equalsIgnoreCase("listshort")) {
                attributeType = short[].class;
            } else {
                report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributetype2", type), Issue.Level.SEVERE));
                return;
            }

            //Add to model
            ColumnDraft column = null;
            if ("node".equalsIgnoreCase(forStr) || "all".equalsIgnoreCase(forStr)) {
                if (container.getNodeColumn(id) != null) {
                    report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributecolumn_exist", id));
                    return;
                }
                column = container.addNodeColumn(id, attributeType);
                column.setTitle(title);
                report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_nodeattribute", title, attributeType.getCanonicalName()));
            } else if ("edge".equalsIgnoreCase(forStr) || "all".equalsIgnoreCase(forStr)) {
                if (container.getEdgeColumn(id) != null) {
                    report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributecolumn_exist", id));
                    return;
                }
                column = container.addEdgeColumn(id, attributeType);
                column.setTitle(title);
                report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_edgeattribute", title, attributeType.getCanonicalName()));
            }

            if (column != null && !defaultStr.isEmpty()) {
                try {
                    column.setDefaultValueString(defaultStr);
                    report.log(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_log_default", defaultStr, title));
                } catch (Exception e) {
                    report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributedefault", title, attributeType.getCanonicalName()), Issue.Level.SEVERE));
                }
            }
        } else {
            report.logIssue(new Issue(NbBundle.getMessage(ImporterGraphML.class, "importerGraphML_error_attributeempty", title), Issue.Level.SEVERE));
        }
    }

    private float parseFloat(String str) {
        str = str.replace(',', '.');
        return Float.parseFloat(str);
    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
