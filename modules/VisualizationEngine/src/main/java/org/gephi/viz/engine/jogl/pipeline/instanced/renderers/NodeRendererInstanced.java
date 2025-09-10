package org.gephi.viz.engine.jogl.pipeline.instanced.renderers;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.InstancedDraw;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeRenderer;
import org.gephi.viz.engine.jogl.pipeline.instanced.InstancedNodeData;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @author Eduardo Ramos
 */
public class NodeRendererInstanced extends AbstractNodeRenderer {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final InstancedNodeData nodeData;

    public NodeRendererInstanced(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, InstancedNodeData nodeData) {
        this.engine = engine;
        this.nodeData = nodeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }

    @Override
    public void worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        nodeData.updateBuffers(target.getDrawable().getGL());
    }

    private final float[] mvpFloats = new float[16];

    @Override
    public void render(VizEngineModel model, JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvpFloats);
        nodeData.drawInstanced(target.getDrawable().getGL().getGL2ES3(), layer, engine, model, mvpFloats);
    }

    @Override
    public int getPreferenceInCategory() {
        return InstancedDraw.getPreferenceInCategory();
    }

    @Override
    public String getName() {
        return "Nodes (Instanced)";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return InstancedDraw.isAvailable(engine, target.getDrawable());
    }
}
