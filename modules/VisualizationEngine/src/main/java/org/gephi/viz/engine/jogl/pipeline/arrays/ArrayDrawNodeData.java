package org.gephi.viz.engine.jogl.pipeline.arrays;

import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3ES3;
import java.nio.FloatBuffer;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractNodeData;
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

    public ArrayDrawNodeData(final NodesCallback nodesCallback) {
        super(nodesCallback, false, false);
    }

    public void drawArrays(GL3ES3 gl, RenderingLayer layer, NodeWorldData data,
                           float[] mvpFloats) {
        refreshTime();

        drawArraysInternal(gl, layer, data, mvpFloats);
    }

    public void drawArraysInternal(final GL3ES3 gl,
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
        final float[] attrs = new float[ATTRIBS_STRIDE];
        int index = instancesOffset * ATTRIBS_STRIDE;

        //We have to perform one draw call per instance because repeating the attributes without instancing per each vertex would use too much memory:
        //TODO: Maybe we can batch a few nodes at once though
        final FloatBuffer attribs = attributesBuffer.floatBuffer();

        attribs.position(index);
        for (int i = 0; i < instanceCount; i++) {
            attribs.get(attrs);

            final float size = attrs[3];

            //Define instance attributes:
            gl.glVertexAttrib2fv(SHADER_POSITION_LOCATION, attrs, 0);

            //No vertexAttribArray, we have to unpack rgba manually:
            final int argb = Float.floatToRawIntBits(attrs[2]);

            final int a = ((argb >> 24) & 0xFF);
            final int r = ((argb >> 16) & 0xFF);
            final int g = ((argb >> 8) & 0xFF);
            final int b = (argb & 0xFF);

            gl.glVertexAttrib4f(SHADER_COLOR_LOCATION, b, g, r, a);

            gl.glVertexAttrib1f(SHADER_SIZE_LOCATION, size);

            //Draw the instance:
            GLFunctions.drawArraysSingleInstance(gl, 0, 6);
        }

        GLFunctions.stopUsingProgram(gl);
        unsetupVertexArrayAttributes(gl);
    }

    public void updateBuffers() {
        instanceCounter.promoteCountToDraw();
    }

    @Override
    protected void initBuffers(final GL gl) {
        super.initBuffers(gl);

        gl.glGenBuffers(bufferName.length, bufferName, 0);

        initCirclesGLVertexBuffer(gl, bufferName[VERT_BUFFER]);
    }
}
