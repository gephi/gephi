package org.gephi.viz.engine.jogl.pipeline.arrays;

import static org.gephi.viz.engine.util.gl.Constants.SHADER_ELEMENT_INDEX_LOCATION;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeData;
import org.gephi.viz.engine.jogl.pipeline.common.NodeDataTextureStore;
import org.gephi.viz.engine.jogl.pipeline.common.NodeWorldData;
import org.gephi.viz.engine.jogl.util.gl.GLFunctions;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class ArrayDrawNodeData extends AbstractNodeData {

    private final int[] bufferName = new int[1];

    private static final int VERT_BUFFER = 0;

    public ArrayDrawNodeData(final NodesCallback nodesCallback, final NodeDataTextureStore nodeDataTextureStore) {
        super(nodesCallback, nodeDataTextureStore, false, false);
    }

    public void drawArrays(GL2ES2 gl, RenderingLayer layer, NodeWorldData data,
                           float[] mvpFloats) {
        refreshTime();

        drawArraysInternal(gl, layer, data, mvpFloats);
    }

    public void drawArraysInternal(final GL2ES2 gl,
                                   final RenderingLayer layer,
                                   final NodeWorldData data,
                                   final float[] mvpFloats) {
        final int instanceCount =
            setupShaderProgramForRenderingLayer(gl, layer, data, mvpFloats);

        if (instanceCount <= 0) {
            GLFunctions.stopUsingProgram(gl);
            unsetupVertexArrayAttributes(gl);
            return;
        }

        final boolean renderingUnselectedNodes = layer.getLevel() == 1;
        final int instancesOffset = renderingUnselectedNodes ? 0 : instanceCounter.unselectedCountToDraw;


        final float zoom = data.getZoom();
        final float nodeScale = data.getNodeScale();
        final float[] attrs = new float[ATTRIBS_STRIDE];
        int index = instancesOffset * ATTRIBS_STRIDE;

        //We have to perform one draw call per instance because repeating the attributes without instancing per each vertex would use too much memory:
        //TODO: Maybe we can batch a few nodes at once though
        final FloatBuffer attribs = attributesBuffer.floatBuffer();

        attribs.position(index);
        for (int i = 0; i < instanceCount; i++) {
            attribs.get(attrs);

            //The single attribute is the node store id used to fetch x/y/size/color from the node texture.
            final float elementIndex = attrs[0];
            final int storeId = (int) elementIndex;

            //Choose LOD (size comes from the shared node data texture, like the shader does):
            final float size = nodeDataTextureStore.getRawSize(storeId) * nodeScale;
            final float observedSize = size * zoom;

            final int circleVertexCount;
            final int firstVertex;
            if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_64) {
                circleVertexCount = circleMesh64.vertexCount;
                firstVertex = firstVertex64;
            } else if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_32) {
                circleVertexCount = circleMesh32.vertexCount;
                firstVertex = firstVertex32;
            } else if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_16) {
                circleVertexCount = circleMesh16.vertexCount;
                firstVertex = firstVertex16;
            } else {
                circleVertexCount = circleMesh8.vertexCount;
                firstVertex = firstVertex8;
            }

            //Define the per-draw element index as a constant generic vertex attribute:
            gl.glVertexAttrib1f(SHADER_ELEMENT_INDEX_LOCATION, elementIndex);

            //Draw the instance:
            GLFunctions.drawArraysSingleInstance(gl, firstVertex, circleVertexCount);
        }

        GLFunctions.stopUsingProgram(gl);
        unsetupVertexArrayAttributes(gl);
    }

    public void updateBuffers(GL gl) {
        // Upload the shared node data texture (sampled by node and edge shaders).
        nodeDataTextureStore.upload(gl);
        instanceCounter.promoteCountToDraw();
    }

    @Override
    protected void initBuffers(final GL gl) {
        super.initBuffers(gl);

        gl.glGenBuffers(bufferName.length, bufferName, 0);

        initCirclesGLVertexBuffer(gl, bufferName[VERT_BUFFER]);
    }
}
