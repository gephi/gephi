package org.gephi.viz.engine.jogl.models.edgeline;

import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_TARGET_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SOURCE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_TARGET_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;

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

    public static final int[] USED_ATTRIBUTE_LOCATIONS = {
        SHADER_VERT_LOCATION,
        SHADER_POSITION_LOCATION,
        SHADER_POSITION_TARGET_LOCATION,
        SHADER_SIZE_LOCATION,
        SHADER_COLOR_LOCATION,
        SHADER_SOURCE_SIZE_LOCATION,
        SHADER_TARGET_SIZE_LOCATION
    };

    public static final int[] INSTANCED_ATTRIBUTE_LOCATIONS = {
        SHADER_POSITION_LOCATION,
        SHADER_POSITION_TARGET_LOCATION,
        SHADER_SIZE_LOCATION,
        SHADER_COLOR_LOCATION,
        SHADER_SOURCE_SIZE_LOCATION,
        SHADER_TARGET_SIZE_LOCATION
    };
}
