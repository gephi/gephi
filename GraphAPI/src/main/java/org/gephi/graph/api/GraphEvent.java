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
     * Graph event interface, that {@link GraphListener} receives when the graph is
     * modified. Each event is specific to a particular view, which can be get
     * from the <code>getSource()</code> method.
     * <p>
     * <table>
     * <tr><td><b>- ADD_NODES_AND_EDGES:</b></td><td>Add nodes and edges to the graph</td></tr>
     * <tr><td><b>- REMOVE_NODES_AND_EDGES:</b></td><td>Remove nodes and edges from the graph</td></tr>
     * <tr><td><b>- MOVE_NODE:</b></td><td>Move nodes on the hierarchy, parent node is changed</td></tr>
     * <tr><td><b>- VISIBLE_VIEW:</b></td><td>Current visible view is changed</td></tr>
     * <tr><td><b>- NEW_VIEW:</b></td><td>A new view is created</td></tr>
     * <tr><td><b>- DESTROY_VIEW:</b></td><td>A view is destroyed</td></tr>
     * <tr><td><b>- EXPAND:</b></td><td>Expand nodes in the hierarchy</td></tr>
     * <tr><td><b>- RETRACT:</b></td><td>Retract nodes in the hierarchy</td></tr>
     * <tr><td><b>- META_EDGES_UPDATE</b></td><td>Meta-Edges are updated</td></tr></table>
     *
     * @author Mathieu Bastian
     * @see GraphView
     */
public interface GraphEvent {

    /**
     * <table>
     * <tr><td><b>- ADD_NODES_AND_EDGES:</b></td><td>Add nodes and edges to the graph</td></tr>
     * <tr><td><b>- REMOVE_NODES_AND_EDGES:</b></td><td>Remove nodes and edges from the graph, with their edges</td></tr>
     * <tr><td><b>- MOVE_NODE:</b></td><td>Move nodes on the hierarchy, parent node is changed</td></tr>
     * <tr><td><b>- VISIBLE_VIEW:</b></td><td>Current visible view is changed</td></tr>
     * <tr><td><b>- NEW_VIEW:</b></td><td>A new view is created</td></tr>
     * <tr><td><b>- DESTROY_VIEW:</b></td><td>A view is destroyed</td></tr>
     * <tr><td><b>- EXPAND:</b></td><td>Expand nodes in the hierarchy</td></tr>
     * <tr><td><b>- RETRACT:</b></td><td>Retract nodes in the hierarchy</td></tr>
     * <tr><td><b>- META_EDGES_UPDATE</b></td><td>Meta-Edges are updated</td></tr></table>
     */
    public enum EventType {

        ADD_NODES_AND_EDGES,
        REMOVE_NODES_AND_EDGES,
        MOVE_NODES,
        VISIBLE_VIEW,
        NEW_VIEW,
        DESTROY_VIEW,
        EXPAND,
        RETRACT,
        META_EDGES_UPDATE
    };

    /**
     * Returns the type of event.
     * @return      the type of event, can't be <code>null</code>
     */
    public EventType getEventType();

    /**
     * Returns the data associated to this event.
     * @return      the graph event data
     */
    public GraphEventData getData();

    /**
     * Returns the view this event is triggered.
     * @return      the source of the vent
     */
    public GraphView getSource();

    /**
     * Returns <code>true</code> if this event is one of these in parameters.
     * @param type  the event types that are to be compared with this event
     * @return      <code>true</code> if this event is <code>type</code>,
     *              <code>false</code> otherwise
     */
    public boolean is(EventType... type);
}
