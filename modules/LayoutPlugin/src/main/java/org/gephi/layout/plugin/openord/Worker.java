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
package org.gephi.layout.plugin.openord;

import gnu.trove.TIntFloatHashMap;
import gnu.trove.TIntFloatIterator;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Mathieu Bastian
 */
public class Worker implements Runnable {

    //Thread
    private final int id;
    private final int numThreads;
    private final CyclicBarrier barrier;
    private boolean done = false;
    //Data
    private Node[] positions;
    private TIntFloatHashMap[] neighbors;
    private DensityGrid densityGrid;
    private boolean firstAdd = true;
    private boolean fineFirstAdd = true;
    //Settings
    private float attraction;
    private int STAGE;
    private float temperature;
    private float dampingMult;
    private float minEdges;
    private float cutEnd;
    private float cutOffLength;
    private boolean fineDensity;
    protected Random random;

    public Worker(int id, int numThreads, CyclicBarrier barrier) {
        this.barrier = barrier;
        this.id = id;
        this.numThreads = numThreads;
        this.densityGrid = new DensityGrid();
        this.densityGrid.init();
    }

    @Override
    public void run() {
        while (!isDone()) {
            //System.out.println("Execute worker " + id);

            //Updates nodes
            for (int i = id; i < positions.length; i += numThreads) {
                updateNodePos(i);
            }

            //Execute one more random if other threads manage one more node
            if (positions.length % numThreads != 0 && id > positions.length % numThreads - 1) {
                getNextRandom();
                getNextRandom();
            }

            firstAdd = false;
            if (fineDensity) {
                fineFirstAdd = false;
            }

            try {
                barrier.await();
            } catch (InterruptedException ex) {
                return;
            } catch (BrokenBarrierException ex) {
                return;
            }
        }
    }

    private void updateNodePos(int nodeIndex) {
        Node n = positions[nodeIndex];
        if (n.fixed) {
            getNextRandom();
            getNextRandom();
            return;
        }

        float[] energies = new float[2];
        float[][] updatedPos = new float[2][2];
        float jumpLength = 0.01f * temperature;
        densityGrid.substract(n, firstAdd, fineFirstAdd, fineDensity);

        energies[0] = getNodeEnergy(nodeIndex);
        solveAnalytic(nodeIndex);
        updatedPos[0][0] = n.x;
        updatedPos[0][1] = n.y;

        updatedPos[1][0] = updatedPos[0][0] + (.5f - getNextRandom()) * jumpLength;
        updatedPos[1][1] = updatedPos[0][1] + (.5f - getNextRandom()) * jumpLength;

        n.x = updatedPos[1][0];
        n.y = updatedPos[1][1];
        energies[1] = getNodeEnergy(nodeIndex);

        if (energies[0] < energies[1]) {
            n.x = updatedPos[0][0];
            n.y = updatedPos[0][1];
            n.energy = energies[0];
        } else {
            n.x = updatedPos[1][0];
            n.y = updatedPos[1][1];
            n.energy = energies[1];
        }

        densityGrid.add(n, fineDensity);
    }

    private float getNodeEnergy(int nodeIndex) {
        double attraction_factor = attraction * attraction
                * attraction * attraction * 2e-2;

        float xDis, yDis;
        float energyDistance;
        float nodeEnergy = 0;

        Node n = positions[nodeIndex];

        if (neighbors[nodeIndex] != null) {
            for (TIntFloatIterator itr = neighbors[nodeIndex].iterator(); itr.hasNext();) {
                itr.advance();
                float weight = itr.value();
                Node m = positions[itr.key()];

                xDis = n.x - m.x;
                yDis = n.y - m.y;

                energyDistance = xDis * xDis + yDis * yDis;
                if (STAGE < 2) {
                    energyDistance *= energyDistance;
                }

                if (STAGE == 0) {
                    energyDistance *= energyDistance;
                }

                nodeEnergy += weight * attraction_factor * energyDistance;
            }
        }

        nodeEnergy += densityGrid.getDensity(n.x, n.y, fineDensity);

        return nodeEnergy;
    }

    private void solveAnalytic(int nodeIndex) {
        float totalWeight = 0;
        float xDis, yDis, xCen = 0, yCen = 0;
        float x = 0, y = 0;
        float damping;

        TIntFloatHashMap map = neighbors[nodeIndex];
        if (map != null) {
            Node n = positions[nodeIndex];

            for (TIntFloatIterator itr = map.iterator(); itr.hasNext();) {
                itr.advance();
                float weight = itr.value();
                Node m = positions[itr.key()];

                totalWeight += weight;
                x += weight * m.x;
                y += weight * m.y;
            }

            if (totalWeight > 0) {
                xCen = x / totalWeight;
                yCen = y / totalWeight;
                damping = 1f - dampingMult;
                float posX = damping * n.x + (1f - damping) * xCen;
                float posY = damping * n.y + (1f - damping) * yCen;
                n.x = posX;
                n.y = posY;
            }

            if (minEdges == 99) {
                return;
            }
            if (cutEnd >= 39500) {
                return;
            }

            float maxLength = 0;
            int maxIndex = -1;
            int neighborsCount = map.size();
            if (neighborsCount >= minEdges) {
                for (TIntFloatIterator itr = neighbors[nodeIndex].iterator(); itr.hasNext();) {
                    itr.advance();
                    Node m = positions[itr.key()];

                    xDis = xCen - m.x;
                    yDis = yCen - m.y;
                    float dis = xDis * xDis + yDis * yDis;
                    dis *= Math.sqrt(neighborsCount);
                    if (dis > maxLength) {
                        maxLength = dis;
                        maxIndex = itr.key();
                    }
                }
            }

            if (maxLength > cutOffLength && maxIndex != -1) {
                map.remove(maxIndex);
            }
        }
    }

    public float getTotEnergy() {
        float myTotEnergy = 0;
        for (int i = id; i < positions.length; i += numThreads) {
            myTotEnergy += positions[i].energy;
        }
        return myTotEnergy;
    }

    public float getNextRandom() {
        float rand = 0;
        for (int i = 0; i < numThreads; i++) {
            if (i == id) {
                rand = random.nextFloat();
            } else {
                random.nextFloat(); //For other threads
            }
        }
        return rand;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setPositions(Node[] positions) {
        this.positions = positions;
    }

    public void setNeighbors(TIntFloatHashMap[] neighbors) {
        this.neighbors = neighbors;
    }

    public Node[] getPositions() {
        return positions;
    }

    public boolean isFineDensity() {
        return fineDensity;
    }

    public boolean isFineFirstAdd() {
        return fineFirstAdd;
    }

    public boolean isFirstAdd() {
        return firstAdd;
    }

    public DensityGrid getDensityGrid() {
        return densityGrid;
    }

    public TIntFloatHashMap[] getNeighbors() {
        return neighbors;
    }

    public void setSTAGE(int STAGE) {
        this.STAGE = STAGE;
    }

    public void setAttraction(float attraction) {
        this.attraction = attraction;
    }

    public void setCutOffLength(float cutOffLength) {
        this.cutOffLength = cutOffLength;
    }

    public void setDampingMult(float dampingMult) {
        this.dampingMult = dampingMult;
    }

    public void setMinEdges(float minEdges) {
        this.minEdges = minEdges;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setFineDensity(boolean fineDensity) {
        this.fineDensity = fineDensity;
    }

    public void setDensityGrid(DensityGrid densityGrid) {
        this.densityGrid = densityGrid;
    }

    public int getId() {
        return id;
    }
}
