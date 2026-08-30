package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_POSITION;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BORDER_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_DARKEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;

import com.jogamp.opengl.GL3ES3;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.gl.Constants;

/**
 * @author Eduardo Ramos
 */
public class NodeDiskModelSelectionSelected {

    private GLShaderProgram program;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "node";

    private static final String SHADERS_NODE_CIRCLE_SOURCE_VS = "node_with_selection_selected";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_FS = "node";

    public void initGLPrograms(GL3ES3 gl) {
        program =
            new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE_VS,
                SHADERS_NODE_CIRCLE_SOURCE_FS)
                .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
                .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
                .addUniformName(UNIFORM_NAME_SELECTION_TIME)
                .addUniformName(UNIFORM_NAME_BORDER_SIZE)
                .addUniformName(UNIFORM_NAME_DARKEN_FACTOR)
                .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
                .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
                .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
                .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
                .init(gl);


    }

    public void useProgram(GL3ES3 gl, float[] mvpFloats,
                           float globalTime, float selectedTime, float nodeBorderColorFactor) {
        //Circle:
        program.use(gl);

        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1,
            false, mvpFloats, 0);

        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME), selectedTime);

        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_BORDER_SIZE),
            Constants.getNodeBorderSize());
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_DARKEN_FACTOR),
            nodeBorderColorFactor);
    }


    public void destroy(GL3ES3 gl) {
        if (program != null) {
            program.destroy(gl);
            program = null;
        }
    }
}
