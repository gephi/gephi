package org.gephi.viz.engine.jogl.models.edgecircle;

import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SELFLOOP_NODE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;

public final class CommonEdgeCircleSelfLoop {
    // Attributes 5
    // Index
    // 0: posX
    // 1: posY
    // 2: color
    // 3: size
    // 4: nodeSize
    public static final int VERTEX_FLOATS = 2;
    public static final int POSITION_FLOATS = 2;
    public static final int COLOR_FLOATS = 1;
    public static final int SIZE_FLOATS = 1;
    public static final int NODE_SIZE_FLOATS = 1;

    public static final int TOTAL_ATTRIBUTES_FLOATS
        = POSITION_FLOATS
        + COLOR_FLOATS
        + SIZE_FLOATS
        + NODE_SIZE_FLOATS;

    public static final int[] USED_ATTRIBUTE_LOCATIONS = {
        SHADER_VERT_LOCATION,
        SHADER_POSITION_LOCATION,
        SHADER_COLOR_LOCATION,
        SHADER_SIZE_LOCATION,
        SHADER_SELFLOOP_NODE_SIZE_LOCATION
    };

    public static final int[] INSTANCED_ATTRIBUTE_LOCATIONS = {
        SHADER_POSITION_LOCATION,
        SHADER_COLOR_LOCATION,
        SHADER_SIZE_LOCATION,
        SHADER_SELFLOOP_NODE_SIZE_LOCATION
    };
}
