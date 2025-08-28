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
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLExtensions;
import com.jogamp.opengl.GLProfile;


/**
 * Utility for working with {@link QuadPipeline}'s.
 */
/*ThreadSafe*/
public final class QuadPipelines {

    /**
     * Prevents instantiation.
     */
    private QuadPipelines() {
        // pass
    }

    /**
     * Creates a {@link QuadPipeline} based on the current OpenGL context.
     *
     * @param gl Current OpenGL context
     * @param program Shader program to use, or zero to use default
     * @return New quad pipeline for the version of OpenGL in use, not null
     * @throws NullPointerException if context is null
     * @throws IllegalArgumentException if shader program is negative
     * @throws UnsupportedOperationException if GL is unsupported
     */
    /*@Nonnull*/
    public QuadPipeline get(/*@Nonnull*/ final GL gl,
                            /*@Nonnegative*/ final int program) {

        Check.notNull(gl, "Context cannot be null");
        Check.argument(program >= 0, "Program cannot be negative");

        final GLProfile profile = gl.getGLProfile();

        if (profile.isGL3()) {
            final GL3 gl3 = gl.getGL3();
            return new QuadPipelineGL30(gl3, program);
        } else if (profile.isGL2()) {
            final GL2 gl2 = gl.getGL2();
            if (gl2.isExtensionAvailable(GLExtensions.VERSION_1_5)) {
                return new QuadPipelineGL15(gl2);
            } else if (gl2.isExtensionAvailable("GL_VERSION_1_1")) {
                return new QuadPipelineGL11();
            } else {
                return new QuadPipelineGL10();
            }
        } else {
            throw new UnsupportedOperationException("Profile currently unsupported");
        }
    }
}
