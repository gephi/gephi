package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Edge;
import org.gephi.viz.engine.util.structure.EdgesCallback;

public class EdgeLabelData extends AbstractLabelData<Edge> {

    public EdgeLabelData(EdgesCallback edgesCallback) {
        super(edgesCallback);
    }

    public EdgesCallback getEdgesCallback() {
        return (EdgesCallback) getElementsCallback();
    }
}
