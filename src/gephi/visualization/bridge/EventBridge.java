/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.bridge;

import gephi.visualization.opengl.Object3d;

/**
 *
 * @author Mathieu
 */
public interface EventBridge {
    public void initEvents();
    public void mouseClick(Object3d[] clickedObjects);
}
