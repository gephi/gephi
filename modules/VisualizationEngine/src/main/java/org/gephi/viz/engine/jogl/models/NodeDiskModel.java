package org.gephi.viz.engine.jogl.models;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_POSITION;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BACKGROUND_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_COLOR_LIGHTEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_COLOR_MULTIPLIER;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_MODE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SIZE_MULTIPLIER;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_BYTES;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL4;
import java.time.Instant;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.gl.Constants;

/**
 * @author Eduardo Ramos
 */
public class NodeDiskModel {

    public static final int VERTEX_FLOATS = 2;
    public static final int POSITION_FLOATS = 2;
    public static final int COLOR_FLOATS = 1;
    public static final int SIZE_FLOATS = 1;

    public static final int TOTAL_ATTRIBUTES_FLOATS
        = POSITION_FLOATS
        + COLOR_FLOATS
        + SIZE_FLOATS;

    private GLShaderProgram program;
    private GLShaderProgram programWithSelectionSelected;
    private GLShaderProgram programWithSelectionUnselected;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "node";

    private static final String SHADERS_NODE_CIRCLE_SOURCE = "node";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_WITH_SELECTION_SELECTED = "node_with_selection_selected";
    private static final String SHADERS_NODE_CIRCLE_SOURCE_WITH_SELECTION_UNSELECTED = "node_with_selection_unselected";

    public void initGLPrograms(GL2ES2 gl) {
        program = new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE, SHADERS_NODE_CIRCLE_SOURCE)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_SIZE_MULTIPLIER)
            .addUniformName(UNIFORM_NAME_COLOR_MULTIPLIER)
            .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
            .addUniformName(UNIFORM_NAME_SELECTION_MODE)
            .addUniformName(UNIFORM_NAME_SELECTION_TIME)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .init(gl);

        programWithSelectionSelected =
            new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE_WITH_SELECTION_SELECTED,
                SHADERS_NODE_CIRCLE_SOURCE)
                .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
                .addUniformName(UNIFORM_NAME_SIZE_MULTIPLIER)
                .addUniformName(UNIFORM_NAME_COLOR_MULTIPLIER)
                .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
                .addUniformName(UNIFORM_NAME_SELECTION_MODE)
                .addUniformName(UNIFORM_NAME_SELECTION_TIME)
                .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
                .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
                .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
                .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
                .init(gl);

        programWithSelectionUnselected =
            new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE_WITH_SELECTION_UNSELECTED,
                SHADERS_NODE_CIRCLE_SOURCE)
                .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
                .addUniformName(UNIFORM_NAME_COLOR_MULTIPLIER)
                .addUniformName(UNIFORM_NAME_BACKGROUND_COLOR)
                .addUniformName(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR)
                .addUniformName(UNIFORM_NAME_SIZE_MULTIPLIER)
                .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
                .addUniformName(UNIFORM_NAME_SELECTION_MODE)
                .addUniformName(UNIFORM_NAME_SELECTION_TIME)
                .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
                .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
                .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
                .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
                .init(gl);
    }

    public void drawArraysSingleInstance(GL2ES2 gl, int firstVertexIndex, int vertexCount) {
        gl.glDrawArrays(GL_TRIANGLES, firstVertexIndex, vertexCount);
    }

    public void drawInstanced(GL2ES3 gl, int vertexOffset, int vertexCount, int instanceCount) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glDrawArraysInstanced(GL_TRIANGLES, vertexOffset, vertexCount, instanceCount);
    }

    public void drawIndirect(GL4 gl, int instanceCount, int instancesOffset) {
        if (instanceCount <= 0) {
            return;
        }
        gl.glMultiDrawArraysIndirect(GL_TRIANGLES, (long) instancesOffset * INDIRECT_DRAW_COMMAND_BYTES, instanceCount,
            0);
    }
    // 0 : Program
    // 1 : Selected
    // 2 : Unselected
    public void useProgramWithSelectionSelected(GL2ES2 gl, float[] mvpFloats, float sizeMultiplier,
                                                float colorMultiplier,float globalTime,float selectionTime) {
        //Circle:
        programWithSelectionSelected.use(gl);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_SELECTION_TIME),  selectionTime);
        gl.glUniformMatrix4fv(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1,
            false, mvpFloats, 0);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_SIZE_MULTIPLIER), sizeMultiplier);
        gl.glUniform1f(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_COLOR_MULTIPLIER), colorMultiplier);
        gl.glUniform1i(programWithSelectionSelected.getUniformLocation(UNIFORM_NAME_SELECTION_MODE),1);

    }

    public void useProgramWithSelectionUnselected(GL2ES2 gl, float[] mvpFloats, float sizeMultiplier,
                                                  float[] backgroundColorFloats, float colorLightenFactor,
                                                  float colorMultiplier,float globalTime,float selectionTime) {
        //Circle:
        programWithSelectionUnselected.use(gl);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_SELECTION_TIME),  selectionTime);
        gl.glUniform1i(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_SELECTION_MODE),2);
        gl.glUniformMatrix4fv(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1,
            false, mvpFloats, 0);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_COLOR_MULTIPLIER),
            colorMultiplier);
        gl.glUniform4fv(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_BACKGROUND_COLOR), 1,
            backgroundColorFloats, 0);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR),
            colorLightenFactor);
        gl.glUniform1f(programWithSelectionUnselected.getUniformLocation(UNIFORM_NAME_SIZE_MULTIPLIER), sizeMultiplier);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float sizeMultiplier, float colorMultiplier,float globalTime,float selectionTime) {
        //Circle:
        program.use(gl);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME),  globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME),  selectionTime);
        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SIZE_MULTIPLIER), sizeMultiplier);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_COLOR_MULTIPLIER), colorMultiplier);
        gl.glUniform1i(program.getUniformLocation(UNIFORM_NAME_SELECTION_MODE),0);

    }

    public void stopUsingProgram(GL2ES2 gl) {
        gl.glUseProgram(0);
    }
}
