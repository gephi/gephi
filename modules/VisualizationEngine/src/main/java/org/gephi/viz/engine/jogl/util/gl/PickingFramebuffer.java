package org.gephi.viz.engine.jogl.util.gl;

import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_PACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;

import com.jogamp.opengl.GL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A single-sample offscreen framebuffer (RGBA8 texture color attachment) used for GPU picking: the
 * scene's pickable elements are drawn into it with a flat per-element id color, and a single pixel is
 * read back to identify the element under the cursor.
 * <p>
 * A dedicated non-multisampled target is required because the on-screen framebuffer is multisampled
 * (and uses sample-alpha-to-coverage), which would average neighbouring ids together.
 *
 * @author Eduardo Ramos
 */
public class PickingFramebuffer {

    private int fbo = -1;
    private int colorTexture = -1;
    private int width = 0;
    private int height = 0;
    private boolean unavailable = false;

    private final ByteBuffer pixel = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());

    /**
     * Ensures the framebuffer exists with the given size, (re)creating it if needed. Returns
     * {@code true} if the framebuffer is complete and ready to be used, {@code false} if it could not
     * be created (in which case picking should be considered unavailable).
     */
    public boolean ensureSize(GL gl, int w, int h) {
        if (unavailable || w <= 0 || h <= 0) {
            return false;
        }
        if (fbo != -1 && w == width && h == height) {
            return true;
        }

        dispose(gl);

        final int[] names = new int[1];
        gl.glGenTextures(1, names, 0);
        colorTexture = names[0];
        gl.glBindTexture(GL_TEXTURE_2D, colorTexture);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.glBindTexture(GL_TEXTURE_2D, 0);

        gl.glGenFramebuffers(1, names, 0);
        fbo = names[0];
        gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0);

        final int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);
        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            dispose(gl);
            unavailable = true;
            return false;
        }

        width = w;
        height = h;
        return true;
    }

    public void bind(GL gl) {
        gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    }

    /**
     * Reads the single pixel at the given framebuffer coordinates (origin bottom-left) and decodes
     * the stored element id, assuming it was encoded as {@code (id + 1)} in the RGB channels (so a
     * cleared/background pixel decodes to {@code -1}).
     *
     * @return the decoded element id, or {@code -1} if no element covers the pixel
     */
    public int readPixelId(GL gl, int x, int y) {
        gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);
        pixel.clear();
        gl.glReadPixels(x, y, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);

        final int r = pixel.get(0) & 0xFF;
        final int g = pixel.get(1) & 0xFF;
        final int b = pixel.get(2) & 0xFF;
        final int encoded = (r << 16) | (g << 8) | b;
        return encoded == 0 ? -1 : encoded - 1;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void dispose(GL gl) {
        if (fbo != -1) {
            gl.glDeleteFramebuffers(1, new int[] {fbo}, 0);
            fbo = -1;
        }
        if (colorTexture != -1) {
            gl.glDeleteTextures(1, new int[] {colorTexture}, 0);
            colorTexture = -1;
        }
        width = 0;
        height = 0;
    }
}
