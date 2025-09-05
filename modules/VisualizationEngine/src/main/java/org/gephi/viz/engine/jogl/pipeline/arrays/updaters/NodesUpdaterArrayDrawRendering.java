package org.gephi.viz.engine.jogl.pipeline.arrays.updaters;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.ArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawNodeData;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.WorldUpdater;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesUpdaterArrayDrawRendering implements WorldUpdater<JOGLRenderingTarget> {

    private final VizEngine engine;
    private final ArrayDrawNodeData nodeData;

    public NodesUpdaterArrayDrawRendering(VizEngine engine, ArrayDrawNodeData nodeData) {
        this.engine = engine;
        this.nodeData = nodeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        nodeData.init(target.getDrawable().getGL().getGL2ES2());
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        nodeData.dispose(target.getDrawable().getGL().getGL2ES2());
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        nodeData.update(model.getGraphIndex(), model.getGraphSelection(), model.getRenderingOptions(),
            engine.getViewBoundaries());
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE;
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

    @Override
    public int getOrder() {
        return 0;
    }

}
