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


/**
 * Utility for computing projections.
 */
/*@NotThreadSafe*/
final class Projection {

    /**
     * Prevents instantiation.
     */
    private Projection() {
        // empty
    }

    /**
     * Computes an orthographic projection matrix.
     *
     * @param v Computed matrix values, in row-major order
     * @param width Width of current OpenGL viewport
     * @param height Height of current OpenGL viewport
     * @throws NullPointerException if array is null
     * @throws IllegalArgumentException if width or height is negative
     */
    static void orthographic(/*@Nonnull*/ final float[] v,
                             /*@Nonnegative*/ final int width,
                             /*@Nonnegative*/ final int height) {

        Check.notNull(v, "Matrix cannot be null");
        Check.argument(width >= 0, "Width cannot be negative");
        Check.argument(height >= 0, "Height cannot be negative");

        // Zero out
        for (int i = 0; i < 16; ++i) {
            v[i] = 0;
        }

        // Translate to origin
        v[3] = -1;
        v[7] = -1;

        // Scale to unit cube
        v[0] = 2f / width;
        v[5] = 2f / height;
        v[10] = -1;
        v[15] = 1;
    }
}
