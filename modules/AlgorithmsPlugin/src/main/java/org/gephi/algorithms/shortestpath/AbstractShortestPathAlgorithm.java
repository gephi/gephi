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
package org.gephi.algorithms.shortestpath;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractShortestPathAlgorithm {

    protected final HashMap<Node, Color> colors;
    protected final HashMap<Node, Double> distances;
    protected final Node sourceNode;
    protected double maxDistance = 0;

    public AbstractShortestPathAlgorithm(Node sourceNode) {
        this.sourceNode = sourceNode;
        colors = new HashMap<>();
        distances = new HashMap<>();
    }

    protected boolean relax(Edge edge) {
        Node source = edge.getSource();
        Node target = edge.getTarget();
        double distSource = distances.get(source);
        double distTarget = distances.get(target);
        double weight = edgeWeight(edge);

        double sourceWeight = distSource + weight;
        if (sourceWeight < distTarget) {
            distances.put(target, sourceWeight);
            maxDistance = Math.max(maxDistance, sourceWeight);
            return true;
        } else {
            return false;
        }
    }

    protected double edgeWeight(Edge edge) {
        return edge.getWeight();
    }

    public abstract void compute();

    public abstract Map<Node, Edge> getPredecessors();

    public final Node getPredecessor(Node node) {
        Edge edge = getPredecessors().get(node);
        if (edge != null) {
            if (edge.getSource() != node) {
                return edge.getSource();
            } else {
                return edge.getTarget();
            }
        }
        return null;
    }

    public final Edge getPredecessorIncoming(Node node) {
        return getPredecessors().get(node);
    }

    public HashMap<Node, Double> getDistances() {
        return distances;
    }

    public double getMaxDistance() {
        return maxDistance;
    }
}
