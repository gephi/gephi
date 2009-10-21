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

//    public Iterator<UnidirectionalEdge> getUnidirectionalEdges();

//    public Iterator<BidirectionalEdge> getBidirectionalEdges();

    public Iterator<SelfLoop> getSelfLoops();

    public Iterator<Node> getNodes();

    public boolean showNodes();

	public boolean showEdges();

	public boolean showSelfLoops();
}
