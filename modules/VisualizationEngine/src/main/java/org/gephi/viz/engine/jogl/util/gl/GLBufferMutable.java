package org.gephi.viz.engine.jogl.util.gl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3ES3;
import java.nio.Buffer;
import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;
import static org.gephi.viz.engine.util.gl.Buffers.bufferElementBytes;

/**
 *
 * @author Eduardo Ramos
 */
public class GLBufferMutable implements GLBuffer {

    public static final int GL_BUFFER_TYPE_ARRAY = GL.GL_ARRAY_BUFFER;
    public static final int GL_BUFFER_TYPE_ELEMENT_INDICES = GL.GL_ELEMENT_ARRAY_BUFFER;
    public static final int GL_BUFFER_TYPE_DRAW_INDIRECT = GL3ES3.GL_DRAW_INDIRECT_BUFFER;
    public static final int GL_BUFFER_USAGE_STATIC_DRAW = GL.GL_STATIC_DRAW;
    public static final int GL_BUFFER_USAGE_STREAM_DRAW = GL2ES2.GL_STREAM_DRAW;
    public static final int GL_BUFFER_USAGE_DYNAMIC_DRAW = GL.GL_DYNAMIC_DRAW;

    private final int id;
    private final int type;

    private int usage = -1;
    private long sizeBytes = -1;

    public GLBufferMutable(int id, int type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public void bind(GL gl) {
        gl.glBindBuffer(type, id);
    }

    @Override
    public void unbind(GL gl) {
        gl.glBindBuffer(type, 0);
    }

    @Override
    public void init(GL gl, long sizeBytes, int usage) {
        if (!isBound(gl)) {
            throw new IllegalStateException("You should bind the buffer first!");
        }

        this.usage = usage;
        this.sizeBytes = sizeBytes;

        gl.glBufferData(type, sizeBytes, null, usage);
    }

    @Override
    public void init(GL gl, Buffer buffer, int usage) {
        if (!isBound(gl)) {
            throw new IllegalStateException("You should bind the buffer first!");
        }

        this.usage = usage;
        final int elementBytes = bufferElementBytes(buffer);

        sizeBytes = buffer.capacity() * elementBytes;

        gl.glBufferData(type, sizeBytes, buffer, usage);
    }

    @Override
    public void update(GL gl, Buffer buffer) {
        update(gl, buffer, buffer.remaining() * bufferElementBytes(buffer));
    }

    @Override
    public void update(GL gl, Buffer buffer, long sizeBytes) {
        update(gl, buffer, 0, sizeBytes);
    }

    @Override
    public void update(GL gl, Buffer buffer, long offsetBytes, long sizeBytes) {
        if (!isInitialized()) {
            throw new IllegalStateException("You should initialize the buffer first!");
        }
        if (!isBound(gl)) {
            throw new IllegalStateException("You should bind the buffer first!");
        }

        final long neededBytesCapacity = offsetBytes + sizeBytes;
        ensureCapacity(gl, neededBytesCapacity);

        gl.glBufferSubData(type, offsetBytes, sizeBytes, buffer);
    }

    @Override
    public void updateWithOrphaning(GL gl, Buffer buffer) {
        if (!isBound(gl)) {
            throw new IllegalStateException("You should bind the buffer first!");
        }

        gl.glBufferData(type, sizeBytes, null, usage);
        update(gl, buffer);
    }

    @Override
    public void updateWithOrphaning(GL gl, Buffer buffer, long sizeBytes) {
        if (!isBound(gl)) {
            throw new IllegalStateException("You should bind the buffer first!");
        }

        gl.glBufferData(type, sizeBytes, null, usage);
        update(gl, buffer, sizeBytes);
    }

    @Override
    public void destroy(GL gl) {
        if (!isInitialized()) {
            throw new IllegalStateException("You should initialize the buffer first!");
        }

        gl.glDeleteBuffers(1, new int[]{id}, 0);
        sizeBytes = -1;
    }

    @Override
    public long size() {
        return sizeBytes;
    }

    public void ensureCapacity(GL gl, long neededBytes) {
        if (sizeBytes < neededBytes) {
            long newSizeBytes = getNextPowerOf2(neededBytes);

            System.out.println("Growing GL buffer from " + sizeBytes + " to " + newSizeBytes + " bytes");
            init(gl, newSizeBytes, usage);
        }
    }

    @Override
    public boolean isInitialized() {
        return sizeBytes != -1;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean isBound(GL gl) {
        return gl.getBoundBuffer(type) == id;
    }

    @Override
    public int getUsageFlags() {
        return usage;
    }

    @Override
    public long getSizeBytes() {
        return sizeBytes;
    }

    @Override
    public boolean isMutable() {
        return true;
    }
}
