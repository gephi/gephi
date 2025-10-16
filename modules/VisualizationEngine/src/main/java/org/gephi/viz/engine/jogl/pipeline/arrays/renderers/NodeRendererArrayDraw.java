package org.gephi.viz.engine.jogl.pipeline.arrays.renderers;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.ArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawNodeData;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeRenderer;
import org.gephi.viz.engine.jogl.pipeline.common.NodeWorldData;
import org.gephi.viz.engine.pipeline.RenderingLayer;

/**
 *
 * @author Eduardo Ramos
 */
public class NodeRendererArrayDraw extends AbstractNodeRenderer {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final ArrayDrawNodeData nodeData;

    public NodeRendererArrayDraw(VizEngine<JOGLRenderingTarget, NEWTEvent> engine, ArrayDrawNodeData nodeData) {
        this.engine = engine;
        this.nodeData = nodeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }

    @Override
    public NodeWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        nodeData.updateBuffers();
        return nodeData.createWorldData(model, engine);
    }

    private final float[] mvpFloats = new float[16];

    @Override
    public void render(NodeWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        engine.getModelViewProjectionMatrixFloats(mvpFloats);

        nodeData.drawArrays(target.getDrawable().getGL().getGL2ES2(), layer, data, mvpFloats);
    }

    @Override
    public int getPreferenceInCategory() {
        return ArrayDraw.getPreferenceInCategory();
    }

    @Override
    public String getName() {
        return "Nodes (Vertex Array)";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return ArrayDraw.isAvailable(engine, target.getDrawable());
    }
}
