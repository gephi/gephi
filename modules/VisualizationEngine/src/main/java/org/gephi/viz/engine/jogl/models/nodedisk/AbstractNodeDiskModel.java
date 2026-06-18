package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_ELEMENT_INDEX;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_ELEMENT_INDEX_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BORDER_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_DARKEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_POS_TEXTURE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_SCALE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_STYLE_TEXTURE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_TEXTURE_WIDTH;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.pipeline.common.NodeDataTextureStore;
import org.gephi.viz.engine.jogl.util.gl.GLDataTexture;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.gl.Constants;

/**
 * Shared boilerplate for the node-disk shader programs. All node models draw the same SDF quad and
 * read node positions/sizes/colors from the shared node data texture; they only differ in their
 * shader sources and in the extra uniforms they set on top of the common model-view-projection,
 * node-scale and node-data-texture set.
 *
 * @author Eduardo Ramos
 */
abstract class AbstractNodeDiskModel {

    protected static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "node";

    protected GLShaderProgram program;

    /**
     * Builds and links the program with the common node uniforms/attributes plus the given extra
     * uniform names, then sets the constant node-data-texture sampler unit and width.
     */
    protected final void initProgram(GL2ES2 gl, String vertexShader, String fragmentShader,
                                     String... extraUniformNames) {
        program = new GLShaderProgram(SHADERS_ROOT, vertexShader, fragmentShader)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_NODE_SCALE)
            .addUniformName(UNIFORM_NAME_NODE_POS_TEXTURE)
            .addUniformName(UNIFORM_NAME_NODE_STYLE_TEXTURE)
            .addUniformName(UNIFORM_NAME_TEXTURE_WIDTH)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_ELEMENT_INDEX, SHADER_ELEMENT_INDEX_LOCATION);
        for (String uniform : extraUniformNames) {
            program.addUniformName(uniform);
        }
        program.init(gl);

        program.use(gl);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_NODE_POS_TEXTURE),
            NodeDataTextureStore.NODE_POS_TEXTURE_UNIT);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_NODE_STYLE_TEXTURE),
            NodeDataTextureStore.NODE_STYLE_TEXTURE_UNIT);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_TEXTURE_WIDTH), GLDataTexture.TEXTURE_WIDTH);
        program.stopUsing(gl);
    }

    /**
     * Binds the program and sets the model-view-projection and node-scale uniforms common to all node
     * models.
     */
    protected final void useCommon(GL2ES2 gl, float[] mvpFloats, float nodeScale) {
        program.use(gl);
        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_NODE_SCALE), nodeScale);
    }

    /** Sets the node border size and darken/lighten factor uniforms. */
    protected final void setBorder(GL2ES2 gl, float nodeBorderColorFactor) {
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_BORDER_SIZE), Constants.getNodeBorderSize());
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_DARKEN_FACTOR), nodeBorderColorFactor);
    }

    /** Sets the animation global-time and selection-time uniforms. */
    protected final void setTimes(GL2ES2 gl, float globalTime, float selectionTime) {
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME), selectionTime);
    }

    public void destroy(GL2ES2 gl) {
        if (program != null) {
            program.destroy(gl);
            program = null;
        }
    }
}
