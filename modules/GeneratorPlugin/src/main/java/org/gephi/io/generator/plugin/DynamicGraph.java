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
package org.gephi.io.generator.plugin;

import java.util.Random;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Generator.class)
public class DynamicGraph implements Generator {

    protected int numberOfNodes = 50;
    protected double wiringProbability = 0.05;

    @Override
    public void generate(ContainerLoader container) {
        Random random = new Random();
        double start = 2000.0;
        double end = 2015.0;
        double tick = 1.0;
        ColumnDraft col = container.addNodeColumn("score", Integer.class, true);
        container.setTimeRepresentation(TimeRepresentation.TIMESTAMP);

        NodeDraft[] nodeArray = new NodeDraft[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            NodeDraft nodeDraft = container.factory().newNodeDraft("n" + i);
            container.addNode(nodeDraft);

            Random r = new Random();
            for (double t = start; t < end; t += tick) {
                if (r.nextBoolean()) {
                    nodeDraft.addTimestamp(t);
                    nodeDraft.setValue(col.getId(), r.nextInt(5), t);
                }
            }

            nodeArray[i] = nodeDraft;
        }

        if (wiringProbability > 0) {
//            AttributeColumn oldWeight = container.getGraphModel().getEdgeTable().getColumn(PropertiesColumn.EDGE_WEIGHT.getIndex());
//            AttributeColumn weightCol = container.getGraphModel().getEdgeTable().replaceColumn(oldWeight, PropertiesColumn.EDGE_WEIGHT.getId(), PropertiesColumn.EDGE_WEIGHT.getTitle(), AttributeType.DYNAMIC_FLOAT, AttributeOrigin.PROPERTY, null);

            for (int i = 0; i < numberOfNodes - 1; i++) {
                NodeDraft node1 = nodeArray[i];
                for (int j = i + 1; j < numberOfNodes; j++) {
                    NodeDraft node2 = nodeArray[j];
                    if (random.nextDouble() < wiringProbability) {
                        EdgeDraft edgeDraft = container.factory().newEdgeDraft();
                        edgeDraft.setSource(node1);
                        edgeDraft.setTarget(node2);

                        Random r = new Random();
//                        DynamicFloat dynamicWeight = new DynamicFloat(new Interval<Float>(2010, 2012, false, true, new Float(r.nextInt(3) + 1)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2012, 2014, false, true, new Float(r.nextInt(3) + 2)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2014, 2016, false, true, new Float(r.nextInt(3) + 3)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2016, 2018, false, true, new Float(r.nextInt(3) + 4)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2018, 2020, false, true, new Float(r.nextInt(3) + 5)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2020, 2022, false, true, new Float(r.nextInt(3) + 6)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2022, 2024, false, true, new Float(r.nextInt(3) + 7)));
//                        dynamicWeight = new DynamicFloat(dynamicWeight, new Interval<Float>(2024, 2026, false, false, new Float(r.nextInt(3) + 8)));
//                        edgeDraft.addAttributeValue(weightCol, dynamicWeight);

                        container.addEdge(edgeDraft);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DynamicGraph.class, "DynamicGraph.name");
    }

    @Override
    public GeneratorUI getUI() {
        return null;
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
    }
}
