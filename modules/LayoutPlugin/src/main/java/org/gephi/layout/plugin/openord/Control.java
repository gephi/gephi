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

import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Mathieu Bastian
 */
public class Control {

    //Settings
    private int STAGE;
    private int iterations;
    private float temperature;
    private float attraction;
    private float dampingMult;
    private float minEdges;
    private float cutEnd;
    private float cutLengthEnd;
    private float cutOffLength;
    private float cutRate;
    private boolean fineDensity;
    //Vars
    private float edgeCut;
    private float realParm;
    //Exec
    private long startTime;
    private long stopTime;
    private int numNodes;
    private float highestSimilarity;
    private int realIterations;
    private boolean realFixed;
    private int totIterations;
    private int totExpectedIterations;
    private long totalTime;
    private Params params;
    private ProgressTicket progressTicket;

    public void initParams(Params params, int totalIterations) {
        this.params = params;
        STAGE = 0;
        iterations = 0;
        initStage(params.getInitial());
        minEdges = 20;
        fineDensity = false;

        cutEnd = cutLengthEnd = 40000f * (1f - edgeCut);
        if (cutLengthEnd <= 1f) {
            cutLengthEnd = 1f;
        }

        float cutLengthStart = 4f * cutLengthEnd;

        cutOffLength = cutLengthStart;
        cutRate = (cutLengthStart - cutLengthEnd) / 400f;

        totExpectedIterations = totalIterations;

        int fullCompIters = totExpectedIterations + 3;

        if (realParm < 0) {
            realIterations = (int) realParm;
        } else if (realParm == 1) {
            realIterations = fullCompIters + params.getSimmer().getIterationsTotal(totalIterations) + 100;
        } else {
            realIterations = (int) (realParm * fullCompIters);
        }
        System.out.println("Real iterations " + realIterations);

        if (realIterations > 0) {
            realFixed = true;
        } else {
            realFixed = false;
        }

        Progress.switchToDeterminate(progressTicket, totExpectedIterations);
    }

    private void initStage(Params.Stage stage) {
        temperature = stage.getTemperature();
        attraction = stage.getAttraction();
        dampingMult = stage.getDampingMult();
    }

    public void initWorker(Worker worker) {
        worker.setAttraction(attraction);
        worker.setCutOffLength(cutOffLength);
        worker.setDampingMult(dampingMult);
        worker.setMinEdges(minEdges);
        worker.setSTAGE(STAGE);
        worker.setTemperature(temperature);
        worker.setFineDensity(fineDensity);
    }

    public boolean udpateStage(float totEnergy) {
        int MIN = 1;

        totIterations++;
        if (totIterations >= realIterations) {
            realFixed = false;
        }

        Progress.progress(progressTicket, totIterations);
        //System.out.println("Progress "+progress+"%");

        if (STAGE == 0) {

            if (iterations == 0) {
                startTime = System.currentTimeMillis() / 1000;
                System.out.println(
                        "Entering liquid stage...");
            }

            if (iterations < params.getLiquid().getIterationsTotal(totExpectedIterations)) {
                initStage(params.getLiquid());
                iterations++;
            } else {
                stopTime = System.currentTimeMillis() / 1000;
                long timeElapsed = (stopTime - startTime);
                totalTime += timeElapsed;
                initStage(params.getExpansion());
                iterations = 0;

                System.out.println(String.format(
                        "Liquid stage completed in %d seconds, total energy = %f",
                        new Object[]{timeElapsed, totEnergy}));

                STAGE = 1;
                startTime = System.currentTimeMillis() / 1000;

                System.out.println(
                        "Entering expansion stage...");
            }
        }

        if (STAGE == 1) {

            if (iterations < params.getExpansion().getIterationsTotal(totExpectedIterations)) {
                // Play with vars
                if (attraction > 1) {
                    attraction -= .05;
                }
                if (minEdges > 12) {
                    minEdges -= .05;
                }
                cutOffLength -= cutRate;
                if (dampingMult > .1) {
                    dampingMult -= .005;
                }
                iterations++;

            } else {

                stopTime = System.currentTimeMillis() / 1000;
                long timeElapsed = (stopTime - startTime);
                totalTime += timeElapsed;

                System.out.println(String.format(
                        "Expansion stage completed in %d seconds, total energy = %f",
                        new Object[]{timeElapsed, totEnergy}));

                STAGE = 2;
                minEdges = 12;
                initStage(params.getCooldown());
                iterations = 0;
                startTime = System.currentTimeMillis() / 1000;

                System.out.println(
                        "Entering cool-down stage...");
            }
        } else if (STAGE == 2) {

            if (iterations < params.getCooldown().getIterationsTotal(totExpectedIterations)) {

                // Reduce temperature
                if (temperature > 50) {
                    temperature -= 10;
                }

                // Reduce cut length
                if (cutOffLength > cutLengthEnd) {
                    cutLengthEnd -= cutRate * 2;
                }
                if (minEdges > MIN) {
                    minEdges -= .2;
                }
                //min_edges = 99;
                iterations++;

            } else {

                stopTime = System.currentTimeMillis() / 1000;
                long timeElapsed = (stopTime - startTime);
                totalTime += timeElapsed;

                cutOffLength = cutLengthEnd;
                minEdges = MIN;
                //min_edges = 99; // In other words: no more cutting

                System.out.println(String.format(
                        "Cool-down stage completed in %d seconds, total energy = %f",
                        new Object[]{timeElapsed, totEnergy}));

                STAGE = 3;
                iterations = 0;
                initStage(params.getCrunch());
                startTime = System.currentTimeMillis() / 1000;

                System.out.println(
                        "Entering crunch stage...");
            }
        } else if (STAGE == 3) {

            if (iterations < params.getCrunch().getIterationsTotal(totExpectedIterations)) {
                iterations++;
            } else {
                stopTime = System.currentTimeMillis() / 1000;
                long timeElapsed = (stopTime - startTime);
                totalTime += timeElapsed;

                iterations = 0;
                initStage(params.getSimmer());
                minEdges = 99;
                fineDensity = true;

                System.out.println(String.format(
                        "Crunch stage completed in %d seconds, total energy = %f",
                        new Object[]{timeElapsed, totEnergy}));

                STAGE = 5;
                startTime = System.currentTimeMillis() / 1000;

                System.out.println(
                        "Entering simmer stage...");
            }
        } else if (STAGE == 5) {

            if (iterations < params.getSimmer().getIterationsTotal(totExpectedIterations)) {
                if (temperature > 50) {
                    temperature -= 2;
                }
                iterations++;

            } else {
                stopTime = System.currentTimeMillis() / 1000;
                long timeElapsed = (stopTime - startTime);
                totalTime += timeElapsed;

                System.out.println(String.format(
                        "Simmer stage completed in %d seconds, total energy = %f",
                        new Object[]{timeElapsed, totEnergy}));

                STAGE = 6;

                System.out.println(String.format(
                        "Layout completed in %d seconds with %d iterations",
                        new Object[]{totalTime, totIterations}));
            }
        } else if (STAGE == 6) {
            return false;
        }

        return true;
    }

    public boolean isRealFixed() {
        return realFixed;
    }

    public float getHighestSimilarity() {
        return highestSimilarity;
    }

    public void setHighestSimilarity(float highestSimilarity) {
        this.highestSimilarity = highestSimilarity;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public void setEdgeCut(float edgeCut) {
        this.edgeCut = edgeCut;
    }

    public void setRealParm(float realParm) {
        this.realParm = realParm;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
