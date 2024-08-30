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

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory.RepulsionForce;
import org.gephi.layout.plugin.forceAtlas2.force.IGravity;
import org.gephi.layout.plugin.forceAtlas2.force.IRepulsionNode;
import org.gephi.layout.plugin.forceAtlas2.force.IRepulsionRegion;

/**
 * @author Mathieu Jacomy
 */
public class NodesThread implements Runnable {

    private final Node[] nodes;
    private final int from;
    private final int to;
    private final Region rootRegion;
    private final IGravity GravityForce;
    private final ForceAtlas2.ForceAtlas2Params params;
    private final IRepulsionNode repulsionNode;
    private final IRepulsionRegion repulsionRegion;


    public NodesThread(Node[] nodes, int from, int to, ForceAtlas2.ForceAtlas2Params params, Region rootRegion, IGravity GravityForce,IRepulsionNode repulsionNode,IRepulsionRegion repulsionRegion) {
        this.nodes = nodes;
        this.from = from;
        this.to = to;
        this.rootRegion = rootRegion;
        this.params = params;
        this.repulsionNode=repulsionNode;
        this.repulsionRegion = repulsionRegion;
        this.GravityForce = GravityForce;
     
    }

    @Override
    public void run() {
        // Repulsion
        if (params.barnesHutOptimize) {
            for (int nIndex = from; nIndex < to; nIndex++) {
                Node n = nodes[nIndex];
                rootRegion.applyForce(n, this.repulsionNode,this.repulsionRegion, params.barnesHutTheta);
                GravityForce.accept(n, params.gravity / params.scalingRatio);
            }
        } else {
            for (int n1Index = from; n1Index < to; n1Index++) {
                Node n1 = nodes[n1Index];
                for (int n2Index = 0; n2Index < n1Index; n2Index++) {
                    Node n2 = nodes[n2Index];
                    repulsionNode.accept(n1, n2);
                }
                GravityForce.accept(n1, params.gravity / params.scalingRatio);
            }
        }

       
    }
}
