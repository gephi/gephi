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
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.awt.TextRenderer.RenderDelegate;
import com.jogamp.opengl.util.packrect.BackingStoreManager;
import com.jogamp.opengl.util.packrect.Rect;
import com.jogamp.opengl.util.packrect.RectVisitor;
import com.jogamp.opengl.util.packrect.RectanglePacker;
import com.jogamp.opengl.util.texture.TextureCoords;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;


/**
 * Storage of glyphs in an OpenGL texture.
 *
 * <p>
 * {@code GlyphCache} andles storing glyphs in a 2D texture and retrieving their coordinates.
 *
 * <p>
 * The first step in using a {@code GlyphCache} is to make sure it's set up by calling {@link
 * #beginRendering(GL)}.  Then glyphs can be added using {@link #upload(Glyph)}.  Each glyph will
 * be packed efficiently into the texture with a small amount of space around it using {@link
 * RectanglePacker}.  When all glyphs have been added, be sure to call {@link #update(GL)} or
 * {@link #endRendering(GL)} before trying to render with the texture, as the glyphs are not
 * actually drawn into the texture right away in order to increase performance.  Texture
 * coordinates of individual glyphs can be determined with {@link #find(Glyph)}.  When reusing the
 * glyph cache, {@link #contains(Glyph)} should be called to make sure a glyph is not already
 * stored.
 *
 * <p>
 * <em>Events fired when:</em>
 * <ul>
 *   <li>A glyph has not been used recently (CLEAN, glyph);
 *   <li>The backing store is going to be flushed.
 * </ul>
 *
 * <p>
 * GlyphCache is compatible with GL2 or GL3.
 *
 * @see TextureBackingStore
 */
/*@NotThreadSafe*/
public final class GlyphCache implements TextureBackingStore.EventListener {

    /**
     * Whether or not glyph cache should print debugging information.
     */
    private static final boolean DEBUG = false;

    /**
     * Number used to determine size of cache based on font size.
     */
    /*@Nonnegative*/
    private static final int FONT_SIZE_MULTIPLIER = 5;

    /**
     * How much fragmentation to allow before compacting.
     */
    /*@Nonnegative*/
    private static final float MAX_VERTICAL_FRAGMENTATION = 0.7f;

    /**
     * Number of render cycles before clearing unused entries.
     */
    /*@Nonnegative*/
    private static final int CYCLES_PER_FLUSH = 100;

    /**
     * Minimum size of backing store in pixels.
     */
    /*@Nonnegative*/
    private static final int MIN_BACKING_STORE_SIZE = 256;

    /**
     * Delegate to render text.
     */
    /*@Nonnull*/
    private final RenderDelegate renderDelegate;

    /**
     * Observers of glyph cache.
     */
    /*@Nonnull*/
    private final List<EventListener> listeners = new ArrayList<EventListener>();

    /**
     * Delegate to create textures.
     */
    /*@Nonnull*/
    private final TextureBackingStoreManager manager;

    /**
     * Delegate to position glyphs.
     */
    /*@Nonnull*/
    private final RectanglePacker packer;

    /**
     * Texture to draw into.
     *
     * <p>
     * This will be null until {@link #beginRendering} is called.
     */
    /*@CheckForNull*/
    private TextureBackingStore backingStore;

    /**
     * Times cache has been used.
     */
    /*@Nonnegative*/
    private int numRenderCycles = 0;

    /**
     * True if done initializing.
     */
    private boolean ready = false;

    /**
     * Constructs a {@link GlyphCache}.
     *
     * @param font Font that was used to create glyphs that will be stored, assumed not null
     * @param rd Controller of rendering bitmapped text, assumed not null
     * @param antialias True to render glyphs with smooth edges
     * @param subpixel True to consider subpixel positioning
     * @param mipmap True to create multiple sizes of texture
     * @see #newInstance
     */
    private GlyphCache(/*@Nonnull*/ final Font font,
                       /*@Nonnull*/ final RenderDelegate rd,
                       final boolean antialias,
                       final boolean subpixel,
                       final boolean mipmap) {
        this.renderDelegate = rd;
        this.manager = new TextureBackingStoreManager(font, antialias, subpixel, mipmap);
        this.packer = createPacker(font, manager);
    }

