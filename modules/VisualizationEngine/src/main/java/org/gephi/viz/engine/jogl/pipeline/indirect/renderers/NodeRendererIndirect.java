package org.gephi.viz.engine.jogl.pipeline.indirect.renderers;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.IndirectDraw;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeRenderer;
import org.gephi.viz.engine.jogl.pipeline.indirect.IndirectNodeData;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @author Eduardo Ramos
 */
public class NodeRendererIndirect extends AbstractNodeRenderer {

    private final VizEngine engine;
    private final IndirectNodeData nodeData;

    public NodeRendererIndirect(VizEngine engine, IndirectNodeData nodeData) {
        this.engine = engine;
        this.nodeData = nodeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {
        nodeData.updateBuffers(target.getDrawable().getGL().getGL4());
    }

    private final float[] mvpFloats = new float[16];

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvpFloats);
        nodeData.drawIndirect(target.getDrawable().getGL().getGL4(), layer, engine, mvpFloats);
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
