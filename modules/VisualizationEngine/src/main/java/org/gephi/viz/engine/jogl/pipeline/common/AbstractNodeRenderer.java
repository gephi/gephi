package org.gephi.viz.engine.jogl.pipeline.common;

import java.util.EnumSet;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.util.gl.Constants;

public abstract class AbstractNodeRenderer implements Renderer<JOGLRenderingTarget, NodeWorldData> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(
        RenderingLayer.MIDDLE1,
        RenderingLayer.MIDDLE2
    );

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return Constants.RENDERING_ORDER_NODES;
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE;
    }

    /**
     * The node data backing this renderer (positions/colors/sizes and per-node GL buffers).
     */
    public abstract AbstractNodeData getNodeData();

    /**
     * Resolves the node under the given screen position using GPU picking. Must be called on the GL
     * thread, after the current frame has been rendered.
     *
     * @return the node under the cursor, or {@code null} if there is none / picking is unavailable
     */
    public Node pickNode(final JOGLRenderingTarget target, final int screenX, final int screenY, final int width,
                         final int height, final float[] mvpFloats, final float nodeScale) {
        return getNodeData()
            .pickNode(target.getDrawable().getGL(), screenX, screenY, width, height, mvpFloats, nodeScale);
    }

    /**
     * Whether GPU node picking is usable for this renderer.
     */
    public boolean isPickingAvailable() {
        return getNodeData().isPickingAvailable();
    }
}
