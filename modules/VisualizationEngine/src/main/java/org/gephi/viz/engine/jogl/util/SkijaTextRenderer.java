package org.gephi.viz.engine.jogl.util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.paragraph.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static com.jogamp.opengl.GL.*;

/**
 * Generic batched text renderer using Skija for rasterization and a GL texture atlas for batching.
 * <p>
 * - Call {@link #beginFrame(int, int)}, then {@link #addText(String, float, float, int, int, float, GL2ES2)}  per label,
 * then {@link #draw(GL2ES2)}.
 * - Positions are in <b>screen-space pixels</b> (origin top-left). Rendering centers the text on (x,y).
 * - Size and color are per-label. Color is ARGB (premultiplied alpha handled internally).
 * - Uses a cache + atlas pages; draws one batch per atlas page.
 *
 * <p>Not thread-safe.</p>
 */
public final class SkijaTextRenderer {
    // ---- Config knobs ----
    private static final int SIZE_BUCKET_PX = 8;
    // coarser buckets => more cache hits. Font sizes are approximated to multiples of this value.
    private static final float DEFAULT_RASTER_SCALE = 0.6f; // render at 60% of logical px to reduce atlas size
    private static final boolean DEFAULT_USE_MIPMAPS = true;

    // Per-vertex layout: 4 floats (x, y, u, v). We draw triangles: 6 verts/quad.
    private static final int FLOATS_PER_VERTEX = 4;

    // Skia shaping
    private FontCollection fontCollection;
    private final ParagraphStyle paragraphStyle = new ParagraphStyle();

    // Cache + Atlas
    private final LabelAtlasCache cache;

    // Tiny GL pipeline
    private int prog = 0;
    private int vbo = 0;
    private int aPos = 0;
    private int aUV = 1;
    private int uTex = 0;

    // Frame state
    private int viewportWidth = 0, viewportHeight = 0;
    private final Map<AtlasPage, BatchedVertices> frameBatches = new LinkedHashMap<>();

    // Blend scratch
    private final int[] intData = new int[1];
    private final byte[] booleanData = new byte[1];

    // Options
    private float rasterScale = DEFAULT_RASTER_SCALE;
    private boolean useMipmaps = DEFAULT_USE_MIPMAPS;

    public SkijaTextRenderer() {
        this(4096, 2048, 2048, 1);
    }

    public SkijaTextRenderer(int cacheMaxEntries, int pageW, int pageH, int paddingPx) {
        this.cache = new LabelAtlasCache(cacheMaxEntries, pageW, pageH, paddingPx);
        this.fontCollection = new FontCollection();
        this.fontCollection.setDefaultFontManager(FontMgr.getDefault());
    }

    /**
     * Optional: provide a custom FontCollection (e.g., to register fonts).
     */
    public void setFontCollection(FontCollection collection) {
        this.fontCollection = collection != null ? collection : this.fontCollection;
    }

    /**
     * Optional: change raster scale applied to atlas entries.
     */
    public void setRasterScale(float scale) {
        this.rasterScale = Math.max(0.1f, Math.min(2.0f, scale));
    }

    /**
     * Optional: enable/disable mipmaps on atlas pages.
     */
    public void setUseMipmaps(boolean use) {
        this.useMipmaps = use;
    }

    /**
     * Initialize GL resources (program, buffers). Call once after GL context creation.
     */
    public void init(GL2ES2 gl) {
        if (gl == null) {
            return;
        }
        prog = buildProgram(gl, VERT_SRC, FRAG_SRC);
        aPos = gl.glGetAttribLocation(prog, "aPos");
        aUV = gl.glGetAttribLocation(prog, "aUV");
        uTex = gl.glGetUniformLocation(prog, "uTex");

        final int[] ids = new int[1];
        gl.glGenBuffers(1, ids, 0);
        vbo = ids[0];
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo);
        gl.glBufferData(GL_ARRAY_BUFFER, 1024, null, GL2ES2.GL_STREAM_DRAW); // small initial, will orphan & grow
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Begin a new frame. All positions are interpreted in screen-space pixels for this viewport size.
     *
     * @param viewportWWidth framebuffer width in pixels
     * @param viewportHeight framebuffer height in pixels
     */
    public void beginFrame(int viewportWWidth, int viewportHeight) {
        this.viewportWidth = Math.max(0, viewportWWidth);
        this.viewportHeight = Math.max(0, viewportHeight);
        frameBatches.clear();
    }

