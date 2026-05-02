package org.gephi.viz.engine.util.structure;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIndex;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
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
public class EdgesCallback implements ElementsCallback<Edge> {

    private int edgeWeightVersion = -1;
    private float minWeight = 0f;
    private float maxWeight = 1f;
    private Edge[] edgesArray = new Edge[0];
    private float[] edgeWeightsArray = new float[0];
    private GraphView graphView;
    private Column[] edgeLabelColumns;
    private String[] edgeLabelsArray = new String[0];
    private boolean hasSelection = false;
    private BitSet selectedBitSet = new BitSet();
    private boolean hasLabels = false;
    private boolean hideNonSelectedLabels = false;
    private int maxIndex = 0;
    private int edgeCount = 0;
    private boolean directed = false;
    private boolean undirected = false;
    private boolean hasSelfLoop = false;

    @Override
    public void run(GraphIndex graphIndex, GraphRenderingOptions renderingOptions, Rect2D viewBoundaries) {
        if (!renderingOptions.isShowEdges()) {
            return;
        }
        graphIndex.getVisibleEdges(this, renderingOptions, viewBoundaries);
    }

    @Override
    public void start(Graph graph, GraphRenderingOptions graphRenderingOptions, GraphSelection graphSelection) {
        directed = graph.isDirected();
        undirected = graph.isUndirected();
        Arrays.fill(edgesArray, null);
        edgesArray = ensureEdgesArraySize(edgesArray, graph.getModel().getMaxEdgeStoreId() + 1);
        edgeWeightsArray = ensureEdgeWeightArraySize(edgeWeightsArray, graph.getModel().getMaxEdgeStoreId() + 1);
        maxIndex = 0;
        edgeCount = 0;
        hasSelfLoop = false;

        hasSelection = graphSelection.someNodesOrEdgesSelection();
        if (hasSelection) {
            BitSet sourceBitSet = ((GraphSelectionImpl) graphSelection).getEdges();
            selectedBitSet.clear();
            selectedBitSet.or(sourceBitSet);
        }

        hideNonSelectedLabels = graphRenderingOptions.isHideNonSelectedEdgeLabels();
        hasLabels = graphRenderingOptions.isShowEdgeLabels() && !(hideNonSelectedLabels && !hasSelection);
        if (hasLabels) {
            edgeLabelsArray = ensureEdgesLabelsArraySize(edgeLabelsArray, graph.getModel().getMaxEdgeStoreId() + 1);
            graphView = graph.getView();
            edgeLabelColumns = graphRenderingOptions.getEdgeLabelColumns();
        }
    }

    @Override
    public void accept(Edge edge) {
        int storeId = edge.getStoreId();
        if (storeId > maxIndex) {
            maxIndex = storeId;
        }
        edgesArray[storeId] = edge;

        if (!hasSelfLoop && edge.isSelfLoop()) {
            hasSelfLoop = true;
        }

        if (hasLabels && edge.getTextProperties().isVisible() && (!hideNonSelectedLabels || isSelected(storeId))) {
            edgeLabelsArray[storeId] = TextLabelBuilder.buildText(edge, graphView, edgeLabelColumns);
        } else if (hasLabels) {
            edgeLabelsArray[storeId] = null;
        }
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

    @Override
    public void reset() {
        edgesArray = new Edge[0];
        edgeWeightsArray = new float[0];
        maxIndex = 0;
        edgeCount = 0;
        directed = false;
        undirected = false;
        hasSelfLoop = false;
        hasSelection = false;
        hideNonSelectedLabels = false;
        selectedBitSet = new BitSet();
        edgeLabelColumns = null;
        edgeLabelsArray = new String[0];
        hasLabels = false;
    }

    public Edge[] getEdgesArray() {
        return edgesArray;
    }

    public float[] getEdgeWeightsArray() {
        return edgeWeightsArray;
    }

    public String[] getEdgeLabelsArray() {
        return edgeLabelsArray;
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

    public boolean hasSelfLoop() {
        return hasSelfLoop;
    }

    public boolean isSelected(int edgeStoreId) {
        return hasSelection && selectedBitSet.get(edgeStoreId);
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    protected Edge[] ensureEdgesArraySize(Edge[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);
            Logger.getLogger(VizEngine.class.getSimpleName()).log(
                Level.FINE, "Growing edge vector from " + array.length + " to " + newSize + " elements");

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

    protected String[] ensureEdgesLabelsArraySize(String[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);

            final String[] newVector = new String[newSize];
            System.arraycopy(array, 0, newVector, 0, array.length);

            return newVector;
        } else {
            return array;
        }
    }
}
