package org.gephi.viz.engine.jogl.models.edgeline.directed;

import static org.gephi.viz.engine.jogl.models.edgeline.directed.CommonEdgeLineDirected.VERTEX_COUNT;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_POSITION;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_POSITION_TARGET;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SOURCE_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_TARGET_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_TARGET_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SOURCE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_TARGET_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_INSET;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MAX;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MIN;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MIN_WEIGHT;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_SCALE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.NumberUtils;
import org.gephi.viz.engine.util.gl.Constants;

/**
 *
 * @author Eduardo Ramos
 */

public class EdgeLineDirectedModelNoSelection {

    private GLShaderProgram program;

    public int getVertexCount() {
        return VERTEX_COUNT;
    }

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "edge";

    private static final String SHADERS_EDGE_LINE_SOURCE_VS = "edge-line-directed";
    private static final String SHADERS_EDGE_LINE_SOURCE_FS = "edge-line-directed";


    public void initProgram(GL2ES2 gl) {
        program = new GLShaderProgram(SHADERS_ROOT, SHADERS_EDGE_LINE_SOURCE_VS, SHADERS_EDGE_LINE_SOURCE_FS)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
            .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
            .addUniformName(UNIFORM_NAME_NODE_SCALE)
            .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
            .addUniformName(UNIFORM_NAME_EDGE_INSET)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION_TARGET, SHADER_POSITION_TARGET_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_SIZE, SHADER_SOURCE_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_SIZE, SHADER_TARGET_SIZE_LOCATION)
            .init(gl);

    }


    public void useProgram(GL2ES2 gl, float[] mvpFloats, float edgeScale, float minWeight, float maxWeight,
                           float edgeRescaleMin, float edgeRescaleMax, float nodeScale, float edgeInset) {
        program.use(gl);
        prepareProgramData(gl, mvpFloats, edgeScale, minWeight, maxWeight, nodeScale, edgeRescaleMin, edgeRescaleMax,
            edgeInset);
    }


    private void prepareProgramData(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight,
                                    float nodeScale, float edgeRescaleMin, float edgeRescaleMax, float edgeInset) {
        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_NODE_SCALE), nodeScale);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_INSET), edgeInset);
        if (NumberUtils.equalsEpsilon(minWeight, maxWeight, 1e-3f)) {
            // All weights equal: rescaling is vacuous, fall back to raw weight × edgeScale
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), scale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), scale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), 1);
        } else {
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), edgeRescaleMin * scale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), edgeRescaleMax * scale);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), maxWeight - minWeight);
        }
    }


    public void destroy(GL2ES2 gl) {
        if (program != null) {
            program.destroy(gl);
            program = null;
        }
    }
}
