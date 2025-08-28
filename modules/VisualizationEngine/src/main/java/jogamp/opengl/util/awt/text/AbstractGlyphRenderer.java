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
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.TextureCoords;

import java.util.ArrayList;
import java.util.List;


/**
 * Skeletal implementation of {@link GlyphRenderer}.
 */
abstract class AbstractGlyphRenderer implements GlyphRenderer, QuadPipeline.EventListener {

    // Default color
    private static float DEFAULT_RED = 1.0f;
    private static float DEFAULT_GREEN = 1.0f;
    private static float DEFAULT_BLUE = 1.0f;
    private static float DEFAULT_ALPHA = 1.0f;

    /**
     * Listeners to send events to.
     */
    /*@Nonnull*/
    private final List<EventListener> listeners = new ArrayList<EventListener>();

    /**
     * Quad to send to pipeline.
     */
    /*@Nonnull*/
    private final Quad quad = new Quad();

    /**
     * Buffer of quads.
     */
    /*@CheckForNull*/
    private QuadPipeline pipeline = null;

    /**
     * Whether pipeline needs to be flushed.
     */
    private boolean pipelineDirty = true;

    /**
     * True if between begin and end calls.
     */
    private boolean inRenderCycle = false;

    /**
     * True if orthographic.
     */
    private boolean orthoMode = false;

    /**
     * Red component of color.
     */
    private float r = DEFAULT_RED;

    /**
     * Green component of color.
     */
    private float g = DEFAULT_GREEN;

    /**
     * Blue component of color.
     */
    private float b = DEFAULT_BLUE;

    /**
     * Alpha component of color.
     */
    private float a = DEFAULT_ALPHA;

    /**
     * True if color needs to be updated.
     */
    private boolean colorDirty = true;

    /**
     * Transformation matrix for 3D mode.
     */
    /*@Nonnull*/
    private final float[] transform = new float[16];

    /**
     * Whether transformation matrix is in row-major order instead of column-major.
     */
    private boolean transposed = false;

    // TODO: Should `transformDirty` start out as true?
    /**
     * Whether transformation matrix needs to be updated.
     */
    private boolean transformDirty = false;

    /**
     * Constructs an {@link AbstractGlyphRenderer}.
     */
    AbstractGlyphRenderer() {
        // empty
    }

    @Override
    public final void addListener(/*@Nonnull*/ final EventListener listener) {

        Check.notNull(listener, "Listener cannot be null");

        listeners.add(listener);
    }

    @Override
    public final void beginRendering(/*@Nonnull*/ final GL gl,
                                     final boolean ortho,
                                     /*@Nonnegative*/ final int width,
                                     /*@Nonnegative*/ final int height,
                                     final boolean disableDepthTest) {

        Check.notNull(gl, "GL cannot be null");
        Check.argument(width >= 0, "Width cannot be negative");
        Check.argument(height >= 0, "Height cannot be negative");

        // Perform hook
        doBeginRendering(gl, ortho, width, height, disableDepthTest);

        // Store text renderer state
        inRenderCycle = true;
        orthoMode = ortho;

        // Make sure the pipeline is made
        if (pipelineDirty) {
            setPipeline(gl, doCreateQuadPipeline(gl));
        }

        // Pass to quad renderer
        pipeline.beginRendering(gl);

        // Make sure color is correct
        if (colorDirty) {
            doSetColor(gl, r, g, b, a);
            colorDirty = false;
        }

        // Make sure transform is correct
        if (transformDirty) {
            doSetTransform3d(gl, transform, transposed);
            transformDirty = false;
        }
    }

    /**
     * Requests that the pipeline be replaced on the next call to {@link #beginRendering}.
     */
    protected final void dirtyPipeline() {
        pipelineDirty = true;
    }

    @Override
    public final void dispose(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        doDispose(gl);
        listeners.clear();
        pipeline.dispose(gl);
    }

