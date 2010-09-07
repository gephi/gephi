/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.generator.plugin;

import java.util.Random;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Generator.class)
public class DynamicGraph implements Generator {

    protected int numberOfNodes = 50;
    protected double wiringProbability = 0.05;

    public void generate(ContainerLoader container) {
        Random random = new Random();

        AttributeColumn col = container.getAttributeModel().getNodeTable().addColumn("score", AttributeType.DYNAMIC_INT);

        NodeDraft[] nodeArray = new NodeDraft[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            NodeDraft nodeDraft = container.factory().newNodeDraft();
            nodeDraft.setId("n" + i);
            container.addNode(nodeDraft);

            Random r = new Random();
            int randomStart = r.nextInt(10) + 2000;
            int randomEnd = randomStart + 20 + r.nextInt(10);
            nodeDraft.addTimeInterval("" + randomStart, "" + randomEnd);

            randomEnd = randomStart + r.nextInt(10);
            nodeDraft.addAttributeValue(col, r.nextInt(5), ""+randomStart, ""+randomEnd);
            randomStart = randomEnd +1;
            randomEnd = randomStart + r.nextInt(10);
            nodeDraft.addAttributeValue(col, r.nextInt(5), ""+randomStart, ""+randomEnd);

            nodeArray[i] = nodeDraft;
        }

        if (wiringProbability > 0) {
            for (int i = 0; i < numberOfNodes - 1; i++) {
                NodeDraft node1 = nodeArray[i];
                for (int j = i + 1; j < numberOfNodes; j++) {
                    NodeDraft node2 = nodeArray[j];
                    if (random.nextDouble() < wiringProbability) {
                        EdgeDraft edgeDraft = container.factory().newEdgeDraft();
                        edgeDraft.setSource(node1);
                        edgeDraft.setTarget(node2);
                        container.addEdge(edgeDraft);
                    }
                }
            }
        }
    }

    public String getName() {
        return "Dynamic Graph";
    }

    public GeneratorUI getUI() {
        return null;
    }

    public boolean cancel() {
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
    }
}
