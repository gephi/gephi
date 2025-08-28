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
import com.jogamp.opengl.util.texture.TextureCoords;


/**
 * Utility for drawing glyphs.
 */
public interface GlyphRenderer {

    /**
     * Registers an {@link EventListener} with this {@link GlyphRenderer}.
     *
     * @param listener Listener to register
     * @throws NullPointerException if listener is null
     */
    void addListener(/*@Nonnull*/ EventListener listener);

    /**
     * Starts a render cycle with this {@link GlyphRenderer}.
     *
     * @param gl Current OpenGL context
     * @param ortho True if using orthographic projection
     * @param width Width of current OpenGL viewport
     * @param height Height of current OpenGL viewport
     * @param disableDepthTest True if should ignore depth values
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if width or height is negative
     * @throws GLException if context is unexpected version
     */
    void beginRendering(/*@Nonnull*/ GL gl,
                        boolean ortho,
                        /*@Nonnegative*/ int width,
                        /*@Nonnegative*/ int height,
                        boolean disableDepthTest);

    /**
     * Frees resources used by this {@link GlyphRenderer}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    void dispose(/*@Nonnull*/ GL gl);

    /**
     * Draws a glyph with this {@link GlyphRenderer}.
     *
     * @param gl Current OpenGL context
     * @param glyph Visual representation of a character
     * @param x Position to draw on X axis, which may be negative
     * @param y Position to draw on Y axis, which may be negative
     * @param z Position to draw on Z axis, which may be negative
     * @param scale Relative size of glyph, which may be negative
     * @param coords Texture coordinates of glyph
     * @return Distance to next character, which may be negative
     * @throws NullPointerException if context, glyph, or texture coordinate is null
     * @throws GLException if context is unexpected version
     */
    /*@CheckForSigned*/
    float drawGlyph(/*@Nonnull*/ GL gl,
                    /*@Nonnull*/ Glyph glyph,
                    /*@CheckForSigned*/ float x,
                    /*@CheckForSigned*/ float y,
                    /*@CheckForSigned*/ float z,
                    /*@CheckForSigned*/ float scale,
                    /*@Nonnull*/ TextureCoords coords);

    /**
     * Finishes a render cycle with this {@link GlyphRenderer}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    void endRendering(/*@Nonnull*/ GL gl);

    /**
     * Forces all stored text to be rendered.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is <tt>null</tt>
     * @throws GLException if context is unexpected version
     * @throws IllegalStateException if not in a render cycle
     */
    void flush(/*@Nonnull*/ GL gl);

    /**
     * Checks if this {@link GlyphRenderer} is using vertex arrays.
     *
     * @return True if this renderer is using vertex arrays
     */
    boolean getUseVertexArrays();

    /**
     * Changes the color used to draw the text.
     *
     * @param r Red component of color
     * @param g Green component of color
     * @param b Blue component of color
     * @param a Alpha component of color
     */
    void setColor(float r, float g, float b, float a);

    /**
     * Changes the transformation matrix for drawing in 3D.
     *
     * @param gl Current OpenGL context
     * @param value Matrix as float array
     * @param transpose True if array is in in row-major order
     * @throws IndexOutOfBoundsException if value's length is less than sixteen
     * @throws IllegalStateException if in orthographic mode
     */
    void setTransform(/*@Nonnull*/ float[] value, boolean transpose);

    /**
     * Changes whether vertex arrays are in use.
     *
     * @param useVertexArrays <tt>true</tt> to use vertex arrays
     */
    void setUseVertexArrays(boolean useVertexArrays);

    /**
     * <i>Observer</i> of a {@link GlyphRenderer}.
     */
    public interface EventListener {

        /**
         * Responds to an event from a glyph renderer.
         *
         * @param type Type of event
         * @throws NullPointerException if event type is <tt>null</tt>
         */
        public void onGlyphRendererEvent(EventType type);
    }

    /**
     * Type of event fired from the renderer.
     */
    public static enum EventType {

        /**
         * Renderer is automatically flushing queued glyphs, e.g., when it's full or color changes.
         */
        AUTOMATIC_FLUSH;
    }
}
