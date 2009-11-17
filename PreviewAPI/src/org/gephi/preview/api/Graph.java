package org.gephi.preview.api;

import processing.core.PVector;

/**
 * Interface of a preview graph.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Graph {

    /**
     * Returns the closest position in the graph to (0,0).
     *
     * @return the closest position in the graph to (0,0).
     */
    public PVector getMinPos();

    /**
     * Returns the farther position in the graph to (0,0).
     *
     * @return the farther position in the graph to (0,0).
     */
    public PVector getMaxPos();

    /**
     * Returns an iterable on the graph's nodes.
     *
     * @return an iterable on the graph's nodes
     */
    public Iterable<Node> getNodes();

    /**
     * Returns an iterable on the graph's self-loops.
     *
     * @return an iterable on the graph's self-loops
     */
    public Iterable<SelfLoop> getSelfLoops();

    /**
     * Returns an iterable on the graph's unidirectional edges.
     *
     * @return an iterable on the graph's unidirectional edges
     */
    public Iterable<UnidirectionalEdge> getUnidirectionalEdges();

    /**
     * Returns an iterable on the graph's bidirectional edges.
     *
     * @return an iterable on the graph's bidirectional edges
     */
    public Iterable<BidirectionalEdge> getBidirectionalEdges();

    /**
     * Returns the number or nodes in the graph.
     *
     * @return the number or nodes in the graph
     */
    public float countNodes();

    /**
     * Returns true if the nodes must be displayed in the preview.
     *
     * @return true if the nodes must be displayed in the preview
     */
    public Boolean showNodes();

    /**
     * Returns true if the edges must be displayed in the preview.
     *
     * @return true if the edges must be displayed in the preview
     */
    public Boolean showEdges();

    /**
     * Returns true if the self-loops must be displayed in the preview.
     *
     * @return true if the self-loops must be displayed in the preview
     */
    public Boolean showSelfLoops();
}