    // Replace the fixed cap with a dynamic one
    private int dynamicMaxLabelPxHeight() {
        // fit within atlas page height and viewport height (if known)
        int atlasLimit = cache.pageHeightMinusPadding();
        int viewportLimit = Math.max(1, viewportHeight);            // if vpH==0 during init, ignore
        // Don’t waste effort rasterizing larger than (say) half the viewport height
        int softLimit = Math.max(48, Math.min(atlasLimit, viewportLimit / 2));

        // Clamp by a hard upper bound to avoid absurd sizes
        return Math.min(256, softLimit);
    }

    private static final int MAX_LABEL_PX_HEIGHT = 96;

    private int bucketHeight(int pxHeight) {
        int h = Math.max(10, pxHeight);
        h = (h + SIZE_BUCKET_PX / 2) / SIZE_BUCKET_PX * SIZE_BUCKET_PX;
        h = Math.min(MAX_LABEL_PX_HEIGHT, h);
        return h;
    }

    private static boolean isFinite(float v) {
        return !Float.isNaN(v) && !Float.isInfinite(v);
    }

    /**
     * Queue a label for rendering, centered on (cx, cy) in screen-space pixels (origin top-left).
     * The text is NOT width-limited; anything outside the viewport will be clipped by GL.
     *
     * @param text     Text to draw (ignored if null/empty)
     * @param centerX  Center X in pixels
     * @param centerY  Center Y in pixels
     * @param pxHeight Desired text height in pixels (bucketed & clamped internally)
     * @param argb     ARGB color (0xAARRGGBB). Premultiplied alpha handled internally.
     */
    public void addText(String text, float centerX, float centerY, int pxHeight, int argb,
                        float maxDrawWidthPx, GL2ES2 gl) {
        if (viewportWidth <= 0 || viewportHeight <= 0 || gl == null) return;
        if (text == null || text.isEmpty()) return;
        if (!isFinite(centerX) || !isFinite(centerY)) return;

        // 1) Initial bucketed height
        int h0 = bucketHeight(pxHeight);

        // 2) Measure at h0 WITHOUT raster upload (fast path)
        float logicalW0 = cache.measureLogicalWidth(fontCollection, paragraphStyle, text, h0);
        if (logicalW0 <= 0) return;

        // 3) If a max width is given, shrink the height pre-cache
        int hFinal = h0;
        if (maxDrawWidthPx > 0 && logicalW0 > maxDrawWidthPx) {
            float s = maxDrawWidthPx / logicalW0;              // <= 1
            int h1 = bucketHeight(Math.max(10, Math.round(h0 * s)));
            if (h1 < h0) hFinal = h1;                          // only rebucket smaller
        }

        // 4) Fetch sprite at the FINAL (smaller) height
        final LabelSprite sprite = cache.getOrCreate(gl, fontCollection, paragraphStyle, text, hFinal, argb, rasterScale, useMipmaps);
        if (sprite == null) return;

        // 5) Draw at sprite.logicalW/H (already width-fit by using hFinal)
        float drawW = sprite.logicalW;
        float drawH = sprite.logicalH;

        // center-quad → NDC (top-left origin)
        final float x0 = centerX - drawW * 0.5f;
        final float y0 = (viewportHeight - centerY) - drawH * 0.5f;
        final float x1 = x0 + drawW, y1 = y0 + drawH;

        final float x0n = (x0 / viewportWidth) * 2f - 1f;
        final float x1n = (x1 / viewportWidth) * 2f - 1f;
        final float y0n = 1f - (y0 / viewportHeight) * 2f;
        final float y1n = 1f - (y1 / viewportHeight) * 2f;

        BatchedVertices bv = frameBatches.computeIfAbsent(sprite.page, k -> new BatchedVertices());
        float u0 = sprite.u0, v0 = sprite.v0, u1 = sprite.u1, v1 = sprite.v1;
        bv.putQuad(
            x0n, y0n, u0, v1,
            x0n, y1n, u0, v0,
            x1n, y1n, u1, v0,
            x0n, y0n, u0, v1,
            x1n, y1n, u1, v0,
            x1n, y0n, u1, v1
        );
    }


