package org.gephi.viz.engine.jogl.pipeline.indirect.updaters;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.availability.IndirectDraw;
import org.gephi.viz.engine.jogl.pipeline.indirect.IndirectNodeData;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.WorldUpdater;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesUpdaterIndirectRendering implements WorldUpdater<JOGLRenderingTarget> {

    private final VizEngine engine;
    private final IndirectNodeData nodeData;

    public NodesUpdaterIndirectRendering(VizEngine engine, IndirectNodeData nodeData) {
        this.engine = engine;
        this.nodeData = nodeData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        nodeData.init(target.getDrawable().getGL().getGL4());
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        nodeData.dispose(target.getDrawable().getGL().getGL4());
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

    @Override
    public int getOrder() {
        return 0;
    }

}
