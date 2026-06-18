package org.gephi.viz.engine.jogl.pipeline.common;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import java.util.Arrays;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.jogl.util.gl.GLDataTexture;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.DataUploadStats;

/**
 * Shared holder for the node data textures (indexed by {@code node.getStoreId()}).
 * <p>
 * A single instance is shared between the node and edge pipelines: the active node pipeline fills
 * and uploads it from <b>all</b> nodes of the visible graph (not just the viewport-visible ones, so
 * that edges can resolve endpoints that are off-screen), and both node and edge shaders sample it
 * via {@code texelFetch}.
 * <p>
 * The per-node texel is split across two {@code RG32F} textures so a running layout only re-uploads
 * what actually changes:
 * <ul>
 *   <li><b>position</b> texture: {@code (x, y)} — streams during layout, and only the touched rows
 *       are uploaded (a per-row position hash yields the contiguous dirty span);</li>
 *   <li><b>style</b> texture: {@code (rawSize, colorBits)} where {@code rawSize} is the unscaled
 *       {@code node.size()} and {@code colorBits} are the raw int bits of {@code node.getRGBA()} —
 *       re-uploaded only when its content hash changes (size/color edits, add/remove).</li>
 * </ul>
 * Each texture is content-hashed during the single fill pass so unchanged data skips the GPU
 * transfer entirely. See {@link #fillFromGraph(Graph)} / {@link #upload(GL)}.
 *
 * @author Eduardo Ramos
 */
public class NodeDataTextureStore {

    public static final int NODE_POS_FLOATS = 2;
    public static final int NODE_STYLE_FLOATS = 2;
    /** Texture unit used to bind the node position texture. */
    public static final int NODE_POS_TEXTURE_UNIT = Constants.NODE_POS_TEXTURE_UNIT;
    /** Texture unit used to bind the node style texture. */
    public static final int NODE_STYLE_TEXTURE_UNIT = Constants.NODE_STYLE_TEXTURE_UNIT;

    private static final int ROW_WIDTH = GLDataTexture.TEXTURE_WIDTH;

    private final GLDataTexture posTexture = new GLDataTexture(NODE_POS_FLOATS);
    private final GLDataTexture styleTexture = new GLDataTexture(NODE_STYLE_FLOATS);

    // Kept to answer getRawSize() on the CPU side (array-draw LOD) without re-reading the graph.
    private FloatBuffer styleBuffer;
    private int texelCount = 0;

    // Position: per-row XOR hashes for dirty-row sub-range uploads, plus what is currently on the GPU
    // and the contiguous [firstRow, firstRow+rowCount) span that changed since the last upload.
    private long[] rowPosHash = new long[0];
    private long[] uploadedRowPosHash;
    private boolean posDirty = false;
    private int posDirtyFirstRow = 0;
    private int posDirtyRowCount = 0;

    // Style: a single whole-texture content hash (it changes far less often than positions).
    private boolean styleDirty = false;
    private long pendingStyleHash;
    private long uploadedStyleHash;

    private int uploadedTexelCount = -1;

    public void init(GL gl) {
        posTexture.init(gl);
        styleTexture.init(gl);
    }

    /**
     * Fills the CPU buffers from all nodes in the given (visible) graph, indexed by store id, and
     * computes the per-row position hashes / style hash so {@link #upload(GL)} can skip unchanged
     * uploads and stream only the touched position rows.
     */
    public void fillFromGraph(Graph graph) {
        final int count = Math.max(0, graph.getModel().getMaxNodeStoreId() + 1);
        final FloatBuffer posBuf = posTexture.beginFill(Math.max(count, 1));
        final FloatBuffer styleBuf = styleTexture.beginFill(Math.max(count, 1));

        final int rows = count <= 0 ? 0 : (count + ROW_WIDTH - 1) / ROW_WIDTH;
        if (rowPosHash.length != rows) {
            rowPosHash = new long[rows];
        } else {
            Arrays.fill(rowPosHash, 0L);
        }

        long styleHash = 0L;
        graph.readLock();
        try {
            for (Node node : graph.getNodes()) {
                final int storeId = node.getStoreId();
                final int pbase = storeId * NODE_POS_FLOATS;
                final int sbase = storeId * NODE_STYLE_FLOATS;
                final float x = node.x();
                final float y = node.y();
                final float size = node.size();
                final int rgba = node.getRGBA();
                posBuf.put(pbase, x);
                posBuf.put(pbase + 1, y);
                styleBuf.put(sbase, size);
                styleBuf.put(sbase + 1, Float.intBitsToFloat(rgba));

                // Order-independent accumulation (XOR of a strong per-node mix) so the iteration
                // order does not affect the hash; any moved node flips its row's position hash, any
                // recolor/resize flips the style hash.
                long kp = storeId;
                kp = kp * 1099511628211L ^ Float.floatToRawIntBits(x);
                kp = kp * 1099511628211L ^ Float.floatToRawIntBits(y);
                rowPosHash[storeId / ROW_WIDTH] ^= fmix64(kp);

                long ks = storeId;
                ks = ks * 1099511628211L ^ Float.floatToRawIntBits(size);
                ks = ks * 1099511628211L ^ rgba;
                styleHash ^= fmix64(ks);
            }
        } finally {
            graph.readUnlock();
        }

        this.styleBuffer = styleBuf;
        this.texelCount = count;
        this.pendingStyleHash = styleHash;
        this.styleDirty = uploadedTexelCount != count || uploadedStyleHash != styleHash;
        computePosDirtySpan(count, rows);
    }

