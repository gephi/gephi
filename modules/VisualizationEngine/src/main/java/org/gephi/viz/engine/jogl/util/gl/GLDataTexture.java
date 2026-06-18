package org.gephi.viz.engine.jogl.util.gl;

import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.util.gl.DataUploadStats;

/**
 * A 2D float texture used to store per-element data (one texel per element) that shaders read using
 * {@code texelFetch}. The component count is chosen from {@code texelFloats}: 1 -&gt; {@code R32F},
 * 2 -&gt; {@code RG32F}, 3 -&gt; {@code RGB32F}, 4 -&gt; {@code RGBA32F}. Using only as many channels as
 * needed (e.g. {@code RG32F} for an x/y position stream) halves the upload bandwidth versus a full
 * {@code RGBA32F} texel.
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

    // OpenGL pixel/internal float-format enums by component count. Declared by value to avoid
    // depending on the exact JOGL profile interface that exposes each one (GL2ES2/GL2ES3/GL2GL3).
    private static final int GL_RED_FORMAT = 0x1903;
    private static final int GL_RG_FORMAT = 0x8227;
    private static final int GL_RGB_FORMAT = 0x1907;
    private static final int GL_RGBA_FORMAT = 0x1908;
    private static final int GL_R32F_INTERNAL = 0x822E;
    private static final int GL_RG32F_INTERNAL = 0x8230;
    private static final int GL_RGB32F_INTERNAL = 0x8815;
    private static final int GL_RGBA32F_INTERNAL = 0x8814;

    // PBO streaming enums (declared by value for the same profile-independence reason as above).
    private static final int GL_PIXEL_UNPACK_BUFFER = 0x88EC;
    private static final int GL_STREAM_DRAW = 0x88E0;
    private static final int GL_MAP_WRITE_BIT = 0x0002;
    private static final int GL_MAP_INVALIDATE_BUFFER_BIT = 0x0008;
    private static final int PBO_RING = 2;

    // Upload texture data through a ping-pong PBO (orphaned + mapped) so the CPU->GPU copy overlaps
    // with the GPU consuming the previous frame, avoiding sync stalls while a layout streams
    // positions. On by default; disable with -Dviz.engine.texturePbo=false to force the direct
    // glTexSubImage2D path. Either way, a driver that refuses the mapping falls back to the direct
    // path automatically.
    private static final boolean USE_PBO =
        !"false".equalsIgnoreCase(System.getProperty("viz.engine.texturePbo", "true"));

    private final int texelFloats;
    private final int pixelFormat;
    private final int internalFormat;

    private int id = -1;
    private int rows = 0;

    // CPU side buffer (RGBA floats per element), kept aligned to full texture rows for uploads.
    // Created lazily so the texture can be safely re-initialized after a dispose (e.g. GL context recreation).
    private ManagedDirectBuffer buffer;

    // Ping-pong PBOs used when USE_PBO is enabled; created lazily on first upload.
    private int[] pbos;
    private int pboIndex;

    // Content hash + texel count of the data currently on the GPU, so the gated upload() can skip the
    // transfer when nothing changed (e.g. edge texels are constant while only node positions move
    // during a layout).
    private long uploadedHash;
    private int uploadedTexelCount = -1;
    private boolean uploadedOnce = false;

    public GLDataTexture(int texelFloats) {
        this.texelFloats = texelFloats;
        switch (texelFloats) {
            case 1:
                this.pixelFormat = GL_RED_FORMAT;
                this.internalFormat = GL_R32F_INTERNAL;
                break;
            case 2:
                this.pixelFormat = GL_RG_FORMAT;
                this.internalFormat = GL_RG32F_INTERNAL;
                break;
            case 3:
                this.pixelFormat = GL_RGB_FORMAT;
                this.internalFormat = GL_RGB32F_INTERNAL;
                break;
            default:
                this.pixelFormat = GL_RGBA_FORMAT;
                this.internalFormat = GL_RGBA32F_INTERNAL;
                break;
        }
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
     * Uploads the first {@code texelCount} elements currently held in the internal CPU buffer,
     * skipping the transfer when the content is identical to what is already on the GPU.
     */
    public void upload(GL gl, int texelCount) {
        final long h = contentHash(texelCount);
        if (uploadedOnce && uploadedTexelCount == texelCount && uploadedHash == h) {
            DataUploadStats.recordTextureUploadSkipped();
            return;
        }
        uploadInternal(gl.getGL2ES2(), texelCount, 0, rowsFor(texelCount));
        uploadedHash = h;
        uploadedTexelCount = texelCount;
        uploadedOnce = true;
    }

    /**
     * Uploads the first {@code texelCount} elements unconditionally. For callers that already gate
     * uploads with their own change detection (e.g. {@code NodeDataTextureStore} hashes during its
     * fill pass), avoiding a redundant second content scan here.
     */
    public void uploadAlways(GL gl, int texelCount) {
        uploadAlways(gl, texelCount, 0, rowsFor(texelCount));
    }

    /**
     * Uploads only texture rows {@code [firstRow, firstRow + rowCount)} of the first
     * {@code texelCount} elements (a "dirty row" sub-range), unconditionally. Callers that track
     * which rows changed (e.g. {@code NodeDataTextureStore} per-row position hashes) can stream just
     * the touched rows instead of the whole texture. A (re)allocation forces a full upload.
     */
    public void uploadAlways(GL gl, int texelCount, int firstRow, int rowCount) {
        uploadInternal(gl.getGL2ES2(), texelCount, firstRow, rowCount);
        // The caller owns change detection; invalidate the content-hash gate so a later gated
        // upload() (a different caller) re-evaluates instead of trusting a now-stale hash.
        uploadedOnce = false;
        uploadedTexelCount = texelCount;
    }

    // FNV-1a over the meaningful texels; cheap relative to the avoided GPU transfer.
    private long contentHash(int texelCount) {
        if (texelCount <= 0 || buffer == null) {
            return 0L;
        }
        final FloatBuffer fb = buffer.floatBuffer();
        final int n = texelCount * texelFloats;
        long h = 1469598103934665603L;
        for (int i = 0; i < n; i++) {
            h = (h ^ Float.floatToRawIntBits(fb.get(i))) * 1099511628211L;
        }
        return h;
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
        uploadInternal(gl.getGL2ES2(), texelCount, 0, rowsFor(texelCount));
    }

    private void uploadInternal(GL2ES2 gl, int texelCount, int firstRow, int rowCount) {
        if (id == -1) {
            init(gl);
        }
        final int needRows = rowsFor(texelCount);
        gl.glBindTexture(GL_TEXTURE_2D, id);
        if (needRows > rows) {
            allocate(gl, needRows);
            // Fresh storage is fully undefined: ignore the requested sub-range and upload everything.
            firstRow = 0;
            rowCount = needRows;
        }
        if (texelCount <= 0) {
            gl.glBindTexture(GL_TEXTURE_2D, 0);
            return;
        }
        // Clamp the requested row range to the rows that actually exist.
        if (firstRow < 0) {
            firstRow = 0;
        }
        if (firstRow + rowCount > needRows) {
            rowCount = needRows - firstRow;
        }
        if (rowCount > 0) {
            final FloatBuffer data = buffer.floatBuffer();
            final int rowFloats = TEXTURE_WIDTH * texelFloats;
            final int startFloat = firstRow * rowFloats;
            final int floats = rowCount * rowFloats;
            boolean uploaded = false;
            if (USE_PBO) {
                uploaded = pboUpload(gl, firstRow, rowCount, data, startFloat, floats);
            }
            if (!uploaded) {
                data.limit(startFloat + floats);
                data.position(startFloat);
                gl.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, firstRow, TEXTURE_WIDTH, rowCount,
                    pixelFormat, GL_FLOAT, data);
            }
            DataUploadStats.recordTextureUpload((long) floats * Float.BYTES);
        }
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Streams the row sub-range through an orphaned, mapped ping-pong PBO. Returns false (so the
    // caller falls back to a direct glTexSubImage2D) if the context is not GL2ES3 or the driver
    // refuses the buffer mapping.
    private boolean pboUpload(GL2ES2 gl, int firstRow, int rowCount, FloatBuffer data,
                             int startFloat, int floats) {
        final GL2ES3 g3 = gl.isGL2ES3() ? gl.getGL2ES3() : null;
        if (g3 == null) {
            return false;
        }
        if (pbos == null) {
            pbos = new int[PBO_RING];
            g3.glGenBuffers(PBO_RING, pbos, 0);
        }
        pboIndex = (pboIndex + 1) % PBO_RING;
        final long bytes = (long) floats * Float.BYTES;
        g3.glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pbos[pboIndex]);
        g3.glBufferData(GL_PIXEL_UNPACK_BUFFER, bytes, null, GL_STREAM_DRAW);
        final ByteBuffer mapped = g3.glMapBufferRange(GL_PIXEL_UNPACK_BUFFER, 0, bytes,
            GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        if (mapped == null) {
            g3.glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
            return false;
        }
        final FloatBuffer mappedFloats = mapped.order(ByteOrder.nativeOrder()).asFloatBuffer();
        data.limit(startFloat + floats);
        data.position(startFloat);
        mappedFloats.put(data);
        g3.glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
        // Source is the bound PBO: the last arg is a byte offset into it (long overload).
        g3.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, firstRow, TEXTURE_WIDTH, rowCount,
            pixelFormat, GL_FLOAT, 0L);
        g3.glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        return true;
    }

    private void allocate(GL2ES2 gl, int needRows) {
        rows = getNextPowerOf2(needRows);
        if (rows < 1) {
            rows = 1;
        }
        gl.glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, TEXTURE_WIDTH, rows, 0, pixelFormat, GL_FLOAT, null);
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
        if (pbos != null) {
            gl.glDeleteBuffers(pbos.length, pbos, 0);
            pbos = null;
            pboIndex = 0;
        }
        rows = 0;
        uploadedOnce = false;
        uploadedTexelCount = -1;
        uploadedHash = 0L;
        if (buffer != null) {
            buffer.destroy();
            buffer = null;
        }
    }
}
