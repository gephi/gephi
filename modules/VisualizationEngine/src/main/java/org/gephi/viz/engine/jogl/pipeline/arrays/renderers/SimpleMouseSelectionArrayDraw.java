package org.gephi.viz.engine.jogl.pipeline.arrays.renderers;


import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_BLEND_DST_ALPHA;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;

import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.models.NodeDiskVertexDataGenerator;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class SimpleMouseSelectionArrayDraw implements Renderer<JOGLRenderingTarget> {
    private final VizEngine engine;

    float radius;

    final float[] mvpFloats = new float[16];

    private static final int VERT_BUFFER = 0;

    public static final int VERTEX_FLOATS = 2;

    private final int[] bufferName = new int[1];
    private ManagedDirectBuffer circleVertexDataBuffer;
    private GLBufferMutable vertexGLBuffer;
    private SelectionMouseVAO vao;
    private boolean render = false;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "simpleMouseSelection";
    private GLShaderProgram shaderProgram;

    private final int[] intData = new int[1];
    private final byte[] booleanData = new byte[1];

    private final NodeDiskVertexDataGenerator generator64;
    private final int circleVertexCount64;

    public SimpleMouseSelectionArrayDraw(VizEngine engine) {
        generator64 = new NodeDiskVertexDataGenerator(64);
        circleVertexCount64 = generator64.getVertexCount();

        this.engine = engine;
    }

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        final GraphSelection graphSelection = engine.getGraphSelection();

        if (graphSelection.getMode() != GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION &&
            graphSelection.getMode() != GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION) {
            render = false;
            return;
        }

        final Vector2f mousePosition = graphSelection.getMousePosition();
        float mouseSelectionDiameter = graphSelection.getMouseSelectionDiameter();

        if (mousePosition != null && mouseSelectionDiameter > 1) {
            if (!graphSelection.getMouseSelectionDiameterZoomProportional()) {
                Matrix4f mvp = new Matrix4f();
                mvp.set(mvpFloats);

                Vector3f scale = new Vector3f();

                mvp.getScale(scale);

                graphSelection.setSimpleMouseSelectionMVPScale(scale.x);


            }
            mouseSelectionDiameter = graphSelection.getMouseSelectionEffectiveDiameter();
            final FloatBuffer floatBuffer = circleVertexDataBuffer.floatBuffer();
            // Vertex = 2 Float (xy)
            float[] vertexData = Arrays.copyOf(generator64.getVertexData(), circleVertexCount64 * VERTEX_FLOATS);

            for (int vertexIndex = 0; vertexIndex < circleVertexCount64 * VERTEX_FLOATS; vertexIndex += 2) {
                vertexData[vertexIndex] = vertexData[vertexIndex] * mouseSelectionDiameter + mousePosition.x;
                vertexData[vertexIndex + 1] = vertexData[vertexIndex + 1] * mouseSelectionDiameter + mousePosition.y;

            }
            floatBuffer.put(vertexData);
            floatBuffer.position(0);

            vertexGLBuffer.bind(gl);
            vertexGLBuffer.update(gl, floatBuffer);
            vertexGLBuffer.unbind(gl);

            render = true;
        } else {
            render = false;
        }
    }

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        if (render) {
            shaderProgram.use(gl);
            engine.getModelViewProjectionMatrixFloats(mvpFloats);

            gl.glUniformMatrix4fv(shaderProgram.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false,
                mvpFloats, 0);

            vao.use(gl);


            gl.glGetBooleanv(GL_BLEND, booleanData, 0);
            gl.glGetIntegerv(GL_BLEND_DST_ALPHA, intData, 0);

            final boolean blendEnabled = booleanData[0] > 0;
            final int blendFunc = intData[0];

            if (!blendEnabled) {
                gl.glEnable(GL_BLEND);
            }

            if (blendFunc != GL_ONE_MINUS_SRC_ALPHA) {
                gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }

            gl.glDrawArrays(GL_TRIANGLES, 0, circleVertexCount64);

            //Restore state:
            if (!blendEnabled) {
                gl.glDisable(GL_BLEND);
            }
            if (blendFunc != GL_ONE_MINUS_SRC_ALPHA) {
                gl.glBlendFunc(GL_SRC_ALPHA, blendFunc);
            }

            vao.stopUsing(gl);

            shaderProgram.stopUsing(gl);
        }
    }

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return EnumSet.of(RenderingLayer.FRONT4);
    }

    @Override
    public String getCategory() {
        return PipelineCategory.MOUSE_SELECTION;
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Simple Mouse Selection";
    }


    @Override
    public void init(JOGLRenderingTarget target) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        shaderProgram = new GLShaderProgram(SHADERS_ROOT, "simpleMouseSelection", "simpleMouseSelection")
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .init(gl);

        gl.glGenBuffers(bufferName.length, bufferName, 0);

        circleVertexDataBuffer = new ManagedDirectBuffer(GL_FLOAT, Float.BYTES * circleVertexCount64 * VERTEX_FLOATS);

        vertexGLBuffer = new GLBufferMutable(bufferName[VERT_BUFFER], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBuffer.bind(gl);
        vertexGLBuffer.init(gl, (long) Float.BYTES * circleVertexCount64 * VERTEX_FLOATS,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        vertexGLBuffer.unbind(gl);
        vao = new SelectionMouseVAO(
            engine.getLookup().lookup(GLCapabilitiesSummary.class),
            engine.getLookup().lookup(OpenGLOptions.class)
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private class SelectionMouseVAO extends GLVertexArrayObject {

        public SelectionMouseVAO(GLCapabilitiesSummary capabilities, OpenGLOptions openGLOptions) {
            super(capabilities, openGLOptions);
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBuffer.bind(gl);
            {
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            }
            vertexGLBuffer.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            return null;
        }
    }
}
