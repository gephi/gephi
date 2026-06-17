package org.gephi.viz.engine.jogl.util.gl;

import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL2ES3.GL_RGBA32F;
import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;

/**
 * A 2D {@code RGBA32F} texture used to store per-element data (one RGBA texel per element) that
 * shaders read using {@code texelFetch}.
 * <p>
 * The texture has a fixed {@link #TEXTURE_WIDTH width}; its height grows on demand to hold all the
 * elements. A texel index {@code i} maps to coordinates {@code (i % width, i / width)} (see
 * {@code common.datatexture.glsl}). Requires a GL3 / GLSL 330 (or GLES3) context for float textures
 * and {@code texelFetch}.
 *
 * @author Eduardo Ramos
 */
public class GLDataTexture {

    /**
     * Fixed texture width. Indexing uses {@code (index % TEXTURE_WIDTH, index / TEXTURE_WIDTH)}, so
     * this value must match the {@code u_texWidth} uniform passed to the shaders.
     */
    public static final int TEXTURE_WIDTH = 1024;

    private final int texelFloats;

    private int id = -1;
    private int rows = 0;

    // CPU side buffer (RGBA floats per element), kept aligned to full texture rows for uploads.
    // Created lazily so the texture can be safely re-initialized after a dispose (e.g. GL context recreation).
    private ManagedDirectBuffer buffer;

    public GLDataTexture(int texelFloats) {
        this.texelFloats = texelFloats;
    }

    public int getId() {
        return id;
    }

    private void ensureBuffer() {
        if (buffer == null) {
            buffer = new ManagedDirectBuffer(GL_FLOAT, TEXTURE_WIDTH * texelFloats);
        }
    }

    public void init(GL gl) {
        ensureBuffer();
        if (id != -1) {
            return;
        }
        final int[] names = new int[1];
        gl.glGenTextures(1, names, 0);
        id = names[0];
    }

    private static int rowsFor(int texelCount) {
        if (texelCount <= 0) {
            return 1;
        }
        return (texelCount + TEXTURE_WIDTH - 1) / TEXTURE_WIDTH;
    }

    /**
     * Ensures the CPU buffer can hold {@code texelCount} elements (aligned to full rows) and returns
     * it ready to be written (position 0). Write {@code texelCount * texelFloats} floats then call
     * {@link #upload(GL, int)}.
     */
    public FloatBuffer beginFill(int texelCount) {
        ensureBuffer();
        buffer.ensureCapacity(rowsFor(texelCount) * TEXTURE_WIDTH * texelFloats);
        return buffer.floatBuffer();
    }

    /**
     * Uploads the first {@code texelCount} elements currently held in the internal CPU buffer.
     */
    public void upload(GL gl, int texelCount) {
        uploadInternal(gl.getGL2ES2(), texelCount);
    }

    /**
     * Copies {@code texelCount} elements from {@code src} (starting at float offset
     * {@code floatOffset}) into the internal CPU buffer and uploads them.
     */
    public void uploadFrom(GL gl, FloatBuffer src, int floatOffset, int texelCount) {
        final FloatBuffer dst = beginFill(texelCount);
        if (texelCount > 0) {
            dst.position(0);
            src.limit(floatOffset + texelCount * texelFloats);
            src.position(floatOffset);
            dst.put(src);
        }
        uploadInternal(gl.getGL2ES2(), texelCount);
    }

    private void uploadInternal(GL2ES2 gl, int texelCount) {
        if (id == -1) {
            init(gl);
        }
        final int needRows = rowsFor(texelCount);
        gl.glBindTexture(GL_TEXTURE_2D, id);
        if (needRows > rows) {
            allocate(gl, needRows);
        }
        if (texelCount > 0) {
            final FloatBuffer data = buffer.floatBuffer();
            data.limit(needRows * TEXTURE_WIDTH * texelFloats);
            data.position(0);
            gl.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, TEXTURE_WIDTH, needRows, GL_RGBA, GL_FLOAT, data);
        }
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void allocate(GL2ES2 gl, int needRows) {
        rows = getNextPowerOf2(needRows);
        if (rows < 1) {
            rows = 1;
        }
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, TEXTURE_WIDTH, rows, 0, GL_RGBA, GL_FLOAT, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        buffer.ensureCapacity(rows * TEXTURE_WIDTH * texelFloats);
    }

    /**
     * Binds this texture to the given texture unit (e.g. {@code 0} for {@code GL_TEXTURE0}).
     */
    public void bind(GL gl, int unit) {
        gl.glActiveTexture(GL_TEXTURE0 + unit);
        gl.glBindTexture(GL_TEXTURE_2D, id);
    }

    public void dispose(GL gl) {
        if (id != -1) {
            gl.glDeleteTextures(1, new int[] {id}, 0);
            id = -1;
        }
        rows = 0;
        if (buffer != null) {
            buffer.destroy();
            buffer = null;
        }
    }
}
