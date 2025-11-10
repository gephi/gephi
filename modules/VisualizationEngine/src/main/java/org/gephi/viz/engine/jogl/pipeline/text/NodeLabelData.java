package org.gephi.viz.engine.jogl.pipeline.text;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import jogamp.text.TextRenderer;
import jogamp.text.util.Glyph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelData {

    private static final boolean SMOOTHING = false;
    private static final boolean ANTIALIASED = true;
    private static final boolean FRACTIONAL_METRICS = true;
    private static final boolean MIPMAP = true;

    private final NodesCallback nodesCallback;

    // Array of label batches indexed by node storeId
    private volatile LabelBatch[] labelBatches = new LabelBatch[0];

    // Maximum valid index in the batches array (updated by updater thread)
    private volatile int maxValidIndex = -1;

    // TextRenderer for glyph preparation (doesn't need GL context)
    // Volatile to ensure visibility across threads
    private volatile TextRenderer textRenderer;
    private volatile Font currentFont;

    public NodeLabelData(NodesCallback nodesCallback) {
        this.nodesCallback = nodesCallback;
    }

    public NodesCallback getNodesCallback() {
        return nodesCallback;
    }

    public void dispose() {
        textRenderer = null;
        currentFont = null;
        labelBatches = new LabelBatch[0];
        maxValidIndex = -1;
    }

    /**
     * Ensures the text renderer is initialized with the correct font.
     * This is called from the updater thread and doesn't require GL context.
     */
    public void ensureTextRenderer(Font font, boolean vaoSupported, boolean mipMapSupported) {
        if (textRenderer == null || !font.equals(currentFont)) {
            textRenderer = new TextRenderer(font, ANTIALIASED, FRACTIONAL_METRICS, null, mipMapSupported && MIPMAP);
            textRenderer.setUseVertexArrays(vaoSupported);
            textRenderer.setSmoothing(SMOOTHING);

            currentFont = font;

            // Font changed - invalidate all cached glyphs
            invalidateAllGlyphs();
        }
    }

    /**
     * Invalidates all cached glyphs when font changes.
     */
    private void invalidateAllGlyphs() {
        for (LabelBatch batch : labelBatches) {
            if (batch != null) {
                batch.invalidateGlyphs();
            }
        }
    }

    /**
     * Gets the bounds of text using the text renderer.
     */
    public Rectangle2D getTextBounds(String text) {
        if (textRenderer == null || text == null || text.isEmpty()) {
            return null;
        }
        return textRenderer.getBounds(text);
    }

    /**
     * Ensures the label batches array is large enough to hold the given storeId.
     */
    public void ensureLabelBatchesSize(int maxStoreId) {
        if (maxStoreId >= labelBatches.length) {
            int newSize = getNextPowerOf2(maxStoreId + 1);
            LabelBatch[] newArray = new LabelBatch[newSize];
            System.arraycopy(labelBatches, 0, newArray, 0, labelBatches.length);
            labelBatches = newArray;
        }
    }

    /**
     * Updates the label data for a specific node (by storeId).
     * Only recomputes glyphs if text changed, only recomputes bounds if text or sizeFactor changed.
     * Called by updater thread - writes to write buffer of the batch.
     *
     * @param storeId    The node's storeId
     * @param text       The label text
     * @param sizeFactor The size factor (for caching bounds)
     * @param nodeX      Node position X (will be centered)
     * @param nodeY      Node position Y (will be centered)
     * @param r          Red component
     * @param g          Green component
     * @param b          Blue component
     * @param a          Alpha component
     * @return The updated LabelBatch (for overlap detection)
     */
    public LabelBatch updateBatch(Node node, int storeId, String text, float sizeFactor, float nodeX, float nodeY,
                                  float r, float g, float b, float a) {

        // Get or create batch for this storeId
        LabelBatch batch = labelBatches[storeId];
        if (batch == null) {
            batch = new LabelBatch();
            labelBatches[storeId] = batch;
        }

        // Check if we need to recompute glyphs (expensive)
        boolean textChanged = !text.equals(batch.writeText);

        if (textChanged) {
            // Text changed - must recreate glyphs
            final List<Glyph> glyphs = textRenderer.getGlyphProducer().createGlyphs(text);
            if (glyphs == null || glyphs.isEmpty()) {
                batch.markInvalid();
                return batch;
            }

            // Store new glyphs
            if (batch.writeGlyphs == null) {
                batch.writeGlyphs = new ArrayList<>(glyphs);
            } else {
                batch.writeGlyphs.clear();
                batch.writeGlyphs.addAll(glyphs);
            }
            batch.writeText = text;
        }

        // Check if we need to recompute bounds (expensive)
        boolean sizeFactorChanged = Math.abs(sizeFactor - batch.writeScale) > 0.0001f;

        float width, height, ascent;
        if (textChanged || sizeFactorChanged) {
            // Recompute bounds
            final Rectangle2D bounds = getTextBounds(text);
            if (bounds == null) {
                batch.markInvalid();
                return batch;
            }

            width = (float) bounds.getWidth() * sizeFactor;
            height = (float) bounds.getHeight() * sizeFactor;
            ascent = (float) (-bounds.getY());

            node.getTextProperties().setDimensions(width, height);
        } else {
            // Use cached bounds
            width = node.getTextProperties().getWidth();
            height = node.getTextProperties().getHeight();
            ascent = batch.writeAscent;
        }

        // Compute centered draw position using cached bounds
        final float descentPx = (height / sizeFactor) - ascent;
        final float drawX = nodeX - width * 0.5f;
        final float drawY = nodeY - ((ascent - descentPx) * sizeFactor) * 0.5f;

        // Always update position, scale, and color (cheap)
        batch.writeAscent = ascent;
        batch.writeX = drawX;
        batch.writeY = drawY;
        batch.writeScale = sizeFactor;
        batch.writeR = r;
        batch.writeG = g;
        batch.writeB = b;
        batch.writeA = a;
        batch.writeValid = true;

        return batch;
    }

    /**
     * Gets a label batch by storeId (for overlap detection).
     * Returns null if no batch exists at that index.
     */
    public LabelBatch getBatch(int storeId) {
        if (storeId >= 0 && storeId < labelBatches.length) {
            return labelBatches[storeId];
        }
        return null;
    }

    /**
     * Marks a batch as invalid (e.g., node has no label).
     * Called by updater thread.
     */
    public void invalidateBatch(int storeId) {
        if (storeId < labelBatches.length && labelBatches[storeId] != null) {
            labelBatches[storeId].markInvalid();
        }
    }

    /**
     * Sets the maximum valid index for this update cycle.
     * Called by updater thread.
     */
    public void setMaxValidIndex(int maxIndex) {
        this.maxValidIndex = maxIndex;
    }

    /**
     * Gets the maximum valid index.
     * Called by renderer thread via worldUpdated().
     */
    public int getMaxValidIndex() {
        return maxValidIndex;
    }


    /**
     * Swaps the read/write buffers for all batches.
     * Called by renderer thread in worldUpdated() after updater completes.
     * This makes the newly prepared data visible to the renderer.
     */
    public void swapBuffers() {
        final LabelBatch[] batches = labelBatches; // Read volatile once
        for (LabelBatch batch : batches) {
            if (batch != null) {
                batch.swap();
            }
        }
    }

    /**
     * Gets the label batch array for rendering.
     * Called by renderer thread - reads from read buffer of each batch.
     */
    public LabelBatch[] getLabelBatches() {
        return labelBatches;
    }

    /**
     * Gets the TextRenderer for use in the rendering thread.
     * The renderer needs to call begin/end rendering with GL context.
     */
    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    /**
     * Pre-computed batch containing glyphs and rendering parameters.
     * Each batch is double-buffered: updater writes to write* fields,
     * renderer reads from read* fields. swap() atomically publishes changes.
     */
    public static class LabelBatch {
        // Read buffer (accessed by renderer)
        private volatile boolean readValid = false;
        private List<Glyph> readGlyphs;
        private float readX;
        private float readY;
        private float readScale;
        private float readR;
        private float readG;
        private float readB;
        private float readA;

        // Write buffer (accessed by updater)
        private boolean writeValid = false;
        private List<Glyph> writeGlyphs;
        private String writeText = null;
        private float writeAscent;
        private float writeX;
        private float writeY;
        private float writeScale;
        private float writeR;
        private float writeG;
        private float writeB;
        private float writeA;

        /**
         * Swaps read and write buffers, publishing the write data to the renderer.
         * Called by renderer thread at synchronization point.
         */
        public void swap() {
            readValid = writeValid;
            if (writeValid) {
                // Create a new list for readGlyphs to avoid concurrent modification
                // when the updater modifies writeGlyphs in the next update cycle
                if (writeGlyphs != null) {
                    if (readGlyphs == null) {
                        readGlyphs = new ArrayList<>(writeGlyphs);
                    } else {
                        readGlyphs.clear();
                        readGlyphs.addAll(writeGlyphs);
                    }
                } else {
                    readGlyphs = null;
                }
                readX = writeX;
                readY = writeY;
                readScale = writeScale;
                readR = writeR;
                readG = writeG;
                readB = writeB;
                readA = writeA;
            }
        }

        /**
         * Marks this batch as invalid (no label to render).
         * Called by updater thread.
         */
        public void markInvalid() {
            writeValid = false;
        }

        /**
         * Invalidates cached glyphs (e.g., when font changes).
         */
        public void invalidateGlyphs() {
            writeText = null;
            writeGlyphs = null;
        }

        // Renderer read methods

        public boolean isValid() {
            return readValid;
        }

        // Updater access methods (for overlap detection)

        public boolean isWriteValid() {
            return writeValid;
        }

        public float getWriteScale() {
            return writeScale;
        }

        public List<Glyph> getGlyphs() {
            return readGlyphs;
        }

        public float getX() {
            return readX;
        }

        public float getY() {
            return readY;
        }

        public float getScale() {
            return readScale;
        }

        public float getR() {
            return readR;
        }

        public float getG() {
            return readG;
        }

        public float getB() {
            return readB;
        }

        public float getA() {
            return readA;
        }
    }
}
