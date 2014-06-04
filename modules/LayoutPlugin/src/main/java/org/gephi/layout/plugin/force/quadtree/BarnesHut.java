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
package org.gephi.layout.plugin.force.quadtree;

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.ForceVectorUtils;
import org.gephi.layout.plugin.force.AbstractForce;
import org.gephi.layout.plugin.force.ForceVector;

/**
 * Barnes-Hut's O(n log n) force calculation algorithm.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class BarnesHut {

    /* theta is the parameter for Barnes-Hut opening criteria
     */
    private float theta = (float) 1.2;
    private AbstractForce force;

    public BarnesHut(AbstractForce force) {
        this.force = force;
    }

    /* Calculates the ForceVector on node against every other node represented
     * in the tree with respect to force.
     */
    public ForceVector calculateForce(Node node, QuadTree tree) {
        if (tree.mass() <= 0) {
            return null;
        }

        float distance = ForceVectorUtils.distance(node, tree);

        if (tree.isIsLeaf() || tree.mass() == 1) {
            // this is probably the case where tree has only the node.
            if (distance < 1e-8) {
                return null;
            }
            return force.calculateForce(node, tree);
        }

        if (distance * theta > tree.size()) {
            ForceVector f = force.calculateForce(node, tree, distance);
            f.multiply(tree.mass());
            return f;
        }

        ForceVector f = new ForceVector();
        for (QuadTree child : tree.getChildren()) {
            f.add(calculateForce(node, child));
        }
        return f;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public float getTheta() {
        return theta;
    }
}
