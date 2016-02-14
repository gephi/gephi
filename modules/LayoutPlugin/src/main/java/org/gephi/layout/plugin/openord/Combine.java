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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.gephi.graph.api.Graph;

/**
 *
 * @author Mathieu Bastian
 */
public class Combine implements Runnable {

    private final OpenOrdLayout layout;
    private final Object lock = new Object();
    private final Control control;

    public Combine(OpenOrdLayout layout) {
        this.layout = layout;
        this.control = layout.getControl();
    }

    @Override
    public void run() {
        //System.out.println("Combine results");

        Worker[] workers = layout.getWorkers();

        //Gather positions
        Node[] positions = null;
        for (Worker w : workers) {
            if (positions == null) {
                positions = w.getPositions();
            } else {
                Node[] workerPositions = w.getPositions();
                for (int i = w.getId(); i < positions.length; i += workers.length) {
                    positions[i] = workerPositions[i];
                }
            }
        }

        //Unfix positions if necessary
        if (!control.isRealFixed()) {
            for (Node n : positions) {
                n.fixed = false;
            }
        }

        //Combine density
        for (Worker w : workers) {
            DensityGrid densityGrid = w.getDensityGrid();
            boolean fineDensity = w.isFineDensity();
            boolean firstAdd = w.isFirstAdd();
            boolean fineFirstAdd = w.isFineFirstAdd();
            Node[] wNodes = w.getPositions();
            for (Worker z : workers) {
                if (w != z) {
                    Node[] zNodes = w.getPositions();
                    for (int i = z.getId(); i < wNodes.length; i += workers.length) {
                        densityGrid.substract(wNodes[i], firstAdd, fineFirstAdd, fineDensity);
                        densityGrid.add(zNodes[i], fineDensity);
                    }
                }
            }
        }

        //Redistribute positions to workers
        if (workers.length > 1) {
            for (Worker w : workers) {
                Node[] positionsCopy = new Node[positions.length];
                for (int i = 0; i < positions.length; i++) {
                    positionsCopy[i] = positions[i].clone();
                }
                w.setPositions(positionsCopy);
            }
        }

        float totEnergy = getTotEnergy();
        boolean done = !control.udpateStage(totEnergy);

        //Params
        for (Worker w : layout.getWorkers()) {
            control.initWorker(w);
        }

        //Write positions to nodes
        Graph graph = layout.getGraph();
        for (org.gephi.graph.api.Node n : graph.getNodes()) {
            if (n.getLayoutData() != null && n.getLayoutData() instanceof OpenOrdLayoutData) {
                OpenOrdLayoutData layoutData = n.getLayoutData();
                Node node = positions[layoutData.nodeId];
                n.setX(node.x * 10f);
                n.setY(node.y * 10f);
            }
        }

        //Finish
        if (!layout.canAlgo() || done) {
            for (Worker w : layout.getWorkers()) {
                w.setDone(true);
            }
            layout.setRunning(false);
        }

        //Synchronize with layout goAlgo()
        synchronized (lock) {
            lock.notify();
        }
    }

    private void printPositions(Node[] nodes) {
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMaximumFractionDigits(2);
        for (Node node : nodes) {
            String xStr = formatter.format((double) node.x);
            String yStr = formatter.format((double) node.y);
        }
    }

    public float getTotEnergy() {
        float totEnergy = 0;
        for (Worker w : layout.getWorkers()) {
            totEnergy += w.getTotEnergy();
        }
        return totEnergy;
    }

    public void waitForIteration() {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
