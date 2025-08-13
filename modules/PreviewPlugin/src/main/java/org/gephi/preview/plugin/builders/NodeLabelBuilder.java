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
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.TextProperties;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class, position = 200)
public class NodeLabelBuilder extends AbstractLabelBuilder implements ItemBuilder {

    @Override
    public Item[] getItems(Graph graph) {
        //Build text
        VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
        Workspace workspace = WorkspaceHelper.getWorkspace(graph);
        VisualisationModel vizModel = vizController.getModel(workspace);
        Column[] nodeColumns = vizModel.getNodeLabelColumns();

        List<Item> items = new ArrayList<>();
        NodeIterable nodeIterable = graph.getNodes();
        try {
            for (Node n : nodeIterable) {
                NodeLabelItem labelItem = new NodeLabelItem(n);
                String label = getLabel(n, nodeColumns, graph.getView());
                labelItem.setData(NodeLabelItem.LABEL, label);
                TextProperties textData = n.getTextProperties();
                if (textData != null) {
                    if (textData.getR() != -1) {
                        labelItem.setData(NodeLabelItem.COLOR, new Color((int) (textData.getR() * 255),
                            (int) (textData.getG() * 255),
                            (int) (textData.getB() * 255),
                            (int) (textData.getAlpha() * 255)));
                    }
//                labelItem.setData(NodeLabelItem.WIDTH, textData.getWidth());
//                labelItem.setData(NodeLabelItem.HEIGHT, textData.getHeight());
                    labelItem.setData(NodeLabelItem.SIZE, textData.getSize());
                    labelItem.setData(NodeLabelItem.VISIBLE, textData.isVisible());
                    labelItem.setData(NodeLabelItem.LABEL, label);
                    if (textData.isVisible() && !label.isEmpty()) {
                        items.add(labelItem);
                    }
                } else if (!label.isEmpty()) {
                    items.add(labelItem);
                }
            }
        } catch (Exception e) {
            nodeIterable.doBreak();
            Exceptions.printStackTrace(e);
        }
        return items.toArray(new Item[0]);
    }

    @Override
    public String getType() {
        return ItemBuilder.NODE_LABEL_BUILDER;
    }
}
