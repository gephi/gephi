package org.gephi.viz.engine.jogl.pipeline.indirect;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeData;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndexImpl;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_BYTES;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_INTS_COUNT;

/**
 *
 * @author Eduardo Ramos
 */
public class IndirectNodeData extends AbstractNodeData {

    private final int[] bufferName = new int[4];

    private static final int VERT_BUFFER = 0;
    private static final int ATTRIBS_BUFFER = 1;
    private static final int ATTRIBS_BUFFER_SECONDARY = 2;
    private static final int INDIRECT_DRAW_BUFFER = 3;

    public IndirectNodeData() {
        super(true, true);
    }

    public void update(VizEngine engine, GraphIndexImpl spatialIndex) {
        updateData(
            engine.getZoom(),
            spatialIndex,
            engine.getLookup().lookup(GraphRenderingOptions.class),
            engine.getLookup().lookup(GraphSelection.class)
        );
    }

    public void drawIndirect(GL4 gl, RenderingLayer layer, VizEngine engine, float[] mvpFloats) {
        //First we draw outside circle (for border) and then inside circle:
        //FIXME: all node parts should be drawn at the same time, otherwise internal parts of nodes can cover external parts!
        drawIndirectInternal(gl, layer, engine, mvpFloats, true);
        drawIndirectInternal(gl, layer, engine, mvpFloats, false);
    }

    private void drawIndirectInternal(final GL4 gl,
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

        final boolean renderingUnselectedNodes = layer.isBack();
        final int instancesOffset = renderingUnselectedNodes ? 0 : instanceCounter.unselectedCountToDraw;

        commandsGLBuffer.bind(gl);
        diskModel.drawIndirect(
            gl, instanceCount, instancesOffset
        );
        commandsGLBuffer.unbind(gl);
        diskModel.stopUsingProgram(gl);
        unsetupVertexArrayAttributes(gl);
    }

    @Override
    protected void initBuffers(final GL gl) {
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

        commandsGLBuffer = new GLBufferMutable(bufferName[INDIRECT_DRAW_BUFFER], GLBufferMutable.GL_BUFFER_TYPE_DRAW_INDIRECT);
        commandsGLBuffer.bind(gl);
        commandsGLBuffer.init(gl, INDIRECT_DRAW_COMMAND_BYTES * BATCH_NODES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        commandsGLBuffer.unbind(gl);
    }

    public void updateBuffers(final GL4 gl) {
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

        final IntBuffer commandsBufferData = commandsBuffer.intBuffer();
        commandsBufferData.position(0);
        commandsBufferData.limit(instanceCounter.total() * INDIRECT_DRAW_COMMAND_INTS_COUNT);

        commandsGLBuffer.bind(gl);
        commandsGLBuffer.updateWithOrphaning(gl, commandsBufferData);
        commandsGLBuffer.unbind(gl);

        instanceCounter.promoteCountToDraw();
        maxNodeSizeToDraw = maxNodeSize;
    }
}
