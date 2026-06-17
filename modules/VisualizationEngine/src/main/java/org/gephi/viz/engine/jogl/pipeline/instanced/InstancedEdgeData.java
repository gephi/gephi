package org.gephi.viz.engine.jogl.pipeline.instanced;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.FloatBuffer;
import org.gephi.viz.engine.jogl.models.edgeline.directed.CommonEdgeLineDirected;
import org.gephi.viz.engine.jogl.models.edgeline.undirected.CommonEdgeLineUndirected;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractEdgeData;
import org.gephi.viz.engine.jogl.pipeline.common.EdgeWorldData;
import org.gephi.viz.engine.jogl.pipeline.common.NodeDataTextureStore;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLFunctions;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.util.structure.EdgesCallback;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class InstancedEdgeData extends AbstractEdgeData {

    private final int[] bufferName = new int[3];

    private static final int VERT_BUFFER_UNDIRECTED = 0;
    private static final int VERT_BUFFER_DIRECTED = 1;
    private static final int VERT_BUFFER_SELF_LOOP = 2;

    public InstancedEdgeData(final EdgesCallback edgesCallback, final NodesCallback nodesCallback,
                             final NodeDataTextureStore nodeDataTextureStore) {
        super(edgesCallback, nodesCallback, nodeDataTextureStore, true);
    }

    public void drawInstanced(GL3ES3 gl, RenderingLayer layer, EdgeWorldData data,
                              float[] mvpFloats) {
        refreshTime();
        if (edgesCallback.hasSelfLoop()) {
            drawSelfLoop(gl, data, layer, mvpFloats);
        }
        drawUndirected(gl, data, layer, mvpFloats);
        drawDirected(gl, data, layer, mvpFloats);
    }

    private void drawSelfLoop(GL3ES3 gl, EdgeWorldData data,
                              RenderingLayer layer,
                              float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerSelfLoop(gl, layer, data, mvpFloats);

        GLFunctions.drawInstanced(gl, 0, selfLoopMesh.vertexCount, instanceCount);
        GLFunctions.stopUsingProgram(gl);
        unsetupSelfLoopVertexArrayAttributes(gl);
    }

    private void drawUndirected(GL3ES3 gl, EdgeWorldData data,
                                RenderingLayer layer,
                                float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerUndirected(gl, layer, data, mvpFloats);

        GLFunctions.drawInstanced(gl, 0, CommonEdgeLineUndirected.VERTEX_COUNT, instanceCount);
        GLFunctions.stopUsingProgram(gl);
        unsetupUndirectedVertexArrayAttributes(gl);
    }

    private void drawDirected(GL3ES3 gl, EdgeWorldData data,
                              RenderingLayer layer,
                              float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerDirected(gl, layer, data, mvpFloats);

        GLFunctions.drawInstanced(gl, 0, CommonEdgeLineDirected.VERTEX_COUNT, instanceCount);
        GLFunctions.stopUsingProgram(gl);
        unsetupDirectedVertexArrayAttributes(gl);
    }

    @Override
    protected void initBuffers(GL gl) {
        super.initBuffers(gl);
        gl.glGenBuffers(bufferName.length, bufferName, 0);

        final FloatBuffer undirectedVertexData =
            GLBuffers.newDirectFloatBuffer(undirectedEdgeMesh.vertexData);
        vertexGLBufferUndirected =
            new GLBufferMutable(bufferName[VERT_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBufferUndirected.bind(gl);
        vertexGLBufferUndirected.init(gl, undirectedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBufferUndirected.unbind(gl);

        final FloatBuffer directedVertexData = GLBuffers.newDirectFloatBuffer(directedEdgeMesh.vertexData);
        vertexGLBufferDirected =
            new GLBufferMutable(bufferName[VERT_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBufferDirected.bind(gl);
        vertexGLBufferDirected.init(gl, directedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBufferDirected.unbind(gl);

        final FloatBuffer selfLoopVertexData =
            GLBuffers.newDirectFloatBuffer(selfLoopMesh.vertexData);
        vertexGLBufferSelfLoop =
            new GLBufferMutable(bufferName[VERT_BUFFER_SELF_LOOP], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBufferSelfLoop.bind(gl);
        vertexGLBufferSelfLoop.init(gl, selfLoopVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBufferSelfLoop.unbind(gl);
    }
}
