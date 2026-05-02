package org.gephi.viz.engine.jogl.pipeline.arrays.renderers;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.ArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawEdgeData;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractEdgeRenderer;
import org.gephi.viz.engine.jogl.pipeline.common.EdgeWorldData;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgeRendererArrayDraw extends AbstractEdgeRenderer {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final ArrayDrawEdgeData edgeData;

    public EdgeRendererArrayDraw(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, ArrayDrawEdgeData edgeData) {
        this.engine = engine;
        this.edgeData = edgeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }

    @Override
    public EdgeWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        edgeData.updateBuffers();
        return edgeData.createWorldData(model, engine);
    }

    private final float[] mvpFloats = new float[16];

    @Override
    public void render(EdgeWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvpFloats);

        edgeData.drawArrays(target.getDrawable().getGL().getGL2ES2(), layer, data, mvpFloats);
    }

    @Override
    public int getPreferenceInCategory() {
        return ArrayDraw.getPreferenceInCategory();
    }

    @Override
    public String getName() {
        return "Edges (Vertex Array)";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return ArrayDraw.isAvailable(engine, target.getDrawable());
    }
}
