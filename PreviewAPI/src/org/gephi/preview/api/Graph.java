package org.gephi.preview.api;

import java.util.Iterator;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface Graph {

    public PVector getMinPos();

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

    public boolean showNodes();

	public boolean showEdges();

	public boolean showSelfLoops();
}
