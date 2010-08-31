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

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Graph;
import org.gephi.partition.spi.Transformer;
import org.gephi.partition.spi.TransformerBuilder;

/**
 *
 * @author Mathieu Bastian
 */
public interface PartitionController {

    public void setSelectedPartition(Partition partition);

    public void setSelectedPartitioning(int partitioning);

    public void setSelectedTransformerBuilder(TransformerBuilder builder);

    public void refreshPartitions();

    public Partition buildPartition(AttributeColumn column, Graph graph);

    public void transform(Partition partition, Transformer transformer);

    public boolean isGroupable(Partition partition);

    public boolean isUngroupable(Partition partition);

    public void group(Partition partition);

    public void ungroup(Partition partition);

    public void showPie(boolean showPie);

    public PartitionModel getModel();
}
