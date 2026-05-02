package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphView;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.structure.EdgesCallback;

public class EdgeLabelUpdater extends AbstractLabelUpdater<Edge> {

    // Multiplier to make self-loop stroke visually match regular edge thickness (same as shader)
    private static final float STROKE_MULTIPLIER = 1.3f;

    public EdgeLabelUpdater(VizEngine engine, EdgeLabelData edgeLabelData) {
        super(engine, edgeLabelData);
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        final GraphRenderingOptions options = model.getRenderingOptions();
        final GraphView view = model.getGraphModel().getVisibleView();

        if (!options.isShowEdgeLabels()) {
            labelData.dispose();
            return;
        }

        // Get edges and their properties
        final EdgesCallback edgesCallback = (EdgesCallback) labelData.getElementsCallback();
        final boolean someSelection = edgesCallback.hasSelection();
        final String[] texts = edgesCallback.getEdgeLabelsArray();

        if (texts == null || texts.length == 0) {
            labelData.setMaxValidIndex(-1);
            return;
        }

        // Get edges array
        final Edge[] edges = edgesCallback.getEdgesArray();
        final int maxIndex = edgesCallback.getMaxIndex();

        // Rendering parameters
        final GraphRenderingOptions.LabelColorMode labelColorMode = options.getEdgeLabelColorMode();
        final GraphRenderingOptions.LabelSizeMode labelSizeMode = options.getEdgeLabelSizeMode();
        final GraphRenderingOptions.EdgeColorMode edgeColorMode = options.getEdgeColorMode();
        final float lightenNonSelectedFactor = options.isLightenNonSelected() ? options.getLightenNonSelectedFactor() : 0f;
        final float edgeLabelScale = options.getEdgeLabelScale();
        final float nodeScale = options.getNodeScale();
        final boolean hideNonSelectedLabels = options.isHideNonSelectedEdgeLabels();
        final float zoom = options.getZoom();

        // Self-loop thickness parameters
        final float edgeScale = options.getEdgeScale();
        final float edgeRescaleMin = options.getEdgeRescaleMin();
        final float edgeRescaleMax = options.getEdgeRescaleMax();
        final float minWeight = edgesCallback.getMinWeight();
        final float maxWeight = edgesCallback.getMaxWeight();

        // No labels to show
        if (hideNonSelectedLabels && !someSelection) {
            labelData.setMaxValidIndex(-1);
            return;
        }

        // Ensure label batches array is large enough
        labelData.ensureLabelBatchesSize(maxIndex);

        // Ensure we have a text renderer with the right font
        labelData.ensureTextRenderer(options.getEdgeLabelFont(), vaoSupported, mipMapSupported);

        // Set the max valid index for this frame (used by renderer to limit iteration)
        labelData.setMaxValidIndex(maxIndex);

        // Update label data for each edge
        // Only recomputes glyphs if text changed, only recomputes bounds if sizeFactor changed
        for (int i = 0; i <= maxIndex; i++) {
            final Edge edge = edges[i];

            if (edge == null) {
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

            boolean selected = someSelection && edgesCallback.isSelected(i);

            if (hideNonSelectedLabels && !selected) {
                // Mark as invalid (hidden)
                labelData.invalidateBatch(i);
                continue;
            }

            // Size calculation
            final float edgeSizeFactor = (float) Math.sqrt(edge.getTextProperties().getSize());
            float sizeFactor = edgeLabelScale * edgeSizeFactor;
            if (labelSizeMode.equals(GraphRenderingOptions.LabelSizeMode.SCREEN)) {
                sizeFactor /= zoom;
            }

            // Color calculation
            final int rgba =
                labelColorMode.equals(GraphRenderingOptions.LabelColorMode.OBJECT) ? getEdgeColor(edge, edgeColorMode) :
                    edge.getTextProperties().getRGBA();
            final float r = (rgba >> 16 & 255) / 255.0F;
            final float g = (rgba >> 8 & 255) / 255.0F;
            final float b = (rgba & 255) / 255.0F;
            final float a = ((rgba >> 24) & 0xFF) / 255f;

            // The blend mode is GL_ONE, GL_ONE_MINUS_SRC_ALPHA (premultiplied alpha), so RGB
            // must be premultiplied by the effective alpha for correct blending on any background.
            // Without premultiplication, a non-black color at reduced alpha is still added at
            // full RGB strength, making the lightening effect invisible on dark backgrounds.
            final float finalR, finalG, finalB, finalA;
            if (someSelection && !selected) {
                finalA = a * (1 - lightenNonSelectedFactor);
                finalR = r * finalA;
                finalG = g * finalA;
                finalB = b * finalA;
            } else {
                finalA = a;
                finalR = r * a;
                finalG = g * a;
                finalB = b * a;
            }

            // Position of the label
            float x, y;

            // Get node sizes (scaled)
            final float sourceSize = edge.getSource().size() * nodeScale;
            final float targetSize = edge.getTarget().size() * nodeScale;

            // Calculate edge vector
            final float dx = edge.getTarget().x() - edge.getSource().x();
            final float dy = edge.getTarget().y() - edge.getSource().y();
            final float edgeLength = (float) Math.sqrt(dx * dx + dy * dy);

            if (edge.isSelfLoop()) {
                // Self-loop: position label at the upper-right of the loop circle (45 degrees)
                final float weight = (float) edge.getWeight(view);
                final float thickness = edgeThickness(edgeScale * edgeRescaleMin, edgeScale * edgeRescaleMax,
                    weight, minWeight, maxWeight);
                final float strokeWidth = thickness * STROKE_MULTIPLIER;
                final float loopRadius = sourceSize * 0.5f + strokeWidth * 0.33f;

                // The loop center is at (node.x + loopRadius, node.y + loopRadius)
                // Position label at 45 degrees (upper-right) on the loop circumference
                // cos(45°) = sin(45°) ≈ 0.707
                final float cos45 = 0.707f;
                x = edge.getSource().x() + loopRadius * (1 + cos45);
                y = edge.getSource().y() + loopRadius * (1 + cos45);
            } else if (edgeLength > 0) {
                // Normalize edge vector
                final float ndx = dx / edgeLength;
                final float ndy = dy / edgeLength;

                if (edge.isDirected()) {
                    // Position at 2/3 from source to target, accounting for node sizes
                    final float offsetFromSource = sourceSize + (edgeLength - sourceSize - targetSize) * 2f / 3f;
                    x = edge.getSource().x() + ndx * offsetFromSource;
                    y = edge.getSource().y() + ndy * offsetFromSource;
                } else {
                    // Position at midpoint, accounting for node sizes
                    final float offsetFromSource = sourceSize + (edgeLength - sourceSize - targetSize) * 0.5f;
                    x = edge.getSource().x() + ndx * offsetFromSource;
                    y = edge.getSource().y() + ndy * offsetFromSource;
                }
            } else {
                // Fallback for zero-length edges (overlapping nodes)
                x = edge.getSource().x();
                y = edge.getSource().y();
            }

            // Update batch
            labelData.updateBatch(edge, i, text, sizeFactor, x, y,
                finalR, finalG, finalB, finalA);
        }
    }

    private int getEdgeColor(final Edge edge, GraphRenderingOptions.EdgeColorMode edgeColorMode) {
        switch (edgeColorMode) {
            case SOURCE: {
                return edge.getSource().getRGBA();
            }
            case TARGET: {
                return edge.getTarget().getRGBA();
            }
            case MIXED: {
                final int s = edge.getSource().getRGBA();
                final int t = edge.getTarget().getRGBA();
                if (s == t) {
                    return s;
                }
                final int b0 = ((s) & 0xFF) + ((t) & 0xFF);
                final int b1 = ((s >>> 8) & 0xFF) + ((t >>> 8) & 0xFF);
                final int b2 = ((s >>> 16) & 0xFF) + ((t >>> 16) & 0xFF);
                final int b3 = ((s >>> 24) & 0xFF) + ((t >>> 24) & 0xFF);
                return ((b3 >>> 1) << 24) | ((b2 >>> 1) << 16) | ((b1 >>> 1) << 8) | (b0 >>> 1);
            }
            case SELF:
            default: {
                return edge.getRGBA();
            }
        }
    }

    /**
     * Computes edge thickness matching the GLSL edge_thickness function.
     */
    private float edgeThickness(float edgeScaleMin, float edgeScaleMax, float weight,
                                float minWeight, float maxWeight) {
        if (Math.abs(edgeScaleMin - edgeScaleMax) < 1e-3f) {
            return edgeScaleMin;
        }
        float weightDivisor = maxWeight - minWeight;
        if (Math.abs(weightDivisor) < 1e-3f) {
            weightDivisor = 1f;
        }
        float t = (weight - minWeight) / weightDivisor;
        t = Math.max(0f, Math.min(1f, t)); // clamp to [0, 1]
        return edgeScaleMin + (edgeScaleMax - edgeScaleMin) * t;
    }

    @Override
    public String getCategory() {
        return PipelineCategory.EDGE_LABEL;
    }

    @Override
    public String getName() {
        return "Edges Labels";
    }

}
