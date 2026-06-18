package org.gephi.viz.engine.jogl.models.nodedisk;

import com.jogamp.opengl.GL2ES2;

/**
 * Shader program used to render nodes into an offscreen buffer for GPU picking: each node's quad is
 * filled with a flat color encoding its store id, so the pixel under the cursor identifies the node.
 *
 * @author Eduardo Ramos
 */
public class NodeDiskModelPicking extends AbstractNodeDiskModel {

    private static final String SHADER_VS = "node_picking";
    private static final String SHADER_FS = "node_picking";

    public void initGLPrograms(GL2ES2 gl) {
        initProgram(gl, SHADER_VS, SHADER_FS);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float nodeScale) {
        useCommon(gl, mvpFloats, nodeScale);
    }
}
