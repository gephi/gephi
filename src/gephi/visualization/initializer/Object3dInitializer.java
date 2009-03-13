/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.initializer;

import gephi.visualization.Renderable;
import gephi.visualization.opengl.Object3d;
import javax.swing.JPanel;

/**
 *
 * @author Mathieu
 */
public interface Object3dInitializer {

    public Object3d<Renderable> initObject(Renderable n);
	public String getName();
	public JPanel getPanel();
    
    @Override
	public String toString();
}
