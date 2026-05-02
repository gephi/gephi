package org.gephi.viz.engine.jogl.pipeline.instanced.renderers;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.InstancedDraw;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractEdgeRenderer;
import org.gephi.viz.engine.jogl.pipeline.common.EdgeWorldData;
import org.gephi.viz.engine.jogl.pipeline.instanced.InstancedEdgeData;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgeRendererInstanced extends AbstractEdgeRenderer {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final InstancedEdgeData edgeData;

    public EdgeRendererInstanced(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, InstancedEdgeData edgeData) {
        this.engine = engine;
        this.edgeData = edgeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }

    @Override
    public EdgeWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        edgeData.updateBuffers(target.getDrawable().getGL());
        return edgeData.createWorldData(model, engine);
    }

    private final float[] mvpFloats = new float[16];

    @Override
    public void render(EdgeWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvpFloats);
        edgeData.drawInstanced(
            target.getDrawable().getGL().getGL3ES3(),
            layer,
            data, mvpFloats
        );
    }

    @Override
    public int getPreferenceInCategory() {
        return InstancedDraw.getPreferenceInCategory();
    }

    @Override
    public String getName() {
        return "Edges (Instanced)";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return InstancedDraw.isAvailable(engine, target.getDrawable());
    }
}
