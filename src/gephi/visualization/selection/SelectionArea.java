/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.selection;

import gephi.visualization.Renderable;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.gleem.linalg.Vec3d;
import gephi.visualization.opengl.gleem.linalg.Vec3f;

/**
 *
 * @author Mathieu
 */
public interface SelectionArea {

    public abstract float[] getSelectionAreaRectancle();
    public abstract boolean mouseTest(Vec3f distanceFromMouse, Object3d object);
    public abstract boolean select(Renderable object);
	public abstract boolean unselect(Renderable object);
}
