/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.opengl.compatibility.initializer;

import gephi.visualization.Renderable;
import gephi.visualization.initializer.Object3dInitializer;
import gephi.visualization.opengl.Object3d;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 *
 * @author Mathieu
 */
public interface CompatibilityObject3dInitializer<O extends Renderable> extends Object3dInitializer {

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr);
    public void chooseModel(Object3d<O> obj);
	public void initFromOpenGLThread();
}
