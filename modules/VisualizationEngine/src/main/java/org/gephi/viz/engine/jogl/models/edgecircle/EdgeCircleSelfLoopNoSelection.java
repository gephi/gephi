package org.gephi.viz.engine.jogl.models.edgecircle;

import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MAX;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MIN;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MIN_WEIGHT;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_SCALE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.models.DataTextureModelSupport;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.gl.Constants;

public class EdgeCircleSelfLoopNoSelection {

    private GLShaderProgram program;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "edge";

    private static final String SHADERS_NODE_CIRCLE_SOURCE_VS = "selfloop";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_FS = "selfloop";

    public void initGLProgram(GL2ES2 gl) {
        program = DataTextureModelSupport.addDataTextureUniforms(
            new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE_VS, SHADERS_NODE_CIRCLE_SOURCE_FS)
                .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
                .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
                .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
                .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
                .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
                .addUniformName(UNIFORM_NAME_NODE_SCALE)
                .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION))
            .init(gl);

        DataTextureModelSupport.initSamplers(gl, program);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float edgeScale, float minWeight, float maxWeight,
                           float edgeRescaleMin, float edgeRescaleMax, float nodeScale, int vertsPerElement) {
        program.use(gl);
        DataTextureModelSupport.setVertsPerElement(gl, program, vertsPerElement);

        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_NODE_SCALE), nodeScale);
        DataTextureModelSupport.setWeightUniforms(gl, program, edgeScale, minWeight, maxWeight, edgeRescaleMin,
            edgeRescaleMax);
    }

    public GLShaderProgram getProgram() {
        return program;
    }

    public void destroy(GL2ES2 gl) {
        if (program != null) {
            program.destroy(gl);
            program = null;
        }
    }
}