    /**
     * Registers an {@link EventListener} with this {@link GlyphCache}.
     *
     * @param listener Listener to register
     * @throws NullPointerException if listener is null
     */
    public void addListener(/*@Nonnull*/ final EventListener listener) {

        Check.notNull(listener, "Listener cannot be null");

        listeners.add(listener);
    }

    /**
     * Sets up the cache for rendering.
     *
     * <p>
     * After calling this method the texture storing the glyphs will be bound.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    public void beginRendering(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "Context cannot be null");

        // Set up if first time rendering
        if (!ready) {
            setMaxSize(gl);
            ready = true;
        }

        // Bind the backing store
        final TextureBackingStore bs = getBackingStore();
        bs.bind(gl, GL.GL_TEXTURE0);
    }

    /**
     * Clears all the texture coordinates stored in glyphs.
     */
    private void clearTextureCoordinates() {

        log("Clearing texture coordinates");

        packer.visit(new RectVisitor() {

            @Override
            public void visit(/*@Nonnull*/ final Rect rect) {
                final Glyph glyph = ((TextData) rect.getUserData()).glyph;
                glyph.coordinates = null;
            }
        });
    }

    /**
     * Clears entries that haven't been used in awhile.
     */
    private void clearUnusedEntries() {

        log("Trying to clear unused entries...");

        // Find rectangles in backing store that haven't been used recently
        final List<Rect> deadRects = new ArrayList<Rect>();
        packer.visit(new RectVisitor() {

            @Override
            public void visit(/*@Nonnull*/ final Rect rect) {
                final TextData data = (TextData) rect.getUserData();
                if (data.used()) {
                    data.clearUsed();
                } else {
                    deadRects.add(rect);
                }
            }
        });

        // Remove each of those rectangles
        final TextureBackingStore bs = getBackingStore();
        for (final Rect rect : deadRects) {
            packer.remove(rect);
            final Glyph glyph = ((TextData) rect.getUserData()).glyph;
            glyph.location = null;
            fireEvent(EventType.CLEAN, glyph);
            log("Cleared rectangle for glyph: %s", glyph);
            if (DEBUG) {
                bs.clear(rect.x(), rect.y(), rect.w(), rect.h());
            }
        }

        // If we removed dead rectangles this cycle, try to do a compaction
        final float frag = packer.verticalFragmentationRatio();
        if (!deadRects.isEmpty() && (frag > MAX_VERTICAL_FRAGMENTATION)) {
            log("Compacting due to fragmentation %s", frag);
            packer.compact();
        }

        // Force the backing store to update
        if (DEBUG) {
            bs.mark(0, 0, bs.getWidth(), bs.getHeight());
        }
    }

    /**
     * Computes the normalized coordinates of a glyph's location.
     *
     * @param glyph Glyph being uploaded, assumed not null
     */
    private void computeCoordinates(/*@Nonnull*/ final Glyph glyph) {

        // Determine dimensions in pixels
        final int cacheWidth = getWidth();
        final int cacheHeight = getHeight();
        final float left = getLeftBorderLocation(glyph);
        final float bottom = getBottomBorderLocation(glyph);

        // Convert to normalized texture coordinates
        final float l = left / cacheWidth;
        final float b = bottom / cacheHeight;
        final float r = (left + glyph.width) / cacheWidth;
        final float t = (bottom - glyph.height) / cacheHeight;

        // Store in glyph
        glyph.coordinates = new TextureCoords(l, b, r, t);
    }

