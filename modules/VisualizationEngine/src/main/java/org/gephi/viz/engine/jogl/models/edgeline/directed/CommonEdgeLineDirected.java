package org.gephi.viz.engine.jogl.models.edgeline.directed;

import org.gephi.viz.engine.jogl.models.edgeline.CommonEdgeLineModel;

public class CommonEdgeLineDirected extends CommonEdgeLineModel {
    public static final int VERTEX_FLOATS = 3;

    private static final int VERTEX_PER_TRIANGLE = 3;

    public static final int TRIANGLE_COUNT = 3;
    public static final int VERTEX_COUNT = TRIANGLE_COUNT * VERTEX_PER_TRIANGLE;
}
