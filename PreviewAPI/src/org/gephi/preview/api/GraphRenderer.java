package org.gephi.preview.api;

/**
 * Interface of a preview graph renderer.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface GraphRenderer {

    /**
     * Renders the given preview graph.
     *
     * @param graph  the graph to render
     */
    public void renderGraph(Graph graph);

    /**
     * Renders the edges of the given preview graph.
     *
     * @param graph  the graph to render the edges
     */
    public void renderGraphEdges(Graph graph);

    /**
     * Renders the self-loops of the given preview graph.
     *
     * @param graph  the graph to render the self-loops
     */
    public void renderGraphSelfLoops(Graph graph);

    /**
     * Renders the unidirectional edges of the given preview graph.
     *
     * @param graph  the graph to render the unidirectional edges
     */
    public void renderGraphUnidirectionalEdges(Graph graph);

    /**
     * Renders the bidirectional edges of the given preview graph.
     *
     * @param graph  the graph to render the bidirectional edges
     */
    public void renderGraphBidirectionalEdges(Graph graph);

    /**
     * Renders the undirected edges of the given preview graph.
     *
     * @param graph  the graph to render the undirected edges
     */
    public void renderGraphUndirectedEdges(Graph graph);

    /**
     * Renders the nodes of the given preview graph.
     *
     * @param graph  the graph to render the nodes
     */
    public void renderGraphNodes(Graph graph);

    /**
     * Renders the labels of the given preview graph.
     *
     * @param graph  the graph to render the labels
     */
    public void renderGraphLabels(Graph graph);

    /**
     * Renders the label borders of the given preview graph.
     *
     * @param graph  the graph to render the label borders
     */
    public void renderGraphLabelBorders(Graph graph);

    /**
     * Renders the given preview node.
     *
     * @param node  the node to render
     */
    public void renderNode(Node node);

    /**
     * Renders the given preview node label.
     *
     * @param label  the node label to render
     */
    public void renderNodeLabel(NodeLabel label);

    /**
     * Renders the given preview node label border.
     *
     * @param border  the node label border to render
     */
    public void renderNodeLabelBorder(NodeLabelBorder border);

    /**
     * Renders the given preview self-loop.
     *
     * @param selfLoop  the self-loop to render
     */
    public void renderSelfLoop(SelfLoop selfLoop);

    /**
     * Renders the given preview directed edge.
     *
     * @param edge  the directed edge to render
     */
    public void renderDirectedEdge(DirectedEdge edge);

    /**
     * Renders the given preview edge.
     *
     * @param edge  the edge to render
     */
    public void renderEdge(Edge edge);

    /**
     * Renders the given preview edge as a straight edge.
     *
     * @param edge  the edge to render
     */
    public void renderStraightEdge(Edge edge);

    /**
     * Renders the given preview edge as a curved edge.
     *
     * @param edge  the edge to render
     */
    public void renderCurvedEdge(Edge edge);

    /**
     * Renders the arrows of the given preview edge.
     *
     * @param edge  the edge to render the arrows
     */
    public void renderEdgeArrows(DirectedEdge edge);

    /**
     * Renders the mini-labels of the given preview edge.
     *
     * @param edge  the edge to render the mini-labels
     */
    public void renderEdgeMiniLabels(DirectedEdge edge);

    /**
     * Renders the given preview edge arrow.
     *
     * @param arrow  the edge arrow to render
     */
    public void renderEdgeArrow(EdgeArrow arrow);

    /**
     * Renders the given preview edge label.
     *
     * @param label  the edge label to render
     */
    public void renderEdgeLabel(EdgeLabel label);

    /**
     * Renders the given preview edge mini-label.
     *
     * @param label  the edge mini-label to render
     */
    public void renderEdgeMiniLabel(EdgeMiniLabel miniLabel);
}
