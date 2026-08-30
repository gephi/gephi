package org.gephi.viz.engine.jogl.pipeline.arrays.renderers;


import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_BLEND_DST_RGB;
import static com.jogamp.opengl.GL.GL_BLEND_SRC_RGB;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GL3ES3;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.models.mesh.NodeDiskVertexMeshGenerator;
import org.gephi.viz.engine.jogl.pipeline.common.VoidWorldData;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.Mesh;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class SimpleMouseSelectionArrayDraw implements Renderer<JOGLRenderingTarget, VoidWorldData> {
    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;

    private static final int VERT_BUFFER = 0;

    public static final int VERTEX_FLOATS = 2;

    private final int[] bufferName = new int[1];
    private ManagedDirectBuffer circleVertexDataBuffer;
    private GLBufferMutable vertexGLBuffer;
    private SelectionMouseVAO vao;
    private boolean render = false;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "simpleMouseSelection";
    private static final String UNIFORM_NAME_COLOR = "color";
    private static final float[] CIRCLE_COLOR_DARK = {0.3f, 0.3f, 0.3f, 0.2f};
    private static final float[] CIRCLE_COLOR_LIGHT = {0.85f, 0.85f, 0.85f, 0.2f};
    private GLShaderProgram shaderProgram;

    private boolean backgroundIsDark = false;
    private final int[] intData = new int[1];
    private final byte[] booleanData = new byte[1];

    private final Mesh meshCircle64 = NodeDiskVertexMeshGenerator.generateFilledCircle(64);


    public SimpleMouseSelectionArrayDraw(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        this.engine = engine;
    }

    @Override
    public VoidWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target, float[] mvpFloats) {
        final GL3ES3 gl = target.getDrawable().getGL().getGL3ES3();

        final GraphSelection graphSelection = model.getGraphSelection();
        backgroundIsDark = model.getRenderingOptions().isBackgroundColorDark();

        if (graphSelection.getMode() != GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION &&
            graphSelection.getMode() != GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION) {
            render = false;
            return VoidWorldData.INSTANCE;
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
            float[] vertexData = Arrays.copyOf(meshCircle64.vertexData, meshCircle64.vertexData.length);

            for (int vertexIndex = 0; vertexIndex < meshCircle64.vertexData.length; vertexIndex += 2) {
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
        return VoidWorldData.INSTANCE;
    }

    @Override
    public void render(VoidWorldData data, JOGLRenderingTarget target, RenderingLayer layer, float[] mvpFloats) {

        final GL3ES3 gl = target.getDrawable().getGL().getGL3ES3();

        if (render) {
            shaderProgram.use(gl);

            gl.glUniformMatrix4fv(shaderProgram.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false,
                mvpFloats, 0);

            final float[] circleColor = backgroundIsDark ? CIRCLE_COLOR_LIGHT : CIRCLE_COLOR_DARK;
            gl.glUniform4fv(shaderProgram.getUniformLocation(UNIFORM_NAME_COLOR), 1, circleColor, 0);

            vao.use(gl);


            gl.glGetBooleanv(GL_BLEND, booleanData, 0);
            gl.glGetIntegerv(GL_BLEND_SRC_RGB, intData, 0);
            final boolean blendEnabled = booleanData[0] > 0;
            final int savedBlendSrc = intData[0];
            gl.glGetIntegerv(GL_BLEND_DST_RGB, intData, 0);
            final int savedBlendDst = intData[0];

            if (!blendEnabled) {
                gl.glEnable(GL_BLEND);
            }
            gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            gl.glDrawArrays(GL_TRIANGLES, 0, meshCircle64.vertexCount);

            //Restore state:
            if (!blendEnabled) {
                gl.glDisable(GL_BLEND);
            }
            gl.glBlendFunc(savedBlendSrc, savedBlendDst);

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
        final GL3ES3 gl = target.getDrawable().getGL().getGL3ES3();

        shaderProgram = new GLShaderProgram(SHADERS_ROOT, "simpleMouseSelection", "simpleMouseSelection")
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_COLOR)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .init(gl);

        gl.glGenBuffers(bufferName.length, bufferName, 0);

        circleVertexDataBuffer = new ManagedDirectBuffer(GL_FLOAT, Float.BYTES * meshCircle64.vertexData.length);

        vertexGLBuffer = new GLBufferMutable(bufferName[VERT_BUFFER], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBuffer.bind(gl);
        vertexGLBuffer.init(gl, (long) Float.BYTES * meshCircle64.vertexData.length,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        vertexGLBuffer.unbind(gl);
        vao = new SelectionMouseVAO(
            engine.getOpenGLOptions()
        );
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        final GL3ES3 gl = target.getDrawable().getGL().getGL3ES3();

        if (shaderProgram != null) {
            shaderProgram.destroy(gl);
            shaderProgram = null;
        }

        if (vertexGLBuffer != null) {
            vertexGLBuffer.destroy(gl);
            vertexGLBuffer = null;
        }

        if (circleVertexDataBuffer != null) {
            circleVertexDataBuffer.destroy();
            circleVertexDataBuffer = null;
        }

        if (vao != null) {
            vao.destroy(gl);
            vao = null;
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private class SelectionMouseVAO extends GLVertexArrayObject {

        public SelectionMouseVAO(OpenGLOptions openGLOptions) {
            super(openGLOptions);
        }

        @Override
        protected void configure(GL3ES3 gl) {
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