    /**
     * Draw all queued labels and clear the queue.
     */
    public void draw(GL2ES2 gl) {
        if (gl == null || viewportWidth <= 0 || viewportHeight <= 0) {
            frameBatches.clear();
            return;
        }

        // Save current blend state and set premultiplied alpha blending
        gl.glGetBooleanv(GL_BLEND, booleanData, 0);
        gl.glGetIntegerv(GL_BLEND_DST_ALPHA, intData, 0);
        final boolean blendEnabled = booleanData[0] > 0;
        final int blendFunc = intData[0];

        if (!blendEnabled) {
            gl.glEnable(GL_BLEND);
        }

        if (blendFunc != GL_ONE_MINUS_SRC_ALPHA) {
            gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glUseProgram(prog);
        gl.glUniform1i(uTex, 0);
        gl.glActiveTexture(GL_TEXTURE0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo);
        gl.glEnableVertexAttribArray(aPos);
        gl.glEnableVertexAttribArray(aUV);
        gl.glVertexAttribPointer(aPos, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 0);
        gl.glVertexAttribPointer(aUV, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 2 * Float.BYTES);

        for (Map.Entry<AtlasPage, BatchedVertices> e : frameBatches.entrySet()) {
            final AtlasPage page = e.getKey();
            final BatchedVertices bv = e.getValue();
            final int vertCount = bv.vertexCount();
            if (vertCount <= 0) {
                continue;
            }

            final ByteBuffer src = bv.flipView();
            final int byteCount = src.remaining();
            gl.glBufferData(GL_ARRAY_BUFFER, byteCount, null, GL2ES2.GL_STREAM_DRAW); // orphan
            gl.glBufferSubData(GL_ARRAY_BUFFER, 0, byteCount, src);

            gl.glBindTexture(GL_TEXTURE_2D, page.texId);
            if (page.dirty && useMipmaps) {
                gl.glBindTexture(GL_TEXTURE_2D, page.texId);
                gl.glGenerateMipmap(GL_TEXTURE_2D);
                page.dirty = false;
            }
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertCount);
        }

        // Restore state + clean
        if (!blendEnabled) {
            gl.glDisable(GL_BLEND);
        }
        if (blendFunc != GL_ONE_MINUS_SRC_ALPHA) {
            gl.glBlendFunc(GL_SRC_ALPHA, blendFunc);
        }

        gl.glDisableVertexAttribArray(aPos);
        gl.glDisableVertexAttribArray(aUV);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl.glBindTexture(GL_TEXTURE_2D, 0);
        gl.glUseProgram(0);

        frameBatches.clear();
    }

    /**
     * Clears and disposes atlas pages and cached sprites.
     */
    public void clearCache(GL2ES2 gl) {
        cache.clear(gl);
    }

    /**
     * Dispose GL resources (call when destroying the GL context).
     */
    public void dispose(GL2ES2 gl) {
        if (gl == null) {
            return;
        }
        clearCache(gl);
        if (vbo != 0) {
            gl.glDeleteBuffers(1, new int[] {vbo}, 0);
            vbo = 0;
        }
        if (prog != 0) {
            gl.glDeleteProgram(prog);
            prog = 0;
        }
    }

    // ---------- GL mini-pipeline ----------

    private static int buildProgram(GL2ES2 gl, String vsSrc, String fsSrc) {
        int vs = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        gl.glShaderSource(vs, 1, new String[] {vsSrc}, new int[] {vsSrc.length()}, 0);
        gl.glCompileShader(vs);
        checkShader(gl, vs);

        int fs = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
        gl.glShaderSource(fs, 1, new String[] {fsSrc}, new int[] {fsSrc.length()}, 0);
        gl.glCompileShader(fs);
        checkShader(gl, fs);

        int prog = gl.glCreateProgram();
        gl.glAttachShader(prog, vs);
        gl.glAttachShader(prog, fs);
        gl.glBindAttribLocation(prog, 0, "aPos");
        gl.glBindAttribLocation(prog, 1, "aUV");
        gl.glLinkProgram(prog);
        checkProgram(gl, prog);
        gl.glDeleteShader(vs);
        gl.glDeleteShader(fs);
        return prog;
    }

