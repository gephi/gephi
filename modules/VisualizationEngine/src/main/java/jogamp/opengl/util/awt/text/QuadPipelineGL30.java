/*
 * Copyright 2012 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package jogamp.opengl.util.awt.text;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;


/**
 * {@link QuadPipeline} for use with OpenGL 3.
 *
 * <p>
 * {@code QuadPipelineGL30} draws quads using OpenGL 3 features.  It uses a Vertex Buffer Object to
 * store vertices in graphics memory and a Vertex Array Object to quickly switch which vertex
 * attributes are enabled.
 *
 * <p>
 * Since {@code GL_QUAD} has been deprecated in OpenGL 3, this implementation uses two triangles to
 * represent one quad.  An alternative implementation using one {@code GL_FAN} per quad was also
 * tested, but proved slower in most cases.  Apparently the penalty imposed by the extra work
 * required by the driver outweighed the benefit of transferring less vertices.
 */
/*@VisibleForTesting*/
/*@NotThreadSafe*/
public final class QuadPipelineGL30 extends AbstractQuadPipeline {

    /**
     * Name of point attribute in shader program.
     */
    /*@Nonnull*/
    private static final String POINT_ATTRIB_NAME = "MCVertex";

    /**
     * Name of texture coordinate attribute in shader program.
     */
    /*@Nonnull*/
    private static final String COORD_ATTRIB_NAME = "TexCoord0";

    /**
     * Number of vertices per primitive.
     */
    /*@Nonnegative*/
    private static final int VERTS_PER_PRIM = 3;

    /**
     * Number of primitives per quad.
     */
    /*@Nonnegative*/
    private static final int PRIMS_PER_QUAD = 2;

    /**
     * Vertex Buffer Object with vertex data.
     */
    /*@Nonnegative*/
    private final int vbo;

    /**
     * Vertex Array Object with vertex attribute state.
     */
    /*@Nonnegative*/
    private final int vao;

    /**
     * Constructs a {@link QuadPipelineGL30}.
     *
     * @param gl Current OpenGL context
     * @param shaderProgram Shader program to render quads with
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if shader program is less than one
     */
    /*@VisibleForTesting*/
    public QuadPipelineGL30(/*@Nonnull*/ final GL3 gl, /*@Nonnegative*/ final int shaderProgram) {

        super(VERTS_PER_PRIM, PRIMS_PER_QUAD);

        Check.notNull(gl, "GL cannot be null");
        Check.argument(shaderProgram > 0, "Shader program cannot be less than one");

        this.vbo = createVertexBufferObject(gl, BYTES_PER_BUFFER);
        this.vao = createVertexArrayObject(gl, shaderProgram, vbo);
    }

    @Override
    public void beginRendering(/*@Nonnull*/ final GL gl) {

        super.beginRendering(gl);

        final GL3 gl3 = gl.getGL3();

        // Bind the VBO and VAO
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo);
        gl3.glBindVertexArray(vao);
    }

    /**
     * Creates a vertex array object for use with the pipeline.
     *
     * @param gl Current OpenGL context, assumed not null
     * @param program OpenGL handle to the shader program, assumed not negative
     * @param vbo OpenGL handle to VBO holding vertices, assumed not negative
     * @return OpenGL handle to resulting VAO
     */
    /*@Nonnegative*/
    private static int createVertexArrayObject(/*@Nonnull*/ final GL3 gl,
                                               /*@Nonnegative*/ final int program,
                                               /*@Nonnegative*/ final int vbo) {

        // Generate
        final int[] handles = new int[1];
        gl.glGenVertexArrays(1, handles, 0);
        final int vao = handles[0];

        // Bind
        gl.glBindVertexArray(vao);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo);

        // Points
        final int pointLoc = gl.glGetAttribLocation(program, POINT_ATTRIB_NAME);
        if (pointLoc == -1) {
            throw new IllegalStateException("Could not find point attribute location!");
        } else {
            gl.glEnableVertexAttribArray(pointLoc);
            gl.glVertexAttribPointer(
                    pointLoc,            // location
                    FLOATS_PER_POINT,    // number of components
                    GL3.GL_FLOAT,        // type
                    false,               // normalized
                    STRIDE,              // stride
                    POINT_OFFSET);       // offset
        }

        // Coords
        final int coordLoc = gl.glGetAttribLocation(program, COORD_ATTRIB_NAME);
        if (coordLoc != -1) {
            gl.glEnableVertexAttribArray(coordLoc);
            gl.glVertexAttribPointer(
                    coordLoc,            // location
                    FLOATS_PER_COORD,    // number of components
                    GL3.GL_FLOAT,        // type
                    false,               // normalized
                    STRIDE,              // stride
                    COORD_OFFSET);       // offset
        }

        // Unbind
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(0);

        return vao;
    }

    @Override
    public void dispose(/*@Nonnull*/ final GL gl) {

        super.dispose(gl);

        final GL3 gl3 = gl.getGL3();

        // Delete VBO and VAO
        final int[] handles = new int[1];
        handles[0] = vbo;
        gl3.glDeleteBuffers(1, handles, 0);
        handles[0] = vao;
        gl3.glDeleteVertexArrays(1, handles, 0);
    }

    @Override
    protected void doAddQuad(/*@Nonnull*/ final Quad quad) {

        Check.notNull(quad, "Quad cannot be null");

        // Add upper-left triangle
        addPoint(quad.xr, quad.yt, quad.z);
        addCoord(quad.sr, quad.tt);
        addPoint(quad.xl, quad.yt, quad.z);
        addCoord(quad.sl, quad.tt);
        addPoint(quad.xl, quad.yb, quad.z);
        addCoord(quad.sl, quad.tb);

        // Add lower-right triangle
        addPoint(quad.xr, quad.yt, quad.z);
        addCoord(quad.sr, quad.tt);
        addPoint(quad.xl, quad.yb, quad.z);
        addCoord(quad.sl, quad.tb);
        addPoint(quad.xr, quad.yb, quad.z);
        addCoord(quad.sr, quad.tb);
    }

    @Override
    protected void doFlush(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        final GL3 gl3 = gl.getGL3();

        // Upload data
        rewind();
        gl3.glBufferSubData(
                GL3.GL_ARRAY_BUFFER, // target
                0,                   // offset
                getSizeInBytes(),    // size
                getData());          // data

        // Draw
        gl3.glDrawArrays(
                GL3.GL_TRIANGLES,     // mode
                0,                    // first
                getSizeInVertices()); // count
        clear();
    }

    @Override
    public void endRendering(/*@Nonnull*/ final GL gl) {

        super.endRendering(gl);

        final GL3 gl3 = gl.getGL3();

        // Unbind the VBO and VAO
        gl3.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
        gl3.glBindVertexArray(0);
    }
}
