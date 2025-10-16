package org.gephi.viz.engine.jogl.pipeline.arrays.updaters;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.ArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawEdgeData;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.WorldUpdater;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgesUpdaterArrayDrawRendering implements WorldUpdater<JOGLRenderingTarget> {

    private final VizEngine engine;
    private final ArrayDrawEdgeData edgeData;

    public EdgesUpdaterArrayDrawRendering(VizEngine engine, ArrayDrawEdgeData edgeData) {
        this.engine = engine;
        this.edgeData = edgeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        edgeData.init(target.getDrawable().getGL().getGL2ES2());
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        edgeData.dispose(target.getDrawable().getGL().getGL2ES2());
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        edgeData.update(model.getGraphIndex(), model.getGraphSelection(), model.getRenderingOptions(),
            engine.getViewBoundaries());
    }

    @Override
    public String getCategory() {
        return PipelineCategory.EDGE;
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

    @Override
    public int getOrder() {
        return 0;
    }

}
