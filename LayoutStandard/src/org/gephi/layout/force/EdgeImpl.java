/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Node;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class EdgeImpl implements Edge {

    private Node source,  target;

    public EdgeImpl(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    public Node N1() {
        return source;
    }

    public Node N2() {
        return target;
    }

    public int getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public float getWeight() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isVisible() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeData getEdgeData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        Edge e = (Edge) obj;
        return (e.getSource() == getSource() && e.getTarget() == getTarget());
    }

    @Override
    public int hashCode() {
        return 10 * source.hashCode() + target.hashCode();
    }
}