    // Derives the contiguous dirty row span for the position texture by diffing the freshly computed
    // per-row hashes against what is on the GPU. A texel-count change or a missing/resized baseline
    // forces a full upload.
    private void computePosDirtySpan(int count, int rows) {
        if (uploadedRowPosHash == null || uploadedRowPosHash.length != rows || uploadedTexelCount != count) {
            posDirty = rows > 0;
            posDirtyFirstRow = 0;
            posDirtyRowCount = rows;
            return;
        }
        int min = -1;
        int max = -1;
        for (int r = 0; r < rows; r++) {
            if (rowPosHash[r] != uploadedRowPosHash[r]) {
                if (min < 0) {
                    min = r;
                }
                max = r;
            }
        }
        if (min < 0) {
            posDirty = false;
            posDirtyFirstRow = 0;
            posDirtyRowCount = 0;
        } else {
            posDirty = true;
            posDirtyFirstRow = min;
            posDirtyRowCount = max - min + 1;
        }
    }

    /**
     * Uploads the current CPU buffers to the GPU textures, streaming only the changed position rows
     * and skipping the style transfer when its content is identical to what is already on the GPU.
     */
    public void upload(GL gl) {
        posTexture.init(gl);
        styleTexture.init(gl);
        if (posDirty) {
            posTexture.uploadAlways(gl, texelCount, posDirtyFirstRow, posDirtyRowCount);
            if (uploadedRowPosHash == null || uploadedRowPosHash.length != rowPosHash.length) {
                uploadedRowPosHash = rowPosHash.clone();
            } else {
                System.arraycopy(rowPosHash, 0, uploadedRowPosHash, 0, rowPosHash.length);
            }
            posDirty = false;
        } else {
            DataUploadStats.recordTextureUploadSkipped();
        }
        if (styleDirty) {
            styleTexture.uploadAlways(gl, texelCount);
            uploadedStyleHash = pendingStyleHash;
            styleDirty = false;
        } else {
            DataUploadStats.recordTextureUploadSkipped();
        }
        uploadedTexelCount = texelCount;
    }

    // MurmurHash3 64-bit finalizer: cheap, strong avalanche so small position deltas change the hash.
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    /**
     * Binds both node textures to their fixed texture units (position and style), matching the
     * sampler uniforms set by the node/edge programs after link.
     */
    public void bind(GL2ES2 gl) {
        posTexture.bind(gl, NODE_POS_TEXTURE_UNIT);
        styleTexture.bind(gl, NODE_STYLE_TEXTURE_UNIT);
    }

    /**
     * Raw (unscaled) size of the node with the given store id, for CPU-side LOD in the array-draw
     * fallback. Returns 0 if not available.
     */
    public float getRawSize(int storeId) {
        if (styleBuffer == null) {
            return 0f;
        }
        final int idx = storeId * NODE_STYLE_FLOATS;
        if (idx < 0 || idx >= styleBuffer.capacity()) {
            return 0f;
        }
        return styleBuffer.get(idx);
    }

    public int getTexelCount() {
        return texelCount;
    }

    public void dispose(GL gl) {
        posTexture.dispose(gl);
        styleTexture.dispose(gl);
        styleBuffer = null;
        texelCount = 0;
        rowPosHash = new long[0];
        uploadedRowPosHash = null;
        posDirty = false;
        posDirtyFirstRow = 0;
        posDirtyRowCount = 0;
        styleDirty = false;
        uploadedTexelCount = -1;
        uploadedStyleHash = 0L;
        pendingStyleHash = 0L;
    }
}
