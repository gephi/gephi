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
package org.gephi.importer.container;

import org.gephi.importer.EdgeDraftImpl;
import org.gephi.importer.NodeDraftImpl;
import java.util.Collection;
import org.gephi.data.attributes.api.AttributeManager;
import org.gephi.importer.api.*;
import java.util.HashMap;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerImpl implements ImportContainer {

    //Parameters
    private ImportContainerParameters parameters;

    //Maps
    private HashMap<String, NodeDraftImpl> nodeMap;
    private HashMap<String, EdgeDraftImpl> edgeMap;

    //States
    private boolean hasHierarchy = false;

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
        if (edgeDraftImpl.getNodeSource() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            logger(new ImportContainerException(message));
        }
        if (edgeDraftImpl.getNodeTarget() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
            logger(new ImportContainerException(message));
        }

        String id = edgeDraftImpl.getId();
        if (id == null) {
            id = edgeDraftImpl.getNodeSource().getId() + " - " + edgeDraftImpl.getNodeTarget().getId();
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

    public boolean hasHierarchy() {
        return hasHierarchy;
    }

    public void addNode(NodeDraft node, NodeDraft parent) {
        NodeDraftImpl nodeImpl = (NodeDraftImpl) node;
        addNode(node);
        parent.addChild(node);
        nodeImpl.hasParent = true;

        hasHierarchy = true;
    }

    public Collection<? extends NodeDraft> getNodes() {
        return nodeMap.values();
    }

    public Collection<? extends EdgeDraft> getEdges() {
        return edgeMap.values();
    }

    public NodeDraft newNodeDraft() {
        return new NodeDraftImpl(this);
    }

    public EdgeDraft newEdgeDraft() {
        return new EdgeDraftImpl(this);
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
