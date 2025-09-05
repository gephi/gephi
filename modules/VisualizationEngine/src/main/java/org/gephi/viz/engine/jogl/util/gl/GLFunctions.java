package org.gephi.viz.engine.jogl.util.gl;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import java.nio.IntBuffer;

public class GLFunctions {

    public static void glGenVertexArrays(GL2ES2 gl, int n, IntBuffer arrays) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glGenVertexArrays(n, arrays);
        } else {
            gl.getGLES2().glGenVertexArraysOES(n, arrays);
        }
    }

    public static void glBindVertexArray(GL2ES2 gl, int array) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glBindVertexArray(array);
        } else {
            gl.getGLES2().glBindVertexArrayOES(array);
        }
    }

    public static void glUnbindVertexArray(GL2ES2 gl) {
        if (gl.isGL2GL3()) {
            gl.getGL2GL3().glBindVertexArray(0);
        } else {
            gl.getGLES2().glBindVertexArrayOES(0);
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
}
