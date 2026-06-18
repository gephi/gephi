package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BORDER_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_DARKEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Eduardo Ramos
 */
public class NodeDiskModelSelectionSelected extends AbstractNodeDiskModel {

    private static final String SHADER_VS = "node_with_selection_selected";
    private static final String SHADER_FS = "node";

    public void initGLPrograms(GL2ES2 gl) {
        initProgram(gl, SHADER_VS, SHADER_FS,
            UNIFORM_NAME_GLOBAL_TIME, UNIFORM_NAME_SELECTION_TIME,
            UNIFORM_NAME_BORDER_SIZE, UNIFORM_NAME_DARKEN_FACTOR);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats,
                           float globalTime, float selectedTime, float nodeBorderColorFactor, float nodeScale) {
        useCommon(gl, mvpFloats, nodeScale);
        setTimes(gl, globalTime, selectedTime);
        setBorder(gl, nodeBorderColorFactor);
    }
}
