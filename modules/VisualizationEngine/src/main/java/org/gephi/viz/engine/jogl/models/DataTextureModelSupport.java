package org.gephi.viz.engine.jogl.models;

import static org.gephi.viz.engine.util.gl.Constants.ELEMENT_TEXTURE_UNIT;
import static org.gephi.viz.engine.util.gl.Constants.NODE_TEXTURE_UNIT;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_ELEMENT_OFFSET;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_ELEMENT_TEXTURE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_NODE_TEXTURE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_TEXTURE_WIDTH;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_VERTS_PER_ELEMENT;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.util.gl.GLDataTexture;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;

/**
 * Helper for edge shader programs that read their per-element data and their endpoint node data from
 * {@code RGBA32F} data textures via {@code texelFetch} (see {@code common.datatexture.glsl} and
 * {@code common.edge.index.glsl}).
 *
 * @author Eduardo Ramos
 */
public final class DataTextureModelSupport {

    private DataTextureModelSupport() {
    }

    /**
     * Registers the data-texture related uniforms ({@code u_elementTexture}, {@code u_nodeTexture},
     * {@code u_texWidth}, {@code u_vertsPerElement}, {@code u_elementOffset}) on the given (not yet
     * initialized) program.
     */
    public static GLShaderProgram addDataTextureUniforms(GLShaderProgram program) {
        return program
            .addUniformName(UNIFORM_NAME_ELEMENT_TEXTURE)
            .addUniformName(UNIFORM_NAME_NODE_TEXTURE)
            .addUniformName(UNIFORM_NAME_TEXTURE_WIDTH)
            .addUniformName(UNIFORM_NAME_VERTS_PER_ELEMENT)
            .addUniformName(UNIFORM_NAME_ELEMENT_OFFSET);
    }

    /**
     * Sets the constant sampler units and the texture width once after the program has been linked.
     */
    public static void initSamplers(GL2ES2 gl, GLShaderProgram program) {
        program.use(gl);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_ELEMENT_TEXTURE), ELEMENT_TEXTURE_UNIT);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_NODE_TEXTURE), NODE_TEXTURE_UNIT);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_TEXTURE_WIDTH), GLDataTexture.TEXTURE_WIDTH);
        program.stopUsing(gl);
    }

    /**
     * Selects the edge index source for the current draw: {@code 0} for instanced rendering (uses
     * {@code gl_InstanceID}) or the number of vertices per edge for array-draw (uses
     * {@code gl_VertexID / vertsPerElement}). The program must be in use.
     */
    public static void setVertsPerElement(GL2ES2 gl, GLShaderProgram program, int vertsPerElement) {
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_VERTS_PER_ELEMENT), vertsPerElement);
    }

    /**
     * Sets the base element index for the currently drawn range (added to {@code gl_InstanceID} /
     * {@code gl_VertexID / vertsPerElement} in the shader). The program must be in use.
     */
    public static void setElementOffset(GL2ES2 gl, GLShaderProgram program, int elementOffset) {
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_ELEMENT_OFFSET), elementOffset);
    }
}
