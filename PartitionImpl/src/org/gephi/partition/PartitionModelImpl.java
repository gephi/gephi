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
package org.gephi.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.api.Transformer;
import org.gephi.partition.api.TransformerBuilder;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionModelImpl implements PartitionModel {

    //Architecture
    private List<ChangeListener> listeners;

    //Data
    private int selectedPartitioning = NODE_PARTITIONING;
    private Partition nodePartition;
    private Partition edgePartition;
    private TransformerBuilder nodeBuilder;
    private TransformerBuilder edgeBuilder;
    private HashMap<Class, Transformer> transformersMap;
    private NodePartition[] nodePartitions = new NodePartition[0];
    private EdgePartition[] edgePartitions = new EdgePartition[0];

    public PartitionModelImpl() {
        listeners = new ArrayList<ChangeListener>();
        transformersMap = new HashMap<Class, Transformer>();
    }

    public NodePartition[] getNodePartitions() {
        return nodePartitions;
    }

    public EdgePartition[] getEdgePartitions() {
        return edgePartitions;
    }

    public TransformerBuilder getNodeTransformerBuilder() {
        return nodeBuilder;
    }

    public Transformer getNodeTransformer() {
        if (nodeBuilder == null) {
            return null;
        }
        if (transformersMap.get(nodeBuilder.getClass()) != null) {
            return transformersMap.get(nodeBuilder.getClass());
        } else {
            Transformer t = nodeBuilder.getTransformer();
            transformersMap.put(nodeBuilder.getClass(), t);
            return t;
        }
    }

    public TransformerBuilder getEdgeTransformerBuilder() {
        return edgeBuilder;
    }

    public Transformer getEdgeTransformer() {
        if (edgeBuilder == null) {
            return null;
        }
        if (transformersMap.get(edgeBuilder.getClass()) != null) {
            return transformersMap.get(edgeBuilder.getClass());
        } else {
            Transformer t = edgeBuilder.getTransformer();
            transformersMap.put(edgeBuilder.getClass(), t);
            return t;
        }
    }

    public Partition getSelectedPartition() {
        if (selectedPartitioning == PartitionModel.NODE_PARTITIONING) {
            return nodePartition;
        } else if (selectedPartitioning == PartitionModel.EDGE_PARTITIONING) {
            return edgePartition;
        }
        return null;
    }

    public int getSelectedPartitioning() {
        return selectedPartitioning;
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    }

    //Setters
    public void setNodeBuilder(TransformerBuilder nodeBuilder) {
        this.nodeBuilder = nodeBuilder;
        fireChangeEvent();
    }

    public void setEdgeBuilder(TransformerBuilder edgeBuilder) {
        this.edgeBuilder = edgeBuilder;
        fireChangeEvent();
    }

    public void setSelectedPartitioning(int selectedPartitioning) {
        this.selectedPartitioning = selectedPartitioning;
        fireChangeEvent();
    }

    public void setNodePartition(Partition nodePartition) {
        this.nodePartition = nodePartition;
        fireChangeEvent();
    }

    public void setEdgePartition(Partition edgePartition) {
        this.edgePartition = edgePartition;
        fireChangeEvent();
    }

    public void setNodePartitions(NodePartition[] nodePartitions) {
        this.nodePartitions = nodePartitions;
        fireChangeEvent();
    }

    public void setEdgePartitions(EdgePartition[] edgePartitions) {
        this.edgePartitions = edgePartitions;
        fireChangeEvent();
    }
}
