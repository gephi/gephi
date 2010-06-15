/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.api;

/**
 *
 * @author Mathieu Bastian
 */
public interface GraphEventData {

    public Node[] addedNodes();

    public Node[] removedNodes();

    public Edge[] addedEdges();

    public Edge[] removedEdges();

    public Node[] expandedNodes();

    public Node[] retractedNodes();

    public Node[] movedNodes();

    public GraphView newView();

    public GraphView destroyView();

    public GraphView visibleView();
}
