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
 * Wrap graph event elements, like added nodes.
 *
 * @author Mathieu Bastian
 * @see GraphEvent
 */
public interface GraphEventData {

    /**
     * Returns nodes added to the graph.
     * @return added nodes, or <code>null</code> if the event is not <code>ADD_NODES</code>
     */
    public Node[] addedNodes();

    /**
     * Returns nodes removed from the graph.
     * @return removed nodes, or <code>null</code> if the event is not <code>REMOVE_NODES</code>
     */
    public Node[] removedNodes();

    /**
     * Returns edges added to the graph.
     * @return added edges, or <code>null</code> if the event is not <code>ADD_EDGES</code>
     */
    public Edge[] addedEdges();

    /**
     * Returns edges removed from the graph.
     * @return removed edges, or <code>null</code> if the event is not <code>REMOVE_EDGES</code>
     */
    public Edge[] removedEdges();

    /**
     * Returns nodes expanded in the graph hierarchy.
     * @return expanded nodes, or <code>null</code> if the event is not <code>EXPAND</code>
     */
    public Node[] expandedNodes();

    /**
     * Returns nodes retracted in the graph hierarchy.
     * @return retracted nodes, or <code>null</code> if the event is not <code>RETRACT</code>
     */
    public Node[] retractedNodes();

    /**
     * Returns nodes moved in the graph hierarchy, their parent node has changed.
     * @return moved nodes, or <code>null</code> if the event is not <code>MOVE_NODES</code>
     */
    public Node[] movedNodes();

    /**
     * Returns the new view created in the model.
     * @return the new view, or <code>null</code> if the event is not <code>NEW_VIEW</code>
     */
    public GraphView newView();

    /**
     * Returns the view destroyed in the model.
     * @return the destroyed view, or <code>null</code> if the event is not <code>DESTROY_VIEW</code>
     */
    public GraphView destroyView();

    /**
     * Returns the current visible view.
     * @return the visible view, or <code>null</code> if the event is not <code>VISIBLE_VIEW</code>
     */
    public GraphView visibleView();
}
