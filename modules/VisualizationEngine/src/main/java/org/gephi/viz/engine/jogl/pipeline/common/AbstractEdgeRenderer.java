package org.gephi.viz.engine.jogl.pipeline.common;

import java.util.EnumSet;

import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.util.gl.Constants;

public abstract class AbstractEdgeRenderer implements Renderer<JOGLRenderingTarget> {
    private static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(
            RenderingLayer.BACK1,
            RenderingLayer.MIDDLE1
    );

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return Constants.RENDERING_ORDER_EDGES;
    }

    @Override
    public String getCategory() {
        return PipelineCategory.EDGE;
    }

}
