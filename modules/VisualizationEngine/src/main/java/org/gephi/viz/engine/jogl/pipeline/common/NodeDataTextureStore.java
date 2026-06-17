package org.gephi.viz.engine.jogl.pipeline.common;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.jogl.util.gl.GLDataTexture;

/**
 * Shared holder for the node data texture (RGBA32F, one texel per node, indexed by
 * {@code node.getStoreId()}).
 * <p>
 * A single instance is shared between the node and edge pipelines: the active node pipeline fills
 * and uploads it from <b>all</b> nodes of the visible graph (not just the viewport-visible ones, so
 * that edges can resolve endpoints that are off-screen), and both node and edge shaders sample it
 * via {@code texelFetch}.
 * <p>
 * Texel layout: {@code (x, y, rawSize, colorBits)} where {@code rawSize} is the unscaled
 * {@code node.size()} and {@code colorBits} are the raw int bits of {@code node.getRGBA()}.
 *
 * @author Eduardo Ramos
 */
public class NodeDataTextureStore {

    public static final int NODE_TEXEL_FLOATS = 4;
    /**
     * Texture unit used to bind the node texture (kept distinct from the per-element texture unit).
     */
    public static final int NODE_TEXTURE_UNIT = org.gephi.viz.engine.util.gl.Constants.NODE_TEXTURE_UNIT;

    private final GLDataTexture texture = new GLDataTexture(NODE_TEXEL_FLOATS);

    private FloatBuffer cpuBuffer;
    private int texelCount = 0;
    private boolean dirty = false;

    public void init(GL gl) {
        texture.init(gl);
    }

    /**
     * Fills the CPU buffer from all nodes in the given (visible) graph, indexed by store id.
     */
    public void fillFromGraph(Graph graph) {
        final int count = Math.max(0, graph.getModel().getMaxNodeStoreId() + 1);
        final FloatBuffer buf = texture.beginFill(Math.max(count, 1));

        graph.readLock();
        try {
            for (Node node : graph.getNodes()) {
                final int base = node.getStoreId() * NODE_TEXEL_FLOATS;
                buf.put(base, node.x());
                buf.put(base + 1, node.y());
                buf.put(base + 2, node.size());
                buf.put(base + 3, Float.intBitsToFloat(node.getRGBA()));
            }
        } finally {
            graph.readUnlock();
        }

        this.cpuBuffer = buf;
        this.texelCount = count;
        this.dirty = true;
    }

    /**
     * Uploads the current CPU buffer to the GPU texture (no-op if nothing changed since last upload).
     */
    public void upload(GL gl) {
        texture.init(gl);
        if (dirty) {
            texture.upload(gl, texelCount);
            dirty = false;
        }
    }

    public void bind(GL2ES2 gl, int unit) {
        texture.bind(gl, unit);
    }

    /**
     * Raw (unscaled) size of the node with the given store id, for CPU-side LOD in the array-draw
     * fallback. Returns 0 if not available.
     */
    public float getRawSize(int storeId) {
        if (cpuBuffer == null) {
            return 0f;
        }
        final int idx = storeId * NODE_TEXEL_FLOATS + 2;
        if (idx < 0 || idx >= cpuBuffer.capacity()) {
            return 0f;
        }
        return cpuBuffer.get(idx);
    }

    public int getTexelCount() {
        return texelCount;
    }

    public void dispose(GL gl) {
        texture.dispose(gl);
        cpuBuffer = null;
        texelCount = 0;
        dirty = false;
    }
}
