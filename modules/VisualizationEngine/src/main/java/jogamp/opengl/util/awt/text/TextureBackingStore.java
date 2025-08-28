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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;


/**
 * Wrapper for an OpenGL texture that can be drawn into.
 *
 * <p>
 * {@code TextureBackingStore} provides the ability to draw into a grayscale texture using Java 2D.
 * To increase performance, the backing store maintains a local copy as a {@link BufferedImage}.
 * Changes are applied to the image first and then pushed to the texture all at once.
 *
 * <p>
 * After creating a backing store, a client simply needs to grab its {@link Graphics2D} and use its
 * AWT or Java 2D drawing methods.  Then the area that was drawn to should be noted with the {@link
 * #mark(int, int, int, int)} method.  After everything is drawn, activate the texture using {@link
 * #bind(GL, int)} and call {@link #update(GL)} to actually push the dirty regions to the texture.
 * If further changes need to made, consider using {@link #clear(int, int, int, int)} to erase old
 * data.
 *
 * <p>
 * Note that since texturing hasn't changed much, BackingStore is compatible with GL2 or GL3.  For
 * that reason, it only requests simple GL objects.
 */
/*@NotThreadSafe*/
final class TextureBackingStore {

    /**
     * Size in X direction.
     */
    /*@Nonnegative*/
    private final int width;

    /**
     * Size in Y direction.
     */
    /*@Nonnegative*/
    private final int height;

    /**
     * Local copy of texture.
     */
    /*@Nonnull*/
    private final BufferedImage image;

    /**
     * Java2D utility for drawing into image.
     */
    /*@Nonnull*/
    private final Graphics2D g2d;

    /**
     * Raw image data for pushing to texture.
     */
    /*@Nonnull*/
    private final ByteBuffer pixels;

    /**
     * True for quality texturing.
     */
    /*@Nonnull*/
    private final boolean mipmap;

    /**
     * OpenGL texture on video card.
     */
    /*@CheckForNull*/
    private Texture2D texture = null;

    /**
     * Area in image not pushed to texture.
     */
    /*@CheckForNull*/
    private Rectangle dirtyRegion = null;

    /**
     * True to interpolate samples.
     */
    private boolean smooth;

    /**
     * True if interpolation has changed.
     */
    private boolean smoothChanged = false;

    /**
     * Constructs a {@link TextureBackingStore}.
     *
     * @param width Width of backing store
     * @param height Height of backing store
     * @param font Style of text
     * @param antialias True to render smooth edges
     * @param subpixel True to use subpixel accuracy
     * @param smooth True to interpolate samples
     * @param mipmap True for quality texturing
     * @throws IllegalArgumentException if width or height is negative
     * @throws NullPointerException if font is null
     */
    TextureBackingStore(/*@Nonnegative*/ final int width,
                        /*@Nonnegative*/ final int height,
                        /*@Nonnull*/ final Font font,
                        final boolean antialias,
                        final boolean subpixel,
                        final boolean smooth,
                        final boolean mipmap) {

        Check.argument(width >= 0, "Width cannot be negative");
        Check.argument(height >= 0, "Height cannot be negative");
        Check.notNull(font, "Font cannot be null");

        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        this.g2d = createGraphics(image, font, antialias, subpixel);
        this.pixels = getPixels(image);
        this.mipmap = mipmap;
        this.smooth = smooth;
    }

    /**
     * Binds the underlying OpenGL texture on a texture unit.
     *
     * @param gl Current OpenGL context
     * @param unit OpenGL enumeration for a texture unit (e.g., {@code GL_TEXTURE0})
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if unit is invalid
     */
    void bind(/*@Nonnull*/ final GL gl, final int unit) {

        Check.notNull(gl, "GL cannot be null");
        Check.argument(unit >= GL.GL_TEXTURE0, "Unit is invalid");

        ensureTexture(gl);
        texture.bind(gl, unit);
    }

    /**
     * Clears out an area in the backing store.
     *
     * @param x Position of area's left edge
     * @param y Position of area's top edge
     * @param width Width of area
     * @param height Height of area
     * @throws IllegalArgumentException if x, y, width, or height is negative
     */
    void clear(/*@Nonnegative*/ final int x,
               /*@Nonnegative*/ final int y,
               /*@Nonnegative*/ final int width,
               /*@Nonnegative*/ final int height) {

        Check.argument(x >= 0, "X cannot be negative");
        Check.argument(y >= 0, "Y cannot be negative");
        Check.argument(width >= 0, "Width cannot be negative");
        Check.argument(height >= 0, "Height cannot be negative");

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(x, y, width, height);
        g2d.setComposite(AlphaComposite.Src);
    }

