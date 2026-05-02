package org.gephi.viz.engine.jogl.pipeline.indirect.renderers;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.IndirectDraw;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeRenderer;
import org.gephi.viz.engine.jogl.pipeline.common.NodeWorldData;
import org.gephi.viz.engine.jogl.pipeline.indirect.IndirectNodeData;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @author Eduardo Ramos
 */
public class NodeRendererIndirect extends AbstractNodeRenderer {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final IndirectNodeData nodeData;

    public NodeRendererIndirect(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, IndirectNodeData nodeData) {
        this.engine = engine;
        this.nodeData = nodeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }

    @Override
    public NodeWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        nodeData.updateBuffers(target.getDrawable().getGL().getGL4());
        return nodeData.createWorldData(model, engine);
    }

    private final float[] mvpFloats = new float[16];

    @Override
    public void render(NodeWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvpFloats);
        nodeData.drawIndirect(target.getDrawable().getGL().getGL4(), layer, data, mvpFloats);
    }

    @Override
    public int getPreferenceInCategory() {
        return IndirectDraw.getPreferenceInCategory();
    }

    @Override
    public String getName() {
        return "Nodes (Indirect)";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return IndirectDraw.isAvailable(engine, target.getDrawable());
    }
}
