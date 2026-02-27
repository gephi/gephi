package org.gephi.viz.engine.jogl.models.edgeline.undirected;

import org.gephi.viz.engine.jogl.models.edgeline.CommonEdgeLineModel;

public final class CommonEdgeLineUndirected extends CommonEdgeLineModel {
    public static final int VERTEX_FLOATS = 2;
    public static final int VERTEX_PER_TRIANGLE = 3;

    public static final int TRIANGLE_COUNT = 2;
    public static final int VERTEX_COUNT = TRIANGLE_COUNT * VERTEX_PER_TRIANGLE;
}
