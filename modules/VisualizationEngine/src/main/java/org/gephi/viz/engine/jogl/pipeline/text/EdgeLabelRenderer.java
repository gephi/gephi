package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Edge;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.pipeline.PipelineCategory;


public class EdgeLabelRenderer extends AbstractLabelRenderer<Edge> {

    public EdgeLabelRenderer(VizEngine engine, EdgeLabelData edgeLabelData) {
        super(engine, edgeLabelData);
    }

    @Override
    public String getCategory() {
        return PipelineCategory.EDGE_LABEL;
    }

    @Override
    public String getName() {
        return "Edge Labels";
    }
}

