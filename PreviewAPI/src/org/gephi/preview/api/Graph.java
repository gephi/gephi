package org.gephi.preview.api;

/**
 * Interface of a preview graph.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Graph {

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
     * Returns an iterable on the graph's undirected edges.
     *
     * @return an iterable on the graph's undirected edges
     */
    public Iterable<UndirectedEdge> getUndirectedEdges();

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the number of nodes in the graph
     */
    public int countNodes();

    /**
     * Returns the number of self-loops in the graph.
     *
     * @return the number of self-loops in the graph
     */
    public int countSelfLoops();

    /**
     * Returns the number of unidirectional edges in the graph.
     *
     * @return the number of unidirectional edges in the graph
     */
    public int countUnidirectionalEdges();

    /**
     * Returns the number of bidirectional edges in the graph.
     *
     * @return the number of bidirectional edges in the graph
     */
    public int countBidirectionalEdges();

    /**
     * Returns the number of undirected edges in the graph.
     *
     * @return the number of undirected edges in the graph
     */
    public int countUndirectedEdges();

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
