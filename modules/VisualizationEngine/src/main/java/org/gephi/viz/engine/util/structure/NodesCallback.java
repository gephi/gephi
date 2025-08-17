package org.gephi.viz.engine.util.structure;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.structure.GraphIndex.ElementsCallback;

import java.util.Arrays;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesCallback implements ElementsCallback<Node> {

    private Node[] nodesArray = new Node[0];
    private int nextIndex = 0;

    @Override
    public void start(Graph graph) {
        nodesArray = ensureNodesArraySize(nodesArray, graph.getNodeCount());
        nextIndex = 0;
    }

    @Override
    public void accept(Node node) {
        nodesArray[nextIndex++] = node;
    }

    @Override
    public void end(Graph graph) {
        //NOOP
    }
    
    public void reset() {
        nodesArray = new Node[0];
        nextIndex = 0;
    }

    public Iterable<Node> getNodes() {
        return Arrays
                .asList(nodesArray)
                .subList(0, nextIndex);
    }

    public Node[] getNodesArray() {
        return nodesArray;
    }

    public int getCount() {
        return nextIndex;
    }

    protected Node[] ensureNodesArraySize(Node[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);
            System.out.println("Growing node vector from " + array.length + " to " + newSize + " elements");

            final Node[] newVector = new Node[newSize];
            System.arraycopy(array, 0, newVector, 0, array.length);

            return newVector;
        } else {
            return array;
        }
    }
}