    private static void checkShader(GL2ES2 gl, int s) {
        int[] ok = new int[1];
        gl.glGetShaderiv(s, GL2ES2.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == GL_FALSE) {
            int[] len = new int[1];
            gl.glGetShaderiv(s, GL2ES2.GL_INFO_LOG_LENGTH, len, 0);
            byte[] log = new byte[Math.max(len[0], 1)];
            gl.glGetShaderInfoLog(s, log.length, null, 0, log, 0);
            throw new RuntimeException("Shader compile error: " + new String(log));
        }
    }

    private static void checkProgram(GL2ES2 gl, int p) {
        int[] ok = new int[1];
        gl.glGetProgramiv(p, GL2ES2.GL_LINK_STATUS, ok, 0);
        if (ok[0] == GL_FALSE) {
            int[] len = new int[1];
            gl.glGetProgramiv(p, GL2ES2.GL_INFO_LOG_LENGTH, len, 0);
            byte[] log = new byte[Math.max(len[0], 1)];
            gl.glGetProgramInfoLog(p, log.length, null, 0, log, 0);
            throw new RuntimeException("Program link error: " + new String(log));
        }
    }

    private static final String VERT_SRC =
        "attribute vec2 aPos;\n" +
            "attribute vec2 aUV;\n" +
            "varying vec2 vUV;\n" +
            "void main(){ vUV = aUV; gl_Position = vec4(aPos, 0.0, 1.0); }";

    private static final String FRAG_SRC =
        "uniform sampler2D uTex;\n" +
            "varying vec2 vUV;\n" +
            "void main(){ gl_FragColor = texture2D(uTex, vUV); }";

    // ---------- Atlas-backing cache ----------

    private static final class LabelSprite {
        final AtlasPage page;             // owner texture page
        final float logicalW, logicalH;   // full logical size (px)
        final float u0, v0, u1, v1;       // UVs inside page (0..1), padded + half-texel corrected

        LabelSprite(AtlasPage page, float logicalW, float logicalH, float u0, float v0, float u1, float v1) {
            this.page = page;
            this.logicalW = logicalW;
            this.logicalH = logicalH;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }
    }

