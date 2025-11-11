package org.gephi.viz.engine.jogl;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3ES3;

import static com.jogamp.opengl.GL.GL_TRIANGLES;

public class EdgeGLCommands {
    static public void stopUsingProgram(GL2ES2 gl) {
        gl.glUseProgram(0);
    }

    public static void drawArraysMultipleInstance(GL2ES2 gl, final int drawBatchCount, final int vertexCount) {
        if (drawBatchCount <= 0) {
            return;
        }
        //Multiple lines, attributes must be in the buffer once per vertex count:
        gl.glDrawArrays(GL_TRIANGLES, 0, vertexCount * drawBatchCount);
    }

    public static void drawInstanced(GL3ES3 gl, int instanceCount, int vertexCount) {
        gl.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, instanceCount);
    }

}
