package org.gephi.viz.engine.jogl.pipeline.instanced;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.FloatBuffer;
import org.gephi.graph.api.Edge;
import org.gephi.viz.engine.jogl.models.edgeline.directed.CommonEdgeLineDirected;
import org.gephi.viz.engine.jogl.models.edgeline.undirected.CommonEdgeLineUndirected;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractEdgeData;
import org.gephi.viz.engine.jogl.pipeline.common.EdgeWorldData;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLFunctions;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.util.structure.EdgesCallback;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class InstancedEdgeData extends AbstractEdgeData {

    private final int[] bufferName = new int[9];

    private static final int VERT_BUFFER_UNDIRECTED = 0;
    private static final int VERT_BUFFER_DIRECTED = 1;
    private static final int ATTRIBS_BUFFER_UNDIRECTED = 2;
    private static final int ATTRIBS_BUFFER_UNDIRECTED_SECONDARY = 3;
    private static final int ATTRIBS_BUFFER_DIRECTED = 4;
    private static final int ATTRIBS_BUFFER_DIRECTED_SECONDARY = 5;

    private static final int VERT_BUFFER_SELF_LOOP = 6;
    private static final int ATTRIBS_BUFFER_SELF_LOOP = 7;
    private static final int ATTRIBS_BUFFER_SELF_LOOP_SECONDARY = 8;

    public InstancedEdgeData(final EdgesCallback edgesCallback, final NodesCallback nodesCallback) {
        super(edgesCallback, nodesCallback, true, true);
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

        //Initialize for batch edges size:
        attributesGLBufferDirected =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferDirected.bind(gl);
        attributesGLBufferDirected.init(gl, (long) ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferDirected.unbind(gl);

        attributesGLBufferDirectedSecondary =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_DIRECTED_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferDirectedSecondary.bind(gl);
        attributesGLBufferDirectedSecondary.init(gl, (long) ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferDirectedSecondary.unbind(gl);

        attributesGLBufferUndirected =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferUndirected.bind(gl);
        attributesGLBufferUndirected.init(gl, (long) ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferUndirected.unbind(gl);

        attributesGLBufferUndirectedSecondary =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_UNDIRECTED_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferUndirectedSecondary.bind(gl);
        attributesGLBufferUndirectedSecondary.init(gl, (long) ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferUndirectedSecondary.unbind(gl);

        attributesGLBufferSelfLoop =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_SELF_LOOP], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferSelfLoop.bind(gl);
        attributesGLBufferSelfLoop.init(gl, (long) ATTRIBS_STRIDE_SELFLOOP * Float.BYTES * BATCH_SELFLOOP_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferSelfLoop.unbind(gl);

        attributesGLBufferSelfLoopSecondary =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_SELF_LOOP_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferSelfLoopSecondary.bind(gl);
        attributesGLBufferSelfLoopSecondary.init(gl,
            (long) ATTRIBS_STRIDE_SELFLOOP * Float.BYTES * BATCH_SELFLOOP_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferSelfLoopSecondary.unbind(gl);

    }

    public void updateBuffers(GL gl) {
        {
            final FloatBuffer buf = attributesBuffer.floatBuffer();


            buf.limit(undirectedInstanceCounter.unselectedCount * ATTRIBS_STRIDE);
            buf.position(0);

            attributesGLBufferUndirectedSecondary.bind(gl);
            attributesGLBufferUndirectedSecondary.updateWithOrphaning(gl, buf);
            attributesGLBufferUndirectedSecondary.unbind(gl);

            int offset = buf.limit();
            buf.limit(offset + undirectedInstanceCounter.selectedCount * ATTRIBS_STRIDE);
            buf.position(offset);

            attributesGLBufferUndirected.bind(gl);
            attributesGLBufferUndirected.updateWithOrphaning(gl, buf);
            attributesGLBufferUndirected.unbind(gl);

            offset = buf.limit();
            buf.limit(offset + directedInstanceCounter.unselectedCount * ATTRIBS_STRIDE);
            buf.position(offset);

            attributesGLBufferDirectedSecondary.bind(gl);
            attributesGLBufferDirectedSecondary.updateWithOrphaning(gl, buf);
            attributesGLBufferDirectedSecondary.unbind(gl);

            offset = buf.limit();
            buf.limit(offset + directedInstanceCounter.selectedCount * ATTRIBS_STRIDE);
            buf.position(offset);

            attributesGLBufferDirected.bind(gl);
            attributesGLBufferDirected.updateWithOrphaning(gl, buf);
            attributesGLBufferDirected.unbind(gl);
        }

        if (edgesCallback.hasSelfLoop()) {
            final FloatBuffer selfLoopBuf = selfLoopAttributesBuffer.floatBuffer();

            selfLoopBuf.limit(selfLoopCounter.unselectedCount * ATTRIBS_STRIDE_SELFLOOP);
            selfLoopBuf.position(0);

            attributesGLBufferSelfLoopSecondary.bind(gl);
            attributesGLBufferSelfLoopSecondary.updateWithOrphaning(gl, selfLoopBuf);
            attributesGLBufferSelfLoopSecondary.unbind(gl);

            int offset = selfLoopBuf.limit();
            selfLoopBuf.limit(offset + selfLoopCounter.selectedCount * ATTRIBS_STRIDE_SELFLOOP);
            selfLoopBuf.position(offset);

            attributesGLBufferSelfLoop.bind(gl);
            attributesGLBufferSelfLoop.updateWithOrphaning(gl, selfLoopBuf);
            attributesGLBufferSelfLoop.unbind(gl);
        }
        undirectedInstanceCounter.promoteCountToDraw();
        directedInstanceCounter.promoteCountToDraw();
        selfLoopCounter.promoteCountToDraw();
    }

    @Override
    protected void updateData(final GraphSelection selection) {
        final int totalEdges = edgesCallback.getCount();

        attributesBuffer.ensureCapacity(totalEdges * ATTRIBS_STRIDE);

        final FloatBuffer attribsDirectBuffer = attributesBuffer.floatBuffer();
        final Edge[] visibleEdgesArray = edgesCallback.getEdgesArray();
        final float[] edgeWeightsArray = edgesCallback.getEdgeWeightsArray();
        final int maxIndex = edgesCallback.getMaxIndex();
        final boolean isDirected = edgesCallback.isDirected();
        final boolean isUndirected = edgesCallback.isUndirected();
        final boolean hasSelfLoop = edgesCallback.hasSelfLoop();

        if (hasSelfLoop) {
            selfLoopAttributesBuffer.ensureCapacity(totalEdges * ATTRIBS_STRIDE_SELFLOOP);
            final FloatBuffer attribsSelfLoopBuffer = selfLoopAttributesBuffer.floatBuffer();
            updateSelfLoop(maxIndex,
                visibleEdgesArray,
                edgeWeightsArray,
                selfLoopAttributesBufferBatch,
                0,
                attribsSelfLoopBuffer);
        } else {
            selfLoopCounter.clearCount();
        }
        updateUndirectedData(
            isDirected,
            maxIndex,
            visibleEdgesArray,
            edgeWeightsArray,
            attributesBufferBatch,
            0,
            attribsDirectBuffer
        );
        updateDirectedData(
            isUndirected,
            maxIndex,
            visibleEdgesArray,
            edgeWeightsArray,
            attributesBufferBatch,
            0,
            attribsDirectBuffer
        );
    }

    @Override
    public void dispose(GL gl) {
        super.dispose(gl);
        attributesBufferBatch = null;
        selfLoopAttributesBufferBatch = null;
        if (attributesBuffer != null) {
            attributesBuffer.destroy();
            attributesBuffer = null;
        }
        if (selfLoopAttributesBuffer != null) {
            selfLoopAttributesBuffer.destroy();
            selfLoopAttributesBuffer = null;
        }
    }
}
