package org.gephi.viz.engine.jogl.util.gl;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.IntBuffer;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

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
        GLFunctions.glUnbindVertexArray(gl);
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
