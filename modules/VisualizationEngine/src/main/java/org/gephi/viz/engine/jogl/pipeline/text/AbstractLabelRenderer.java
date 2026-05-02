package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.TextureCoords;
import java.util.EnumSet;
import java.util.List;
import jogamp.text.TextRenderer;
import jogamp.text.util.Glyph;
import org.gephi.graph.api.Element;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.pipeline.common.LabelWorldData;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;

@SuppressWarnings("rawtypes")
public abstract class AbstractLabelRenderer<E extends Element>
    implements Renderer<JOGLRenderingTarget, LabelWorldData> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(RenderingLayer.FRONT1);

    private final VizEngine engine;
    private final AbstractLabelData<E> labelData;
    private TextRenderer textRenderer;

    // Scratch
    private final float[] mvp = new float[16];

    public AbstractLabelRenderer(VizEngine engine, AbstractLabelData<E> labelData) {
        this.engine = engine;
        this.labelData = labelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        // Nothing to do
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        if (textRenderer != null) {
            textRenderer.dispose();
            textRenderer = null;
        }
    }

    @Override
    public LabelWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        // This is the synchronization point between updater and renderer threads
        // The updater has finished preparing batches, now swap the buffers
        labelData.swapBuffers();

        return new LabelWorldData(
            labelData.getTextRenderer(),
            labelData.getLabelBatches(),
            labelData.getMaxValidIndex()
        );
    }

    @Override
    public void render(LabelWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        // Dispose any old renderer that was replaced (e.g., due to font change)
        // This must be done in render thread because dispose() requires GL context
        if (textRenderer != null && data.getTextRenderer() != null && textRenderer != data.getTextRenderer()) {
            textRenderer.dispose();
        }

        // Update to the new TextRenderer
        if (data.getTextRenderer() == null) {
            if (textRenderer != null) {
                textRenderer.dispose();
                textRenderer = null;
            }
            return;
        } else {
            textRenderer = data.getTextRenderer();
        }

        // Get the pre-computed batches from the WorldData (captured at worldUpdated time)
        // This ensures we use a consistent snapshot for this frame
        final AbstractLabelData.LabelBatch[] batches = data.getLabelBatches();
        final int maxIndex = data.getMaxIndex();

        if (batches == null || batches.length == 0 || maxIndex < 0) {
            return;
        }

        engine.getModelViewProjectionMatrixFloats(mvp);

        final GL gl = GLContext.getCurrentGL();

        textRenderer.begin3DRendering();
        textRenderer.setTransform(mvp);

        // Render each prepared batch up to maxIndex
        // All glyphs, positions, colors were pre-computed in the updater thread
        for (int i = 0; i <= maxIndex && i < batches.length; i++) {
            final AbstractLabelData.LabelBatch batch = batches[i];

            // Skip null or invalid batches
            if (batch == null || !batch.isValid()) {
                continue;
            }

            final List<Glyph> glyphs = batch.getGlyphs();
            if (glyphs == null || glyphs.isEmpty()) {
                continue;
            }

            // Set color for this batch
            textRenderer.setColor(batch.getR(), batch.getG(), batch.getB(), batch.getA());

            // Render each glyph in the batch
            float x = batch.getX();
            final float y = batch.getY();
            final float scale = batch.getScale();

            for (final Glyph glyph : glyphs) {
                // Upload glyph to texture cache if needed (requires GL)
                // Note: After the fix in GlyphCache.clearUnusedEntries(), 
                // both location and coordinates are cleared on eviction.
                // GlyphCache.find() will recompute coordinates if needed.
                if (glyph.location == null) {
                    textRenderer.getGlyphCache().upload(glyph);
                }

                // Get texture coordinates (will recompute if needed)
                final TextureCoords coords = textRenderer.getGlyphCache().find(glyph);

                // Draw the glyph
                final float advance = textRenderer.getGlyphRenderer().drawGlyph(
                    gl, glyph, x, y, 0f, scale, coords
                );
                x += advance * scale;
            }
        }

        textRenderer.end3DRendering();
    }

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return org.gephi.viz.engine.util.gl.Constants.RENDERING_ORDER_LABELS;
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }
}

