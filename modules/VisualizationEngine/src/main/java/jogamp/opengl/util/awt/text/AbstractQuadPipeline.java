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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * Skeletal implementation of {@link QuadPipeline}.
 */
abstract class AbstractQuadPipeline implements QuadPipeline {

    /**
     * Number of bytes in one float.
     */
    /*@Nonnegative*/
    static final int SIZEOF_FLOAT = 4;

    /**
     * Number of bytes in one int.
     */
    /*@Nonnegative*/
    static final int SIZEOF_INT = 4;

    /**
     * Maximum number of quads in the buffer.
     */
    /*@Nonnegative*/
    static final int QUADS_PER_BUFFER = 100;

    /**
     * Number of components in a point attribute.
     */
    /*@Nonnegative*/
    static final int FLOATS_PER_POINT = 3;

    /**
     * Number of components in a texture coordinate attribute
     */
    /*@Nonnegative*/
    static final int FLOATS_PER_COORD = 2;

    /**
     * Total components in vertex.
     */
    /*@Nonnegative*/
    static final int FLOATS_PER_VERT = FLOATS_PER_POINT + FLOATS_PER_COORD;

    /**
     * Size of a point attribute in bytes.
     */
    /*@Nonnegative*/
    static final int BYTES_PER_POINT = FLOATS_PER_POINT * SIZEOF_FLOAT;

    /**
     * Size of a texture coordinate attribute in bytes.
     */
    /*@Nonnegative*/
    static final int BYTES_PER_COORD = FLOATS_PER_COORD * SIZEOF_FLOAT;

    /**
     * Total size of a vertex in bytes.
     */
    /*@Nonnegative*/
    static final int BYTES_PER_VERT = BYTES_PER_POINT + BYTES_PER_COORD;

    /**
     * Number of bytes before first point attribute in buffer.
     */
    /*@Nonnegative*/
    static final int POINT_OFFSET = 0;

    /**
     * Number of bytes before first texture coordinate in buffer.
     */
    /*@Nonnegative*/
    static final int COORD_OFFSET = BYTES_PER_POINT;

    /**
     * Number of bytes between successive values for the same attribute.
     */
    /*@Nonnegative*/
    static final int STRIDE = BYTES_PER_POINT + BYTES_PER_COORD;

    /**
     * Maximum buffer size in floats.
     */
    /*@Nonnegative*/
    final int FLOATS_PER_BUFFER;

    /**
     * Maximum buffer size in bytes.
     */
    /*@Nonnegative*/
    final int BYTES_PER_BUFFER;

    /**
     * Number of vertices per primitive.
     */
    /*@Nonnegative*/
    final int VERTS_PER_PRIM;

    /**
     * Maximum buffer size in primitives.
     */
    /*@Nonnegative*/
    final int PRIMS_PER_BUFFER;

    /**
     * Maximum buffer size in vertices.
     */
    /*@Nonnegative*/
    final int VERTS_PER_BUFFER;

    /**
     * Size of a quad in vertices.
     */
    /*@Nonnegative*/
    final int VERTS_PER_QUAD;

    /**
     * Size of a quad in bytes.
     */
    /*@Nonnegative*/
    final int BYTES_PER_QUAD;

    /**
     * Size of a quad in primitives.
     */
    /*@Nonnegative*/
    final int PRIMS_PER_QUAD;

    /**
     * Observers of events.
     */
    /*@Nonnull*/
    private final List<EventListener> listeners = new ArrayList<EventListener>();

    /**
     * Buffer of vertices.
     */
    /*@Nonnull*/
    private final FloatBuffer data;

    /**
     * Number of outstanding quads in the buffer.
     */
    /*@Nonnegative*/
    private int size = 0;

    /**
     * Constructs an abstract quad pipeline.
     *
     * @param vertsPerPrim Number of vertices per primitive
     * @param primsPerQuad Number of primitives per quad
     * @throws IllegalArgumentException if vertices or primitives is less than one
     */
    AbstractQuadPipeline(/*@Nonnegative*/ final int vertsPerPrim,
                         /*@Nonnegative*/ final int primsPerQuad) {

        Check.argument(vertsPerPrim > 0, "Number of vertices is less than one");
        Check.argument(primsPerQuad > 0, "Number of primitives is less than one");

        VERTS_PER_PRIM = vertsPerPrim;
        PRIMS_PER_QUAD = primsPerQuad;
        PRIMS_PER_BUFFER = primsPerQuad * QUADS_PER_BUFFER;
        VERTS_PER_QUAD = vertsPerPrim * primsPerQuad;
        VERTS_PER_BUFFER = PRIMS_PER_BUFFER * VERTS_PER_PRIM;
        FLOATS_PER_BUFFER = FLOATS_PER_VERT * VERTS_PER_BUFFER;
        BYTES_PER_BUFFER = BYTES_PER_VERT * VERTS_PER_BUFFER;
        BYTES_PER_QUAD = BYTES_PER_VERT * VERTS_PER_QUAD;

        this.data = Buffers.newDirectFloatBuffer(FLOATS_PER_BUFFER);
    }

    /**
     * Adds a texture coordinate to the pipeline.
     *
     * @param s Texture coordinate for X axis
     * @param t Texture coordinate for Y axis
     */
    protected final void addCoord(final float s, final float t) {
        data.put(s).put(t);
    }