    /**
     * Checks if a glyph is stored in this {@link GlyphCache}.
     *
     * @param glyph Glyph to check for, which may be null
     * @return True if glyph is in the cache
     */
    boolean contains(/*@CheckForNull*/ final Glyph glyph) {

        if (glyph == null) {
            return false;
        }

        return glyph.location != null;
    }

    /**
     * Makes a packer for positioning glyphs.
     *
     * @param font Font used to make glyphs being stored, assumed not null
     * @param manager Handler of packer events, assumed not null
     * @return Resulting packer, not null
     */
    /*@Nonnull*/
    private static RectanglePacker createPacker(/*@Nonnull*/ final Font font,
                                                /*@Nonnull*/ final BackingStoreManager manager) {
        final int size = findBackingStoreSizeForFont(font);
        return new RectanglePacker(manager, size, size, 1f);
    }

    /**
     * Destroys resources used by this {@link GlyphCache}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    public void dispose(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "Context cannot be null");

        packer.dispose();
        if (backingStore != null) {
            backingStore.dispose(gl);
            backingStore = null;
        }
    }

    /**
     * Draws a glyph into the backing store.
     *
     * @param glyph Glyph being uploaded, assumed not null
     */
    private void drawInBackingStore(/*@Nonnull*/ final Glyph glyph) {

        // Get the backing store
        final TextureBackingStore bs = getBackingStore();

        // Clear the area
        final Rect loc = glyph.location;
        final int x = loc.x();
        final int y = loc.y();
        final int w = loc.w();
        final int h = loc.h();
        bs.clear(x, y, w, h);

        // Draw the text
        renderDelegate.drawGlyphVector(
                bs.getGraphics(),
                glyph.glyphVector,
                getLeftBaselineLocation(glyph),
                getBottomBaselineLocation(glyph));

        // Mark it dirty
        bs.mark(x, y, w, h);
    }

    /**
     * Finishes setting up the cache for rendering.
     *
     * <p>
     * After calling this method, all uploaded glyphs will be guaranteed to be present in the
     * underlying OpenGL texture.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    public void endRendering(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "Context cannot be null");

        update(gl);

        // Check if reached render cycle limit
        if (++numRenderCycles >= CYCLES_PER_FLUSH) {
            numRenderCycles = 0;
            log("Reached cycle limit.");
            clearUnusedEntries();
        }
    }

    /**
     * Determines the texture coordinates of a glyph in the cache.
     *
     * <p>
     * <b>Notes:</b>
     * <ul>
     * <li>Texture coordinates are in the range 0 to 1
     * <li>Automatically marks the glyph as being used recently
     * <li>If cache has been resized, coordinates are recalculated
     * </ul>
     *
     * @param glyph Glyph already in cache
     * @return Texture coordinates of glyph in the cache, not null
     * @throws NullPointerException if glyph is null
     */
    /*@Nonnull*/
    public TextureCoords find(/*@Nonnull*/ final Glyph glyph) {

        Check.notNull(glyph, "Glyph cannot be null");

        // Mark the glyph as being used
        markGlyphLocationUsed(glyph);

        // Find the coordinates, recalculating if necessary
        if (glyph.coordinates == null) {
            computeCoordinates(glyph);
        }
        return glyph.coordinates;
    }

    /**
     * Returns the initial size of a {@link GlyphCache} for a font.
     *
     * @param font Font to create glyphs from, assumed not null
     */
    /*@Nonnegative*/
    private static int findBackingStoreSizeForFont(/*@Nonnull*/ final Font font) {
        return Math.max(MIN_BACKING_STORE_SIZE, font.getSize() * FONT_SIZE_MULTIPLIER);
    }

    /**
     * Finds a location in the backing store for a glyph.
     *
     * @param glyph Glyph being uploaded, assumed not null
     */
    private void findLocation(/*@Nonnull*/ final Glyph glyph) {

        // Compute a rectangle that includes glyph's margin
        final int x = 0;
        final int y = 0;
        final int w = glyph.margin.left + ((int) glyph.width) + glyph.margin.right;
        final int h = glyph.margin.top + ((int) glyph.height) + glyph.margin.bottom;
        final Rect rect = new Rect(x, y, w, h, new TextData(glyph));

        // Pack it into the cache and store its location
        packer.add(rect);
        glyph.location = rect;
        markGlyphLocationUsed(glyph);
    }

