package org.gephi.io.exporter.plugin;

import java.awt.Color;
import java.io.Writer;
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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
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
    private GraphModel graphModel;
    private AttributeModel attributeModel;
    //Settings
    private boolean normalize = false;
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportSize = true;
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

    public boolean execute() {
        attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        graphModel = workspace.getLookup().lookup(GraphModel.class);
        HierarchicalGraph graph = null;
        if (exportVisible) {
            graph = graphModel.getHierarchicalGraphVisible();
        } else {
            graph = graphModel.getHierarchicalGraph();
        }
        try {
            exportData(createDocument(), graph, attributeModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return !cancel;
    }

    public Document createDocument() throws ParserConfigurationException {
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
    public boolean exportData(Document document, HierarchicalGraph graph, AttributeModel model) throws Exception {
        Progress.start(progressTicket);

        graph.readLock();

        //Options
        calculateMinMax(graph);

        //Calculate progress units count
        int max;
        if (graphModel.isHierarchical()) {
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            max = hgraph.getNodeCount() + hgraph.getEdgeCount();
        } else {
            max = graph.getNodeCount() + graph.getEdgeCount();
        }
        Progress.switchToDeterminate(progressTicket, max);

        Element root = document.createElementNS("http://graphml.graphdrawing.org/xmlns", "graphml");
        document.appendChild(root);

        createKeys(document, root);

        Element graphE = createGraph(document, graph);
        root.appendChild(graphE);

        graph.readUnlockAll();

        if (!cancel) {
            transform(document);
        }

        Progress.finish(progressTicket);
        return !cancel;
    }

    private void createKeys(Document document, Element root) {
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
            Element colorKeyE = document.createElement("key");
            colorKeyE.setAttribute("id", "color");
            colorKeyE.setAttribute("attr.name", "color");
            colorKeyE.setAttribute("attr.type", "integer");
            colorKeyE.setAttribute("for", "node");
            root.appendChild(colorKeyE);
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
        if (attributeModel != null && exportAttributes) {
            //Node attributes
            for (AttributeColumn column : attributeModel.getNodeTable().getColumns()) {
                if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    Element attributeE = createAttribute(document, column);
                    attributeE.setAttribute("for", "node");
                    root.appendChild(attributeE);
                }
            }

            for (AttributeColumn column : attributeModel.getEdgeTable().getColumns()) {
                if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attributeE = createAttribute(document, column);
                    attributeE.setAttribute("for", "edge");
                    root.appendChild(attributeE);
                }
            }
        }
    }

    private Element createGraph(Document document, Graph graph) throws Exception {
        Element graphE = document.createElement("graph");

        if (graphModel.isDirected()) {
            graphE.setAttribute("edgedefault", "directed");
        } else {
            graphE.setAttribute("edgedefault", "undirected"); // defaultValue
        }

        //Nodes
        createNodes(document, graphE, graph, null);

        //Edges
        createEdges(document, graphE, graph);

        return graphE;
    }

    private Element createAttribute(Document document, AttributeColumn column) {
        Element attributeE = document.createElement("key");
        attributeE.setAttribute("id", column.getId());
        attributeE.setAttribute("attr.name", column.getTitle());
        switch (column.getType()) {
            case INT:
                attributeE.setAttribute("attr.type", "int");
                break;
            case LIST_STRING:
                // nothing to do
                break;
            case TIME_INTERVAL:
                // nothing to do
                break;
            default:
                attributeE.setAttribute("type", column.getType().getTypeString().toLowerCase());
                break;
        }
        if (column.getDefaultValue() != null) {
            Element defaultE = document.createElement("default");
            Text defaultTextE = document.createTextNode(column.getDefaultValue().toString());
            defaultE.appendChild(defaultTextE);
        }
        return attributeE;
    }

    private Element createNodeAttvalue(Document document, AttributeColumn column, Node n) throws Exception {
        int index = column.getIndex();
        if (n.getNodeData().getAttributes().getValue(index) != null) {
            String value = n.getNodeData().getAttributes().getValue(index).toString();
            String id = column.getId();

            Element attvalueE = document.createElement("data");
            attvalueE.setAttribute("key", id);
            attvalueE.setTextContent(value);
            return attvalueE;
        }
        return null;
    }

    private Element createEdgeAttvalue(Document document, AttributeColumn column, Edge e) throws Exception {
        int index = column.getIndex();
        if (e.getEdgeData().getAttributes().getValue(index) != null) {
            String value = e.getEdgeData().getAttributes().getValue(index).toString();
            String id = column.getId();

            Element attvalueE = document.createElement("data");
            attvalueE.setAttribute("key", id);
            attvalueE.setTextContent(value);
            return attvalueE;
        }
        return null;
    }

    private void createNodes(Document document, Element parentE, Graph graph, Node nodeParent) throws Exception {

        if (nodeParent != null) {
            Element graphE = document.createElement("graph");

            if (graphModel.isDirected()) {
                graphE.setAttribute("edgedefault", "directed");
            } else {
                graphE.setAttribute("edgedefault", "undirected"); // defaultValue
            }
            // we are inside the tree
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            for (Node n : hgraph.getChildren(nodeParent)) {
                Element childE = createNode(document, graph, n);
                graphE.appendChild(childE);
            }
            parentE.appendChild(graphE);
        } else if (graphModel.isHierarchical()) {
            // we are on the top of the tree
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            for (Node n : hgraph.getTopNodes()) {
                Element nodeE = createNode(document, hgraph, n);
                parentE.appendChild(nodeE);
            }
        } else {
            // there is no tree
            for (Node n : graph.getNodes()) {
                if (cancel) {
                    break;
                }
                Element nodeE = createNode(document, graph, n);
                parentE.appendChild(nodeE);
            }
        }
    }

    private Element createNode(Document document, Graph graph, Node n) throws Exception {
        Element nodeE = document.createElement("node");
        nodeE.setAttribute("id", n.getNodeData().getId());

        //Label
        if (n.getNodeData().getLabel() != null && !n.getNodeData().getLabel().isEmpty()) {
            Element labelE = createNodeLabel(document, n);
            nodeE.appendChild(labelE);
        }

        //Attribute values
        if (attributeModel != null && exportAttributes) {
            for (AttributeColumn column : attributeModel.getNodeTable().getColumns()) {
                if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attvalueE = createNodeAttvalue(document, column, n);
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
            Element colorE = createNodeColors(document, n);
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

        //Hierarchy
        if (graphModel.isHierarchical()) {
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            int childCount = hgraph.getChildrenCount(n);
            if (childCount != 0) {
                createNodes(document, nodeE, graph, n);
            }
        }
        Progress.progress(progressTicket);

        return nodeE;
    }

    private void createEdges(Document document, Element edgesE, Graph graph) throws Exception {

        EdgeIterable it;
        if (graphModel.isHierarchical()) {
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            it = hgraph.getEdgesAndMetaEdges();
        } else {
            it = graph.getEdges();
        }
        for (Edge e : it.toArray()) {
            if (cancel) {
                break;
            }
            Element edgeE = createEdge(document, e);
            edgesE.appendChild(edgeE);
        }
    }

    private Element createEdge(Document document, Edge e) throws Exception {
        Element edgeE = document.createElement("edge");

        edgeE.setAttribute("source", e.getSource().getNodeData().getId());
        edgeE.setAttribute("target", e.getTarget().getNodeData().getId());

        if (e.getEdgeData().getId() != null && !e.getEdgeData().getId().isEmpty() && !String.valueOf(e.getId()).equals(e.getEdgeData().getId())) {
            Element idE = createEdgeId(document, e);
            edgeE.appendChild(idE);
        }

        //Label
        if (e.getEdgeData().getLabel() != null && !e.getEdgeData().getLabel().isEmpty()) {
            Element labelE = createEdgeLabel(document, e);
            edgeE.appendChild(labelE);
        }

        Element weightE = createEdgeWeight(document, e);
        edgeE.appendChild(weightE);

        if (e.isDirected() && !graphModel.isDirected()) {
            edgeE.setAttribute("type", "directed");
        } else if (!e.isDirected() && graphModel.isDirected()) {
            edgeE.setAttribute("type", "undirected");
        }

        //Attribute values
        if (attributeModel != null) {
            for (AttributeColumn column : attributeModel.getEdgeTable().getColumns()) {
                if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attvalueE = createEdgeAttvalue(document, column, e);
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
        float size = n.getNodeData().getSize();
        if (normalize) {
            size = (size - minSize) / (maxSize - minSize);
        }
        sizeE.setAttribute("key", "size");
        sizeE.setTextContent("" + size);

        return sizeE;
    }

    private Element createNodeColors(Document document, Node n) throws Exception {
        Element colorE = document.createElement("data");
        Color color = new Color(n.getNodeData().r(), n.getNodeData().g(), n.getNodeData().b());
        colorE.setAttribute("key", "color");
        colorE.setTextContent("" + color.getRGB());
        return colorE;
    }

    private Element createNodePositionX(Document document, Node n) throws Exception {
        Element positionXE = document.createElement("data");
        float x = n.getNodeData().x();
        if (normalize && x != 0.0) {
            x = (x - minX) / (maxX - minX);
        }
        positionXE.setAttribute("key", "x");
        positionXE.setTextContent("" + x);
        return positionXE;
    }

    private Element createNodePositionY(Document document, Node n) throws Exception {
        Element positionYE = document.createElement("data");
        float y = n.getNodeData().y();
        if (normalize && y != 0.0) {
            y = (y - minY) / (maxY - minY);
        }
        positionYE.setAttribute("key", "y");
        positionYE.setTextContent("" + y);

        return positionYE;
    }

    private Element createNodePositionZ(Document document, Node n) throws Exception {
        Element positionZE = document.createElement("data");
        float z = n.getNodeData().z();
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
        labelE.setTextContent(n.getNodeData().getLabel());

        return labelE;
    }

    private Element createEdgeId(Document document, Edge e) throws Exception {
        Element idE = document.createElement("data");
        idE.setAttribute("key", "edgeid");
        idE.setTextContent(e.getEdgeData().getId());

        return idE;
    }

    private Element createEdgeWeight(Document document, Edge e) throws Exception {
        Element weightE = document.createElement("data");
        weightE.setAttribute("key", "weight");
        weightE.setTextContent(Double.toString(e.getWeight()));

        return weightE;
    }

    private Element createEdgeLabel(Document document, Edge e) throws Exception {
        Element labelE = document.createElement("data");
        labelE.setAttribute("key", "edgelabel");
        labelE.setTextContent(e.getEdgeData().getLabel());

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
            NodeData nodeData = node.getNodeData();
            minX = Math.min(minX, nodeData.x());
            maxX = Math.max(maxX, nodeData.x());
            minY = Math.min(minY, nodeData.y());
            maxY = Math.max(maxY, nodeData.y());
            minZ = Math.min(minZ, nodeData.z());
            maxZ = Math.max(maxZ, nodeData.z());
            minSize = Math.min(minSize, nodeData.getSize());
            maxSize = Math.max(maxSize, nodeData.getSize());
        }
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

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

    public boolean isExportVisible() {
        return exportVisible;
    }

    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
