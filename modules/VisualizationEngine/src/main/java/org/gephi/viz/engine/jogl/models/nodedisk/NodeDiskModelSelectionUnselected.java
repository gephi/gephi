package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_ELEMENT_INDEX;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_ELEMENT_INDEX_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BACKGROUND_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BORDER_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_COLOR_LIGHTEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_DARKEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_SCALE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_TEXTURE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_TEXTURE_WIDTH;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.pipeline.common.NodeDataTextureStore;
import org.gephi.viz.engine.jogl.util.gl.GLDataTexture;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.gl.Constants;

/**
 * @author Eduardo Ramos
 */
public class NodeDiskModelSelectionUnselected {

    private GLShaderProgram program;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "node";

    private static final String SHADERS_NODE_CIRCLE_SOURCE_VS = "node_with_selection_unselected";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_FS = "node_with_selection_unselected";

    public void initGLPrograms(GL2ES2 gl) {
        program = new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE_VS,
            SHADERS_NODE_CIRCLE_SOURCE_FS)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_BACKGROUND_COLOR)
            .addUniformName(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR)
            .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
            .addUniformName(UNIFORM_NAME_SELECTION_TIME)
            .addUniformName(UNIFORM_NAME_BORDER_SIZE)
            .addUniformName(UNIFORM_NAME_DARKEN_FACTOR)
            .addUniformName(UNIFORM_NAME_NODE_SCALE)
            .addUniformName(UNIFORM_NAME_NODE_TEXTURE)
            .addUniformName(UNIFORM_NAME_TEXTURE_WIDTH)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_ELEMENT_INDEX, SHADER_ELEMENT_INDEX_LOCATION)
            .init(gl);

        program.use(gl);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_NODE_TEXTURE), NodeDataTextureStore.NODE_TEXTURE_UNIT);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_TEXTURE_WIDTH), GLDataTexture.TEXTURE_WIDTH);
        program.stopUsing(gl);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats,
                           float[] backgroundColorFloats, float colorLightenFactor,
                           float globalTime, float selectedTime, float nodeBorderColorFactor, float nodeScale) {
        //Circle:
        program.use(gl);

        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1,
            false, mvpFloats, 0);

        gl.glUniform4fv(program.getUniformLocation(UNIFORM_NAME_BACKGROUND_COLOR), 1,
            backgroundColorFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR),
            colorLightenFactor);

        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME), selectedTime);

        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_BORDER_SIZE),
            Constants.getNodeBorderSize());
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_DARKEN_FACTOR),
            nodeBorderColorFactor);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_NODE_SCALE), nodeScale);
    }

    public void destroy(GL2ES2 gl) {
        if (program != null) {
            program.destroy(gl);
            program = null;
        }
    }
}
