package org.gephi.viz.engine.jogl.models;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3ES3;
import org.gephi.viz.engine.util.gl.Constants;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.Constants.*;
import org.gephi.viz.engine.util.NumberUtils;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgeLineModelUndirected {

    public static final int VERTEX_FLOATS = 2;
    public static final int POSITION_SOURCE_FLOATS = 2;
    public static final int POSITION_TARGET_LOCATION = 2;
    public static final int SOURCE_COLOR_FLOATS = 1;
    public static final int TARGET_COLOR_FLOATS = SOURCE_COLOR_FLOATS;
    public static final int COLOR_FLOATS = 1;
    public static final int SIZE_FLOATS = 1;
    public static final int SOURCE_SIZE_FLOATS = 1;
    public static final int TARGET_SIZE_FLOATS = 1;

    public static final int TOTAL_ATTRIBUTES_FLOATS
        = POSITION_SOURCE_FLOATS
        + POSITION_TARGET_LOCATION
        + SOURCE_COLOR_FLOATS
        + TARGET_COLOR_FLOATS
        + COLOR_FLOATS
        + SIZE_FLOATS
        + SOURCE_SIZE_FLOATS
        + TARGET_SIZE_FLOATS;

    private static final int VERTEX_PER_TRIANGLE = 3;

    public static final int TRIANGLE_COUNT = 2;
    public static final int VERTEX_COUNT = TRIANGLE_COUNT * VERTEX_PER_TRIANGLE;

    private GLShaderProgram program;
    private GLShaderProgram programWithSelectionSelected;
    private GLShaderProgram programWithSelectionUnselected;

    public int getVertexCount() {
        return VERTEX_COUNT;
    }

    public void initGLPrograms(GL2ES2 gl) {
        initProgram(gl);
    }

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "edge";

    private static final String SHADERS_EDGE_LINE_SOURCE = "edge-line-undirected";
    private static final String SHADERS_EDGE_LINE_SOURCE_WITH_SELECTION_SELECTED = "edge-line-undirected_with_selection_selected";
    private static final String SHADERS_EDGE_LINE_SOURCE_WITH_SELECTION_UNSELECTED = "edge-line-undirected_with_selection_unselected";

    private void initProgram(GL2ES2 gl) {
        program = new GLShaderProgram(SHADERS_ROOT, SHADERS_EDGE_LINE_SOURCE, SHADERS_EDGE_LINE_SOURCE)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
            .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
            .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION_TARGET, SHADER_POSITION_TARGET_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_COLOR, SHADER_SOURCE_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_COLOR, SHADER_TARGET_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_SIZE, SHADER_SOURCE_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_SIZE, SHADER_TARGET_SIZE_LOCATION)
            .init(gl);

        programWithSelectionSelected = new GLShaderProgram(SHADERS_ROOT, SHADERS_EDGE_LINE_SOURCE_WITH_SELECTION_SELECTED, SHADERS_EDGE_LINE_SOURCE)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
            .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
            .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION_TARGET, SHADER_POSITION_TARGET_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_COLOR, SHADER_SOURCE_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_COLOR, SHADER_TARGET_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_SIZE, SHADER_SOURCE_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_SIZE, SHADER_TARGET_SIZE_LOCATION)
            .init(gl);

        programWithSelectionUnselected = new GLShaderProgram(SHADERS_ROOT, SHADERS_EDGE_LINE_SOURCE_WITH_SELECTION_UNSELECTED, SHADERS_EDGE_LINE_SOURCE)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_BACKGROUND_COLOR)
            .addUniformName(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
            .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
            .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION_TARGET, SHADER_POSITION_TARGET_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_COLOR, SHADER_SOURCE_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_COLOR, SHADER_TARGET_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SOURCE_SIZE, SHADER_SOURCE_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_TARGET_SIZE, SHADER_TARGET_SIZE_LOCATION)
            .init(gl);
    }

    public void drawArraysMultipleInstance(GL2ES2 gl, final int drawBatchCount) {
        if (drawBatchCount <= 0) {
            return;
        }
        //Multiple lines, attributes must be in the buffer once per vertex count:
        gl.glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT * drawBatchCount);
    }

    public void drawInstanced(GL3ES3 gl, int instanceCount) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glDrawArraysInstanced(GL_TRIANGLES, 0, VERTEX_COUNT, instanceCount);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight) {
        //Line:
        program.use(gl);
        prepareProgramData(gl, mvpFloats, scale, minWeight, maxWeight);
    }

    public void useProgramWithSelectionSelected(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight) {
        programWithSelectionSelected.use(gl);
        prepareProgramDataWithSelectionSelected(gl, mvpFloats, scale, minWeight, maxWeight);
    }

    public void useProgramWithSelectionUnselected(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight, float[] backgroundColorFloats, float colorLightenFactor) {
        programWithSelectionUnselected.use(gl);
        prepareProgramDataWithSelectionUnselected(gl, mvpFloats, scale, minWeight, maxWeight, backgroundColorFloats, colorLightenFactor);
    }

    public void stopUsingProgram(GL2ES2 gl) {
        gl.glUseProgram(0);
    }

    private void prepareProgramData(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight) {
        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), EDGE_SCALE_MIN * scale);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), EDGE_SCALE_MAX * scale);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);

        if (NumberUtils.equalsEpsilon(minWeight, maxWeight, 1e-3f)) {
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), 1);
        } else {
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), maxWeight - minWeight);
        }
    }

    private void prepareProgramDataWithSelectionSelected(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight) {
        gl.glUniformMatrix4fv(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), EDGE_SCALE_MIN * scale);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), EDGE_SCALE_MAX * scale);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);

        if (NumberUtils.equalsEpsilon(minWeight, maxWeight, 1e-3f)) {
            gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), 1);
        } else {
            gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), maxWeight - minWeight);
        }
    }

    private void prepareProgramDataWithSelectionUnselected(GL2ES2 gl, float[] mvpFloats, float scale, float minWeight, float maxWeight, float[] backgroundColorFloats, float colorLightenFactor) {
        gl.glUniformMatrix4fv(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform4fv(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_BACKGROUND_COLOR), 1, backgroundColorFloats, 0);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR), colorLightenFactor);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), EDGE_SCALE_MIN * scale);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), EDGE_SCALE_MAX * scale);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);

        if (NumberUtils.equalsEpsilon(minWeight, maxWeight, 1e-3f)) {
            gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), 1);
        } else {
            gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), maxWeight - minWeight);
        }
    }

    public static float[] getVertexData() {
        //lineEnd, sideVector
        return new float[]{
            //Triangle 1
            0, -1,// bottom left corner
            1, -1,// top left corner
            0, 1,// bottom right corner
            //Triangle 2
            0, 1,// bottom right corner
            1, -1,// top left corner
            1, 1// top right corner
        };
    }
}
