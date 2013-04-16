package org.gephi.visualization.model;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizModel;

/**
 *
 * @author mbastian
 */
public interface Model {

    public void display(GL2 gl, GLU glu, VizModel model);
}
