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
package org.gephi.partition.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.spi.Transformer;
import org.gephi.partition.spi.TransformerBuilder;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionModelImpl implements PartitionModel {

    //Architecture
    private List<PropertyChangeListener> listeners;
    //Data
    private int selectedPartitioning = NODE_PARTITIONING;
    private Partition nodePartition;
    private Partition edgePartition;
    private TransformerBuilder nodeBuilder;
    private TransformerBuilder edgeBuilder;
    private HashMap<Class, Transformer> transformersMap;
    private NodePartition[] nodePartitions = new NodePartition[0];
    private EdgePartition[] edgePartitions = new EdgePartition[0];
    private boolean waiting;
    private boolean pie;
    private int visibleViewId = -1;

    public PartitionModelImpl() {
        listeners = new ArrayList<PropertyChangeListener>();
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

    public Transformer getSelectedTransformer() {
        if (selectedPartitioning == PartitionModel.NODE_PARTITIONING) {
            return getNodeTransformer();
        } else if (selectedPartitioning == PartitionModel.EDGE_PARTITIONING) {
            return getEdgeTransformer();
        }
        return null;
    }

    public TransformerBuilder getSelectedTransformerBuilder() {
        if (selectedPartitioning == PartitionModel.NODE_PARTITIONING) {
            return nodeBuilder;
        } else if (selectedPartitioning == PartitionModel.EDGE_PARTITIONING) {
            return edgeBuilder;
        }
        return null;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isPie() {
        return pie;
    }

    public int getVisibleViewId() {
        return visibleViewId;
    }

    public void addPropertyChangeListener(PropertyChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void firePropertyChangeEvent(String key, Object oldValue, Object newValue) {
        //System.out.println("fire "+key+" = "+newValue);
        PropertyChangeEvent evt = new PropertyChangeEvent(this, key, oldValue, newValue);
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(evt);
        }
    }

    //Setters
    public void setNodeBuilder(TransformerBuilder nodeBuilder) {
        if (nodeBuilder != this.nodeBuilder) {
            TransformerBuilder oldValue = this.nodeBuilder;
            this.nodeBuilder = nodeBuilder;
            firePropertyChangeEvent(NODE_TRANSFORMER, oldValue, nodeBuilder);
        }
    }

    public void setEdgeBuilder(TransformerBuilder edgeBuilder) {
        if (edgeBuilder != this.edgeBuilder) {
            TransformerBuilder oldValue = this.edgeBuilder;
            this.edgeBuilder = edgeBuilder;
            firePropertyChangeEvent(EDGE_TRANSFORMER, oldValue, edgeBuilder);
        }
    }

    public void setSelectedPartitioning(int selectedPartitioning) {
        if (selectedPartitioning != this.selectedPartitioning) {
            int oldValue = this.selectedPartitioning;
            this.selectedPartitioning = selectedPartitioning;
            firePropertyChangeEvent(SELECTED_PARTIONING, oldValue, selectedPartitioning);
        }
    }

    public void setNodePartition(Partition nodePartition) {
        if (nodePartition != this.nodePartition) {
            Partition oldValue = this.nodePartition;
            this.nodePartition = nodePartition;
            firePropertyChangeEvent(NODE_PARTITION, oldValue, nodePartition);
        }
    }

    public void setEdgePartition(Partition edgePartition) {
        if (edgePartition != this.edgePartition) {
            Partition oldValue = this.edgePartition;
            this.edgePartition = edgePartition;
            firePropertyChangeEvent(EDGE_PARTITION, oldValue, edgePartition);
        }
    }

    public void setNodePartitions(NodePartition[] nodePartitions) {
        if (nodePartitions != this.nodePartitions) {
            Partition[] oldValue = this.nodePartitions;
            this.nodePartitions = nodePartitions;
            firePropertyChangeEvent(NODE_PARTITIONS, oldValue, nodePartitions);
        }
    }

    public void setEdgePartitions(EdgePartition[] edgePartitions) {
        if (edgePartitions != this.edgePartitions) {
            Partition[] oldValue = this.edgePartitions;
            this.edgePartitions = edgePartitions;
            firePropertyChangeEvent(EDGE_PARTITIONS, oldValue, edgePartitions);
        }
    }

    public void setWaiting(boolean waiting) {
        if (waiting != this.waiting) {
            boolean oldValue = this.waiting;
            this.waiting = waiting;
            firePropertyChangeEvent(WAITING, oldValue, waiting);
        }
    }

    public void setPie(boolean pie) {
        if (pie != this.pie) {
            boolean oldValue = this.pie;
            this.pie = pie;
            firePropertyChangeEvent(PIE, oldValue, pie);
        }
    }

    public void setVisibleViewId(int visibleViewId) {
        this.visibleViewId = visibleViewId;
    }
}
