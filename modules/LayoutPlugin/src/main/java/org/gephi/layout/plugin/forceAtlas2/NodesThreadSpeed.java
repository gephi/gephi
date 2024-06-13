/*
Copyright 2008-2011 Gephi
Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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
package org.gephi.layout.plugin.forceAtlas2;

import java.util.stream.IntStream;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.RepulsionForce;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * @author Mathieu Jacomy
 */
public class NodesThreadSpeed implements Runnable {

    private final double[] nodesInfo;
    private final int[] nodesIndicesToIndexInNodesInfoArray;
    private final int from;
    private final int to;
    private final RegionSpeed rootRegion;
    private final boolean barnesHutOptimize;
    private final RepulsionForce repulsion;
    private final double barnesHutTheta;
    private final double gravity;
    private final RepulsionForce gravityForce;
    private final double scaling;
    private final int VALUES_PER_NODE;

    public NodesThreadSpeed(double[] nodesInfo, int[] nodesIndicesToIndexInNodesInfoArray, int from, int to, boolean barnesHutOptimize, double barnesHutTheta, double gravity,
            RepulsionForce gravityForce, double scaling, RegionSpeed rootRegion, RepulsionForce repulsion, int VALUES_PER_NODE) {
        this.nodesInfo = nodesInfo;
        this.nodesIndicesToIndexInNodesInfoArray = nodesIndicesToIndexInNodesInfoArray;
        this.from = from;
        this.to = to;
        this.rootRegion = rootRegion;
        this.barnesHutOptimize = barnesHutOptimize;
        this.repulsion = repulsion;
        this.barnesHutTheta = barnesHutTheta;
        this.gravity = gravity;
        this.gravityForce = gravityForce;
        this.scaling = scaling;
        this.VALUES_PER_NODE = VALUES_PER_NODE;
    }

    @Override
    public void run() {
        // Repulsion
        if (barnesHutOptimize) {
            IntStream.range(from, to).parallel().forEach(n1Index -> {
                int node1IndexInNodesInfo = nodesIndicesToIndexInNodesInfoArray[n1Index];
                rootRegion.applyForce(nodesInfo, node1IndexInNodesInfo, repulsion, barnesHutTheta);
                gravityForce.applyGravity(nodesInfo, node1IndexInNodesInfo, gravity / scaling);
            });
        } else {
            IntStream.range(from, to).parallel().forEach(n1Index -> {
                int node1IndexInNodesInfo = nodesIndicesToIndexInNodesInfoArray[n1Index];
                IntStream.range(0, n1Index).parallel().forEach(n2Index -> {
                    int node2IndexInNodesInfo = nodesIndicesToIndexInNodesInfoArray[n2Index];
                    repulsion.applyClassicRepulsion(nodesInfo, node1IndexInNodesInfo, node2IndexInNodesInfo);
                });
                gravityForce.applyGravity(nodesInfo, node1IndexInNodesInfo, gravity / scaling);
            });
        }
    }
}
