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
package org.gephi.io.exporter.standard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.XMLExporter;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sebastien Heymann
 */
@ServiceProvider(service=GraphFileExporter.class)
public class ExporterGEXF implements GraphFileExporter, XMLExporter, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;

    //Settings
    private boolean exportColors = true;
    private boolean exportPosition = true;
    private boolean exportSize = true;
    private boolean exportAttributes = true;

    public boolean exportData(Document document, Graph graph) throws Exception {
        Progress.start(progressTicket);

        Element root = document.createElementNS("http://www.gephi.org/gexf/1.1draft", "gexf");
        root.setAttribute("xmlns:viz", "http://gephi.org/gexf/1.1draft/viz");
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

        /*Element creatorE = document.createElement("creator");
        Text creatorTextE = document.createTextNode("Gephi 0.7");
        creatorE.appendChild(creatorTextE);

        Element keywordsE = document.createElement("keywords");
        Text keywordsTextE = document.createTextNode("");
        creatorE.appendChild(keywordsE);

        Element descriptionE = document.createElement("description");
        Text descriptionTextE = document.createTextNode("");
        creatorE.appendChild(descriptionE);*/

        return metaE;
    }

    private Element createGraph(Document document, Graph graph) throws Exception {
        Element graphE = document.createElement("graph");

        if(graph.getGraphModel().isDynamic()) {
            graphE.setAttribute("type", "dynamic");
        }
        else {
            graphE.setAttribute("type", "static");
        }

        if(graph.getGraphModel().isDirected()) {
            graphE.setAttribute("defaultedgetype", "directed");
        }
        else {
            graphE.setAttribute("defaultedgetype", "undirected"); // defaultValue
        }
        graphE.setAttribute("idtype", "string");

        Element nodesE = createNodes(document, graph);
        graphE.appendChild(nodesE);

        Element edgesE = createEdges(document, graph);
        graphE.appendChild(edgesE);

        return graphE;
    }

    private Element createNodes(Document document, Graph graph) throws Exception {
        Element nodesE = document.createElement("nodes");
        nodesE.setAttribute("count", ""+graph.getNodeCount());

        for( Node n : graph.getNodes().toArray() ) {
            Element nodeE = createNode(document, n);
            nodesE.appendChild(nodeE);
        }

        return nodesE;
    }

    private Element createNode(Document document, Node n) throws Exception {
        Element nodeE = document.createElement("node");
        nodeE.setAttribute("id", ""+n.getNodeData().getId());
        nodeE.setAttribute("label", ""+n.getNodeData().getLabel());
        

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
        

        return nodeE;
    }

    private Element createEdges(Document document, Graph graph) throws Exception {
        Element edgesE = document.createElement("edges");
        edgesE.setAttribute("count", ""+graph.getEdgeCount());

        for( Edge e : graph.getEdges().toArray() ) {
            Element edgeE = createEdge(document, graph, e);
            edgesE.appendChild(edgeE);
        }

        return edgesE;
    }

    private Element createEdge(Document document, Graph graph, Edge e) throws Exception {
        Element edgeE = document.createElement("edge");

        edgeE.setAttribute("id", ""+e.getEdgeData().getId());
        edgeE.setAttribute("source", ""+e.getSource().getNodeData().getId());
        edgeE.setAttribute("target", ""+e.getTarget().getNodeData().getId());

        if( e.isDirected() && !graph.getGraphModel().isDirected() ) {
            edgeE.setAttribute("type", "directed");
        }
        else if( !e.isDirected() && graph.getGraphModel().isDirected() ) {
            edgeE.setAttribute("type", "undirected");
        }

        String label = e.getEdgeData().getLabel();
        if( !label.isEmpty() ) {
            edgeE.setAttribute("label", label);
        }

        float weight = e.getWeight();
        if( weight != 1.0) {
            edgeE.setAttribute("weight", ""+weight);
        }

        return edgeE;
    }

    private Element createNodeSize(Document document, Node n) throws Exception {
        Element sizeE = document.createElement("viz:size");
        sizeE.setAttribute("value", ""+n.getNodeData().getSize());

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
        positionE.setAttribute("x", ""+n.getNodeData().x());
        positionE.setAttribute("y", ""+n.getNodeData().y());
        positionE.setAttribute("z", ""+n.getNodeData().z());

        return positionE;
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

    
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd+HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
