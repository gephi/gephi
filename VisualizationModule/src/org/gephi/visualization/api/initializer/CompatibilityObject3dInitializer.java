/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.visualization.api.initializer;


import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import org.gephi.graph.api.Object3d;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.api.Object3dImpl;

/**
 *
 * @author Mathieu Bastian
 */
public interface CompatibilityObject3dInitializer<O extends Renderable> extends Object3dInitializer {

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr);
    public void chooseModel(Object3dImpl<O> obj);
	public void initFromOpenGLThread();
}
