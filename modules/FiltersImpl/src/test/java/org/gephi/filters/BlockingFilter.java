package org.gephi.filters;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Keeps every node, but each computation waits for a permit.
 */
class BlockingFilter implements NodeFilter {

    final Semaphore permits = new Semaphore(0);
    final AtomicInteger inits = new AtomicInteger();

    @Override
    public String getName() {
        return "Blocking";
    }

    @Override
    public FilterProperty[] getProperties() {
        return new FilterProperty[0];
    }

    @Override
    public boolean init(Graph graph) {
        inits.incrementAndGet();
        permits.acquireUninterruptibly();
        return true;
    }

    @Override
    public boolean evaluate(Graph graph, Node node) {
        return true;
    }

    @Override
    public void finish() {
    }
}
