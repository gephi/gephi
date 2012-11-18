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
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphEventData;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphEventDataImpl implements GraphEventData {

    private Node[] addedNodes;
    private Node[] removedNodes;
    private Edge[] addedEdges;
    private Edge[] removedEdges;
    private Node[] expandedNodes;
    private Node[] retractedNodes;
    private Node[] movedNodes;

    private GraphView view;

    public Node[] addedNodes() {
        return addedNodes;
    }

    public Node[] removedNodes() {
        return removedNodes;
    }

    public Edge[] addedEdges() {
        return addedEdges;
    }

    public Edge[] removedEdges() {
        return removedEdges;
    }

    public Node[] expandedNodes() {
        return expandedNodes;
    }

    public Node[] retractedNodes() {
        return retractedNodes;
    }

    public Node[] movedNodes() {
        return movedNodes;
    }

    public void setAddedNodes(Node[] addedNodes) {
        this.addedNodes = addedNodes;
    }

    public void setRemovedNodes(Node[] removedNodes) {
        this.removedNodes = removedNodes;
    }

    public void setAddedEdges(Edge[] addedEdges) {
        this.addedEdges = addedEdges;
    }

    public void setRemovedEdges(Edge[] removedEdges) {
        this.removedEdges = removedEdges;
    }

    public void setExpandedNodes(Node[] expandedNodes) {
        this.expandedNodes = expandedNodes;
    }

    public void setRetractedNodes(Node[] retractedNodes) {
        this.retractedNodes = retractedNodes;
    }

    public void setMovedNodes(Node[] movedNodes) {
        this.movedNodes = movedNodes;
    }

    public GraphView newView() {
        return view;
    }

    public GraphView destroyView() {
        return view;
    }

    public GraphView visibleView() {
        return view;
    }

    public void setView(GraphView view) {
        this.view = view;
    }
}