    /**
     * Adds a point to the pipeline.
     *
     * @param x Position on X axis
     * @param y Position on Y axis
     * @param z Position on Z axis
     */
    protected final void addPoint(final float x, final float y, final float z) {
        data.put(x).put(y).put(z);
    }

    @Override
    public final void addListener(/*@Nonnull*/ final EventListener listener) {

        Check.notNull(listener, "Listener cannot be null");

        listeners.add(listener);
    }

    @Override
    public final void addQuad(/*@Nonnull*/ final GL gl, /*@Nonnull*/ final Quad quad) {

        Check.notNull(gl, "Context cannot be null");
        Check.notNull(quad, "Quad cannot be null");

        doAddQuad(quad);
        if (++size >= QUADS_PER_BUFFER) {
            fireEvent(EventType.AUTOMATIC_FLUSH);
            flush(gl);
        }
    }

    @Override
    public void beginRendering(/*@Nonnull*/ final GL gl) {
        Check.notNull(gl, "GL cannot be null");
    }

    /**
     * Rewinds the buffer and resets the number of outstanding quads.
     */
    protected final void clear() {
        data.rewind();
        size = 0;
    }

    /**
     * Creates a vertex buffer object for use with a pipeline.
     *
     * @param gl Current OpenGL context
     * @param size Size in bytes of buffer
     * @return OpenGL handle to vertex buffer object
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if size is negative
     */
    /*@Nonnegative*/
    protected static int createVertexBufferObject(/*@Nonnull*/ final GL2GL3 gl,
                                                  /*@Nonnegative*/ final int size) {

        Check.notNull(gl, "GL cannot be null");
        Check.argument(size >= 0, "Size cannot be negative");

        // Generate
        final int[] handles = new int[1];
        gl.glGenBuffers(1, handles, 0);
        final int vbo = handles[0];

        // Allocate
        gl.glBindBuffer(GL2GL3.GL_ARRAY_BUFFER, vbo);
        gl.glBufferData(
                GL2GL3.GL_ARRAY_BUFFER, // target
                size,                   // size
                null,                   // data
                GL2GL3.GL_STREAM_DRAW); // usage
        gl.glBindBuffer(GL2GL3.GL_ARRAY_BUFFER, 0);

        return vbo;
    }

    @Override
    public void dispose(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        listeners.clear();
    }

    /**
     * Actually adds vertices from a quad to the buffer.
     *
     * @param quad Quad to add
     * @throws NullPointerException if quad is null
     */
    protected void doAddQuad(/*@Nonnull*/ final Quad quad) {

        Check.notNull(quad, "Quad cannot be null");

        addPoint(quad.xr, quad.yt, quad.z);
        addCoord(quad.sr, quad.tt);
        addPoint(quad.xl, quad.yt, quad.z);
        addCoord(quad.sl, quad.tt);
        addPoint(quad.xl, quad.yb, quad.z);
        addCoord(quad.sl, quad.tb);
        addPoint(quad.xr, quad.yb, quad.z);
        addCoord(quad.sr, quad.tb);
    }

    /**
     * Actually draws everything in the pipeline.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    protected abstract void doFlush(/*@Nonnull*/ final GL gl);

    @Override
    public void endRendering(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        flush(gl);
    }

    /**
     * Fires an event to all observers.
     *
     * @param type Type of event to send to observers
     * @throws NullPointerException if type is null
     */
    protected final void fireEvent(/*@Nonnull*/ final EventType type) {

        Check.notNull(type, "Type cannot be null");

        for (final EventListener listener : listeners) {
            assert listener != null : "addListener rejects null";
            listener.onQuadPipelineEvent(type);
        }
    }

    @Override
    public final void flush(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        if (size > 0) {
            doFlush(gl);
        }
    }

    /**
     * Returns NIO buffer backing the pipeline.
     */
    /*@Nonnull*/
    protected final FloatBuffer getData() {
        return data;
    }

    /**
     * Returns next float in the pipeline.
     */
    /*@CheckForSigned*/
    protected final float getFloat() {
        return data.get();
    }

    /*@Nonnegative*/
    @Override
    public final int getSize() {
        return size;
    }

    @Override
    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns size of vertices in the pipeline in bytes.
     */
    /*@Nonnegative*/
    public final int getSizeInBytes() {
        return size * BYTES_PER_QUAD;
    }

    /**
     * Returns number of primitives in the pipeline.
     */
    /*@Nonnegative*/
    public final int getSizeInPrimitives() {
        return size * PRIMS_PER_QUAD;
    }

    /**
     * Returns number of vertices in the pipeline.
     */
    /*@Nonnegative*/
    public final int getSizeInVertices() {
        return size * VERTS_PER_QUAD;
    }

    /**
     * Changes the buffer's position.
     *
     * @param position Location in buffer to move to
     * @throws IllegalArgumentException if position is out-of-range
     */
    protected final void position(/*@Nonnegative*/ final int position) {
        data.position(position);
    }

    @Override
    public final void removeListener(/*@CheckForNull*/ final EventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Rewinds the data buffer.
     */
    protected final void rewind() {
        data.rewind();
    }
}
