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
    public NodeWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target, float[] mvpFloats) {
        nodeData.updateBuffers();
        return nodeData.createWorldData(model, engine);
    }

    @Override
    public void render(NodeWorldData data, JOGLRenderingTarget target, RenderingLayer layer, float[] mvpFloats) {

        nodeData.drawArrays(target.getDrawable().getGL().getGL3ES3(), layer, data, mvpFloats);
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
