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
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
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
public class MultiGraph implements Generator {

    protected int numberOfNodes = 50;
    protected double wiringProbability = 0.05;
    protected int numberOfEdgeTypes = 3;

    @Override
    public void generate(ContainerLoader container) {

        NodeDraft[] nodeArray = new NodeDraft[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            NodeDraft nodeDraft = container.factory().newNodeDraft("n" + i);
            container.addNode(nodeDraft);

            nodeArray[i] = nodeDraft;
        }

        String[] edgeTypes = new String[numberOfEdgeTypes];
        for (int i = 0; i < edgeTypes.length; i++) {
            edgeTypes[i] = "Type " + i;
        }

        Random random = new Random();

        if (wiringProbability > 0) {
            for (int i = 0; i < numberOfNodes - 1; i++) {
                NodeDraft node1 = nodeArray[i];
                for (int j = i + 1; j < numberOfNodes; j++) {
                    NodeDraft node2 = nodeArray[j];
                    if (random.nextDouble() < wiringProbability) {
                        if (random.nextDouble() < 0.3) {
                            //Double
                            EdgeDraft edgeDraft1 = container.factory().newEdgeDraft();
                            edgeDraft1.setSource(node1);
                            edgeDraft1.setTarget(node2);
                            edgeDraft1.setType(edgeTypes[0]);
                            edgeDraft1.setLabel((String) edgeDraft1.getType());

                            container.addEdge(edgeDraft1);

                            EdgeDraft edgeDraft2 = container.factory().newEdgeDraft();
                            edgeDraft2.setSource(node1);
                            edgeDraft2.setTarget(node2);
                            edgeDraft2.setType(edgeTypes[1]);
                            edgeDraft2.setLabel((String) edgeDraft2.getType());

                            container.addEdge(edgeDraft2);
                        } else {
                            //Single
                            EdgeDraft edgeDraft = container.factory().newEdgeDraft();
                            edgeDraft.setSource(node1);
                            edgeDraft.setTarget(node2);
                            edgeDraft.setType(edgeTypes[random.nextInt(edgeTypes.length)]);
                            edgeDraft.setLabel((String) edgeDraft.getType());

                            container.addEdge(edgeDraft);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MultiGraph.class, "MultiGraph.name");
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