    /**
     * Actually starts a render cycle.
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
    protected abstract void doBeginRendering(/*@Nonnull*/ final GL gl,
                                             final boolean ortho,
                                             /*@Nonnegative*/ final int width,
                                             /*@Nonnegative*/ final int height,
                                             final boolean disableDepthTest);

    /**
     * Actually creates the quad pipeline for rendering quads.
     *
     * @param gl Current OpenGL context
     * @return Quad pipeline to render quads with
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    protected abstract QuadPipeline doCreateQuadPipeline(/*@Nonnull*/ final GL gl);

    /**
     * Actually frees resources used by the renderer.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    protected abstract void doDispose(/*@Nonnull*/ final GL gl);

    /**
     * Actually finishes a render cycle.
     *
     * @param gl Current OpenGL context
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    protected abstract void doEndRendering(/*@Nonnull*/ final GL gl);

    /**
     * Actually changes the color when user calls {@link #setColor}.
     *
     * @param gl Current OpenGL context
     * @param r Red component of color
     * @param g Green component of color
     * @param b Blue component of color
     * @param a Alpha component of color
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     */
    protected abstract void doSetColor(/*@Nonnull*/ final GL gl,
                                       float r,
                                       float g,
                                       float b,
                                       float a);

    /**
     * Actually changes the MVP matrix when using an arbitrary projection.
     *
     * @param gl Current OpenGL context
     * @param value Matrix as float array
     * @param transpose True if in row-major order
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     * @throws IndexOutOfBoundsException if length of value is less than sixteen
     */
    protected abstract void doSetTransform3d(/*@Nonnull*/ GL gl,
                                             /*@Nonnull*/ float[] value,
                                             boolean transpose);

    /**
     * Actually changes the MVP matrix when using orthographic projection.
     *
     * @param gl Current OpenGL context
     * @param width Width of viewport
     * @param height Height of viewport
     * @throws NullPointerException if context is null
     * @throws GLException if context is unexpected version
     * @throws IllegalArgumentException if width or height is negative
     */
    protected abstract void doSetTransformOrtho(/*@Nonnull*/ GL gl,
                                                /*@Nonnegative*/ int width,
                                                /*@Nonnegative*/ int height);

    @Override
    public final float drawGlyph(/*@Nonnull*/ final GL gl,
                                 /*@Nonnull*/ final Glyph glyph,
                                 /*@CheckForSigned*/ final float x,
                                 /*@CheckForSigned*/ final float y,
                                 /*@CheckForSigned*/ final float z,
                                 /*@CheckForSigned*/ final float scale,
                                 /*@Nonnull*/ final TextureCoords coords) {

        Check.notNull(gl, "GL cannot be null");
        Check.notNull(glyph, "Glyph cannot be null");
        Check.notNull(coords, "Texture coordinates cannot be null");

        // Compute position and size
        quad.xl = x + (scale * glyph.kerning);
        quad.xr = quad.xl + (scale * glyph.width);
        quad.yb = y - (scale * glyph.descent);
        quad.yt = quad.yb + (scale * glyph.height);
        quad.z = z;
        quad.sl = coords.left();
        quad.sr = coords.right();
        quad.tb = coords.bottom();
        quad.tt = coords.top();

        // Draw quad
        pipeline.addQuad(gl, quad);

        // Return distance to next character
        return glyph.advance;
    }

    @Override
    public final void endRendering(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

        // Store text renderer state
        inRenderCycle = false;

        // Pass to quad renderer
        pipeline.endRendering(gl);

        // Perform hook
        doEndRendering(gl);
    }

    /**
     * Fires an event to all observers.
     *
     * @param type Kind of event
     * @throws NullPointerException if type is null
     */
    protected final void fireEvent(/*@Nonnull*/ final EventType type) {

        Check.notNull(type, "Event type cannot be null");

        for (final EventListener listener : listeners) {
            assert listener != null : "addListener rejects null";
            listener.onGlyphRendererEvent(type);
        }
    }

    @Override
    public final void flush(/*@Nonnull*/ final GL gl) {

        Check.notNull(gl, "GL cannot be null");

// Commented to work in Jzy3D (uncomment won't prevent tests to pass)
// Check.state(inRenderCycle, "Must be in render cycle");

        pipeline.flush(gl);
        gl.glFlush();
    }

    /**
     * Determines if a color is the same one that is stored.
     *
     * @param r Red component of color
     * @param g Green component of color
     * @param b Blue component of color
     * @param a Alpha component of color
     * @return True if each component matches
     */
    final boolean hasColor(final float r, final float g, final float b, final float a) {
        return (this.r == r) && (this.g == g) && (this.b == b) && (this.a == a);
    }

    // TODO: Rename to `isOrthographic`?
    /**
     * Checks if this {@link GlyphRenderer} using an orthographic projection.
     *
     * @return True if this renderer is using an orthographic projection
     */
    final boolean isOrthoMode() {
        return orthoMode;
    }

    @Override
    public final void onQuadPipelineEvent(/*@Nonnull*/ final QuadPipeline.EventType type) {

        Check.notNull(type, "Event type cannot be null");

        if (type == QuadPipeline.EventType.AUTOMATIC_FLUSH) {
            fireEvent(EventType.AUTOMATIC_FLUSH);
        }
    }

    @Override
    public final void setColor(final float r, final float g, final float b, final float a) {

        // Check if already has the color
        if (hasColor(r, g, b, a)) {
            return;
        }

        // Render any outstanding quads first
        if (pipeline!=null && !pipeline.isEmpty()) {
            fireEvent(EventType.AUTOMATIC_FLUSH);
            final GL gl = GLContext.getCurrentGL();
            flush(gl);
        }

        // Store the color
        this.r = r;
        this.g = g;
        this.b = g;
        this.a = a;

        // Change the color
        if (inRenderCycle) {
            final GL gl = GLContext.getCurrentGL();
            doSetColor(gl, r, g, b, a);
        } else {
            colorDirty = true;
        }
    }

    /**
     * Changes the quad pipeline.
     *
     * @param gl Current OpenGL context
     * @param pipeline Quad pipeline to change to
     */
    private final void setPipeline(/*@Nonnull*/ final GL gl,
                                   /*@Nonnull*/ final QuadPipeline pipeline) {

        assert gl != null : "GL should not be null";
        assert pipeline != null : "Pipeline should not be null";

        final QuadPipeline oldPipeline = this.pipeline;
        final QuadPipeline newPipeline = pipeline;

        // Remove the old pipeline
        if (oldPipeline != null) {
            oldPipeline.removeListener(this);
            oldPipeline.dispose(gl);
            this.pipeline = null;
        }

        // Store the new pipeline
        newPipeline.addListener(this);
        this.pipeline = newPipeline;
        pipelineDirty = false;
    }

    @Override
    public final void setTransform(/*@Nonnull*/ final float[] value, final boolean transpose) {

        Check.notNull(value, "Transform value cannot be null");
        Check.state(!orthoMode, "Must be in 3D mode");

        // Render any outstanding quads first
        if (!pipeline.isEmpty()) {
            fireEvent(EventType.AUTOMATIC_FLUSH);
            final GL gl = GLContext.getCurrentGL();
            flush(gl);
        }

        // Store the transform
        System.arraycopy(value, 0, this.transform, 0, value.length);
        this.transposed = transpose;

        // Change the transform
        if (inRenderCycle) {
            final GL gl = GLContext.getCurrentGL();
            doSetTransform3d(gl, value, transpose);
        } else {
            transformDirty = true;
        }
    }
}
