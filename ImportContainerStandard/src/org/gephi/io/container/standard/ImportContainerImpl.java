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
package org.gephi.io.container.standard;

import java.util.Collection;
import java.util.HashMap;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeManager;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.io.container.EdgeDefault;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.Container;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.ContainerUnloader;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.processor.EdgeDraftGetter;
import org.gephi.io.processor.NodeDraftGetter;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerImpl implements Container, ContainerLoader, ContainerUnloader {

    //MetaData
    private String source;

    //Factory
    ContainerFactory factory;

    //Parameters
    private ImportContainerParameters parameters;

    //Maps
    private HashMap<String, NodeDraftImpl> nodeMap;
    private HashMap<String, EdgeDraftImpl> edgeMap;

    //Attributes
    private AttributeValueFactory attributeFactory;
    private AttributeManager attributeManager;

    public ImportContainerImpl() {
        parameters = new ImportContainerParameters();
        nodeMap = new HashMap<String, NodeDraftImpl>();
        edgeMap = new HashMap<String, EdgeDraftImpl>();
        attributeFactory = Lookup.getDefault().lookup(AttributeController.class).valueFactory();
        attributeManager = Lookup.getDefault().lookup(AttributeController.class).getTemporaryAttributeManager();
        factory = new FactoryImpl();
    }

    public ContainerLoader getLoader() {
        return this;
    }

    public ContainerUnloader getUnloader() {
        return this;
    }

    public ContainerFactory factory() {
        return factory;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void addNode(NodeDraft nodeDraft) {
        NodeDraftImpl nodeDraftImpl = (NodeDraftImpl) nodeDraft;
        if (nodeDraftImpl.getId() == null) {
            logger(new ImportContainerException("ImportContainerException_MissingNodeId"));
        }

        if (nodeMap.containsKey(nodeDraftImpl.getId())) {

            String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId()));
            logger(new ImportContainerException(message));

        }

        nodeMap.put(nodeDraftImpl.getId(), nodeDraftImpl);
    }

    public NodeDraftImpl getNode(String id) {
        NodeDraftImpl node = nodeMap.get(id);
        if (node == null) {
            String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_UnknowNodeId", id));
            logger(new ImportContainerException(message));
        }
        return node;
    }

    public boolean nodeExists(String id) {
        return nodeMap.containsKey(id);
    }

    public void addEdge(EdgeDraft edgeDraft) {
        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        if (edgeDraftImpl.getSource() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            logger(new ImportContainerException(message));
        }
        if (edgeDraftImpl.getTarget() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
            logger(new ImportContainerException(message));
        }

        String id = edgeDraftImpl.getId();
        if (id == null) {
            id = edgeDraftImpl.getSource().getId() + " - " + edgeDraftImpl.getTarget().getId();
        }

        if (edgeMap.containsKey(edgeDraftImpl.getId())) {
            String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist", id));
            logger(new ImportContainerException(message));

        }

        edgeMap.put(id, edgeDraftImpl);
    }

    public boolean edgeExists(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean edgeExists(NodeDraft source, NodeDraft target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeDraft getEdge(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeDraft getEdge(NodeDraft source, NodeDraft target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    

    public Collection<? extends NodeDraftGetter> getNodes() {
        return nodeMap.values();
    }

    public Collection<? extends EdgeDraftGetter> getEdges() {
        return edgeMap.values();
    }

    public AttributeManager getAttributeManager() {
        return attributeManager;
    }

    public AttributeValueFactory getFactory() {
        return attributeFactory;
    }

    private void logger(Exception e) {
        System.err.println(e.getMessage());
    }

    /**
     * Factory for draft objects
     */
    public class FactoryImpl implements ContainerFactory {

        private int nodeIDgen = 0;
        private int edgeIDgen = 0;

        public NodeDraft newNodeDraft() {
            NodeDraftImpl node = new NodeDraftImpl(ImportContainerImpl.this, source);
            node.setId("n"+nodeIDgen);
            nodeIDgen++;
            return node;
        }

        public EdgeDraft newEdgeDraft() {
            EdgeDraftImpl edge = new EdgeDraftImpl(ImportContainerImpl.this, source);
            edge.setId("e"+edgeIDgen);
            edgeIDgen++;
            return edge;
        }
    }

    //PARAMETERS
    public void setAllowAutoNode(boolean value) {
        parameters.setAutoNode(value);
    }

    public void setAllowParallelEdge(boolean value) {
        parameters.setParallelEdges(value);
    }

    public void setAllowSelfLoop(boolean value) {
        parameters.setSelfLoops(value);
    }

    public void setEdgeDefault(EdgeDefault edgeDefault) {
        parameters.setEdgeDefault(edgeDefault);
    }

    public void setErrorMode(ErrorMode errorMode) {
        parameters.setErrorMode(errorMode);
    }

    public ErrorMode getErrorMode() {
        return parameters.getErrorMode();
    }

    public boolean allowAutoNode() {
        return parameters.isAutoNode();
    }

    public boolean allowParallelEdges() {
        return parameters.isParallelEdges();
    }

    public boolean allowSelfLoop() {
        return parameters.isSelfLoops();
    }

    public EdgeDefault getEdgeDefault() {
        return parameters.getEdgeDefault();
    }
}
