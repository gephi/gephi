/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
