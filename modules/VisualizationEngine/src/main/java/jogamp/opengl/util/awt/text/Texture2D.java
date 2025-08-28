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
import com.jogamp.opengl.GL2;

import java.awt.Rectangle;
import java.nio.ByteBuffer;


/**
 * Two-dimensional OpenGL texture.
 */
abstract class Texture2D extends Texture {

    // Size on X axis
    /*@Nonnegative*/
    protected final int width;

    // Size on Y axis
    /*@Nonnegative*/
    protected final int height;

    /**
     * Creates a 2D texture.
     *
     * @param gl Current OpenGL context
     * @param width Size of texture on X axis
     * @param height Size of texture on Y axis
     * @param smooth True to interpolate samples
     * @param mipmap True for high quality
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if width or height is negative
     */
    Texture2D(/*@Nonnull*/ final GL gl,
              /*@Nonnegative*/ final int width,
              /*@Nonnegative*/ final int height,
              final boolean smooth,
              final boolean mipmap) {

        super(gl, GL.GL_TEXTURE_2D, mipmap);

        Check.argument(width >= 0, "Width cannot be negative");
        Check.argument(height >= 0, "Height cannot be negative");

        // Copy parameters
        this.width = width;
        this.height = height;

        // Set up
        bind(gl, GL.GL_TEXTURE0);
        allocate(gl);
        setFiltering(gl, smooth);
    }

    /**
     * Allocates a 2D texture for use with a backing store.
     *
     * @param gl Current OpenGL context, assumed not null
     * @param width Width of texture, assumed not negative
     * @param height Height of texture, assumed not negative
     */
    private void allocate(/*@Nonnull*/ final GL gl) {
        gl.glTexImage2D(
                GL.GL_TEXTURE_2D,          // target
                0,                         // level
                getInternalFormat(gl),     // internal format
                width,                     // width
                height,                    // height
                0,                         // border
                GL.GL_RGB,                 // format (unused)
                GL.GL_UNSIGNED_BYTE,       // type (unused)
                null);                     // pixels
    }

    /**
     * Determines the proper texture format for an OpenGL context.
     *
     * @param gl Current OpenGL context
     * @return Texture format enumeration for OpenGL context
     * @throws NullPointerException if context is null (optional)
     */
    protected abstract int getFormat(/*@Nonnull*/ GL gl);

    /**
     * Determines the proper internal texture format for an OpenGL context.
     *
     * @param gl Current OpenGL context
     * @return Internal texture format enumeration for OpenGL context
     * @throws NullPointerException if context is null (optional)
     */
    protected abstract int getInternalFormat(/*@Nonnull*/ GL gl);

    /**
     * Updates the texture.
     *
     * <p>
     * Copies any areas marked with {@link #mark(int, int, int, int)} from the local image to the
     * OpenGL texture.  Only those areas will be modified.
     *
     * @param gl Current OpenGL context
     * @param pixels Data of entire image
     * @param area Region to update
     * @throws NullPointerException if context, pixels, or area is null
     */
    void update(/*@Nonnull*/ final GL gl,
                /*@Nonnull*/ final ByteBuffer pixels,
                /*@Nonnull*/ final Rectangle area) {

        Check.notNull(gl, "GL cannot be null");
        Check.notNull(pixels, "Pixels cannot be null");
        Check.notNull(area, "Area cannot be null");

        final int parameters[] = new int[4];

        // Store unpack parameters
        gl.glGetIntegerv(GL.GL_UNPACK_ALIGNMENT, parameters, 0);
        gl.glGetIntegerv(GL2.GL_UNPACK_SKIP_ROWS, parameters, 1);
        gl.glGetIntegerv(GL2.GL_UNPACK_SKIP_PIXELS, parameters, 2);
        gl.glGetIntegerv(GL2.GL_UNPACK_ROW_LENGTH, parameters, 3);

        // Change unpack parameters
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, area.y);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, area.x);
        gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, width);

        // Update the texture
        gl.glTexSubImage2D(
                GL.GL_TEXTURE_2D,     // target
                0,                    // mipmap level
                area.x,               // x offset
                area.y,               // y offset
                area.width,           // width
                area.height,          // height
                getFormat(gl),        // format
                GL.GL_UNSIGNED_BYTE,  // type
                pixels);              // pixels

        // Reset unpack parameters
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, parameters[0]);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, parameters[1]);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, parameters[2]);
        gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, parameters[3]);

        // Generate mipmaps
        if (mipmap) {
            gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
        }
    }
}
