/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance.api;

import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.project.api.Workspace;

/**
 * Entry point to access the appearance functions.
 * <p>
 * One model exists for each workspace.
 */
public interface AppearanceModel {

    /**
     * Identifies the non-column-based functions.
     */
    public enum GraphFunction {
        NODE_DEGREE("degree"),
        NODE_INDEGREE("indegree"),
        NODE_OUTDEGREE("outdegree"),
        EDGE_WEIGHT("weight"),
        EDGE_TYPE("type");

        private final String id;

        GraphFunction(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Return the workspace this model is associated with
     *
     * @return the workspace of this model
     */
    public Workspace getWorkspace();

    /**
     * Returns <code>true</code> if rankings are using the currently visible
     * graph as a scale. If <code>false</code> the complete graph is used to
     * determine minimum and maximum values, the ranking scale.
     *
     * @return <code>true</code> if using a local scale, <code>false</code> if
     * global scale
     */
    public boolean isLocalScale();

    /**
     * Returns the node partition for this graph and column.
     *
     * @param graph graph
     * @param column column
     * @return node partition of null if it doesn't exist
     */
    public Partition getNodePartition(Graph graph, Column column);

    /**
     * Returns the edge partition for this graph and column.
     *
     * @param graph graph
     * @param column column
     * @return edge partition of null if it doesn't exist
     */
    public Partition getEdgePartition(Graph graph, Column column);

    /**
     * Returns all node functions for the given graph.
     *
     * @param graph graph
     * @return all node functions
     */
    public Function[] getNodeFunctions(Graph graph);

    /**
     * Returns the node function for the given column and transformer.
     *
     * @param graph graph
     * @param column column
     * @param transformer transformer class
     * @return node function or null if not found
     */
    public Function getNodeFunction(Graph graph, Column column, Class<? extends Transformer> transformer);

    /**
     * Returns the node function for the given graph function identifier and
     * transformer.
     *
     * @param graph graph
     * @param graphFunction graphFunction
     * @param transformer transformer class
     * @return node function or null if not found
     */
    public Function getNodeFunction(Graph graph, GraphFunction graphFunction, Class<? extends Transformer> transformer);

    /**
     * Returns all edge functions for the given graph.
     *
     * @param graph graph
     * @return all edge functions
     */
    public Function[] getEdgeFunctions(Graph graph);

    /**
     * Returns the node function for the given column and transformer.
     *
     * @param graph graph
     * @param column column
     * @param transformer transformer class
     * @return edge function or null if not found
     */
    public Function getEdgeFunction(Graph graph, Column column, Class<? extends Transformer> transformer);

    /**
     * Returns the edge function for the given graph function identifier and
     * transformer.
     *
     * @param graph graph
     * @param graphFunction graphFunction
     * @param transformer transformer class
     * @return edge function or null if not found
     */
    public Function getEdgeFunction(Graph graph, GraphFunction graphFunction, Class<? extends Transformer> transformer);
}
