package org.gephi.viz.engine.jogl.util.gl;

import static com.jogamp.opengl.GL2ES3.GL_VERTEX_ARRAY_BINDING;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.IntBuffer;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

/**
 * VAO abstraction that checks for actual support of VAOs and emulates it if not supported.
 *
 * @author Eduardo Ramos
 */
public class GLVertexArrayObject {

    @FunctionalInterface
    public interface Configurer {
        void configure(GL2ES2 gl);
    }

    private final boolean vaoSupported;
    private final Configurer configurer;
    private final int[] attributeLocations;
    private final int[] instancedAttributeLocations;

    private boolean initialized;
    private int arrayId = -1;
    private final int[] previousArrayId = new int[1];

    public GLVertexArrayObject(OpenGLOptions openGLOptions,
                               int[] usedAttributeLocations,
                               int[] instancedAttributeLocations,
                               Configurer configurer) {
        this.vaoSupported = openGLOptions.isVAOSupported();
        this.attributeLocations =
            usedAttributeLocations != null ? usedAttributeLocations.clone() : new int[0];
        this.instancedAttributeLocations =
            instancedAttributeLocations != null ? instancedAttributeLocations.clone() : new int[0];
        this.configurer = configurer;
    }

    private void init(GL2ES2 gl) {
        if (vaoSupported) {
            IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);

            GLFunctions.glGenVertexArrays(gl, 1, vertexArrayName);
            arrayId = vertexArrayName.get(0);

            // Note: important to store the previous value of active VAO.
            // The OpenGL pipeline always has an active default VAO,
            // and we should restore the status to that one when doing the call to unbind
            // If we fail to restore it, other renderers such as JOGL text will fail and draw nothing
            gl.glGetIntegerv(GL_VERTEX_ARRAY_BINDING, previousArrayId, 0);

            bind(gl);
            configureAll(gl);
            unbind(gl);
        }

        initialized = true;
    }

    public void use(GL2ES2 gl) {
        if (!initialized) {
            init(gl);
        }

        if (vaoSupported) {
            bind(gl);
        } else {
            configureAll(gl);
        }
    }

    public void stopUsing(GL2ES2 gl) {
        if (vaoSupported) {
            unbind(gl);
        } else {
            unconfigureEnabledAttributes(gl);
        }
    }

    private void configureAll(GL2ES2 gl) {
        configurer.configure(gl);
        configureEnabledAttributes(gl);
    }

    private void bind(GL2ES2 gl) {
        GLFunctions.glBindVertexArray(gl, arrayId);
    }

    private void unbind(GL2ES2 gl) {
        GLFunctions.glUnbindVertexArray(gl, previousArrayId[0]);
    }

    private void configureEnabledAttributes(GL2ES2 gl) {
        for (int attributeLocation : attributeLocations) {
            gl.glEnableVertexAttribArray(attributeLocation);
        }
        for (int instancedAttributeLocation : instancedAttributeLocations) {
            GLFunctions.glVertexAttribDivisor(gl, instancedAttributeLocation, 1);
        }
    }

    private void unconfigureEnabledAttributes(GL2ES2 gl) {
        for (int attributeLocation : attributeLocations) {
            gl.glDisableVertexAttribArray(attributeLocation);
        }
        for (int instancedAttributeLocation : instancedAttributeLocations) {
            GLFunctions.glVertexAttribDivisor(gl, instancedAttributeLocation, 0);
        }
    }

    public void destroy(GL2ES2 gl) {
        if (vaoSupported && arrayId != -1) {
            IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);
            vertexArrayName.put(0, arrayId);
            GLFunctions.glDeleteVertexArrays(gl, 1, vertexArrayName);
            arrayId = -1;
        }
        initialized = false;
    }

}
