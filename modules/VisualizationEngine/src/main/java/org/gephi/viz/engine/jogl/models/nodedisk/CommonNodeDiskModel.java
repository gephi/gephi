package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;

import org.gephi.viz.engine.util.gl.Constants;

public final class CommonNodeDiskModel {
    public static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "node";

    public static final int VERTEX_FLOATS = 2;
    public static final int POSITION_FLOATS = 2;
    public static final int COLOR_FLOATS = 1;
    public static final int SIZE_FLOATS = 1;

    public static final int TOTAL_ATTRIBUTES_FLOATS
        = POSITION_FLOATS
        + COLOR_FLOATS
        + SIZE_FLOATS;

    public static final int[] USED_ATTRIBUTE_LOCATIONS = {
        SHADER_VERT_LOCATION,
        SHADER_POSITION_LOCATION,
        SHADER_COLOR_LOCATION,
        SHADER_SIZE_LOCATION
    };

    public static final int[] INSTANCED_ATTRIBUTE_LOCATIONS = {
        SHADER_POSITION_LOCATION,
        SHADER_COLOR_LOCATION,
        SHADER_SIZE_LOCATION
    };
}
