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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class, position = 100)
public class NodeBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {

        Item[] items = new NodeItem[graph.getNodeCount()];
        int i = 0;
        for (Node n : graph.getNodes()) {
            NodeItem nodeItem = new NodeItem(n.getNodeData().getRootNode());
            nodeItem.setData(NodeItem.X, n.getNodeData().x());
            nodeItem.setData(NodeItem.Y, -n.getNodeData().y());
            nodeItem.setData(NodeItem.Z, n.getNodeData().z());
            nodeItem.setData(NodeItem.SIZE, n.getNodeData().getSize() * 2f);
            nodeItem.setData(NodeItem.COLOR, new Color((int) (n.getNodeData().r() * 255),
                    (int) (n.getNodeData().g() * 255),
                    (int) (n.getNodeData().b() * 255),
                    (int) (n.getNodeData().alpha() * 255)));
            items[i++] = nodeItem;
        }
        return items;
    }

    public String getType() {
        return ItemBuilder.NODE_BUILDER;
    }
}
