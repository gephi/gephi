/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.model;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizModel;

/**
 *
 * @author mbastian
 */
public interface Model {

    public void display(GL gl, GLU glu, VizModel model);
}
