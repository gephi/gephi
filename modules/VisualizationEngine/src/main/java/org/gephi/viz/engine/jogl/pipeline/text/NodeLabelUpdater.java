package org.gephi.viz.engine.jogl.pipeline.text;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelUpdater implements WorldUpdater<JOGLRenderingTarget, Node> {

    private static final int GRID_SIZE = 15;

    private final VizEngine engine;
    private final NodeLabelData labelData;
    private boolean vaoSupported = false;
    private boolean mipMapSupported = false;

    public NodeLabelUpdater(VizEngine engine, NodeLabelData labelData) {
        this.engine = engine;
        this.labelData = labelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        final GLCapabilitiesSummary capabilities = target.getGlCapabilitiesSummary();
        final OpenGLOptions openGLOptions = engine.getOpenGLOptions();
        vaoSupported = capabilities.isVAOSupported(openGLOptions);
        // Disable mipmap generation in intel GPUs. See https://github.com/gephi/gephi/issues/1494 (Some label characters fade away when zooming out)
        mipMapSupported = !capabilities.isVendorIntel();
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        labelData.dispose();
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        final GraphRenderingOptions options = model.getRenderingOptions();

        if (!options.isShowNodeLabels()) {
            labelData.dispose();
            return;
        }

        // Get nodes and their properties
        final NodesCallback nodesCallback = labelData.getNodesCallback();
        final boolean someSelection = nodesCallback.hasSelection();
        final String[] texts = nodesCallback.getNodesLabelsArray();

        if (texts == null || texts.length == 0) {
            return;
        }

        // Get nodes array
        final Node[] nodes = nodesCallback.getNodesArray();
        final int maxIndex = nodesCallback.getMaxIndex();

        // Rendering parameters
        final GraphRenderingOptions.LabelColorMode labelColorMode = options.getNodeLabelColorMode();
        final GraphRenderingOptions.LabelSizeMode labelSizeMode = options.getNodeLabelSizeMode();
        final float lightenNonSelectedFactor = options.getLightenNonSelectedFactor();
        final float nodeLabelScale = options.getNodeLabelScale();
        final float fitNodeLabelsToNodeSizeFactor = options.getNodeLabelFitToNodeSizeFactor();
        final boolean fitToNodeSize = options.isNodeLabelFitToNodeSize();
        final boolean hideNonSelectedLabels = options.isHideNonSelectedNodeLabels();
        final boolean avoidOverlap = options.isAvoidNodeLabelOverlap();
        final float zoom = options.getZoom();
        final float nodeScale = options.getNodeScale();

        // No labels to show
        if (hideNonSelectedLabels && !someSelection) {
            return;
        }

        // Ensure label batches array is large enough
        labelData.ensureLabelBatchesSize(maxIndex);

        // Ensure we have a text renderer with the right font
        labelData.ensureTextRenderer(options.getNodeLabelFont(), vaoSupported, mipMapSupported);

        // Set the max valid index for this frame (used by renderer to limit iteration)
        labelData.setMaxValidIndex(maxIndex);

        // Initialize grid for overlap detection if enabled
        Int2IntOpenHashMap gridOccupancy = null; // Maps cell index -> storeId
        float gridMinX = 0, gridMinY = 0;
        float gridWidth = 0, gridHeight = 0;
        int gridCols = 0, gridRows = 0;

        if (avoidOverlap) {
            // Get bounds from NodesCallback with padding
            gridMinX = nodesCallback.getMinX() - GRID_SIZE;
            gridMinY = nodesCallback.getMinY() - GRID_SIZE;
            float gridMaxX = nodesCallback.getMaxX() + GRID_SIZE;
            float gridMaxY = nodesCallback.getMaxY() + GRID_SIZE;
            gridWidth = gridMaxX - gridMinX;
            gridHeight = gridMaxY - gridMinY;

            if (gridWidth > 0 && gridHeight > 0) {
                gridCols = (int) Math.ceil(gridWidth / GRID_SIZE);
                gridRows = (int) Math.ceil(gridHeight / GRID_SIZE);
                gridOccupancy = new Int2IntOpenHashMap();
                gridOccupancy.defaultReturnValue(-1); // -1 means empty cell
            }
        }

        // Update label data for each node
        // Only recomputes glyphs if text changed, only recomputes bounds if sizeFactor changed
        for (int i = 0; i <= maxIndex; i++) {
            final Node node = nodes[i];

            if (node == null) {
                // Mark this slot as invalid
                labelData.invalidateBatch(i);
                continue;
            }

            final String text = texts[i];
            if (text == null) {
                // Mark as invalid (no text)
                labelData.invalidateBatch(i);
                continue;
            }

            boolean selected = someSelection && nodesCallback.isSelected(i);

            if (hideNonSelectedLabels && !selected) {
                // Mark as invalid (hidden)
                labelData.invalidateBatch(i);
                continue;
            }

            // Size calculation
            final float baseNodeSizeFactor = fitToNodeSize ? node.size() * fitNodeLabelsToNodeSizeFactor * nodeScale :
                (float) Math.sqrt(node.getTextProperties().getSize());
            // Add tiny bias (<1%) based on node size to prioritize labels of larger nodes in overlap detection
            final float nodeSizeFactor = baseNodeSizeFactor * (1.0f + node.size() * nodeScale * 0.00001f);
            float sizeFactor = nodeLabelScale * nodeSizeFactor;
            if (labelSizeMode.equals(GraphRenderingOptions.LabelSizeMode.SCREEN)) {
                sizeFactor /= zoom;
            }

            // Color calculation
            final int rgba = labelColorMode.equals(GraphRenderingOptions.LabelColorMode.OBJECT) ? node.getRGBA() :
                node.getTextProperties().getRGBA();
            final float r = (rgba >> 16 & 255) / 255.0F;
            final float g = (rgba >> 8 & 255) / 255.0F;
            final float b = (rgba & 255) / 255.0F;
            final float a = ((rgba >> 24) & 0xFF) / 255f;

            final float finalR, finalG, finalB, finalA;
            if (someSelection && !selected) {
                float lightColorFactor = 1 - lightenNonSelectedFactor;
                finalR = r;
                finalG = g;
                finalB = b;
                finalA = lightColorFactor;
            } else {
                finalR = r;
                finalG = g;
                finalB = b;
                finalA = a;
            }

            // Update batch first - this computes dimensions and glyphs
            NodeLabelData.LabelBatch batch = labelData.updateBatch(node, i, text, sizeFactor, node.x(), node.y(),
                finalR, finalG, finalB, finalA);

            // Check for overlap if enabled (after dimensions are computed)
            boolean shouldRender = true;
            if (avoidOverlap && gridOccupancy != null && batch.isWriteValid()) {
                // Get label dimensions from node text properties (set by updateBatch)
                float width = node.getTextProperties().getWidth();
                float height = node.getTextProperties().getHeight();

                if (width > 0 && height > 0) {
                    // Calculate grid cells this label overlaps
                    float labelMinX = node.x() - width * 0.5f;
                    float labelMaxX = node.x() + width * 0.5f;
                    float labelMinY = node.y() - height * 0.5f;
                    float labelMaxY = node.y() + height * 0.5f;

                    int minCol = Math.max(0, (int) ((labelMinX - gridMinX) / GRID_SIZE));
                    int maxCol = Math.min(gridCols - 1, (int) ((labelMaxX - gridMinX) / GRID_SIZE));
                    int minRow = Math.max(0, (int) ((labelMinY - gridMinY) / GRID_SIZE));
                    int maxRow = Math.min(gridRows - 1, (int) ((labelMaxY - gridMinY) / GRID_SIZE));

                    // Check all overlapping cells
                    for (int row = minRow; row <= maxRow && shouldRender; row++) {
                        for (int col = minCol; col <= maxCol && shouldRender; col++) {
                            int cellIndex = row * gridCols + col;
                            int occupyingStoreId = gridOccupancy.get(cellIndex);

                            if (occupyingStoreId != -1) {
                                // Cell is occupied - check if occupying batch is still valid
                                NodeLabelData.LabelBatch occupyingBatch = labelData.getBatch(occupyingStoreId);
                                if (occupyingBatch != null && occupyingBatch.isWriteValid()) {
                                    // Compare size factors
                                    float occupyingSizeFactor = occupyingBatch.getWriteScale();
                                    if (sizeFactor <= occupyingSizeFactor) {
                                        // Current label is smaller or equal - don't render
                                        shouldRender = false;
                                    } else {
                                        // Current label is larger - invalidate the smaller one
                                        labelData.invalidateBatch(occupyingStoreId);
                                        // Note: We don't clean up grid cells of the invalidated label
                                        // as it doesn't matter - larger labels will overwrite
                                    }
                                }
                                // If occupying batch is invalid or null, treat cell as free
                            }
                        }
                    }

                    if (shouldRender) {
                        // Mark all cells as occupied by this label
                        for (int row = minRow; row <= maxRow; row++) {
                            for (int col = minCol; col <= maxCol; col++) {
                                int cellIndex = row * gridCols + col;
                                gridOccupancy.put(cellIndex, i);
                            }
                        }
                    }
                }
            }

            if (!shouldRender) {
                // Mark as invalid (overlapping with larger label)
                labelData.invalidateBatch(i);
            }
        }
    }

    @Override
    public ElementsCallback<Node> getElementsCallback() {
        return labelData.getNodesCallback();
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE_LABEL;
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Nodes Labels";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
