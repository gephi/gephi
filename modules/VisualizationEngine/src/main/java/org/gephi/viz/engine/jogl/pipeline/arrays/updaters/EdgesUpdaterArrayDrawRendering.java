package org.gephi.viz.engine.jogl.pipeline.arrays.updaters;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.availability.ArrayDraw;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawEdgeData;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.structure.GraphIndexImpl;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgesUpdaterArrayDrawRendering implements WorldUpdater<JOGLRenderingTarget> {

    private final VizEngine engine;
    private final ArrayDrawEdgeData edgeData;
    private final GraphIndexImpl spatialIndex;

    public EdgesUpdaterArrayDrawRendering(VizEngine engine, ArrayDrawEdgeData edgeData, GraphIndexImpl spatialIndex) {
        this.engine = engine;
        this.edgeData = edgeData;
        this.spatialIndex = spatialIndex;
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
    public void updateWorld() {
        edgeData.update(engine, spatialIndex);
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
