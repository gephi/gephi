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
    }

    public ContainerLoader getLoader() {
       return this;
    }

    public ContainerUnloader getUnloader() {
        return this;
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
            switch (parameters.nodeDoublePolicy) {
                case IGNORE:
                    return;
                case UNKNOWN:
                    String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId()));
                    logger(new ImportContainerException(message));
            }
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
            switch (parameters.edgeDoublePolicy) {
                case IGNORE:
                    return;
                case UNKNOWN:
                    String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist", id));
                    logger(new ImportContainerException(message));
            }
        }

        edgeMap.put(id, edgeDraftImpl);
    }

    public void checkNodeLabels() {
        for (NodeDraftImpl n : nodeMap.values()) {
            if (n.getLabel() == null || n.getLabel().isEmpty()) {
                n.setLabel(n.getId());
            }
        }
    }

    public void addNode(NodeDraft node, NodeDraft parent) {
        NodeDraftImpl nodeImpl = (NodeDraftImpl) node;
        addNode(node);
        parent.addChild(node);
        nodeImpl.hasParent = true;

    }

    public Collection<? extends NodeDraftGetter> getNodes() {
        return nodeMap.values();
    }

    public Collection<? extends EdgeDraftGetter> getEdges() {
        return edgeMap.values();
    }

    public NodeDraft newNodeDraft() {
        return new NodeDraftImpl(this, null);
    }

    public EdgeDraft newEdgeDraft() {
        return new EdgeDraftImpl(this, null);
    }

    public AttributeManager getAttributeManager() {
        return attributeManager;
    }

    private void logger(Exception e) {
        System.err.println(e.getMessage());
    }

    public AttributeValueFactory getFactory() {
        return attributeFactory;
    }
}
