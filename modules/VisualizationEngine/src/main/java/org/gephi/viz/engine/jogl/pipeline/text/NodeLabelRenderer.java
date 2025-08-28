package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.opengl.util.awt.TextRenderer;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;
import org.joml.Vector2f;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;

@SuppressWarnings("rawtypes")
public class NodeLabelRenderer implements Renderer<JOGLRenderingTarget> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(RenderingLayer.FRONT1);

    private final VizEngine engine;
    private final NodesCallback nodesCallback = new NodesCallback();

    // Java2D text
    private TextRenderer textRenderer;
    private Font awtFont;

    // Scratch
    private final float[] mvp = new float[16];

    public NodeLabelRenderer(VizEngine engine) {
        this.engine = engine;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        awtFont = new Font("Arial", Font.PLAIN, 80);

        // Antialiased + fractional metrics = nicer text
        textRenderer = new TextRenderer(awtFont, /*antialiased*/ true, /*fractionalMetrics*/ true);

        final GLCapabilitiesSummary capabilities = engine.getLookup().lookup(GLCapabilitiesSummary.class);
        final OpenGLOptions openGLOptions = engine.getLookup().lookup(OpenGLOptions.class);

        textRenderer.setUseVertexArrays(capabilities.isVAOSupported(openGLOptions));
        textRenderer.setSmoothing(true);
    }

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {
        final GraphIndex graphIndex = engine.getGraphIndex();
        final Rect2D viewBoundaries = engine.getViewBoundaries();
        graphIndex.getVisibleNodes(nodesCallback, viewBoundaries);
    }

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvp);

        textRenderer.begin3DRendering();
        textRenderer.setTransform(mvp);

        final Node[] nodes = nodesCallback.getNodesArray();
        final int count = nodesCallback.getCount();

        for (int i = 0; i < count; i++) {
            final Node node = nodes[i];
            final String text = node.getLabel();
            if (text == null || text.isEmpty()) continue;

            final float sizeFactor = node.size() * 0.01f;

            textRenderer.setColor(0, 0, 0, 1);
            textRenderer.draw3D(
                    text,
                    node.x(),
                    node.y(),
                    0f,
                    sizeFactor
            );
        }

        textRenderer.end3DRendering();
    }

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return Constants.RENDERING_ORDER_LABELS;
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
        return "Node Labels";
    }
}

