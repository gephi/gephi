package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BACKGROUND_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BORDER_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_COLOR_LIGHTEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_DARKEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Eduardo Ramos
 */
public class NodeDiskModelSelectionUnselected extends AbstractNodeDiskModel {

    private static final String SHADER_VS = "node_with_selection_unselected";
    private static final String SHADER_FS = "node_with_selection_unselected";

    public void initGLPrograms(GL2ES2 gl) {
        initProgram(gl, SHADER_VS, SHADER_FS,
            UNIFORM_NAME_BACKGROUND_COLOR, UNIFORM_NAME_COLOR_LIGHTEN_FACTOR,
            UNIFORM_NAME_GLOBAL_TIME, UNIFORM_NAME_SELECTION_TIME,
            UNIFORM_NAME_BORDER_SIZE, UNIFORM_NAME_DARKEN_FACTOR);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats,
                           float[] backgroundColorFloats, float colorLightenFactor,
                           float globalTime, float selectedTime, float nodeBorderColorFactor, float nodeScale) {
        useCommon(gl, mvpFloats, nodeScale);
        gl.glUniform4fv(program.getUniformLocation(UNIFORM_NAME_BACKGROUND_COLOR), 1, backgroundColorFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR), colorLightenFactor);
        setTimes(gl, globalTime, selectedTime);
        setBorder(gl, nodeBorderColorFactor);
    }
}
