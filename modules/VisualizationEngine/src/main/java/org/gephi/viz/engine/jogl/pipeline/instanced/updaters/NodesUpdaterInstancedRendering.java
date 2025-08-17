package org.gephi.viz.engine.jogl.pipeline.instanced.updaters;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.availability.InstancedDraw;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.jogl.pipeline.instanced.InstancedNodeData;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.structure.GraphIndexImpl;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesUpdaterInstancedRendering implements WorldUpdater<JOGLRenderingTarget> {

    private final VizEngine engine;
    private final InstancedNodeData nodeData;
    private final GraphIndexImpl spatialIndex;

    public NodesUpdaterInstancedRendering(VizEngine engine, InstancedNodeData nodeData, GraphIndexImpl spatialIndex) {
        this.engine = engine;
        this.nodeData = nodeData;
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        nodeData.init(target.getDrawable().getGL().getGL2ES2());
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        nodeData.dispose(target.getDrawable().getGL());
    }

    @Override
    public void updateWorld() {
        nodeData.update(engine, spatialIndex);
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE;
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

    @Override
    public int getOrder() {
        return 0;
    }

}
