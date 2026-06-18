package org.gephi.viz.engine.jogl.models.nodedisk;

import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BORDER_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_DARKEN_FACTOR;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Eduardo Ramos
 */
public class NodeDiskModelNoSelection extends AbstractNodeDiskModel {

    private static final String SHADER_VS = "node";
    private static final String SHADER_FS = "node";

    public void initGLPrograms(GL2ES2 gl) {
        initProgram(gl, SHADER_VS, SHADER_FS, UNIFORM_NAME_BORDER_SIZE, UNIFORM_NAME_DARKEN_FACTOR);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float nodeBorderColorFactor, float nodeScale) {
        useCommon(gl, mvpFloats, nodeScale);
        setBorder(gl, nodeBorderColorFactor);
    }
}
