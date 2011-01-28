/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
