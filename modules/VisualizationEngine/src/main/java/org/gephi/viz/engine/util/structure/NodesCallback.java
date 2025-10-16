package org.gephi.viz.engine.util.structure;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import java.util.Arrays;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.structure.GraphIndex.ElementsCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesCallback implements ElementsCallback<Node> {

    private Node[] nodesArray = new Node[0];
    private int maxIndex = 0;
    private float maxNodeSize = 0f;
    private int nodeCount = 0;

    @Override
    public void start(Graph graph) {
        Arrays.fill(nodesArray, null);
        nodesArray = ensureNodesArraySize(nodesArray, graph.getModel().getMaxNodeStoreId() + 1);
        maxIndex = 0;
        maxNodeSize = 0f;
        nodeCount = 0;
    }

    @Override
    public void accept(Node node) {
        int storeId = node.getStoreId();
        if (storeId > maxIndex) {
            maxIndex = storeId;
        }
        float size = node.size();
        if (size > maxNodeSize) {
            maxNodeSize = size;
        }
        nodesArray[storeId] = node;
    }

    @Override
    public void end(Graph graph) {
        // Count non-null nodes
        // This can't be done in accept as nodes can be duplicated and accept is called via multiple threads (parallel stream)
        nodeCount = 0;
        for (int i = 0; i <= maxIndex; i++) {
            if (nodesArray[i] != null) {
                nodeCount++;
            }
        }
    }

    public void reset() {
        nodesArray = new Node[0];
        maxIndex = 0;
        maxNodeSize = 0f;
        nodeCount = 0;
    }

    public Node[] getNodesArray() {
        return nodesArray;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public float getMaxNodeSize() {
        return maxNodeSize;
    }

    public int getCount() {
        return nodeCount;
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
