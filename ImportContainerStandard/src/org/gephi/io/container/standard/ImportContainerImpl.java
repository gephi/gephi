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
    private FactoryImpl factory;

    //Parameters
    private ImportContainerParameters parameters;

    //Maps
    private HashMap<String, NodeDraftImpl> nodeMap;
    private HashMap<String, EdgeDraftImpl> edgeMap;
    private HashMap<String, EdgeDraftImpl> edgeSourceTargetMap;

    //Attributes
    private AttributeValueFactory attributeFactory;
    private AttributeManager attributeManager;

    //Error
    private ReportImpl report;

    public ImportContainerImpl() {
        parameters = new ImportContainerParameters();
        nodeMap = new HashMap<String, NodeDraftImpl>();
        edgeMap = new HashMap<String, EdgeDraftImpl>();
        edgeSourceTargetMap = new HashMap<String, EdgeDraftImpl>();
        attributeFactory = Lookup.getDefault().lookup(AttributeController.class).valueFactory();
        attributeManager = Lookup.getDefault().lookup(AttributeController.class).getTemporaryAttributeManager();
        factory = new FactoryImpl();
        report = new ReportImpl();
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
        if (nodeDraft == null) {
            severe("nodeDraft can't be null");
            return;
        }
        NodeDraftImpl nodeDraftImpl = (NodeDraftImpl) nodeDraft;

        if (nodeMap.containsKey(nodeDraftImpl.getId())) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId());
            severe(message);
            return;
        }

        nodeMap.put(nodeDraftImpl.getId(), nodeDraftImpl);
    }

    public NodeDraftImpl getNode(String id) {
        if (id == null || id.isEmpty()) {
            severe("id can't be null");
            return null;
        }
        NodeDraftImpl node = nodeMap.get(id);
        if (node == null) {
            if (parameters.isAutoNode()) {
                //Creates the missing node
                node = factory.newNodeDraft();
                node.setId(id);
                addNode(node);
                warning("Automatic node creation from id=" + id);
            } else {
                String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_UnknowNodeId", id);
                severe(message);
            }
        }
        return node;
    }

    public boolean nodeExists(String id) {
        if (id == null || id.isEmpty()) {
            severe("id can't be null");
            return false;
        }
        return nodeMap.containsKey(id);
    }

    public void addEdge(EdgeDraft edgeDraft) {
        if (edgeDraft == null) {
            severe("edgeDraft can't be null");
            return;
        }
        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        if (edgeDraftImpl.getSource() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            severe(message);
            return;
        }
        if (edgeDraftImpl.getTarget() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
            severe(message);
            return;
        }

        //Self loop
        if (edgeDraftImpl.getSource() == edgeDraftImpl.getTarget() && !parameters.isSelfLoops()) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_SelfLoop");
            severe(message);
            return;
        }

        switch (parameters.getEdgeDefault()) {
            case DIRECTED:
                EdgeDraft.EdgeType type1 = edgeDraftImpl.getType();
                if (type1.equals(EdgeDraft.EdgeType.UNDIRECTED)) {
                    severe(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type"));
                }
                break;
            case UNDIRECTED:
                EdgeDraft.EdgeType type2 = edgeDraftImpl.getType();
                if (type2.equals(EdgeDraft.EdgeType.DIRECTED)) {
                    severe(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type"));
                }
                break;
            case MIXED:
                break;
        }

        String id = edgeDraftImpl.getId();
        if (edgeMap.containsKey(id)) {
            if (!parameters.isParallelEdges()) {
                String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist");
                severe(message);
                return;
            } else {
                //Manage parallel edges
            }
        }

        edgeSourceTargetMap.put(edgeDraftImpl.getSource().getId() + "-" + edgeDraftImpl.getTarget().getId(), edgeDraftImpl);
        edgeMap.put(id, edgeDraftImpl);
    }

    public boolean edgeExists(String id) {
        if (id == null || id.isEmpty()) {
            severe("id can't be null");
            return false;
        }
        return edgeMap.containsKey(id);
    }

    public boolean edgeExists(NodeDraft source, NodeDraft target) {
        if (source == null || target == null) {
            severe("source of target can't be null");
            return false;
        }
        return edgeSourceTargetMap.containsKey(((NodeDraftImpl) source).getId() + "-" + ((NodeDraftImpl) target).getId());
    }

    public EdgeDraft getEdge(String id) {
        if (id == null || id.isEmpty()) {
            severe("id can't be null");
            return null;
        }
        return edgeMap.get(id);
    }

    public EdgeDraft getEdge(NodeDraft source, NodeDraft target) {
        if (source == null || target == null) {
            severe("source of target can't be null");
            return null;
        }
        return edgeSourceTargetMap.get(((NodeDraftImpl) source).getId() + "-" + ((NodeDraftImpl) target).getId());
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

    //LOGGING
    private void severe(String msg) {
        if (parameters.getErrorMode().equals(ErrorMode.ALL)) {
            throw new ImportContainerException(msg);
        }
        report.append(msg);
    }

    private void severe(RuntimeException e) {
        if (parameters.getErrorMode().equals(ErrorMode.ALL)) {
            throw e;
        }
        report.append(e);
    }

    private void warning(String msg) {
        report.append(msg);
    }

    public ContainerReport getReport() {
        return report;
    }

    /**
     * Factory for draft objects
     */
    public class FactoryImpl implements ContainerFactory {

        private int nodeIDgen = 0;
        private int edgeIDgen = 0;

        public NodeDraftImpl newNodeDraft() {
            NodeDraftImpl node = new NodeDraftImpl(ImportContainerImpl.this, source);
            node.setId("n" + nodeIDgen);
            nodeIDgen++;
            return node;
        }

        public EdgeDraftImpl newEdgeDraft() {
            EdgeDraftImpl edge = new EdgeDraftImpl(ImportContainerImpl.this, source);
            edge.setId("e" + edgeIDgen);
            edgeIDgen++;
            return edge;
        }
    }

    /**
     * Report about errors
     */
    public class ReportImpl implements ContainerReport {

        private StringBuilder stringBuilder = new StringBuilder();

        public void append(Exception e) {
            stringBuilder.append(e.getMessage()+"\n");
        }

        public void append(String str) {
            stringBuilder.append(str+"\n");
        }

        public String getReport() {
            return stringBuilder.toString();
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
