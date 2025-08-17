package org.gephi.viz.engine.jogl.pipeline.instanced.updaters;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.availability.InstancedDraw;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.jogl.pipeline.instanced.InstancedEdgeData;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.structure.GraphIndexImpl;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgesUpdaterInstancedRendering implements WorldUpdater<JOGLRenderingTarget> {

    private final VizEngine engine;
    private final InstancedEdgeData edgeData;
    private final GraphIndexImpl spatialIndex;

    public EdgesUpdaterInstancedRendering(VizEngine engine, InstancedEdgeData edgeData, GraphIndexImpl spatialIndex) {
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
        edgeData.dispose(target.getDrawable().getGL());
    }

    @Override
    public void updateWorld() {
        //final long start = TimeUtils.getTimeMillis();
        edgeData.update(engine, spatialIndex);
        //System.out.println("Edges update ms: " + (TimeUtils.getTimeMillis() - start));
    }

    @Override
    public String getCategory() {
        return PipelineCategory.EDGE;
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

    @Override
    public int getOrder() {
        return 0;
    }

}
