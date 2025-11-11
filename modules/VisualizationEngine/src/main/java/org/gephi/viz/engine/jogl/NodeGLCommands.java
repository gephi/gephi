package org.gephi.viz.engine.jogl;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL4;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_BYTES;

public class NodeGLCommands {
    static public void stopUsingProgram(GL2ES2 gl) {
        gl.glUseProgram(0);
    }
    static  public void drawArraysSingleInstance(GL2ES2 gl, int firstVertexIndex, int vertexCount) {
        gl.glDrawArrays(GL_TRIANGLES, firstVertexIndex, vertexCount);
    }
    static public void drawInstanced(GL2ES3 gl, int vertexOffset, int vertexCount, int instanceCount) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glDrawArraysInstanced(GL_TRIANGLES, vertexOffset, vertexCount, instanceCount);
    }

    static public void drawIndirect(GL4 gl, int instanceCount, int instancesOffset) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glMultiDrawArraysIndirect(GL_TRIANGLES, (long) instancesOffset * INDIRECT_DRAW_COMMAND_BYTES, instanceCount,
                0);
    }
}
