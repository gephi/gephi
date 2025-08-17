package org.gephi.viz.engine.jogl.pipeline.arrays.renderers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumSet;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.GLBuffers;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
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
import org.joml.Vector2f;

import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_BLEND_DST_ALPHA;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
public class RectangleSelectionArrayDraw implements Renderer<JOGLRenderingTarget> {
    private final VizEngine engine;

    final float[] mvpFloats = new float[16];

    private static final int VERT_BUFFER = 0;

    public static final int VERTEX_COUNT = 6; // 2 triangles
    public static final int VERTEX_FLOATS = 2;

    private final int[] bufferName = new int[1];
    private ManagedDirectBuffer rectangleVertexDataBuffer;
    private GLBufferMutable vertexGLBuffer;
    private SelectionRectangleVAO vao;

    public RectangleSelectionArrayDraw(VizEngine engine) {
        this.engine = engine;
    }

    @Override
    public String getCategory() {
        return PipelineCategory.RECTANGLE_SELECTION;
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Rectangle Selection";
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        shaderProgram = new GLShaderProgram(SHADERS_ROOT, "rectangleSelection", "rectangleSelection")
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .init(gl);

        gl.glGenBuffers(bufferName.length, bufferName, 0);

        rectangleVertexDataBuffer = new ManagedDirectBuffer(GL_FLOAT, Float.BYTES * VERTEX_COUNT * VERTEX_FLOATS);

        vertexGLBuffer = new GLBufferMutable(bufferName[VERT_BUFFER], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBuffer.bind(gl);
        vertexGLBuffer.init(gl, Float.BYTES * VERTEX_COUNT * VERTEX_FLOATS, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        vertexGLBuffer.unbind(gl);

        vao = new SelectionRectangleVAO(
            engine.getLookup().lookup(GLCapabilitiesSummary.class),
            engine.getLookup().lookup(OpenGLOptions.class)
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean render = false;

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        final GraphSelection graphSelection = engine.getLookup().lookup(GraphSelection.class);

        if (graphSelection.getMode() != GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION) {
            return;
        }

        final Vector2f initialPosition = graphSelection.getRectangleInitialPosition();
        final Vector2f currentPosition = graphSelection.getRectangleCurrentPosition();

        if (initialPosition != null && currentPosition != null) {

            final float minX = Math.min(initialPosition.x, currentPosition.x);
            final float minY = Math.min(initialPosition.y, currentPosition.y);
            final float maxX = Math.max(initialPosition.x, currentPosition.x);
            final float maxY = Math.max(initialPosition.y, currentPosition.y);

            final FloatBuffer floatBuffer = rectangleVertexDataBuffer.floatBuffer();

            final float[] rectangleVertexData = {
                //Triangle 1:
                minX,
                minY,
                minX,
                maxY,
                maxX,
                minY,
                //Triangle 2:
                minX,
                maxY,
                maxX,
                maxY,
                maxX,
                minY
            };

            floatBuffer.put(rectangleVertexData);
            floatBuffer.position(0);

            vertexGLBuffer.bind(gl);
            vertexGLBuffer.update(gl, floatBuffer);
            vertexGLBuffer.unbind(gl);

            render = true;
        } else {
            render = false;
        }
    }

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "rectangleSelection";
    private GLShaderProgram shaderProgram;

    private final int[] intData = new int[1];
    private final byte[] booleanData = new byte[1];

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        if (render) {
            shaderProgram.use(gl);
            engine.getModelViewProjectionMatrixFloats(mvpFloats);

            gl.glUniformMatrix4fv(shaderProgram.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);

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

            gl.glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

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

    private class SelectionRectangleVAO extends GLVertexArrayObject {

        public SelectionRectangleVAO(GLCapabilitiesSummary capabilities, OpenGLOptions openGLOptions) {
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
            return new int[]{
                SHADER_VERT_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            return null;
        }
    }
}
