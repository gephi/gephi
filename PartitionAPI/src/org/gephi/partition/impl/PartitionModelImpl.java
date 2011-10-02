/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.partition.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.spi.Transformer;
import org.gephi.partition.spi.TransformerBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionModelImpl implements PartitionModel {

    //Architecture
    private final List<PropertyChangeListener> listeners;
    //Data
    private int selectedPartitioning = NODE_PARTITIONING;
    private Partition nodePartition;
    private Partition edgePartition;
    private TransformerBuilder nodeBuilder;
    private TransformerBuilder edgeBuilder;
    private final HashMap<Class, Transformer> transformersMap;
    private NodePartition[] nodePartitions = new NodePartition[0];
    private EdgePartition[] edgePartitions = new EdgePartition[0];
    private boolean waiting;
    private boolean pie;
    private int visibleViewId = -1;
    private DynamicModel dynamicModel;

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

    public DynamicModel getDynamicModel() {
        if (dynamicModel == null) {
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            if (dynamicController != null) {
                dynamicModel = dynamicController.getModel();
            }
        }
        return dynamicModel;
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

    public Estimator getEstimator() {
        return dynamicModel.getEstimator();
    }

    public Estimator getNumberEstimator() {
        return dynamicModel.getNumberEstimator();
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
