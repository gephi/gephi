package org.gephi.viz.engine.jogl.pipeline.instanced;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;

import java.nio.FloatBuffer;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeData;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndexImpl;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;

/**
 *
 * @author Eduardo Ramos
 */
public class InstancedNodeData extends AbstractNodeData {

    public InstancedNodeData() {
        super(true, false);
    }

    private final int[] bufferName = new int[3];

    private static final int VERT_BUFFER = 0;
    private static final int ATTRIBS_BUFFER = 1;
    private static final int ATTRIBS_BUFFER_SECONDARY = 2;

    public void update(VizEngine engine, GraphIndexImpl spatialIndex) {
        updateData(
            engine.getZoom(),
            spatialIndex,
            engine.getLookup().lookup(GraphRenderingOptions.class),
            engine.getLookup().lookup(GraphSelection.class)
        );
    }

    public void drawInstanced(GL2ES3 gl, RenderingLayer layer, VizEngine engine, float[] mvpFloats) {
        //First we draw outside circle (for border) and then inside circle:
        //FIXME: all node parts should be drawn at the same time, otherwise internal parts of nodes can cover external parts!
        drawInstancedInternal(gl, layer, engine, mvpFloats, true);
        drawInstancedInternal(gl, layer, engine, mvpFloats, false);
    }

    private void drawInstancedInternal(final GL2ES3 gl,
                                       final RenderingLayer layer,
                                       final VizEngine engine,
                                       final float[] mvpFloats,
                                       final boolean isRenderingOutsideCircle) {
        final int instanceCount = setupShaderProgramForRenderingLayer(gl, layer, engine, mvpFloats, isRenderingOutsideCircle);

        if (instanceCount <= 0) {
            diskModel.stopUsingProgram(gl);
            unsetupVertexArrayAttributes(gl);
            return;
        }

        final float maxObservedSize = maxNodeSizeToDraw * engine.getZoom();
        final int circleVertexCount;
        final int firstVertex;
        if (maxObservedSize > OBSERVED_SIZE_LOD_THRESHOLD_64) {
            circleVertexCount = circleVertexCount64;
            firstVertex = firstVertex64;
        } else if (maxObservedSize > OBSERVED_SIZE_LOD_THRESHOLD_32) {
            circleVertexCount = circleVertexCount32;
            firstVertex = firstVertex32;
        } else if (maxObservedSize > OBSERVED_SIZE_LOD_THRESHOLD_16) {
            circleVertexCount = circleVertexCount16;
            firstVertex = firstVertex16;
        } else {
            circleVertexCount = circleVertexCount8;
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
        attributesGLBuffer.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_NODES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBuffer.unbind(gl);

        attributesGLBufferSecondary = new GLBufferMutable(bufferName[ATTRIBS_BUFFER_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferSecondary.bind(gl);
        attributesGLBufferSecondary.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_NODES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
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
        maxNodeSizeToDraw = maxNodeSize;
    }
}