    private static final class LabelAtlasCache {
        private final int maxEntries;
        private final int pageW, pageH, pad;
        private final boolean useMipmaps = DEFAULT_USE_MIPMAPS;
        private final LinkedHashMap<String, LabelSprite> map = new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, LabelSprite> e) {
                return size() > maxEntries;
            }
        };
        private final List<AtlasPage> pages = new ArrayList<>();

        LabelAtlasCache(int maxEntries, int pageW, int pageH, int pad) {
            this.maxEntries = Math.max(64, maxEntries);
            this.pageW = Math.max(64, pageW);
            this.pageH = Math.max(64, pageH);
            this.pad = Math.max(0, pad);
        }

        private static String key(String text, int pxHeight, int argb, float scale) {
            return pxHeight + "|" + argb + "|" + scale + "|" + text;
        }

        private int effectivePad() {
            return useMipmaps ? Math.max(pad, 2) : pad;
        }

        LabelSprite getOrCreate(GL2ES2 gl, FontCollection fonts, ParagraphStyle paraStyle, String text, int pxHeight,
                                int argbColor, float rasterScale, boolean mipmaps) {
            final String k = key(text, pxHeight, argbColor, rasterScale);
            LabelSprite sprite = map.get(k);
            if (sprite != null && sprite.page != null && sprite.page.isAlive()) {
                return sprite;
            }

            Paragraph p = null;
            float logicalW = 0f, logicalH = 0f;
            try {
                final TextStyle ts = new TextStyle();
                ts.setFontSize(pxHeight);
                ts.setColor(argbColor);
                final ParagraphBuilder pb = new ParagraphBuilder(paraStyle, fonts);
                pb.pushStyle(ts);
                pb.addText(text);
                p = pb.build();
                p.layout(Float.MAX_VALUE);
                logicalW = (float) Math.ceil(p.getMaxIntrinsicWidth());
                logicalH = (float) Math.ceil(p.getHeight());
            } catch (Throwable t) {
                if (p != null) {
                    try {
                        p.close();
                    } catch (Throwable ignore) {
                    }
                }
                return null;
            }
            if (!(logicalW > 0 && logicalH > 0)) {
                if (p != null) {
                    try {
                        p.close();
                    } catch (Throwable ignore) {
                    }
                }
                return null;
            }

            // TODO: Too wide text disappears for some reason

            final int texW = Math.max(1, Math.round(logicalW * rasterScale));
            final int texH = Math.max(1, Math.round(logicalH * rasterScale));
            final int needW = texW + effectivePad() * 2;
            final int needH = texH + effectivePad() * 2;

            AtlasPage page = findPageWithRoom(gl, needW, needH, mipmaps);
            if (page == null) {
                if (p != null) {
                    try {
                        p.close();
                    } catch (Throwable ignore) {
                    }
                }
                return null;
            }

            AtlasPage.Placement place = page.alloc(needW, needH);
            if (place == null) {
                AtlasPage np = new AtlasPage(gl, pageW, pageH, mipmaps);
                if (!np.isAlive()) {
                    if (p != null) {
                        try {
                            p.close();
                        } catch (Throwable ignore) {
                        }
                    }
                    return null;
                }
                pages.add(np);
                place = np.alloc(needW, needH);
                if (place == null) {
                    if (p != null) {
                        try {
                            p.close();
                        } catch (Throwable ignore) {
                        }
                    }
                    return null;
                }
                page = np;
            }

            final int dstX = place.x + effectivePad();
            final int dstY = place.y + effectivePad();

            Surface surface = null;
            Image img = null;
            Pixmap pm = null;
            try {
                surface = Surface.makeRasterN32Premul(texW, texH);
                final Canvas canvas = surface.getCanvas();
                canvas.clear(0x00000000);
                canvas.scale(rasterScale, rasterScale);
                p.paint(canvas, 0, 0);

                img = surface.makeImageSnapshot();
                pm = new Pixmap();
                if (!img.peekPixels(pm)) {
                    return null;
                }

                final int rowBytes = pm.getRowBytes();
                final ByteBuffer src = pm.getBuffer();
                final ByteBuffer tight = ByteBuffer.allocateDirect(texW * texH * 4).order(ByteOrder.nativeOrder());
                final byte[] row = new byte[texW * 4];
                for (int y = 0; y < texH; y++) {
                    src.position(y * rowBytes).get(row, 0, texW * 4);
                    tight.position((texH - 1 - y) * texW * 4).put(row, 0, texW * 4);
                }
                tight.rewind();

                gl.glBindTexture(GL_TEXTURE_2D, page.texId);
                gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
                gl.glTexSubImage2D(GL_TEXTURE_2D, 0, dstX, dstY, texW, texH, GL_RGBA, GL_UNSIGNED_BYTE, tight);
                if (useMipmaps) {
                    gl.glGenerateMipmap(GL_TEXTURE_2D);
                }
                gl.glBindTexture(GL_TEXTURE_2D, 0);
            } catch (Throwable t) {
                return null;
            } finally {
                if (pm != null) {
                    try {
                        pm.close();
                    } catch (Throwable ignore) {
                    }
                }
                if (img != null) {
                    try {
                        img.close();
                    } catch (Throwable ignore) {
                    }
                }
                if (surface != null) {
                    try {
                        surface.close();
                    } catch (Throwable ignore) {
                    }
                }
                if (p != null) {
                    try {
                        p.close();
                    } catch (Throwable ignore) {
                    }
                }
            }

            final float halfU = 0.5f / page.w;
            final float halfV = 0.5f / page.h;
            final float u0 = (dstX + halfU) / page.w;
            final float v0 = (dstY + halfV) / page.h;
            final float u1 = (dstX + texW - halfU) / page.w;
            final float v1 = (dstY + texH - halfV) / page.h;

            sprite = new LabelSprite(page, logicalW, logicalH, u0, v0, u1, v1);
            map.put(k, sprite);
            return sprite;
        }

        float measureLogicalWidth(FontCollection fonts, ParagraphStyle paraStyle, String text, int pxHeight) {
            Paragraph p = null;
            try {
                TextStyle ts = new TextStyle();
                ts.setFontSize(pxHeight);
                ParagraphBuilder pb = new ParagraphBuilder(paraStyle, fonts);
                pb.pushStyle(ts);
                pb.addText(text);
                p = pb.build();
                p.layout(Float.MAX_VALUE);
                return (float) Math.ceil(p.getMaxIntrinsicWidth());
            } catch (Throwable t) {
                return 0f;
            } finally {
                if (p != null) try { p.close(); } catch (Throwable ignore) {}
            }
        }

        private AtlasPage findPageWithRoom(GL2ES2 gl, int needW, int needH, boolean mipmaps) {
            if (needW > pageW || needH > pageH) {
                return null;
            }
            for (AtlasPage p : pages) {
                if (p.canFit(needW, needH)) {
                    return p;
                }
            }
            AtlasPage np = new AtlasPage(gl, pageW, pageH, mipmaps);
            if (!np.isAlive()) {
                return null;
            }
            pages.add(np);
            return np;
        }

        void clear(GL2ES2 gl) {
            for (AtlasPage p : pages) {
                p.dispose(gl);
            }
            pages.clear();
            map.clear();
        }

        public int pageWidthMinusPadding() {
            return pageW - effectivePad() * 2;
        }

        public int pageHeightMinusPadding() {
            return pageH - effectivePad() * 2;
        }
    }

    private static final class AtlasPage {
        final int texId;
        final int w;
        final int h;
        private boolean alive = true;
        int curX = 0, curY = 0, shelfH = 0; // Simple shelf bin packer (top-left origin)
        boolean dirty;

        AtlasPage(GL2ES2 gl, int w, int h, boolean mipmaps) {
            this.w = w;
            this.h = h;
            final int[] id = new int[1];
            gl.glGenTextures(1, id, 0);
            texId = id[0];
            gl.glBindTexture(GL_TEXTURE_2D, texId);
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mipmaps ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
            dirty = true;
            gl.glBindTexture(GL_TEXTURE_2D, 0);
        }

        boolean canFit(int needW, int needH) {
            if (!alive) {
                return false;
            }
            if (needW <= 0 || needH <= 0) {
                return false;
            }
            if (curX + needW <= w && curY + needH <= h) {
                return true;
            }
            return curY + shelfH + needH <= h && needW <= w;
        }

        static final class Placement {
            final int x, y, w, h;

            Placement(int x, int y, int w, int h) {
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
            }
        }

        Placement alloc(int needW, int needH) {
            if (!canFit(needW, needH)) {
                return null;
            }
            if (curX + needW <= w) {
                Placement p = new Placement(curX, curY, needW, needH);
                curX += needW;
                shelfH = Math.max(shelfH, needH);
                return p;
            }
            curY += shelfH;
            curX = 0;
            shelfH = 0;
            if (!canFit(needW, needH)) {
                return null;
            }
            Placement p = new Placement(curX, curY, needW, needH);
            curX += needW;
            shelfH = Math.max(shelfH, needH);
            return p;
        }

        boolean isAlive() {
            return alive;
        }

        void dispose(GL2ES2 gl) {
            if (!alive) {
                return;
            }
            gl.glDeleteTextures(1, new int[] {texId}, 0);
            alive = false;
        }
    }

    private static final class BatchedVertices {
        private ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024).order(ByteOrder.nativeOrder());
        private int verts = 0;

        void putQuad(float... v) {
            final int need = v.length * Float.BYTES;
            if (buffer.remaining() < need) {
                grow(Math.max(buffer.capacity() * 2, buffer.capacity() + need));
            }
            for (float f : v) {
                buffer.putFloat(f);
            }
            verts += v.length / FLOATS_PER_VERTEX;
        }

        int vertexCount() {
            return verts;
        }

        ByteBuffer flipView() {
            final int lim = buffer.position();
            ByteBuffer dup = buffer.duplicate();
            dup.position(0);
            dup.limit(lim);
            return dup.slice().order(ByteOrder.nativeOrder());
        }

        private void grow(int newCap) {
            final ByteBuffer nb = ByteBuffer.allocateDirect(newCap).order(ByteOrder.nativeOrder());
            buffer.flip();
            nb.put(buffer);
            buffer = nb;
        }
    }
}
