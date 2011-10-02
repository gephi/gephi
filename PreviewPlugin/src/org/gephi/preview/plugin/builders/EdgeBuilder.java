/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.plugin.builders;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class, position = 300)
public class EdgeBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {

        //If the edge weight is dynamic, we'll need the time interval
        TimeInterval timeInterval = null;
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        if (dynamicController != null) {
            timeInterval = DynamicUtilities.getVisibleInterval(dynamicController.getModel(graph.getGraphModel().getWorkspace()));
        }
        if (timeInterval == null) {
            timeInterval = new TimeInterval();
        }

        HierarchicalGraph hgraph = (HierarchicalGraph)graph;
        EdgeItem[] items = new EdgeItem[hgraph.getTotalEdgeCount()];
        int i = 0;
        for (Edge e : hgraph.getEdgesAndMetaEdges()) {
            EdgeItem item = new EdgeItem(e);
            item.setData(EdgeItem.WEIGHT, e.getWeight(timeInterval.getLow(), timeInterval.getHigh()));
            item.setData(EdgeItem.DIRECTED, e.isDirected());
            if (graph instanceof HierarchicalDirectedGraph) {
                item.setData(EdgeItem.MUTUAL, graph.getEdge(e.getTarget(), e.getSource()) != null);
            }
            item.setData(EdgeItem.SELF_LOOP, e.isSelfLoop());
            item.setData(EdgeItem.META_EDGE, e instanceof MetaEdge);
            item.setData(EdgeItem.COLOR, e.getEdgeData().r() == -1 ? null : new Color((int) (e.getEdgeData().r() * 255f),
                    (int) (e.getEdgeData().g() * 255f),
                    (int) (e.getEdgeData().b() * 255f),
                    (int) (e.getEdgeData().alpha() * 255f)));
            items[i++] = item;
        }
        return items;
    }

    public String getType() {
        return ItemBuilder.EDGE_BUILDER;
    }
}
