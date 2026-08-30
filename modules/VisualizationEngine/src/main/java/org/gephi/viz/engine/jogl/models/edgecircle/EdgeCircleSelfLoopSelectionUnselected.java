package org.gephi.viz.engine.jogl.models.edgecircle;

import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_POSITION;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SELFLOOP_NODE_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SELFLOOP_NODE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BACKGROUND_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_COLOR_LIGHTEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MAX;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MIN;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MIN_WEIGHT;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_SCALE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR;

import com.jogamp.opengl.GL3ES3;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.NumberUtils;
import org.gephi.viz.engine.util.gl.Constants;

public class EdgeCircleSelfLoopSelectionUnselected {
    private GLShaderProgram program;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "edge";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_VS = "selfloop_unselected";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_FS = "selfloop_unselected";

    public void initGLProgram(GL3ES3 gl) {
        program = new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE_VS, SHADERS_NODE_CIRCLE_SOURCE_FS)
            .addUniformName(UNIFORM_NAME_BACKGROUND_COLOR)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR)
            .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
            .addUniformName(UNIFORM_NAME_SELECTION_TIME)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
            .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
            .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
            .addUniformName(UNIFORM_NAME_NODE_SCALE)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SELFLOOP_NODE_SIZE, SHADER_SELFLOOP_NODE_SIZE_LOCATION)
            .init(gl);
    }

    public void useProgram(GL3ES3 gl, float[] mvpFloats, float[] backgroundColorFloats, float colorLightenFactor,
                           float globalTime, float selectionTime, float edgeScale, float minWeight, float maxWeight,
                           float edgeRescaleMin, float edgeRescaleMax, float nodeScale) {
        program.use(gl);
        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR), colorLightenFactor);
        gl.glUniform4fv(program.getUniformLocation(UNIFORM_NAME_BACKGROUND_COLOR), 1, backgroundColorFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME), selectionTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_NODE_SCALE), nodeScale);
        if (NumberUtils.equalsEpsilon(minWeight, maxWeight, 1e-3f)) {
            // All weights equal: rescaling is vacuous, fall back to raw weight × edgeScale
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), edgeScale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), edgeScale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), 1);
        } else {
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), edgeRescaleMin * edgeScale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), edgeRescaleMax * edgeScale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), maxWeight - minWeight);
        }
    }

    public void destroy(GL3ES3 gl) {
        if (program != null) {
            program.destroy(gl);
            program = null;
        }
    }
}
