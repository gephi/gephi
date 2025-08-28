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

import com.jogamp.opengl.GL2GL3;


/**
 * Uniform for a {@code vec4}.
 */
/*@NotThreadSafe*/
final class Vec4Uniform extends Uniform {

    /**
     * Local copy of vector values.
     */
    /*@Nonnull*/
    final float[] value = new float[4];

    /**
     * Constructs a uniform vector.
     *
     * @param gl2gl3 Current OpenGL context
     * @param program OpenGL handle to shader program
     * @param name Name of the uniform in shader source code
     * @throws NullPointerException if context is null
     */
    Vec4Uniform(/*@Nonnull*/ final GL2GL3 gl,
                /*@Nonnegative*/ final int program,
                /*@Nonnull*/ final String name) {
        super(gl, program, name);
    }

    @Override
    void update(/*@Nonnull*/ final GL2GL3 gl) {
        Check.notNull(gl, "GL cannot be null");
        gl.glUniform4fv(location, 1, value, 0);
    }
}
