package org.gephi.viz.engine.jogl.models.edgeline.undirected;

import static org.gephi.viz.engine.jogl.models.edgeline.undirected.CommonEdgeLineUndirected.VERTEX_COUNT;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MAX;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MIN;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MIN_WEIGHT;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_SCALE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.models.DataTextureModelSupport;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.gl.Constants;

/**
 *
 * @author Eduardo Ramos
 */


public class EdgeLineUndirectedModelSelectionSelected {
    private GLShaderProgram program;

    public int getVertexCount() {
        return VERTEX_COUNT;
    }

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "edge";

    private static final String SHADERS_EDGE_LINE_SOURCE_VS = "edge-line-undirected_with_selection_selected";
    private static final String SHADERS_EDGE_LINE_SOURCE_FS = "edge-line-undirected";


    public void initProgram(GL2ES2 gl) {
        program = DataTextureModelSupport.addDataTextureUniforms(
            new GLShaderProgram(SHADERS_ROOT, SHADERS_EDGE_LINE_SOURCE_VS, SHADERS_EDGE_LINE_SOURCE_FS)
                .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
                .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
                .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
                .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
                .addUniformName(UNIFORM_NAME_NODE_SCALE)
                .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
                .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
                .addUniformName(UNIFORM_NAME_SELECTION_TIME)
                .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION))
            .init(gl);

        DataTextureModelSupport.initSamplers(gl, program);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float edgeScale, float minWeight,
                           float maxWeight, float edgeRescaleMin, float edgeRescaleMax,
                           float nodeScale, float globalTime,
                           float selectionTime, int vertsPerElement) {
        program.use(gl);
        DataTextureModelSupport.setVertsPerElement(gl, program, vertsPerElement);

        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_NODE_SCALE), nodeScale);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME), selectionTime);
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
