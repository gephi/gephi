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

import java.util.Collection;
import org.gephi.importer.api.*;
import java.util.HashMap;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerImpl implements ImportContainer {

    //Parameters
    private ImportContainerParameters parameters;

    //Maps
    private HashMap<String, NodeDraft> nodeMap;
    private HashMap<String, EdgeDraft> edgeMap;

    public ImportContainerImpl() {
        parameters = new ImportContainerParameters();
        nodeMap = new HashMap<String, NodeDraft>();
        edgeMap = new HashMap<String, EdgeDraft>();
    }

    public void addNode(NodeDraft nodeDraft) {
        if (nodeDraft.getId() == null) {
            logger(new ImportContainerException("ImportContainerException_MissingNodeId"));
        }

        if (nodeMap.containsKey(nodeDraft.getId())) {
            switch (parameters.nodeDoublePolicy) {
                case IGNORE:
                    return;
                case UNKNOWN:
                    String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraft.getId()));
                    logger(new ImportContainerException(message));
            }
        }

        nodeMap.put(nodeDraft.getId(), nodeDraft);
    }

    public NodeDraft getNode(String id) {
        NodeDraft node = nodeMap.get(id);
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
        if (edgeDraft.getNodeSource() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            logger(new ImportContainerException(message));
        }
        if (edgeDraft.getNodeTarget() == null) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
            logger(new ImportContainerException(message));
        }

        String id = edgeDraft.getId();
        if (id == null) {
            id = edgeDraft.getNodeSource().getId() + " - " + edgeDraft.getNodeTarget().getId();
        }

        if (edgeMap.containsKey(edgeDraft.getId())) {
            switch (parameters.edgeDoublePolicy) {
                case IGNORE:
                    return;
                case UNKNOWN:
                    String message = String.format(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist", id));
                    logger(new ImportContainerException(message));
            }
        }

        edgeMap.put(id, edgeDraft);
    }

    public Collection<NodeDraft> getNodes()
    {
        return nodeMap.values();
    }

    public Collection<EdgeDraft> getEdges()
    {
        return edgeMap.values();
    }

    private void logger(Exception e) {
        System.err.println(e.getMessage());
    }
}
