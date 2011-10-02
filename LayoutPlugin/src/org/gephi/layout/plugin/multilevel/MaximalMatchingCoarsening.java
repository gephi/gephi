/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.multilevel;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class MaximalMatchingCoarsening implements MultiLevelLayout.CoarseningStrategy {

    public void coarsen(HierarchicalGraph g) {
        HierarchicalGraph graph = g;
        int retract = 0;
        int count = 0;
        for (Edge e : graph.getEdgesAndMetaEdges().toArray()) {
            Node a = e.getSource();
            Node b = e.getTarget();
            count++;
            if (graph.getParent(a) == graph.getParent(b) && graph.getLevel(a) == 0) {
                float x = (a.getNodeData().x() + b.getNodeData().x()) / 2;
                float y = (a.getNodeData().y() + b.getNodeData().y()) / 2;

                Node parent = graph.groupNodes(new Node[]{a, b});
                parent.getNodeData().setX(x);
                parent.getNodeData().setY(y);
                graph.retract(parent);
                retract++;
            }
        }
    }

    public void refine(HierarchicalGraph graph) {
        double r = 10;
        int count = 0;
        int refined = 0;
        for (Node node : graph.getTopNodes().toArray()) {
            count++;
            if (graph.getChildrenCount(node) == 2) {
                refined++;
                float x = node.getNodeData().x();
                float y = node.getNodeData().y();

                for (Node child : graph.getChildren(node)) {
                    double t = Math.random();
                    child.getNodeData().setX((float) (x + r * Math.cos(t)));
                    child.getNodeData().setY((float) (y + r * Math.sin(t)));
                }
                graph.ungroupNodes(node);
            }
        }
        //System.out.println("COUNT = " + count);
        //System.out.println("REFINED = " + refined);
    }
}
