/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.generator.standard;

import java.util.Random;
import org.gephi.ui.generator.standard.RandomGraphPanel;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.generator.Generator;
import org.gephi.ui.generator.GeneratorUI;

/**
 *
 * @author Mathieu Bastian
 */
public class RandomGraph implements Generator {

    protected int numberOfNodes = 50;
    protected double wiringProbability = 0.05;

    public void generate(ContainerLoader container) {

        Random random = new Random();

        NodeDraft[] nodeArray = new NodeDraft[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            NodeDraft nodeDraft = container.factory().newNodeDraft();
            nodeDraft.setId("n"+i);
            container.addNode(nodeDraft);
            nodeArray[i] = nodeDraft;
        }

        for (int i = 0; i < numberOfNodes - 1; i++) {
            NodeDraft node1 = nodeArray[i];
            for (int j = i + 1; j < numberOfNodes; j++) {
                NodeDraft node2 = nodeArray[j];
                if(random.nextDouble() < wiringProbability) {
                    EdgeDraft edgeDraft = container.factory().newEdgeDraft();
                    edgeDraft.setSource(node1);
                    edgeDraft.setTarget(node2);
                    container.addEdge(edgeDraft);
                }
            }
        }
    }

    public String getName() {
        return "Random Graph";
    }

    public GeneratorUI getUI() {
        return new RandomGraphPanel.RandomGraphUI();
    }

    public void setNumberOfNodes(int numberOfNodes) {
        if (numberOfNodes < 0) {
            throw new IllegalArgumentException("# of nodes must be greater than 0");
        }
        this.numberOfNodes = numberOfNodes;
    }

    public void setWiringProbability(double wiringProbability) {
        if (wiringProbability < 0 || wiringProbability > 1) {
            throw new IllegalArgumentException("Wiring probability must be between 0 and 1");
        }
        this.wiringProbability = wiringProbability;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public double getWiringProbability() {
        return wiringProbability;
    }
}
