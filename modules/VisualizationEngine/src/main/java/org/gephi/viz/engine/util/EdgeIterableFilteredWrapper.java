package org.gephi.viz.engine.util;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;

import java.util.*;
import java.util.function.Predicate;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgeIterableFilteredWrapper implements EdgeIterable {

    private final EdgeIterable iterable;
    private final Predicate<Edge> predicate;

    public EdgeIterableFilteredWrapper(EdgeIterable iterable, Predicate<Edge> predicate) {
        this.iterable = iterable;
        this.predicate = predicate;
    }

    @Override
    public Iterator<Edge> iterator() {
        return new EdgePredicateIterator(iterable.iterator());
    }

    @Override
    public Edge[] toArray() {
        return toCollection().toArray(new Edge[0]);
    }

    @Override
    public Collection<Edge> toCollection() {
        Iterator<Edge> iterator = iterator();
        List<Edge> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Override
    public Set<Edge> toSet() {
        Iterator<Edge> iterator = iterator();
        Set<Edge> set = new HashSet<>();
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    @Override
    public void doBreak() {
        iterable.doBreak();
    }

    protected final class EdgePredicateIterator implements Iterator<Edge> {

        private final Iterator<Edge> nodeIterator;
        private Edge pointer;

        public EdgePredicateIterator(Iterator<Edge> nodeIterator) {
            this.nodeIterator = nodeIterator;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                if (!nodeIterator.hasNext()) {
                    return false;
                }
                pointer = nodeIterator.next();
                if (!predicate.test(pointer)) {
                    pointer = null;
                }
            }
            return true;
        }

        @Override
        public Edge next() {
            return pointer;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
