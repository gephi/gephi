package org.gephi.viz.engine.jogl.pipeline.arrays;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
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
import org.gephi.viz.engine.util.ArrayUtils;
import org.gephi.viz.engine.util.structure.EdgesCallback;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class ArrayDrawEdgeData extends AbstractEdgeData {

    private final int[] bufferName = new int[3];

    private static final int VERT_BUFFER_UNDIRECTED = 0;
    private static final int VERT_BUFFER_DIRECTED = 1;
    private static final int VERT_BUFFER_SELFLOOP = 2;

    public ArrayDrawEdgeData(final EdgesCallback edgesCallback, final NodesCallback nodesCallback,
                             final NodeDataTextureStore nodeDataTextureStore) {
        super(edgesCallback, nodesCallback, nodeDataTextureStore, false);
    }

    public void drawArrays(GL2ES2 gl, RenderingLayer layer, EdgeWorldData data, float[] mvpFloats) {
        refreshTime();
        if (edgesCallback.hasSelfLoop()) {
            drawSelfLoop(gl, data, layer, mvpFloats);
        }
        drawUndirected(gl, data, layer, mvpFloats);
        drawDirected(gl, data, layer, mvpFloats);
    }

    /**
     * Draws {@code count} edges (each made of {@code vertsPerEdge} vertices) from the bound element
     * texture starting at element index {@code start}, in vertex-buffer-sized batches. The element
     * offset uniform is updated per batch so the shader resolves the correct texel.
     */
    private void drawInBatches(GL2ES2 gl, int start, int count, int vertsPerEdge, int batchSize) {
        final int end = start + count;
        for (int edgeBase = start; edgeBase < end; edgeBase += batchSize) {
            final int drawBatchCount = Math.min(end - edgeBase, batchSize);
            setElementOffset(gl, edgeBase);
            GLFunctions.drawArraysSingleInstance(gl, 0, vertsPerEdge * drawBatchCount);
        }
    }

    private void drawSelfLoop(GL2ES2 gl, EdgeWorldData data,
                              RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerSelfLoop(gl, layer, data, mvpFloats);

        final boolean renderingUnselectedEdges = layer.getLevel() == 1;
        final int start = renderingUnselectedEdges ? 0 : selfLoopCounter.unselectedCountToDraw;

        drawInBatches(gl, start, instanceCount, selfLoopMesh.vertexCount, BATCH_SELFLOOP_EDGES_SIZE);

        GLFunctions.stopUsingProgram(gl);
        unsetupSelfLoopVertexArrayAttributes(gl);
    }

    private void drawUndirected(GL2ES2 gl, EdgeWorldData data,
                                RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerUndirected(gl, layer, data, mvpFloats);

        final boolean renderingUnselectedEdges = layer.getLevel() == 1;
        final int start = renderingUnselectedEdges ? 0 : undirectedInstanceCounter.unselectedCountToDraw;

        drawInBatches(gl, start, instanceCount, CommonEdgeLineUndirected.VERTEX_COUNT, BATCH_EDGES_SIZE);

        GLFunctions.stopUsingProgram(gl);
        unsetupUndirectedVertexArrayAttributes(gl);
    }

    private void drawDirected(GL2ES2 gl, EdgeWorldData data,
                              RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerDirected(gl, layer, data, mvpFloats);

        final boolean renderingUnselectedEdges = layer.getLevel() == 1;
        final int start = renderingUnselectedEdges ? 0 : directedInstanceCounter.unselectedCountToDraw;

        drawInBatches(gl, start, instanceCount, CommonEdgeLineDirected.VERTEX_COUNT, BATCH_EDGES_SIZE);

        GLFunctions.stopUsingProgram(gl);
        unsetupDirectedVertexArrayAttributes(gl);
    }

    @Override
    protected void initBuffers(GL gl) {
        super.initBuffers(gl);
        gl.glGenBuffers(bufferName.length, bufferName, 0);

        {
            float[] undirectedVertexDataArray = new float[undirectedEdgeMesh.vertexData.length * BATCH_EDGES_SIZE];
            System.arraycopy(undirectedEdgeMesh.vertexData, 0, undirectedVertexDataArray, 0,
                undirectedEdgeMesh.vertexData.length);
            ArrayUtils.repeat(undirectedVertexDataArray, 0, undirectedEdgeMesh.vertexData.length, BATCH_EDGES_SIZE);

            final FloatBuffer undirectedVertexData = GLBuffers.newDirectFloatBuffer(undirectedVertexDataArray);

            vertexGLBufferUndirected =
                new GLBufferMutable(bufferName[VERT_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
            vertexGLBufferUndirected.bind(gl);
            vertexGLBufferUndirected.init(gl, undirectedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
            vertexGLBufferUndirected.unbind(gl);
        }

        {
            float[] directedVertexDataArray = new float[directedEdgeMesh.vertexData.length * BATCH_EDGES_SIZE];
            System.arraycopy(directedEdgeMesh.vertexData, 0, directedVertexDataArray, 0,
                directedEdgeMesh.vertexData.length);
            ArrayUtils.repeat(directedVertexDataArray, 0, directedEdgeMesh.vertexData.length, BATCH_EDGES_SIZE);

            final FloatBuffer directedVertexData = GLBuffers.newDirectFloatBuffer(directedVertexDataArray);

            vertexGLBufferDirected =
                new GLBufferMutable(bufferName[VERT_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
            vertexGLBufferDirected.bind(gl);
            vertexGLBufferDirected.init(gl, directedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
            vertexGLBufferDirected.unbind(gl);
        }

        {
            float[] selfLoopVertexDataArray = new float[selfLoopMesh.vertexData.length * BATCH_SELFLOOP_EDGES_SIZE];
            System.arraycopy(selfLoopMesh.vertexData, 0, selfLoopVertexDataArray, 0,
                selfLoopMesh.vertexData.length);
            ArrayUtils.repeat(selfLoopVertexDataArray, 0, selfLoopMesh.vertexData.length, BATCH_SELFLOOP_EDGES_SIZE);

            final FloatBuffer selfLoopVertexData = GLBuffers.newDirectFloatBuffer(selfLoopVertexDataArray);

            vertexGLBufferSelfLoop =
                new GLBufferMutable(bufferName[VERT_BUFFER_SELFLOOP], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
            vertexGLBufferSelfLoop.bind(gl);
            vertexGLBufferSelfLoop.init(gl, selfLoopVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
            vertexGLBufferSelfLoop.unbind(gl);
        }
    }
}