    /**
     * Creates a graphics for a backing store.
     *
     * @param image Backing store's local copy of data, assumed not null
     * @param font Style of text, assumed not null
     * @param antialias True to smooth edges
     * @param subpixel True to use subpixel accuracy
     * @return Graphics2D for rendering into image, not null
     */
    /*@Nonnull*/
    private static Graphics2D createGraphics(/*@Nonnull*/ final BufferedImage image,
                                             /*@Nonnull*/ final Font font,
                                             final boolean antialias,
                                             final boolean subpixel) {

        final Graphics2D g2d = image.createGraphics();

        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.WHITE);
        g2d.setFont(font);
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                antialias ?
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                subpixel ?
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON :
                        RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        return g2d;
    }

    /**
     * Releases resources used by the backing store.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    void dispose(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        // Dispose of image
        if (image != null) {
            image.flush();
        }

        // Dispose of texture
        if (texture != null) {
            texture.dispose(gl);
        }
    }

    /**
     * Makes sure the texture has been created.
     *
     * @param gl Current OpenGL context, assumed not null
     */
    private void ensureTexture(/*@Nonnull*/ final GL gl) {
        if (texture == null) {
            texture = new GrayTexture2D(gl, width, height, smooth, mipmap);
        }
    }

    /**
     * Returns Java2D Graphics2D object for drawing into this store.
     *
     * @return Java2D graphics for drawing into this store, not null
     */
    /*@Nonnull*/
    final Graphics2D getGraphics() {
        return g2d;
    }

    /**
     * Returns height of the underlying image and texture.
     *
     * @return Height of the underlying image, not negative
     */
    /*@Nonnegative*/
    final int getHeight() {
        return height;
    }

    /**
     * Returns local copy of texture.
     *
     * @return Local copy of texture, not null
     */
    /*@Nonnull*/
    final BufferedImage getImage() {
        return image;
    }

    /**
     * Retrieves the underlying pixels of a buffered image.
     *
     * @param image Image with underlying pixel buffer, assumed not null
     * @return Pixel data of the image as a byte buffer, not null
     * @throws IllegalStateException if image is not stored as bytes
     */
    /*@Nonnull*/
    private static ByteBuffer getPixels(/*@Nonnull*/ final BufferedImage image) {

        final DataBuffer db = image.getRaster().getDataBuffer();
        final byte[] arr;

        if (db instanceof DataBufferByte) {
            arr = ((DataBufferByte) db).getData();
        } else {
            throw new IllegalStateException("Unexpected format in image.");
        }
        return ByteBuffer.wrap(arr);
    }

    /**
     * Returns true if texture is interpolating samples.
     *
     * @return True if texture is interpolating samples
     */
    final boolean getUseSmoothing() {
        return smooth;
    }

    /**
     * Returns width of the underlying image and texture.
     *
     * @return Width of the underlying image, not negative
     */
    /*@Nonnegative*/
    final int getWidth() {
        return width;
    }

    /**
     * Marks an area of the backing store to be updated.
     *
     * <p>
     * The next time the backing store is updated, the area will be pushed to the texture.
     *
     * @param x Position of area's left edge
     * @param y Position of area's top edge
     * @param width Width of area
     * @param height Height of area
     * @throws IllegalArgumentException if x, y, width, or height is negative
     */
    void mark(/*@Nonnegative*/ final int x,
              /*@Nonnegative*/ final int y,
              /*@Nonnegative*/ final int width,
              /*@Nonnegative*/ final int height) {

        Check.argument(x >= 0, "X cannot be negative");
        Check.argument(y >= 0, "Y cannot be negative");
        Check.argument(width >= 0, "Width cannot be negative");
        Check.argument(height >= 0, "Height cannot be negative");

        final Rectangle region = new Rectangle(x, y, width, height);
        if (dirtyRegion == null) {
            dirtyRegion = region;
        } else {
            dirtyRegion.add(region);
        }
    }

    /**
     * Specifies whether the texture should interpolate samples.
     */
    final void setUseSmoothing(final boolean useSmoothing) {
        smoothChanged = (this.smooth != useSmoothing);
        this.smooth = useSmoothing;
    }

    /**
     * Uploads any recently drawn data to the texture.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    void update(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        // Make sure texture is created
        ensureTexture(gl);

        // Check smoothing
        if (smoothChanged) {
            texture.setFiltering(gl, smooth);
            smoothChanged = false;
        }

        // Check texture
        if (dirtyRegion != null) {
            texture.update(gl, pixels, dirtyRegion);
            dirtyRegion = null;
        }
    }

    /**
     * Observer of texture backing store events.
     */
    interface EventListener {

        /**
         * Responds to an event from a texture backing store.
         *
         * @param type Type of event
         * @throws NullPointerException if event type is null (optional)
         */
        public void onBackingStoreEvent(/*@Nonnull*/ EventType type);
    }

    /**
     * Type of event fired from the backing store.
     */
    enum EventType {

        /**
         * Backing store being resized.
         */
        REALLOCATE,

        /**
         * Backing store could not be resized.
         */
        FAILURE;
    }
}
