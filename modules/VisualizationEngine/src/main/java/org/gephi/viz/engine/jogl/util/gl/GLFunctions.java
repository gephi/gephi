package org.gephi.viz.engine.jogl.util.gl;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_BYTES;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL4;
import java.nio.IntBuffer;

public class GLFunctions {

    public static void glGenVertexArrays(GL2ES2 gl, int n, IntBuffer arrays) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glGenVertexArrays(n, arrays);
        } else {
            gl.getGLES2().glGenVertexArraysOES(n, arrays);
        }
    }

    public static void glDeleteVertexArrays(GL2ES2 gl, int n, IntBuffer arrays) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glDeleteVertexArrays(n, arrays);
        } else {
            gl.getGLES2().glDeleteVertexArraysOES(n, arrays);
        }
    }

    public static void glBindVertexArray(GL2ES2 gl, int array) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glBindVertexArray(array);
        } else {
            gl.getGLES2().glBindVertexArrayOES(array);
        }
    }

    public static void glUnbindVertexArray(GL2ES2 gl, int defaultVAO) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glBindVertexArray(defaultVAO);
        } else {
            gl.getGLES2().glBindVertexArrayOES(defaultVAO);
        }
    }

    public static void glVertexAttribDivisor(GL2ES2 gl, int index, int divisor) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glVertexAttribDivisor(index, divisor);
        } else if (gl.isGL2ES2()) {
            gl.getGLES2().glVertexAttribDivisor(index, divisor);
        }
    }

    public static String glGetStringi(GL2ES3 gl, int name, int index) {
        if (gl.isGL2GL3()) {
            return gl.getGL2GL3().glGetStringi(name, index);
        } else {
            return gl.getGL3ES3().glGetStringi(name, index);
        }
    }

    public static void stopUsingProgram(GL2ES2 gl) {
        gl.glUseProgram(0);
    }

    public static void drawArraysSingleInstance(GL2ES2 gl, int firstVertexIndex, int vertexCount) {
        gl.glDrawArrays(GL_TRIANGLES, firstVertexIndex, vertexCount);
    }

    public static void drawInstanced(GL2ES3 gl, int vertexOffset, int vertexCount, int instanceCount) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glDrawArraysInstanced(GL_TRIANGLES, vertexOffset, vertexCount, instanceCount);
    }

    public static void drawIndirect(GL4 gl, int instanceCount, int instancesOffset) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glMultiDrawArraysIndirect(GL_TRIANGLES, (long) instancesOffset * INDIRECT_DRAW_COMMAND_BYTES, instanceCount,
            0);
    }
}
