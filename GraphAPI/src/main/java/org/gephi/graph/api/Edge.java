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
package org.gephi.graph.api;

/**
 * Edge interface. An edge connects two nodes and can be <b>directed</b> or
 * not.
 * <p>
 * If working with several {@link GraphView}, note that <code>source</code> and
 * <code>target</code> are always nodes in the <b>main</b> view. To obtain nodes
 * in other views, do:
 * <pre>
 * GraphView view = ...;
 * Node node = node.getNodeData().getNode(view.getViewId());
 * </pre>
 * 
 * @author Mathieu Bastian
 */
public interface Edge extends Attributable {

    /**
     * Returns the unique identifier of the edge.
     * @return          the Id of the edge
     */
    public int getId();

    /**
     * Returns the source of the edge.
     * @return          the source of the edge
     */
    public Node getSource();

    /**
     * Returns the target of the edge.
     * @return          the target of the edge
     */
    public Node getTarget();

    /**
     * Returns the weight of the edge. Default value is 1.0.
     * @return          the weight of the edge
     */
    public float getWeight();

    /**
     * Returns the weight of the edge for the given time interval.
     * If the weight is dynamic, it has several values over time. This method
     * returns the weight for a particular interval.
     * @param low       the lower interval bound, can be
     *                  <code>Double.NEGATIVE_INFINITY</code>
     * @param high      the upper interval bound, can be
     *                  <code>Double.POSITIVE_INFINITY</code>
     * @return          the weight of the edge at this [low,high] interval
     */
    public float getWeight(double low, double high);

    /**
     * Set the weight of the edge.
     * @param weight    the weight of the edge
     */
    public void setWeight(float weight);

    /**
     * Returns <code>true</code> if the edge is directed or <code>false</code> if it's undirected.
     * Default is directed. Note that value is immutable.<p>
     * Special cases:
     * <ul><li>If the edge has been created from an <b>undirected</b> graph, returns false.</li>
     * <li>If the edge has been created from a <b>directed</b> graph, returns true.</li>
     * <li>If the edge has been created from a <b>mixed</b> graph, returns the value set to the
     * <code>addEdge()</code> method.</li></ul>
     * @return           <code>true</code> if the edge is directed, <code>false</code> otherwise
     */
    public boolean isDirected();

    /**
     * Returns <code>true</code> if edge source and target are the same.
     * @return          <code>true</code> if the edge is a self-loop, <code>false</code> otherwise
     */
    public boolean isSelfLoop();

    /**
     * Returns edge data.
     * @return          edge data instance
     */
    public EdgeData getEdgeData();
}