    /**
     * Determines the maximum texture size supported by OpenGL.
     *
     * @param gl Current OpenGL context, assumed not null
     * @return Maximum texture size
     */
    private static int findMaxSize(/*@Nonnull*/ final GL gl) {
        final int[] size = new int[1];
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, size, 0);
        return size[0];
    }

    /**
     * Sends an event to all the listeners.
     *
     * @param type Kind of event, assumed not null
     * @param data Information to send with event, assumed not null
     */
    private void fireEvent(/*@Nonnull*/ final EventType type, /*@Nonnull*/ final Object data) {
        for (final EventListener listener : listeners) {
            assert listener != null : "addListener rejects null";
            listener.onGlyphCacheEvent(type, data);
        }
    }

    /**
     * Returns object actually storing the rasterized glyphs.
     *
     * @return Object actually storing the rasterized glyphs, not null
     */
    /*@Nonnull*/
    TextureBackingStore getBackingStore() {
        return (TextureBackingStore) packer.getBackingStore();
    }

    /**
     * Determines the location of a glyph's bottom baseline.
     *
     * @param glyph Glyph to determine bottom baseline for, assumed not null
     * @return Location of glyph's bottom baseline, which may be negative
     */
    /*@CheckForSigned*/
    private int getBottomBaselineLocation(/*@Nonnull*/ final Glyph glyph) {
        return (int) (glyph.location.y() + glyph.margin.top + glyph.ascent);
    }

    /**
     * Determines the location of a glyph's bottom border.
     *
     * @param glyph Glyph to determine bottom border for, assumed not null
     * @return Location of glyph's bottom border, which may be negative
     */
    /*@CheckForSigned*/
    private int getBottomBorderLocation(/*@Nonnull*/ final Glyph glyph) {
        return (int) (glyph.location.y() + glyph.margin.top + glyph.height);
    }

    /**
     * Returns the font render context used for text size computations by this {@link GlyphCache}.
     *
     * <p>
     * This object should be considered transient and may become invalidated between {@link
     * #beginRendering} and {@link #endRendering} pairs.
     *
     * @return Font render context used for text size computations, not null
     */
    /*@Nonnull*/
    public FontRenderContext getFontRenderContext() {
        return getBackingStore().getGraphics().getFontRenderContext();
    }

    /**
     * Returns the height of this {@link GlyphCache}.
     *
     * @return Height of this cache, not negative
     */
    /*@Nonnegative*/
    int getHeight() {
        return getBackingStore().getHeight();
    }

    /**
     * Determines the location of a glyph's left baseline.
     *
     * @param glyph Glyph to determine left baseline for, assumed not null
     * @return Location of glyph's left baseline, which may be negative
     */
    /*@CheckForSigned*/
    private int getLeftBaselineLocation(/*@Nonnull*/ final Glyph glyph) {
        return (int) (glyph.location.x() + glyph.margin.left - glyph.kerning);
    }

    /**
     * Determines the location of a glyph's left border.
     *
     * @param glyph Glyph to determine left border for, assumed not null
     * @return Location of glyph's left border, which may be negative
     */
    /*@CheckForSigned*/
    private int getLeftBorderLocation(/*@Nonnull*/ final Glyph glyph) {
        return glyph.location.x() + glyph.margin.left;
    }

    /**
     * Checks if this {@link GlyphCache} is interpolating when sampling.
     *
     * @return True if this glyph cache is interpolating when it samples
     */
    public boolean getUseSmoothing() {
        return ((TextureBackingStoreManager) manager).getUseSmoothing();
    }

    /**
     * Returns the width of this {@link GlyphCache}.
     *
     * @return Width of this cache, not negative
     */
    /*@Nonnegative*/
    int getWidth() {
        return getBackingStore().getWidth();
    }

    /**
     * Checks if Non-Power-Of-Two textures are available.
     *
     * @param gl Current OpenGL context
     * @return True if NPOT textures are available
     * @throws NullPointerException if context is null
     */
    static boolean isNpotTextureAvailable(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        return gl.isExtensionAvailable("GL_ARB_texture_non_power_of_two");
    }

    private static void log(/*@Nonnull*/ final String message) {
        if (DEBUG) {
            System.err.println(message);
        }
    }

    private static void log(/*@Nonnull*/ final String message,
                            /*@CheckForNull*/ final Object arg) {
        if (DEBUG) {
            System.err.println(String.format(message, arg));
        }
    }

    /**
     * Marks a glyph's location as used.
     *
     * @param glyph Glyph to mark
     * @throws NullPointerException if glyph is null
     */
    static void markGlyphLocationUsed(/*@Nonnull*/ final Glyph glyph) {

        Check.notNull(glyph, "Glyph cannot be null");

        ((TextData) glyph.location.getUserData()).markUsed();
    }

    /**
     * Creates a new {@link GlyphCache}.
     *
     * @param font Font that was used to create glyphs that will be stored
     * @param rd Controller of rendering bitmapped text
     * @param antialias Whether to render glyphs with smooth edges
     * @param subpixel Whether to consider subpixel positioning
     * @param mipmap Whether to create multiple sizes for texture
     * @return New glyph cache instance, not null
     * @throws NullPointerException if font or render delegate is null
     * @throws IllegalArgumentException if render delegate wants full color
     */
    /*@Nonnull*/
    public static GlyphCache newInstance(/*@Nonnull*/ final Font font,
                                         /*@Nonnull*/ final RenderDelegate rd,
                                         final boolean antialias,
                                         final boolean subpixel,
                                         final boolean mipmap) {

        Check.notNull(font, "Font cannot be null");
        Check.notNull(rd, "Render delegate cannot be null");

        final GlyphCache gc = new GlyphCache(font, rd, antialias, subpixel, mipmap);
        gc.manager.addListener(gc);
        return gc;
    }

    /**
     * Responds to an event from the backing store.
     *
     * @param type Kind of backing store event
     * @throws NullPointerException if type is null
     */
    @Override
    public void onBackingStoreEvent(/*@Nonnull*/ final TextureBackingStore.EventType type) {

        Check.notNull(type, "Event type cannot be null");

        switch (type) {
        case REALLOCATE:
            onBackingStoreReallocate();
            break;
        case FAILURE:
            onBackingStoreFailure();
            break;
        }
    }

    /**
     * Responds to the backing store failing (reallocation).
     */
    private void onBackingStoreFailure() {
        packer.clear();
        fireEvent(EventType.CLEAR, null);
    }

    /**
     * Handles when a backing store is reallocated.
     *
     * <p>
     * First notifies observers, then tries to remove any unused entries, and finally erases the
     * texture coordinates of each entry since the width and height of the total texture has
     * changed.  Note that since the backing store is just expanded without moving any entries,
     * only the texture coordinates need to be recalculated.  The locations will still be the same.
     *
     * <p>
     * This heuristic and the fact that it clears the used bit of all entries seems to cause
     * cycling of entries in some situations, where the backing store becomes small compared to the
     * amount of text on the screen (see the TextFlow demo) and the entries continually cycle in
     * and out of the backing store, decreasing performance.  If we added a little age information
     * to the entries, and only cleared out entries above a certain age, this behavior would be
     * eliminated.  However, it seems the system usually stabilizes itself, so for now we'll just
     * keep things simple.  Note that if we don't clear the used bit here, the backing store tends
     * to increase very quickly to its maximum size, at least with the TextFlow demo when the text
     * is being continually re-laid out.
     */
    private void onBackingStoreReallocate() {
        fireEvent(EventType.REALLOCATE, null);
        clearUnusedEntries();
        clearTextureCoordinates();
    }

    /**
     * Changes the maximum size of this {@link GlyphCache}'s rectangle packer.
     *
     * @param gl Current OpenGL context, assumed not null
     */
    private void setMaxSize(/*@Nonnull*/ final GL gl) {
        final int maxSize = findMaxSize(gl);
        packer.setMaxSize(maxSize, maxSize);
    }

    /**
     * Changes whether this {@link GlyphCache}'s texture should interpolate when sampling.
     *
     * @param useSmoothing True to use linear interpolation
     */
    public void setUseSmoothing(boolean useSmoothing) {
        ((TextureBackingStoreManager) manager).setUseSmoothing(useSmoothing);
        getBackingStore().setUseSmoothing(useSmoothing);
    }

    /**
     * Forces the cache to update the underlying OpenGL texture.
     *
     * <p>
     * After calling this method, all uploaded glyphs will be guaranteed to be present in the
     * underlying OpenGL texture.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    public void update(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        final TextureBackingStore bs = getBackingStore();
        bs.update(gl);
    }

    /**
     * Stores a glyph in the cache.
     *
     * <p>
     * Determines a place to put the glyph in the underlying OpenGL texture, computes the glyph's
     * texture coordinates for that position, and requests the glyph be drawn into the texture.
     * (Note however that to increase performance the glyph is not guaranteed to actually be in the
     * texture until {@link #update(GL)} or {@link #endRendering(GL)} is called.)
     *
     * @param glyph Glyph not already stored in cache
     * @throws NullPointerException if glyph is null
     */
    public void upload(/*@Nonnull*/ final Glyph glyph) {

        Check.notNull(glyph, "Glyph cannot be null");

        // Perform upload steps
        findLocation(glyph);
        computeCoordinates(glyph);
        drawInBackingStore(glyph);

        // Make sure it's marked as used
        markGlyphLocationUsed(glyph);
    }

    /**
     * Object that wants to be notified of cache events.
     */
    public interface EventListener {

        /**
         * Responds to an event from a {@link GlyphCache}.
         *
         * @param type Type of event
         * @param data Object that triggered the event, i.e., a glyph
         * @throws NullPointerException if event type or data is null (optional)
         */
        void onGlyphCacheEvent(/*@Nonnull*/ EventType type, /*@Nonnull*/ Object data);
    }

    /**
     * Type of event fired from the cache.
     */
    public enum EventType {

        /**
         * All entries were removed from cache.
         */
        CLEAR,

        /**
         * Unused entries were removed from cache.
         */
        CLEAN,

        /**
         * Backing store changed size.
         */
        REALLOCATE;
    }

    /**
     * Data associated with each rectangle of text.
     */
    /*@NotThreadSafe*/
    static final class TextData {

        /**
         * Visual representation of text.
         */
        /*@Nonnull*/
        final Glyph glyph;

        /**
         * True if text was used recently.
         */
        private boolean used;

        /**
         * Constructs a {@link TextData} from a glyph.
         *
         * @param glyph Visual representation of text
         * @throws NullPointerException if glyph is null
         */
        TextData(/*@Nonnull*/ final Glyph glyph) {
            this.glyph = Check.notNull(glyph, "Glyph cannot be null");
        }

        /**
         * Indicates this {@link TextData} is no longer being used.
         */
        void clearUsed() {
            used = false;
        }

        /**
         * Indicates this {@link TextData} was just used.
         */
        void markUsed() {
            used = true;
        }

        /**
         * Returns the actual text stored with a rectangle.
         *
         * @return Actual text stored with a rectangle, not null
         */
        /*@CheckForNull*/
        String string() {
            return glyph.str;
        }

        /**
         * Returns true if text has been used recently.
         */
        boolean used() {
            return used;
        }
    }
}
