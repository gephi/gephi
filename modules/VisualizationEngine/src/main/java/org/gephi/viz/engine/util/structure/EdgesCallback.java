package org.gephi.viz.engine.util.structure;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.viz.engine.structure.GraphIndex.ElementsCallback;

import java.util.Arrays;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgesCallback implements ElementsCallback<Edge> {

    private Edge[] edgesArray = new Edge[0];
    private int nextIndex = 0;

    @Override
    public void start(Graph graph) {
        edgesArray = ensureEdgesArraySize(edgesArray, graph.getEdgeCount());
        nextIndex = 0;
    }

    @Override
    public void accept(Edge edge) {
        edgesArray[nextIndex++] = edge;
    }

    @Override
    public void end(Graph graph) {
        //NOOP
    }

    public void reset() {
        edgesArray = new Edge[0];
        nextIndex = 0;
    }

    public Iterable<Edge> getEdges() {
        return Arrays
                .asList(edgesArray)
                .subList(0, nextIndex);
    }

    public Edge[] getEdgesArray() {
        return edgesArray;
    }

    public int getCount() {
        return nextIndex;
    }

    protected Edge[] ensureEdgesArraySize(Edge[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);
            System.out.println("Growing edge vector from " + array.length + " to " + newSize + " elements");

            final Edge[] newVector = new Edge[newSize];
            System.arraycopy(array, 0, newVector, 0, array.length);

            return newVector;
        } else {
            return array;
        }
    }
}
