package org.gephi.viz.engine.jogl.util.gl;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.IntBuffer;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

import static com.jogamp.opengl.GL2ES3.GL_VERTEX_ARRAY_BINDING;

/**
 * VAO abstraction that checks for actual support of VAOs and emulates it if not supported.
 *
 * @author Eduardo Ramos
 */
public abstract class GLVertexArrayObject {

    private final boolean vaoSupported;

    private int[] attributeLocations;
    private int[] instancedAttributeLocations;
    private int arrayId = -1;
    private int[] previousArrayId = new int[1];

    public GLVertexArrayObject(GLCapabilitiesSummary capabilities, OpenGLOptions openGLOptions) {
        vaoSupported = capabilities.isVAOSupported(openGLOptions);
    }

    private void init(GL2ES2 gl) {
        attributeLocations = getUsedAttributeLocations();
        if (attributeLocations == null) {
            attributeLocations = new int[0];
        } else {
            attributeLocations = attributeLocations.clone();
        }

        instancedAttributeLocations = getInstancedAttributeLocations();
        if (instancedAttributeLocations == null) {
            instancedAttributeLocations = new int[0];
        } else {
            instancedAttributeLocations = instancedAttributeLocations.clone();
        }

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
    }

    public void use(GL2ES2 gl) {
        if (attributeLocations == null) {
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
        configure(gl);
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

    protected abstract void configure(GL2ES2 gl);

    protected abstract int[] getUsedAttributeLocations();

    protected abstract int[] getInstancedAttributeLocations();

}
