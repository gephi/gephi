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


/**
 * Utility for drawing a stream of quads.
 */
/*@VisibleForTesting*/
public interface QuadPipeline {

    /**
     * Registers an {@link EventListener} with this {@link QuadPipeline}.
     *
     * @param listener Listener to register
     * @throws NullPointerException if listener is null
     */
    void addListener(/*@Nonnull*/ EventListener listener);

    /**
     * Adds a quad to this {@link QuadPipeline}.
     *
     * @param gl Current OpenGL context
     * @param quad Quad to add to pipeline
     * @throws NullPointerException if context or quad is null
     * @throws GLException if context is unexpected version
     */
    void addQuad(/*@Nonnull*/ GL gl, /*@Nonnull*/ Quad quad);

    /**
     * Starts a render cycle with this {@link QuadPipeline}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    void beginRendering(/*@Nonnull*/ GL gl);

    /**
     * Frees resources used by this {@link QuadPipeline}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    void dispose(/*@Nonnull*/ GL gl);

    /**
     * Finishes a render cycle with this {@link QuadPipeline}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    void endRendering(/*@Nonnull*/ GL gl);

    /**
     * Draws all vertices in this {@link QuadPipeline}.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    void flush(/*@Nonnull*/ GL gl);

    // TODO: Rename to `size`?
    /**
     * Returns number of quads in this {@link QuadPipeline}.
     *
     * @return Number of quads in this pipeline, not negative
     */
    /*@Nonnegative*/
    int getSize();

    /**
     * Checks if there aren't any quads in this {@link QuadPipeline}.
     *
     * @return True if there aren't any quads in this pipeline
     */
    boolean isEmpty();

    /**
     * Deregisters an {@link EventListener} from this {@link QuadPipeline}.
     *
     * @param listener Listener to deregister, ignored if null or unregistered
     */
    void removeListener(/*@CheckForNull*/ EventListener listener);

    /**
     * <i>Observer</i> of a {@link QuadPipeline}.
     */
    interface EventListener {

        /**
         * Responds to an event from a {@link QuadPipeline}.
         *
         * @param type Type of event
         * @throws NullPointerException if event type is null
         */
        void onQuadPipelineEvent(/*@Nonnull*/ EventType type);
    }

    /**
     * Kind of event.
     */
    enum EventType {

        /**
         * Pipeline is automatically flushing all queued quads, e.g., when it's full.
         */
        AUTOMATIC_FLUSH;
    }
}
