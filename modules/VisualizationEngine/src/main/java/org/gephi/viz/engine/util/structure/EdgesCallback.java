package org.gephi.viz.engine.util.structure;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIndex;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.viz.engine.structure.GraphIndex.ElementsCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgesCallback implements ElementsCallback<Edge> {

    private int edgeWeightVersion = -1;
    private float minWeight = 0f;
    private float maxWeight = 1f;
    private Edge[] edgesArray = new Edge[0];
    private float[] edgeWeightsArray = new float[0];
    private int maxIndex = 0;
    private int edgeCount = 0;
    private boolean directed = false;
    private boolean undirected = false;

    @Override
    public void start(Graph graph) {
        directed = graph.isDirected();
        undirected = graph.isUndirected();
        Arrays.fill(edgesArray, null);
        edgesArray = ensureEdgesArraySize(edgesArray, graph.getModel().getMaxEdgeStoreId() + 1);
        edgeWeightsArray = ensureEdgeWeightArraySize(edgeWeightsArray, graph.getModel().getMaxEdgeStoreId() + 1);
        maxIndex = 0;
        edgeCount = 0;
    }

    @Override
    public void accept(Edge edge) {
        int storeId = edge.getStoreId();
        if (storeId > maxIndex) {
            maxIndex = storeId;
        }
        edgesArray[storeId] = edge;
    }

    @Override
    public void end(Graph graph) {
        // Refresh min/max edge weight (if needed)
        Column weightCol = graph.getModel().getEdgeTable().getColumn(3); //Weight column
        ColumnIndex edgeWeightIndex = graph.getModel().getEdgeIndex().getColumnIndex(weightCol);
        if (edgeWeightIndex.getVersion() != edgeWeightVersion) {
            edgeWeightVersion = edgeWeightIndex.getVersion();
            Number minValue = edgeWeightIndex.getMinValue();
            Number maxValue = edgeWeightIndex.getMaxValue();
            minWeight = minValue != null ? minValue.floatValue() : 0f;
            maxWeight = maxValue != null ? maxValue.floatValue() : 1f;
        }

        // Get actual edge weights
        // And count non-null edges
        for (int i = 0; i <= maxIndex; i++) {
            Edge edge = edgesArray[i];
            if (edge != null) {
                edgeCount++;
                double weight = edge.getWeight(graph.getView());
                edgeWeightsArray[i] = (float) weight;
            }
        }
    }

    public void reset() {
        edgesArray = new Edge[0];
        edgeWeightsArray = new float[0];
        maxIndex = 0;
        edgeCount = 0;
        directed = false;
        undirected = false;
    }

    public Edge[] getEdgesArray() {
        return edgesArray;
    }

    public float[] getEdgeWeightsArray() {
        return edgeWeightsArray;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public int getCount() {
        return edgeCount;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public boolean isDirected() {
        return directed;
    }

    public boolean isUndirected() {
        return undirected;
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

    protected float[] ensureEdgeWeightArraySize(float[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);

            final float[] newVector = new float[newSize];
            System.arraycopy(array, 0, newVector, 0, array.length);

            return newVector;
        } else {
            return array;
        }
    }
}
