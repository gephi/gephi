/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.visualization.swing;

import gephi.visualization.events.VizEventManager;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 *
 * @author Mathieu
 */
public interface GraphIO extends MouseListener, MouseWheelListener, MouseMotionListener, KeyListener {

    public float[] getMousePosition();
    public void startMouseListening();
    public void stopMouseListening();

    public void setVizEventManager(VizEventManager vizEventManager);
    public VizEventManager getVizEventManager();
}
