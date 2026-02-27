package org.gephi.viz.engine.jogl.models.edgeline;

public class CommonEdgeLineModel {

    public static final int POSITION_SOURCE_FLOATS = 2;
    public static final int POSITION_TARGET_FLOATS = 2;
    public static final int COLOR_FLOATS = 1;
    public static final int SOURCE_SIZE_FLOATS = 1;
    public static final int TARGET_SIZE_FLOATS = 1;
    public static final int SIZE_FLOATS = 1;

    public static final int TOTAL_ATTRIBUTES_FLOATS
        = POSITION_SOURCE_FLOATS
        + POSITION_TARGET_FLOATS
        + COLOR_FLOATS
        + SIZE_FLOATS
        + SOURCE_SIZE_FLOATS
        + TARGET_SIZE_FLOATS;
}
