/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>,
 Sebastien Heymann <sebastien.heymann@gephi.org>
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
package org.gephi.io.exporter.plugin;

import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * @author Sebastien Heymann
 * @author Mathieu Bastian
 */
public class ExporterGraphML implements GraphExporter, CharacterExporter, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private Workspace workspace;
    private Writer writer;
    private boolean exportVisible;
    //Settings
    private boolean normalize = false;
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportSize = true;
    private boolean exportDynamicWeight = true;
    private boolean exportAttributes = true;
    //Settings Helper
    private float minSize;
    private float maxSize;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private float minZ;
    private float maxZ;

    @Override
    public boolean execute() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();

        try {
            exportData(createDocument(), graph);
        } catch (Exception e) {
            Logger.getLogger(ExporterGraphML.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        document.setXmlVersion("1.0");
        document.setXmlStandalone(true);
        return document;
    }

    private void transform(Document document) throws TransformerConfigurationException, TransformerException {
        Source source = new DOMSource(document);
        Result result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(source, result);
    }

    /*
     public Schema getSchema() {
     try {
     SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
     return sf.newSchema(new URL("http://www.gexf.net/1.1draft/gexf.xsd"));
     } catch (MalformedURLException ex) {
     Exceptions.printStackTrace(ex);
     } catch (SAXException ex) {
     Exceptions.printStackTrace(ex);
     }
     return null;
     }
     */
    private void exportData(Document document, Graph graph) throws Exception {
        Progress.start(progressTicket);

        //Options
        calculateMinMax(graph);

        //Calculate progress units count
        int max = graph.getNodeCount() + graph.getEdgeCount();

        Progress.switchToDeterminate(progressTicket, max);

        Element root = document.createElementNS("http://graphml.graphdrawing.org/xmlns", "graphml");
        document.appendChild(root);

        createKeys(document, root, graph.getModel());

        Element graphE = createGraph(document, graph);
        root.appendChild(graphE);

        if (!cancel) {
            transform(document);
        }

        Progress.finish(progressTicket);
    }

    private void createKeys(Document document, Element root, GraphModel graphModel) {
        Element nodeLabelKeyE = document.createElement("key");
        nodeLabelKeyE.setAttribute("id", "label");
        nodeLabelKeyE.setAttribute("attr.name", "label");
        nodeLabelKeyE.setAttribute("attr.type", "string");
        nodeLabelKeyE.setAttribute("for", "node");
        root.appendChild(nodeLabelKeyE);

        Element edgeLabelKeyE = document.createElement("key");
        edgeLabelKeyE.setAttribute("id", "edgelabel");
        edgeLabelKeyE.setAttribute("attr.name", "Edge Label");
        edgeLabelKeyE.setAttribute("attr.type", "string");
        edgeLabelKeyE.setAttribute("for", "edge");
        root.appendChild(edgeLabelKeyE);

        Element weightKeyE = document.createElement("key");
        weightKeyE.setAttribute("id", "weight");
        weightKeyE.setAttribute("attr.name", "weight");
        weightKeyE.setAttribute("attr.type", "double");
        weightKeyE.setAttribute("for", "edge");
        root.appendChild(weightKeyE);

        Element edgeIdKeyE = document.createElement("key");
        edgeIdKeyE.setAttribute("id", "edgeid");
        edgeIdKeyE.setAttribute("attr.name", "Edge Id");
        edgeIdKeyE.setAttribute("attr.type", "string");
        edgeIdKeyE.setAttribute("for", "edge");
        root.appendChild(edgeIdKeyE);

        if (exportColors) {
            Element colorRKeyE = document.createElement("key");
            colorRKeyE.setAttribute("id", "r");
            colorRKeyE.setAttribute("attr.name", "r");
            colorRKeyE.setAttribute("attr.type", "int");
            colorRKeyE.setAttribute("for", "node");
            root.appendChild(colorRKeyE);

            Element colorGKeyE = document.createElement("key");
            colorGKeyE.setAttribute("id", "g");
            colorGKeyE.setAttribute("attr.name", "g");
            colorGKeyE.setAttribute("attr.type", "int");
            colorGKeyE.setAttribute("for", "node");
            root.appendChild(colorGKeyE);

            Element colorBKeyE = document.createElement("key");
            colorBKeyE.setAttribute("id", "b");
            colorBKeyE.setAttribute("attr.name", "b");
            colorBKeyE.setAttribute("attr.type", "int");
            colorBKeyE.setAttribute("for", "node");
            root.appendChild(colorBKeyE);
        }

        if (exportPosition) {
            Element positionKeyE = document.createElement("key");
            positionKeyE.setAttribute("id", "x");
            positionKeyE.setAttribute("attr.name", "x");
            positionKeyE.setAttribute("attr.type", "float");
            positionKeyE.setAttribute("for", "node");
            root.appendChild(positionKeyE);

            Element positionKey2E = document.createElement("key");
            positionKey2E.setAttribute("id", "y");
            positionKey2E.setAttribute("attr.name", "y");
            positionKey2E.setAttribute("attr.type", "float");
            positionKey2E.setAttribute("for", "node");
            root.appendChild(positionKey2E);

            if (minZ != 0f || maxZ != 0f) {
                Element positionKey3E = document.createElement("key");
                positionKey3E.setAttribute("id", "z");
                positionKey3E.setAttribute("attr.name", "z");
                positionKey3E.setAttribute("attr.type", "float");
                positionKey3E.setAttribute("for", "node");
                root.appendChild(positionKey3E);
            }
        }

        if (exportSize) {
            Element sizeKeyE = document.createElement("key");
            sizeKeyE.setAttribute("id", "size");
            sizeKeyE.setAttribute("attr.name", "size");
            sizeKeyE.setAttribute("attr.type", "float");
            sizeKeyE.setAttribute("for", "node");
            root.appendChild(sizeKeyE);
        }

        //Attributes
        if (graphModel != null && exportAttributes) {
            //Node attributes
            for (Column column : graphModel.getNodeTable()) {
                if (!column.isProperty()) {
                    Element attributeE = createAttribute(document, column);
                    attributeE.setAttribute("for", "node");
                    root.appendChild(attributeE);
                }
            }

            for (Column column : graphModel.getEdgeTable()) {
                if (!column.isProperty()) {
                    Element attributeE = createAttribute(document, column);
                    attributeE.setAttribute("for", "edge");
                    root.appendChild(attributeE);
                }
            }
        }
    }

    private Element createGraph(Document document, Graph graph) throws Exception {
        Element graphE = document.createElement("graph");

        if (graph.isDirected() || graph.isMixed()) {
            graphE.setAttribute("edgedefault", "directed");
        } else {
            graphE.setAttribute("edgedefault", "undirected"); // defaultValue
        }

        //Nodes
        createNodes(document, graphE, graph);

        //Edges
        createEdges(document, graphE, graph);

        return graphE;
    }

    private Element createAttribute(Document document, Column column) {
        Element attributeE = document.createElement("key");
        attributeE.setAttribute("id", column.getId());
        attributeE.setAttribute("attr.name", column.getTitle());
        if (column.getTypeClass().equals(Integer.class)
                || column.getTypeClass().equals(Short.class)
                || column.getTypeClass().equals(Byte.class)) {
            attributeE.setAttribute("attr.type", "int");
        } else {
            attributeE.setAttribute("attr.type", AttributeUtils.getTypeName(column.getTypeClass()));
        }
        if (column.getDefaultValue() != null) {
            Element defaultE = document.createElement("default");
            Text defaultTextE = document.createTextNode(column.getDefaultValue().toString());
            defaultE.appendChild(defaultTextE);
        }
        return attributeE;
    }

    private Element createNodeAttvalue(Document document, Column column, Graph graph, Node n) throws Exception {
        Object val = n.getAttribute(column, graph.getView());
        if (val != null) {
            String value = val.toString();
            String id = column.getId();

            Element attvalueE = document.createElement("data");
            attvalueE.setAttribute("key", id);
            attvalueE.setTextContent(value);
            return attvalueE;
        }
        return null;
    }

    private Element createEdgeAttvalue(Document document, Column column, Graph graph, Edge e) throws Exception {
        Object val = e.getAttribute(column, graph.getView());
        if (val != null) {
            String value = val.toString();
            String id = column.getId();

            Element attvalueE = document.createElement("data");
            attvalueE.setAttribute("key", id);
            attvalueE.setTextContent(value);
            return attvalueE;
        }
        return null;
    }

    private void createNodes(Document document, Element parentE, Graph graph) throws Exception {
        NodeIterable nodeIterable = graph.getNodes();
        for (Node n : nodeIterable) {
            if (cancel) {
                nodeIterable.doBreak();
                break;
            }
            Element nodeE = createNode(document, graph, n);
            parentE.appendChild(nodeE);
        }
    }

    private Element createNode(Document document, Graph graph, Node n) throws Exception {
        Element nodeE = document.createElement("node");
        nodeE.setAttribute("id", n.getId().toString());

        //Label
        if (n.getLabel() != null && !n.getLabel().isEmpty()) {
            Element labelE = createNodeLabel(document, n);
            nodeE.appendChild(labelE);
        }

        //Attribute values
        if (exportAttributes) {
            for (Column column : n.getAttributeColumns()) {
                if (!column.isProperty()) {
                    //Data or computed
                    Element attvalueE = createNodeAttvalue(document, column, graph, n);
                    if (attvalueE != null) {
                        nodeE.appendChild(attvalueE);
                    }
                }
            }
        }

        //Viz
        if (exportSize) {
            Element sizeE = createNodeSize(document, n);
            nodeE.appendChild(sizeE);
        }
        if (exportColors) {
            Element colorE = createNodeColorR(document, n);
            nodeE.appendChild(colorE);

            colorE = createNodeColorG(document, n);
            nodeE.appendChild(colorE);

            colorE = createNodeColorB(document, n);
            nodeE.appendChild(colorE);
        }
        if (exportPosition) {
            Element positionXE = createNodePositionX(document, n);
            nodeE.appendChild(positionXE);
            Element positionYE = createNodePositionY(document, n);
            nodeE.appendChild(positionYE);
            if (minZ != 0f || maxZ != 0f) {
                Element positionZE = createNodePositionZ(document, n);
                nodeE.appendChild(positionZE);
            }
        }

        Progress.progress(progressTicket);

        return nodeE;
    }

    private void createEdges(Document document, Element edgesE, Graph graph) throws Exception {
        EdgeIterable itr = graph.getEdges();
        for (Edge e : itr) {
            if (cancel) {
                itr.doBreak();
                break;
            }
            Element edgeE = createEdge(document, e, graph);
            edgesE.appendChild(edgeE);
        }
    }

    private Element createEdge(Document document, Edge e, Graph graph) throws Exception {
        Element edgeE = document.createElement("edge");

        edgeE.setAttribute("source", e.getSource().getId().toString());
        edgeE.setAttribute("target", e.getTarget().getId().toString());

        Element idE = createEdgeId(document, e);
        edgeE.appendChild(idE);

        //Label
        if (e.getLabel() != null && !e.getLabel().isEmpty()) {
            Element labelE = createEdgeLabel(document, e);
            edgeE.appendChild(labelE);
        }

        Element weightE = createEdgeWeight(document, e, graph);
        edgeE.appendChild(weightE);

        if (e.isDirected() && !graph.isDirected()) {
            edgeE.setAttribute("type", "directed");
        } else if (!e.isDirected() && graph.isDirected()) {
            edgeE.setAttribute("type", "undirected");
        }

        //Attribute values
        if (exportAttributes) {
            for (Column column : e.getAttributeColumns()) {
                if (!column.isProperty()) {
                    //Data or computed
                    Element attvalueE = createEdgeAttvalue(document, column, graph, e);
                    if (attvalueE != null) {
                        edgeE.appendChild(attvalueE);
                    }
                }
            }
        }

        Progress.progress(progressTicket);

        return edgeE;
    }

    private Element createNodeSize(Document document, Node n) throws Exception {
        Element sizeE = document.createElement("data");
        float size = n.size();
        if (normalize) {
            size = (size - minSize) / (maxSize - minSize);
        }
        sizeE.setAttribute("key", "size");
        sizeE.setTextContent("" + size);

        return sizeE;
    }

    private Element createNodeColorR(Document document, Node n) throws Exception {
        int r = Math.round(n.r() * 255f);
        Element colorE = document.createElement("data");
        colorE.setAttribute("key", "r");
        colorE.setTextContent("" + r);
        return colorE;
    }

    private Element createNodeColorG(Document document, Node n) throws Exception {
        int g = Math.round(n.g() * 255f);
        Element colorE = document.createElement("data");
        colorE.setAttribute("key", "g");
        colorE.setTextContent("" + g);
        return colorE;
    }

    private Element createNodeColorB(Document document, Node n) throws Exception {
        int b = Math.round(n.b() * 255f);
        Element colorE = document.createElement("data");
        colorE.setAttribute("key", "b");
        colorE.setTextContent("" + b);
        return colorE;
    }

    private Element createNodePositionX(Document document, Node n) throws Exception {
        Element positionXE = document.createElement("data");
        float x = n.x();
        if (normalize && x != 0.0) {
            x = (x - minX) / (maxX - minX);
        }
        positionXE.setAttribute("key", "x");
        positionXE.setTextContent("" + x);
        return positionXE;
    }

    private Element createNodePositionY(Document document, Node n) throws Exception {
        Element positionYE = document.createElement("data");
        float y = n.y();
        if (normalize && y != 0.0) {
            y = (y - minY) / (maxY - minY);
        }
        positionYE.setAttribute("key", "y");
        positionYE.setTextContent("" + y);

        return positionYE;
    }

    private Element createNodePositionZ(Document document, Node n) throws Exception {
        Element positionZE = document.createElement("data");
        float z = n.z();
        if (normalize && z != 0.0) {
            z = (z - minZ) / (maxZ - minZ);
        }
        positionZE.setAttribute("key", "z");
        positionZE.setTextContent("" + z);

        return positionZE;
    }

    private Element createNodeLabel(Document document, Node n) throws Exception {
        Element labelE = document.createElement("data");
        labelE.setAttribute("key", "label");
        labelE.setTextContent(n.getLabel());

        return labelE;
    }

    private Element createEdgeId(Document document, Edge e) throws Exception {
        Element idE = document.createElement("data");
        idE.setAttribute("key", "edgeid");
        idE.setTextContent(e.getId().toString());

        return idE;
    }

    private Element createEdgeWeight(Document document, Edge e, Graph graph) throws Exception {
        Element weightE = document.createElement("data");
        weightE.setAttribute("key", "weight");
        Double weight;
        if (exportDynamicWeight) {
            weight = e.getWeight(graph.getView());
        } else {
            weight = e.getWeight();
        }
        weightE.setTextContent(weight.toString());

        return weightE;
    }

    private Element createEdgeLabel(Document document, Edge e) throws Exception {
        Element labelE = document.createElement("data");
        labelE.setAttribute("key", "edgelabel");
        labelE.setTextContent(e.getLabel());

        return labelE;
    }

    private void calculateMinMax(Graph graph) {
        minX = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        minZ = Float.POSITIVE_INFINITY;
        maxZ = Float.NEGATIVE_INFINITY;
        minSize = Float.POSITIVE_INFINITY;
        maxSize = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getNodes()) {
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
            minZ = Math.min(minZ, node.z());
            maxZ = Math.max(maxZ, node.z());
            minSize = Math.min(minSize, node.size());
            maxSize = Math.max(maxSize, node.size());
        }
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "ExporterGraphML_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".graphml", NbBundle.getMessage(getClass(), "fileType_GraphML_Name"));
        return new FileType[]{ft};
    }

    public void setExportAttributes(boolean exportAttributes) {
        this.exportAttributes = exportAttributes;
    }

    public void setExportColors(boolean exportColors) {
        this.exportColors = exportColors;
    }

    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    public void setExportSize(boolean exportSize) {
        this.exportSize = exportSize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public boolean isExportAttributes() {
        return exportAttributes;
    }

    public boolean isExportColors() {
        return exportColors;
    }

    public boolean isExportPosition() {
        return exportPosition;
    }

    public boolean isExportSize() {
        return exportSize;
    }

    public boolean isNormalize() {
        return normalize;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
