package org.gephi.viz.engine.jogl.pipeline.instanced;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import java.nio.FloatBuffer;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeData;
import org.gephi.viz.engine.jogl.pipeline.common.NodeWorldData;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class InstancedNodeData extends AbstractNodeData {

    public InstancedNodeData(NodesCallback nodesCallback) {
        super(nodesCallback, true, false);
    }

    private final int[] bufferName = new int[3];

    private static final int VERT_BUFFER = 0;
    private static final int ATTRIBS_BUFFER = 1;
    private static final int ATTRIBS_BUFFER_SECONDARY = 2;

    public void drawInstanced(GL2ES3 gl, RenderingLayer layer, NodeWorldData data, float[] mvpFloats) {
        refreshTime();

        drawInstancedInternal(gl, layer, data, mvpFloats);
    }

    private void drawInstancedInternal(final GL2ES3 gl,
                                       final RenderingLayer layer,
                                       final NodeWorldData data,
                                       final float[] mvpFloats) {
        final int instanceCount =
            setupShaderProgramForRenderingLayer(gl, layer, data, mvpFloats);

        if (instanceCount <= 0) {
            diskModel.stopUsingProgram(gl);
            unsetupVertexArrayAttributes(gl);
            return;
        }

        final float maxObservedSize = data.getMaxNodeSize() * data.getZoom();
        final int circleVertexCount;
        final int firstVertex;
        if (maxObservedSize > OBSERVED_SIZE_LOD_THRESHOLD_64) {
            circleVertexCount = circleMesh64.vertexCount;
            firstVertex = firstVertex64;
        } else if (maxObservedSize > OBSERVED_SIZE_LOD_THRESHOLD_32) {
            circleVertexCount = circleMesh32.vertexCount;
            firstVertex = firstVertex32;
        } else if (maxObservedSize > OBSERVED_SIZE_LOD_THRESHOLD_16) {
            circleVertexCount = circleMesh16.vertexCount;
            firstVertex = firstVertex16;
        } else {
            circleVertexCount = circleMesh8.vertexCount;
            firstVertex = firstVertex8;
        }

        diskModel.drawInstanced(
            gl,
            firstVertex, circleVertexCount, instanceCount
        );
        diskModel.stopUsingProgram(gl);
        unsetupVertexArrayAttributes(gl);
    }

    @Override
    protected void initBuffers(GL gl) {
        super.initBuffers(gl);
        gl.glGenBuffers(bufferName.length, bufferName, 0);

        initCirclesGLVertexBuffer(gl, bufferName[VERT_BUFFER]);

        //Initialize for batch nodes size:
        attributesGLBuffer = new GLBufferMutable(bufferName[ATTRIBS_BUFFER], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBuffer.bind(gl);
        attributesGLBuffer.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_NODES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBuffer.unbind(gl);

        attributesGLBufferSecondary =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferSecondary.bind(gl);
        attributesGLBufferSecondary.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_NODES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferSecondary.unbind(gl);
    }

    public void updateBuffers(GL gl) {
        final FloatBuffer buf = attributesBuffer.floatBuffer();

        buf.limit(instanceCounter.unselectedCount * ATTRIBS_STRIDE);
        buf.position(0);

        attributesGLBufferSecondary.bind(gl);
        attributesGLBufferSecondary.updateWithOrphaning(gl, buf);
        attributesGLBufferSecondary.unbind(gl);

        final int offset = buf.limit();
        buf.limit(offset + instanceCounter.selectedCount * ATTRIBS_STRIDE);
        buf.position(offset);

        attributesGLBuffer.bind(gl);
        attributesGLBuffer.updateWithOrphaning(gl, buf);
        attributesGLBuffer.unbind(gl);

        instanceCounter.promoteCountToDraw();
    }
}

