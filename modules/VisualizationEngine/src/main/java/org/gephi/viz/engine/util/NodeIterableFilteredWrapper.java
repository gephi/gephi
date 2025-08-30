package org.gephi.viz.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

/**
 *
 * @author Eduardo Ramos
 */
public class NodeIterableFilteredWrapper implements NodeIterable {

    private final NodeIterable iterable;
    private final Predicate<Node> predicate;

    public NodeIterableFilteredWrapper(NodeIterable iterable, Predicate<Node> predicate) {
        this.iterable = iterable;
        this.predicate = predicate;
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodePredicateIterator(iterable.iterator());
    }

    @Override
    public Node[] toArray() {
        return toCollection().toArray(new Node[0]);
    }

    @Override
    public Collection<Node> toCollection() {
        Iterator<Node> iterator = iterator();
        List<Node> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Override
    public Set<Node> toSet() {
        Iterator<Node> iterator = iterator();
        Set<Node> set = new HashSet<>();
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    @Override
    public void doBreak() {
        iterable.doBreak();
    }

    protected final class NodePredicateIterator implements Iterator<Node> {

        private final Iterator<Node> nodeIterator;
        private Node pointer;

        public NodePredicateIterator(Iterator<Node> nodeIterator) {
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
        public Node next() {
            return pointer;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
