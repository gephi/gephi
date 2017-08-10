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

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.*;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.EdgeLabelItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class)
public class EdgeLabelBuilder implements ItemBuilder {

    @Override
    public Item[] getItems(Graph graph) {
        //Build text
        VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
        Column[] edgeColumns = vizController != null ? vizController.getEdgeTextColumns() : null;

        List<Item> items = new ArrayList<>();
        for (Edge e : graph.getEdges()) {
            EdgeLabelItem labelItem = new EdgeLabelItem(e);
            String label = getLabel(e, edgeColumns, graph.getView());
            labelItem.setData(EdgeLabelItem.LABEL, label);
            TextProperties textData = e.getTextProperties();
            if (textData != null) {
                if (textData.getAlpha() != 0) {
                    labelItem.setData(EdgeLabelItem.COLOR, textData.getColor());
                }
//                labelItem.setData(EdgeLabelItem.WIDTH, textData.getWidth());
//                labelItem.setData(EdgeLabelItem.HEIGHT, textData.getHeight());
                labelItem.setData(EdgeLabelItem.SIZE, textData.getSize());
                labelItem.setData(EdgeLabelItem.VISIBLE, textData.isVisible());
                if (textData.isVisible()) {
                    items.add(labelItem);
                }
            } else if (label != null && !label.isEmpty()) {
                items.add(labelItem);
            }
        }
        return items.toArray(new Item[0]);
    }

    private String getLabel(Edge e, Column[] cols, GraphView view) {
        String str = "";
        if (cols != null) {
            int i = 0;
            for (Column c : cols) {
                if (i++ > 0) {
                    str += " - ";
                }
                Object val = e.getAttribute(c, view);
                str += val != null ? val : "";
            }
        }
        if (str.isEmpty()) {
            str = e.getLabel();
        }
        if (str == null) {
            str = "";
        }
        return str;
    }

    @Override
    public String getType() {
        return ItemBuilder.EDGE_LABEL_BUILDER;
    }
}
