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


/**
 * {@link QuadPipeline} for use with OpenGL 1.0.
 */
/*@VisibleForTesting*/
/*@NotThreadSafe*/
public final class QuadPipelineGL10 extends AbstractQuadPipeline {

    /**
     * Number of vertices per primitive.
     */
    /*@Nonnegative*/
    private static final int VERTS_PER_PRIM = 4;

    /**
     * Number of primitives per quad.
     */
    /*@Nonnegative*/
    private static final int PRIMS_PER_QUAD = 1;

    /**
     * Constructs a {@link QuadPipelineGL10}.
     */
    /*@VisibleForTesting*/
    public QuadPipelineGL10() {
        super(VERTS_PER_PRIM, PRIMS_PER_QUAD);
    }

    @Override
    protected void doFlush(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        final GL2 gl2 = gl.getGL2();

        gl2.glBegin(GL2.GL_QUADS);
        try {
            rewind();
            final int size = getSize();
            for (int q = 0; q < size; ++q) {
                for (int v = 0; v < VERTS_PER_QUAD; ++v) {
                    gl2.glVertex3f(getFloat(), getFloat(), getFloat());
                    gl2.glTexCoord2f(getFloat(), getFloat());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            gl2.glEnd();
            clear();
        }
    }
}
