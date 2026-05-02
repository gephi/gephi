package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.pipeline.PipelineCategory;

@SuppressWarnings("rawtypes")
public class NodeLabelRenderer extends AbstractLabelRenderer<Node> {

    public NodeLabelRenderer(VizEngine engine, NodeLabelData nodeLabelData) {
        super(engine, nodeLabelData);
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE_LABEL;
    }

    @Override
    public String getName() {
        return "Node Labels";
    }
}

