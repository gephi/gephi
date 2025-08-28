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
import com.jogamp.opengl.GL3;


/**
 * OpenGL texture.
 */
abstract class Texture {

    /**
     * ID of internal OpenGL texture.
     */
    /*@Nonnegative*/
    protected final int handle;

    /**
     * {@code GL_TEXTURE2D}, etc.
     */
    protected final int type;

    /**
     * True for quality texturing.
     */
    protected final boolean mipmap;

    /**
     * Constructs a {@link Texture}.
     *
     * @param gl Current OpenGL context
     * @param type Type of texture
     * @param mipmap True for quality texturing
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if type is invalid
     */
    Texture(/*@Nonnull*/ final GL gl, final int type, final boolean mipmap) {

        Check.notNull(gl, "GL cannot be null");
        Check.argument(isValidTextureType(type), "Texture type is invalid");

        this.handle = generate(gl);
        this.type = type;
        this.mipmap = mipmap;
    }

    /**
     * Binds underlying OpenGL texture on a texture unit.
     *
     * @param gl Current OpenGL context
     * @param unit OpenGL enumeration for a texture unit, i.e., {@code GL_TEXTURE0}
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if unit is invalid
     */
    void bind(/*@Nonnull*/ final GL gl, final int unit) {

        Check.notNull(gl, "GL cannot be null");
        Check.argument(isValidTextureUnit(unit), "Texture unit is invalid");

        gl.glActiveTexture(unit);
        gl.glBindTexture(type, handle);
    }

    /**
     * Destroys the texture.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     */
    void dispose(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        final int[] handles = new int[] { handle };
        gl.glDeleteTextures(1, handles, 0);
    }

    /**
     * Generates an OpenGL texture object.
     *
     * @param gl Current OpenGL context, assumed not null
     * @return Handle to the OpenGL texture
     */
    private static int generate(/*@Nonnull*/ final GL gl) {
        final int[] handles = new int[1];
        gl.glGenTextures(1, handles, 0);
        return handles[0];
    }

    /**
     * Checks if an integer is a valid OpenGL enumeration for a texture type.
     *
     * @param type Integer to check
     * @return True if type is valid
     */
    private static boolean isValidTextureType(final int type) {
        switch (type) {
        case GL3.GL_TEXTURE_1D:
        case GL3.GL_TEXTURE_2D:
        case GL3.GL_TEXTURE_3D:
            return true;
        default:
            return false;
        }
    }

    /**
     * Checks if an integer is a valid OpenGL enumeration for a texture unit.
     *
     * @param unit Integer to check
     * @return True if unit is valid
     */
    private static boolean isValidTextureUnit(final int unit) {
        return (unit >= GL.GL_TEXTURE0) && (unit <= GL.GL_TEXTURE31);
    }

    /**
     * Updates filter parameters for the texture.
     *
     * @param gl Current OpenGL context
     * @param smooth True to interpolate samples
     * @throws NullPointerException if context is null
     */
    void setFiltering(/*@Nonnull*/ final GL gl, final boolean smooth) {

        Check.notNull(gl, "GL cannot be null");

        final int mag;
        final int min;
        if (smooth) {
            mag = GL.GL_LINEAR;
            min = mipmap ? GL.GL_LINEAR_MIPMAP_NEAREST : GL.GL_LINEAR;
        } else {
            mag = GL.GL_NEAREST;
            min = mipmap ? GL.GL_NEAREST_MIPMAP_NEAREST : GL.GL_NEAREST;
        }

        setParameter(gl, GL.GL_TEXTURE_MAG_FILTER, mag);
        setParameter(gl, GL.GL_TEXTURE_MIN_FILTER, min);
    }

    /**
     * Changes a texture parameter for a 2D texture.
     *
     * @param gl Current OpenGL context, assumed not null
     * @param name Name of the parameter, assumed valid
     * @param value Value of the parameter, assumed valid
     */
    private void setParameter(/*@Nonnull*/ final GL gl, final int name, final int value) {
        gl.glTexParameteri(type, name, value);
    }
}
