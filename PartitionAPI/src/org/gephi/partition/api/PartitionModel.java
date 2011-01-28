/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.partition.api;

import org.gephi.partition.spi.Transformer;
import org.gephi.partition.spi.TransformerBuilder;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Mathieu Bastian
 */
public interface PartitionModel {

    public static final int NODE_PARTITIONING = 1;
    public static final int EDGE_PARTITIONING = 2;
    public static final String NODE_TRANSFORMER = "nodeBuilder";
    public static final String EDGE_TRANSFORMER = "edgeBuilder";
    public static final String SELECTED_PARTIONING = "selectedPartitioning";
    public static final String NODE_PARTITION = "nodePartition";
    public static final String EDGE_PARTITION = "edgePartition";
    public static final String NODE_PARTITIONS = "nodePartitions";
    public static final String EDGE_PARTITIONS = "edgePartitions";
    public static final String WAITING = "waiting";
    public static final String PIE = "pie";

    public NodePartition[] getNodePartitions();

    public EdgePartition[] getEdgePartitions();

    public TransformerBuilder getSelectedTransformerBuilder();

    public TransformerBuilder getNodeTransformerBuilder();

    public TransformerBuilder getEdgeTransformerBuilder();

    public Transformer getSelectedTransformer();

    public Partition getSelectedPartition();

    public int getSelectedPartitioning();

    public boolean isWaiting();

    public boolean isPie();

    public void addPropertyChangeListener(PropertyChangeListener changeListener);

    public void removePropertyChangeListener(PropertyChangeListener changeListener);
}
