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
package org.gephi.io.exporter.plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.gephi.io.exporter.spi.GraphFileExporterSettings;
import org.gephi.io.exporter.spi.XMLGraphFileExporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * @author Sebastien Heymann
 */
@ServiceProvider(service=XMLGraphFileExporter.class)
public class ExporterGEXF implements XMLGraphFileExporter, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;
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

    public boolean exportData(Document document, GraphFileExporterSettings settings) throws Exception {
        try {
            graphModel = settings.getWorkspace().getLookup().lookup(GraphModel.class);
            attributeModel = settings.getWorkspace().getLookup().lookup(AttributeModel.class);
            HierarchicalGraph graph = null;
            if (settings.isExportVisible()) {
                graph = graphModel.getHierarchicalGraphVisible();
            } else {
                graph = graphModel.getHierarchicalGraph();
            }
            exportData(document, graph, attributeModel);
        } catch (Exception e) {
            clean();
            throw e;
        }
        boolean c = cancel;
        clean();
        return !c;
    }

    private void clean() {
        cancel = false;
        progressTicket = null;
        minSize = 0f;
        maxSize = 0f;
        minX = 0f;
        maxX = 0f;
        minY = 0f;
        maxY = 0f;
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

        //Options
        if (normalize) {
            calculateMinMax(graph);
        }

        //Calculate progress units count
        int max;
        if(graphModel.isHierarchical()) {
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            max = hgraph.getNodeCount() + hgraph.getEdgeCount();
        } else {
            max = graph.getNodeCount() + graph.getEdgeCount();
        }
        Progress.switchToDeterminate(progressTicket, max);
        
        Element root = document.createElementNS("http://www.gexf.net/1.1draft", "gexf");
        root.setAttribute("xmlns:viz", "http:///www.gexf.net/1.1draft/viz");
        //root.setAttribute("xsi:schemaLocation", "http://www.gexf.net/1.1draft http://www.gexf.net/1.1draft/gexf.xsd");
        root.setAttribute("version", "1.1");
        document.appendChild(root);


        Element metaE = createMeta(document);
        root.appendChild(metaE);

        Element graphE = createGraph(document, graph);
        root.appendChild(graphE);

        Progress.finish(progressTicket);
        return !cancel;
    }

    private Element createMeta(Document document) throws Exception {
        Element metaE = document.createElement("meta");

        metaE.setAttribute("lastmodifieddate", getDateTime());

        Element creatorE = document.createElement("creator");
        Text creatorTextE = document.createTextNode("Gephi 0.7");
        creatorE.appendChild(creatorTextE);

        //TODO get description from Project properties

        /*Element keywordsE = document.createElement("keywords");
        Text keywordsTextE = document.createTextNode("");
        creatorE.appendChild(keywordsE);

        Element descriptionE = document.createElement("description");
        Text descriptionTextE = document.createTextNode("");
        creatorE.appendChild(descriptionE);*/

        metaE.appendChild(creatorE);

        return metaE;
    }

    private Element createGraph(Document document, Graph graph) throws Exception {
        Element graphE = document.createElement("graph");

        /*if(graphModel.isDynamic()) {
            graphE.setAttribute("type", "dynamic");
        }
        else {*/
            graphE.setAttribute("type", "static");
       /* }*/

        if(graphModel.isDirected()) {
            graphE.setAttribute("defaultedgetype", "directed");
        }
        else {
            graphE.setAttribute("defaultedgetype", "undirected"); // defaultValue
        }
        graphE.setAttribute("idtype", "string");

        //Attributes
        if (attributeModel != null) {
            //Node attributes
            boolean hasNodeColumns = false;
            Element nodeAttributesE = document.createElement("attributes");
            nodeAttributesE.setAttribute("class", "node");
            nodeAttributesE.setAttribute("mode", "static");
            for (AttributeColumn column : attributeModel.getNodeTable().getColumns()) {
                if(!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attributeE = createAttribute(document, column);
                    nodeAttributesE.appendChild(attributeE);
                    //TODO attribute options
                    hasNodeColumns = true;
                }
            }
            if(hasNodeColumns) {
                graphE.appendChild(nodeAttributesE);
            }
            //Edge attributes
            boolean hasEdgeColumns = false;
            Element edgeAttributesE = document.createElement("attributes");
            edgeAttributesE.setAttribute("class", "edge");
            edgeAttributesE.setAttribute("mode", "static");
            for (AttributeColumn column : attributeModel.getEdgeTable().getColumns()) {
                if(!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attributeE = createAttribute(document, column);
                    edgeAttributesE.appendChild(attributeE);
                    //TODO attribute options
                    hasEdgeColumns = true;
                }
            }
            if(hasEdgeColumns) {
                graphE.appendChild(edgeAttributesE);
            }
        }

        //Nodes
        int nodeCount = graph.getNodeCount();
        Element nodesE = createNodes(document, graph, nodeCount, null);
        graphE.appendChild(nodesE);

        //Edges
        Element edgesE = createEdges(document, graph);
        graphE.appendChild(edgesE);

        return graphE;
    }

    private Element createAttribute(Document document, AttributeColumn column) throws Exception {
        Element attributeE = document.createElement("attribute");
        attributeE.setAttribute("id", column.getId());
        attributeE.setAttribute("title", column.getTitle());
        attributeE.setAttribute("type", column.getType().getTypeString().toLowerCase());
        if(column.getDefaultValue() != null) {
            Element defaultE = document.createElement("default");
            Text defaultTextE = document.createTextNode(column.getDefaultValue().toString());
            defaultE.appendChild(defaultTextE);
        }
        return attributeE;
    }

    private Element createNodeAttvalue(Document document, AttributeColumn column, Node n) throws Exception {
        int index = column.getIndex();
        if(n.getNodeData().getAttributes().getValue(index) != null) {
            String value = n.getNodeData().getAttributes().getValue(index).toString();
            String id = column.getId();

            Element attvalueE = document.createElement("attvalue");
            attvalueE.setAttribute("for", id);
            attvalueE.setAttribute("value", value);
            return attvalueE;
        }
        return null;
    }

    private Element createEdgeAttvalue(Document document, AttributeColumn column, Edge e) throws Exception {
        int index = column.getIndex();
        if(e.getEdgeData().getAttributes().getValue(index) != null) {
            String value = e.getEdgeData().getAttributes().getValue(index).toString();
            String id = column.getId();

            Element attvalueE = document.createElement("attvalue");
            attvalueE.setAttribute("for", id);
            attvalueE.setAttribute("value", value);
            return attvalueE;
        }
        return null;
    }

    private Element createNodes(Document document, Graph graph, int count, Node nodeParent) throws Exception {
        Element nodesE = document.createElement("nodes");
        nodesE.setAttribute("count", ""+count);

        if(nodeParent != null) {
            // we are inside the tree
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            for( Node n : hgraph.getChildren(nodeParent)) {
                Element childE = createNode(document, graph, n);
                nodesE.appendChild(childE);
            }
        }
        else if(graphModel.isHierarchical()) {
            // we are on the top of the tree
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            for( Node n : hgraph.getTopNodes()) {
                Element nodeE = createNode(document, hgraph, n);
                nodesE.appendChild(nodeE);
            }
        }
        else {
            // there is no tree
            for( Node n : graph.getNodes() ) {
                Element nodeE = createNode(document, graph, n);
                nodesE.appendChild(nodeE);
            }
        }

        return nodesE;
    }

    private Element createNode(Document document, Graph graph, Node n) throws Exception {
        Element nodeE = document.createElement("node");
        nodeE.setAttribute("id", n.getNodeData().getId());
        nodeE.setAttribute("label", n.getNodeData().getLabel());

        //Attribute values
        if (attributeModel != null) {
            boolean hasColumns = false;
            Element attvaluesE = document.createElement("attvalues");
            for (AttributeColumn column : attributeModel.getNodeTable().getColumns()) {
                if(!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attvalueE = createNodeAttvalue(document, column, n);
                    if(attvalueE != null) {
                        attvaluesE.appendChild(attvalueE);
                        hasColumns = true;
                    }
                }
            }
            if(hasColumns) {
                nodeE.appendChild(attvaluesE);
            }
        }

        //Viz
        if(exportSize) {
            Element sizeE = createNodeSize(document, n);
            nodeE.appendChild(sizeE);
        }
        if(exportColors) {
            Element colorE = createNodeColors(document, n);
            nodeE.appendChild(colorE);
        }
        if(exportPosition) {
            Element positionE = createNodePosition(document, n);
            nodeE.appendChild(positionE);
        }

        //Hierarchy
        if(graphModel.isHierarchical()) {
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            int childCount = hgraph.getChildrenCount(n);
            if(childCount != 0) {
                Element nodesE = createNodes(document, graph, childCount, n);
                nodeE.appendChild(nodesE);
            }
        }
        Progress.progress(progressTicket);

        return nodeE;
    }

    private Element createEdges(Document document, Graph graph) throws Exception {
        Element edgesE = document.createElement("edges");
        edgesE.setAttribute("count", ""+graph.getEdgeCount());

        EdgeIterable it;
        if(graphModel.isHierarchical()) {
            HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
            it = hgraph.getEdgesAndMetaEdges();
        }
        else {
            it = graph.getEdges();
        }
        for( Edge e : it.toArray() ) {
            Element edgeE = createEdge(document, e);
            edgesE.appendChild(edgeE);
        }

        return edgesE;
    }

    private Element createEdge(Document document, Edge e) throws Exception {
        Element edgeE = document.createElement("edge");

        //edgeE.setAttribute("id", e.getEdgeData().getId());
        edgeE.setAttribute("source", e.getSource().getNodeData().getId());
        edgeE.setAttribute("target", e.getTarget().getNodeData().getId());

        if( e.isDirected() && !graphModel.isDirected() ) {
            edgeE.setAttribute("type", "directed");
        }
        else if( !e.isDirected() && graphModel.isDirected() ) {
            edgeE.setAttribute("type", "undirected");
        }

        String label = e.getEdgeData().getLabel();
        if( label != null && !label.isEmpty() ) {
            edgeE.setAttribute("label", label);
        }

        float weight = e.getWeight();
        if( weight != 1.0) {
            edgeE.setAttribute("weight", ""+weight);
        }

        //Attribute values
        if (attributeModel != null) {
            boolean hasColumns = false;
            Element attvaluesE = document.createElement("attvalues");
            for (AttributeColumn column : attributeModel.getEdgeTable().getColumns()) {
                if(!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    //Data or computed
                    Element attvalueE = createEdgeAttvalue(document, column, e);
                    if(attvalueE != null) {
                        attvaluesE.appendChild(attvalueE);
                        hasColumns = true;
                    }
                }
            }
            if(hasColumns) {
                edgeE.appendChild(attvaluesE);
            }
        }
        Progress.progress(progressTicket);

        return edgeE;
    }

    private Element createNodeSize(Document document, Node n) throws Exception {
        Element sizeE = document.createElement("viz:size");
        float size = n.getNodeData().getSize();
        if (normalize) {
            size = (size - minSize) / (maxSize - minSize);
        }
        sizeE.setAttribute("value", ""+size);

        return sizeE;
    }

    private Element createNodeColors(Document document, Node n) throws Exception {
        Element colorE = document.createElement("viz:color");
        colorE.setAttribute("r", ""+(Math.round(n.getNodeData().r() * 255f)));
        colorE.setAttribute("g", ""+(Math.round(n.getNodeData().g() * 255f)));
        colorE.setAttribute("b", ""+(Math.round(n.getNodeData().b() * 255f)));

        return colorE;
    }

    private Element createNodePosition(Document document, Node n) throws Exception {
        Element positionE = document.createElement("viz:position");
        float x = n.getNodeData().x();
        if (normalize && x != 0.0) {
            x = (x - minX) / (maxX - minX);
        }
        positionE.setAttribute("x", ""+x);

        float y = n.getNodeData().y();
        if (normalize && y != 0.0) {
            y = (y - minY) / (maxY - minY);
        }
        positionE.setAttribute("y", ""+y);

        float z = n.getNodeData().z();
        if (normalize && z != 0.0) {
            z = (z - minZ) / (maxZ - minZ);
        }
        positionE.setAttribute("z", ""+z);

        return positionE;
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
        return NbBundle.getMessage(getClass(), "ExporterGEXF_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gexf", NbBundle.getMessage(getClass(), "fileType_GEXF_Name"));
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

    
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd+HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
