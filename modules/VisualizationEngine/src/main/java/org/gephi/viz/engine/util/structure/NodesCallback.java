package org.gephi.viz.engine.util.structure;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import java.util.Arrays;
import java.util.BitSet;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.status.GraphSelectionImpl;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.text.TextLabelBuilder;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesCallback implements ElementsCallback<Node> {

    private Node[] nodesArray = new Node[0];
    private GraphView graphView;
    private Column[] nodeLabelColumns;
    private String[] nodesLabelsArray = new String[0];
    private BitSet selectedBitSet = new BitSet();
    private boolean hasSelection = false;
    private int maxIndex = 0;
    private float maxNodeSize = 0f;
    private int nodeCount = 0;
    private boolean hasLabels = false;
    private boolean hideNonSelectedLabels = false;
    private float minX = Float.POSITIVE_INFINITY;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;

    @Override
    public void run(GraphIndex graphIndex, GraphRenderingOptions renderingOptions, Rect2D viewBoundaries) {
        if (!renderingOptions.isShowNodes()) {
            return;
        }
        graphIndex.getVisibleNodes(this, renderingOptions, viewBoundaries);
    }

    @Override
    public void start(Graph graph, GraphRenderingOptions graphRenderingOptions, GraphSelection graphSelection) {
        Arrays.fill(nodesArray, null);
        nodesArray = ensureNodesArraySize(nodesArray, graph.getModel().getMaxNodeStoreId() + 1);
        maxIndex = 0;
        maxNodeSize = 0f;
        nodeCount = 0;
        minX = Float.POSITIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;

        hasSelection = graphSelection.someNodesOrEdgesSelection();
        if (hasSelection) {
            BitSet sourceBitSet = ((GraphSelectionImpl) graphSelection).getNodesWithNeighbours();
            selectedBitSet.clear();
            selectedBitSet.or(sourceBitSet);
        }

        hideNonSelectedLabels = graphRenderingOptions.isHideNonSelectedNodeLabels();
        hasLabels = graphRenderingOptions.isShowNodeLabels() && !(hideNonSelectedLabels && !hasSelection);
        if (hasLabels) {
            nodesLabelsArray = ensureNodesLabelsArraySize(nodesLabelsArray, graph.getModel().getMaxNodeStoreId() + 1);
            graphView = graph.getView();
            nodeLabelColumns = graphRenderingOptions.getNodeLabelColumns();
        }
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

        if (hasLabels && node.getTextProperties().isVisible() && (!hideNonSelectedLabels || isSelected(storeId))) {
            nodesLabelsArray[storeId] = TextLabelBuilder.buildText(node, graphView, nodeLabelColumns);
        } else if (hasLabels) {
            nodesLabelsArray[storeId] = null;
        }
    }

    @Override
    public void end(Graph graph) {
        // Count non-null nodes and track bounds
        // This can't be done in accept as nodes can be duplicated and accept is called via multiple threads (parallel stream)
        nodeCount = 0;
        for (int i = 0; i <= maxIndex; i++) {
            Node node = nodesArray[i];
            if (node != null) {
                nodeCount++;
                // Track min/max positions for grid bounds
                float x = node.x();
                float y = node.y();
                if (x < minX) {
                    minX = x;
                }
                if (x > maxX) {
                    maxX = x;
                }
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
            }
        }
    }

    @Override
    public void reset() {
        nodesArray = new Node[0];
        maxIndex = 0;
        maxNodeSize = 0f;
        nodeCount = 0;
        selectedBitSet = new BitSet();
        hasSelection = false;
        nodeLabelColumns = null;
        nodesLabelsArray = new String[0];
        hasLabels = false;
        hideNonSelectedLabels = false;
        minX = Float.POSITIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
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

    public boolean isSelected(int nodeStoreId) {
        return hasSelection && selectedBitSet.get(nodeStoreId);
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public String[] getNodesLabelsArray() {
        return nodesLabelsArray;
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
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

    protected String[] ensureNodesLabelsArraySize(String[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);
            System.out.println("Growing node label vector from " + array.length + " to " + newSize + " elements");

            final String[] newVector = new String[newSize];
            System.arraycopy(array, 0, newVector, 0, array.length);

            return newVector;
        } else {
            return array;
        }
    }
}
